package com.group4.medicaladherenceapp.activities

import android.app.TimePickerDialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.group4.medicaladherenceapp.R
import com.group4.medicaladherenceapp.firebase.FirebaseRepository
import com.group4.medicaladherenceapp.remote.MedicationApi
import com.group4.medicaladherenceapp.remote.MedicationSuggestion
import com.group4.medicaladherenceapp.utils.AlarmScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.util.Calendar
import java.util.Locale

class AddMedicationActivity : AppCompatActivity() {
    private val repository = FirebaseRepository()
    private var lookupJob: Job? = null
    private var selectedSuggestion: MedicationSuggestion? = null
    private lateinit var nameField: AutoCompleteTextView
    private lateinit var preview: ImageView
    private lateinit var safetyText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_medication)
        nameField = findViewById(R.id.etMedicationName)
        preview = findViewById(R.id.imgMedicationPreview)
        safetyText = findViewById(R.id.txtMedicationSafety)
        val timeField = findViewById<EditText>(R.id.etTime)
        val dosageField = findViewById<EditText>(R.id.etDosage)

        nameField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                selectedSuggestion = null
                val term = s?.toString().orEmpty().trim()
                lookupJob?.cancel()
                if (term.length < 2) { nameField.dismissDropDown(); return }
                lookupJob = lifecycleScope.launch {
                    delay(250)
                    try {
                        val results = MedicationApi.suggestions(term)
                        val names = results.map { it.name }
                        withContext(Dispatchers.Main) {
                            nameField.setAdapter(ArrayAdapter(this@AddMedicationActivity, android.R.layout.simple_dropdown_item_1line, names))
                            if (names.isNotEmpty() && nameField.hasFocus()) nameField.showDropDown()
                            nameField.setTag(R.id.etMedicationName, results)
                        }
                    } catch (_: Exception) { /* Offline typing still works. */ }
                }
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })
        nameField.setOnItemClickListener { _, _, position, _ ->
            @Suppress("UNCHECKED_CAST")
            val results = nameField.getTag(R.id.etMedicationName) as? List<MedicationSuggestion> ?: emptyList()
            selectedSuggestion = results.getOrNull(position)
            selectedSuggestion?.let { loadSafety(it) }
        }
        timeField.setOnClickListener {
            val now = Calendar.getInstance()
            TimePickerDialog(this, { _, hour, minute -> timeField.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute)) }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show()
        }
        findViewById<Button>(R.id.btnSaveMedication).setOnClickListener {
            val name = nameField.text.toString().trim(); val dosage = dosageField.text.toString().trim(); val time = timeField.text.toString().trim()
            if (name.isBlank() || dosage.isBlank() || time.isBlank()) { toast("Complete all fields"); return@setOnClickListener }
            lifecycleScope.launch {
                try {
                    repository.addMedication(name, dosage, time)
                    val parts = time.split(":")
                    if (parts.size == 2) AlarmScheduler(this@AddMedicationActivity).scheduleMedicationReminder(name, parts[0].toInt(), parts[1].toInt())
                    toast("Medication saved to your account")
                    finish()
                } catch (e: Exception) { toast(e.localizedMessage ?: "Could not save medication") }
            }
        }
    }

    private fun loadSafety(suggestion: MedicationSuggestion) {
        preview.visibility = View.VISIBLE
        safetyText.visibility = View.VISIBLE
        safetyText.text = "Loading official DailyMed image and FDA label safety information…"
        lifecycleScope.launch {
            val result = MedicationApi.safety(suggestion.name, suggestion.rxcui)
            result.imageUrl?.let { url ->
                val bitmap = withContext(Dispatchers.IO) { runCatching { URL(url).openStream().use { stream -> BitmapFactory.decodeStream(stream) } }.getOrNull() }
                if (bitmap != null) preview.setImageBitmap(bitmap) else preview.visibility = View.GONE
            } ?: run { preview.visibility = View.GONE }
            val label = result.overdosage ?: result.warnings
            safetyText.text = if (label.isNullOrBlank()) {
                "No matching FDA overdose section was found. This is not a safety clearance—ask a pharmacist for product-specific advice."
            } else {
                "Official FDA label information (reference only):\n\n${label.take(1800)}\n\nFor a suspected poisoning, contact Poison Control or emergency services."
            }
        }
    }

    override fun onDestroy() { lookupJob?.cancel(); super.onDestroy() }
    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}
