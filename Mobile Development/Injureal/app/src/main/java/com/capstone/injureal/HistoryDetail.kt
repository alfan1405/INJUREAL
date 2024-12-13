package com.capstone.injureal

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.capstone.injureal.riwayat.PredictionHistory

class HistoryDetail : AppCompatActivity() {

    private lateinit var historyImageView: ImageView
    private lateinit var woundNameTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var treatmentTextView: TextView
    private lateinit var warningsTextView: TextView
    private lateinit var doNotTextView: TextView
    private lateinit var recommendedMedicinesTextView: TextView
    private lateinit var backButton: ImageButton

    companion object {
        const val EXTRA_HISTORY_ITEM = "history_item"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_detail)

        historyImageView = findViewById(R.id.result_image)
        woundNameTextView = findViewById(R.id.wound_name)
        descriptionTextView = findViewById(R.id.description)
        treatmentTextView = findViewById(R.id.treatment)
        warningsTextView = findViewById(R.id.warnings)
        doNotTextView = findViewById(R.id.do_not)
        recommendedMedicinesTextView = findViewById(R.id.recommended_medicines)
        backButton = findViewById(R.id.back_button)

        val historyItem: PredictionHistory? = intent.getParcelableExtra(EXTRA_HISTORY_ITEM)

        if (historyItem != null) {
            populateData(historyItem)
        }

        backButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun populateData(historyItem: PredictionHistory) {
        Glide.with(this).load(historyItem.imageUrl).into(historyImageView)

        woundNameTextView.text = historyItem.woundName
        descriptionTextView.text = historyItem.description
        treatmentTextView.text = historyItem.treatment
        warningsTextView.text = historyItem.warnings
        doNotTextView.text = historyItem.doNot
        recommendedMedicinesTextView.text = historyItem.recommendedMedicines
    }
}
