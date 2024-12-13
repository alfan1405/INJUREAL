package com.capstone.injureal.riwayat

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "prediction_history")
data class PredictionHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val imageUrl: String?,
    val woundName: String,
    val description: String?,
    val treatment: String?,
    val warnings: String?,
    val doNot: String?,
    val recommendedMedicines: String?,
    val timestamp: Long = System.currentTimeMillis()
): Parcelable