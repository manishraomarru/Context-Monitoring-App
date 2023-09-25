package com.example.contextmonitoring

import java.util.Date
import androidx.room.PrimaryKey
import androidx.room.Entity

@Entity
data class UserData(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var timestamp: Date? = null,
    var HeartRate: Float = 0f,
    var RespiratoryRate: Float = 0f,
    var Nausea: Float = 0f,
    var Headache: Float = 0f,
    var Diarrhea: Float = 0f,
    var SoarThroat: Float = 0f,
    var Fever: Float = 0f,
    var MuscleAche: Float = 0f,
    var LossOfSmellOrTaste: Float = 0f,
    var Cough: Float = 0f,
    var ShortnessOfBreath: Float = 0f,
    var FeelingTired: Float = 0f
)

