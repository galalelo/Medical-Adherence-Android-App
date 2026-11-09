package com.group4.medicaladherenceapp.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.group4.medicaladherenceapp.R
import com.group4.medicaladherenceapp.adapters.MedicationAdapter
import com.group4.medicaladherenceapp.firebase.FirebaseRepository
import com.group4.medicaladherenceapp.models.Medication
import kotlinx.coroutines.launch

class MedicationActivity : AppCompatActivity() {
    private val repository = FirebaseRepository()
    private lateinit var recyclerView: RecyclerView
    private var allMedications: List<Medication> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medication)
        findViewById<Button>(R.id.btnAddMedication).setOnClickListener { startActivity(Intent(this, AddMedicationActivity::class.java)) }
        recyclerView = findViewById(R.id.recyclerViewMedications)
        recyclerView.layoutManager = LinearLayoutManager(this)
        findViewById<EditText>(R.id.etMedicationSearch).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = render(s?.toString().orEmpty())
            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    override fun onResume() { super.onResume(); loadMedications() }

    private fun loadMedications() {
        lifecycleScope.launch {
            try { allMedications = repository.getMedications(); render(findViewById<EditText>(R.id.etMedicationSearch).text.toString()) }
            catch (e: Exception) { Toast.makeText(this@MedicationActivity, e.localizedMessage ?: "Could not load medications", Toast.LENGTH_SHORT).show() }
        }
    }

    private fun render(query: String) {
        val term = query.trim().lowercase()
        val list = allMedications.filter { term.isBlank() || it.name.lowercase().contains(term) || it.dosage.lowercase().contains(term) }
        recyclerView.adapter = MedicationAdapter(list,
            { medication -> startActivity(Intent(this, EditMedicationActivity::class.java).putExtra("MEDICATION_ID", medication.id)) },
            { medication -> lifecycleScope.launch { try { repository.deleteMedication(medication.id); Toast.makeText(this@MedicationActivity, "Medication deleted", Toast.LENGTH_SHORT).show(); loadMedications() } catch (e: Exception) { Toast.makeText(this@MedicationActivity, e.localizedMessage ?: "Could not delete medication", Toast.LENGTH_SHORT).show() } } },
            { medication -> startActivity(Intent(this, ConfirmMedicationActivity::class.java).putExtra("MEDICATION_ID", medication.id).putExtra("MEDICATION_NAME", medication.name)) })
    }
}
