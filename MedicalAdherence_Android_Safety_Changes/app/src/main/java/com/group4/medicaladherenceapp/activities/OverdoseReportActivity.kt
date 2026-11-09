package com.group4.medicaladherenceapp.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.group4.medicaladherenceapp.R
import com.group4.medicaladherenceapp.remote.MedicationApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class OverdoseReportActivity : AppCompatActivity() {
    private val repository = com.group4.medicaladherenceapp.firebase.FirebaseRepository()
    private var lookupJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_overdose_report)
        val medication = findViewById<AutoCompleteTextView>(R.id.etReportMedication)
        medication.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                lookupJob?.cancel(); val term = s?.toString().orEmpty().trim(); if (term.length < 2) return
                lookupJob = lifecycleScope.launch { delay(250); runCatching { MedicationApi.suggestions(term) }.onSuccess { results -> medication.setAdapter(ArrayAdapter(this@OverdoseReportActivity, android.R.layout.simple_dropdown_item_1line, results.map { it.name })); if (medication.hasFocus()) medication.showDropDown() } }
            }
            override fun afterTextChanged(s: android.text.Editable?) = Unit
        })
        findViewById<Button>(R.id.btnCallPoisonControl).setOnClickListener { startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:18002221222"))) }
        findViewById<Button>(R.id.btnSaveOverdoseReport).setOnClickListener {
            val amount = findViewById<EditText>(R.id.etReportAmount).text.toString().trim()
            val occurredAt = findViewById<EditText>(R.id.etReportWhen).text.toString().trim()
            val symptoms = findViewById<EditText>(R.id.etReportSymptoms).text.toString().trim()
            val medicationName = medication.text.toString().trim()
            if (medicationName.isBlank() || amount.isBlank() || occurredAt.isBlank()) { toast("Complete the medication, amount, and time fields"); return@setOnClickListener }
            lifecycleScope.launch {
                try {
                    repository.addOverdoseReport(medicationName, amount, occurredAt, symptoms)
                    toast("Report saved. Contact Poison Control for expert guidance.")
                    startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:18002221222")))
                } catch (e: Exception) { toast(e.localizedMessage ?: "Could not save report") }
            }
        }
    }

    override fun onDestroy() { lookupJob?.cancel(); super.onDestroy() }
    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}
