package com.group4.medicaladherenceapp.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/** Public, no-key services: NLM RxNorm, NIH DailyMed, and FDA openFDA. */
data class MedicationSuggestion(val name: String, val rxcui: String)
data class MedicationSafety(
    val imageUrl: String? = null,
    val overdosage: String? = null,
    val warnings: String? = null
)

object MedicationApi {
    private suspend fun get(url: String): String = withContext(Dispatchers.IO) {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 12_000
        connection.readTimeout = 12_000
        connection.setRequestProperty("Accept", "application/json")
        try {
            val code = connection.responseCode
            if (code !in 200..299) error("Service returned HTTP $code")
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally { connection.disconnect() }
    }

    suspend fun suggestions(term: String): List<MedicationSuggestion> {
        if (term.trim().length < 2) return emptyList()
        val encoded = URLEncoder.encode(term.trim(), "UTF-8")
        val root = JSONObject(get("https://rxnav.nlm.nih.gov/REST/approximateTerm.json?term=$encoded&maxEntries=8&option=1"))
        val candidates = root.optJSONObject("approximateGroup")?.optJSONArray("candidate") ?: return emptyList()
        val results = mutableListOf<MedicationSuggestion>()
        val seen = mutableSetOf<String>()
        for (index in 0 until candidates.length()) {
            val item = candidates.optJSONObject(index) ?: continue
            val name = item.optString("name").trim()
            if (name.isNotEmpty() && seen.add(name)) results += MedicationSuggestion(name, item.optString("rxcui"))
            if (results.size == 8) break
        }
        return results
    }

    suspend fun safety(name: String, rxcui: String): MedicationSafety = withContext(Dispatchers.IO) {
        var imageUrl: String? = null
        try {
            val drugName = URLEncoder.encode(name, "UTF-8")
            val splRoot = JSONObject(get("https://dailymed.nlm.nih.gov/dailymed/services/v2/spls.json?drug_name=$drugName"))
            val setId = splRoot.optJSONArray("data")?.optJSONObject(0)?.optString("setid").orEmpty()
            if (setId.isNotEmpty()) {
                val mediaRoot = JSONObject(get("https://dailymed.nlm.nih.gov/dailymed/services/v2/spls/$setId/media.json"))
                val media = mediaRoot.optJSONObject("data")?.optJSONArray("media")
                for (index in 0 until (media?.length() ?: 0)) {
                    val item = media?.optJSONObject(index) ?: continue
                    if (item.optString("mime_type").startsWith("image/")) { imageUrl = item.optString("url"); break }
                }
            }
        } catch (_: Exception) { }

        var overdose: String? = null
        var warnings: String? = null
        try {
            val query = if (rxcui.isNotBlank()) {
                "search=openfda.rxcui:%22${URLEncoder.encode(rxcui, "UTF-8")}%22&limit=1"
            } else {
                "search=openfda.generic_name:%22${URLEncoder.encode(name, "UTF-8")}%22&limit=1"
            }
            val labelRoot = JSONObject(get("https://api.fda.gov/drug/label.json?$query"))
            val record = labelRoot.optJSONArray("results")?.optJSONObject(0)
            overdose = record?.optJSONArray("overdosage")?.join(" ")?.takeIf { it.isNotBlank() }
            warnings = record?.optJSONArray("boxed_warning")?.join(" ")?.takeIf { it.isNotBlank() }
                ?: record?.optJSONArray("warnings_and_cautions")?.join(" ")?.takeIf { it.isNotBlank() }
                ?: record?.optJSONArray("warnings")?.join(" ")?.takeIf { it.isNotBlank() }
        } catch (_: Exception) { }
        MedicationSafety(imageUrl, overdose, warnings)
    }
}
