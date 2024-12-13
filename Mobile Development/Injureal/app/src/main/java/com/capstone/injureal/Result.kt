package com.capstone.injureal

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.capstone.injureal.riwayat.AppDatabase
import com.capstone.injureal.riwayat.PredictionHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Result : AppCompatActivity() {

    companion object {
        const val IMAGE_URL = "image_url"
        const val WOUND_NAME = "wound_name"
        const val DESCRIPTION = "description"
        const val TREATMENT = "treatment"
        const val WARNINGS = "warnings"
        const val DO_NOT = "do_not"
        const val RECOMMENDED_MEDICINES = "recommended_medicines"
    }

    private lateinit var resultImageView: ImageView
    private lateinit var woundNameTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var treatmentTextView: TextView
    private lateinit var warningsTextView: TextView
    private lateinit var doNotTextView: TextView
    private lateinit var recommendedMedicinesTextView: TextView
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        resultImageView = findViewById(R.id.result_image)
        woundNameTextView = findViewById(R.id.wound_name)
        descriptionTextView = findViewById(R.id.description)
        treatmentTextView = findViewById(R.id.treatment)
        warningsTextView = findViewById(R.id.warnings)
        doNotTextView = findViewById(R.id.do_not)
        recommendedMedicinesTextView = findViewById(R.id.recommended_medicines)
        saveButton = findViewById(R.id.saveButton)

        val imageUrl = intent.getStringExtra(IMAGE_URL)
        val woundName = intent.getStringExtra(WOUND_NAME)
        val description = intent.getStringExtra(DESCRIPTION)
        val treatment = intent.getStringArrayListExtra(TREATMENT)
        val warnings = intent.getStringArrayListExtra(WARNINGS)
        val doNot = intent.getStringArrayListExtra(DO_NOT)
        val recommendedMedicines = intent.getStringArrayListExtra(RECOMMENDED_MEDICINES)

        imageUrl?.let {
            Glide.with(this)
                .load(it)
                .into(resultImageView)
        }

        woundNameTextView.text = woundName ?: "Unknown Wound"
        descriptionTextView.text = description ?: "No description available."
        treatmentTextView.text = treatment?.joinToString("\n") ?: "No treatment steps available."
        warningsTextView.text = warnings?.joinToString("\n") ?: "No warnings available."
        doNotTextView.text = doNot?.joinToString("\n") ?: "No 'do not' instructions available."
        recommendedMedicinesTextView.text = recommendedMedicines?.joinToString("\n") ?: "No recommended medicines available."

        saveButton.setOnClickListener {
            savePredictionToDatabase(
                PredictionHistory(
                    imageUrl = imageUrl ?: "",
                    woundName = woundName ?: "Unknown Wound",
                    description = description ?: "No description available.",
                    treatment = treatment?.joinToString("\n") ?: "No treatment steps available.",
                    warnings = warnings?.joinToString("\n") ?: "No warnings available.",
                    doNot = doNot?.joinToString("\n") ?: "No 'do not' instructions available.",
                    recommendedMedicines = recommendedMedicines?.joinToString("\n") ?: "No recommended medicines available."
                )
            )
        }
    }

    private fun savePredictionToDatabase(prediction: PredictionHistory) {
        lifecycleScope.launch(Dispatchers.IO) {
            val database = AppDatabase.getDatabase(this@Result)
            database.predictionHistoryDao().insertPrediction(prediction)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@Result, "Save is successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}