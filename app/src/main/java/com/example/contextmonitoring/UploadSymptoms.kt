package com.example.contextmonitoring

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class UploadSymptoms : AppCompatActivity() {
    private var db: AppDatabase? = null
    private val data = UserData()
    private val listOfRatings = FloatArray(10) { 0.0f } // Initialize with zeros

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.upload_symtoms_activity)

        // Find and set up individual RatingBars for each symptom
        val nauseaRatingBar = findViewById<RatingBar>(R.id.RatingNausea)
        val headacheRatingBar = findViewById<RatingBar>(R.id.RatingHeadache)
        val diarrheaRatingBar = findViewById<RatingBar>(R.id.RatingDiarrhea)
        val soarThroatRatingBar = findViewById<RatingBar>(R.id.RatingSoarThroat)
        val feverRatingBar = findViewById<RatingBar>(R.id.RatingFever)
        val muscleAcheRatingBar = findViewById<RatingBar>(R.id.RatingMuscleAche)
        val smellRatingBar  = findViewById<RatingBar>(R.id.RatingSmell)
        val coughRatingBar  = findViewById<RatingBar>(R.id.RatingCough)
        val breathRatingBar = findViewById<RatingBar>(R.id.RatingBreath)
        val tiredRatingBar  = findViewById<RatingBar>(R.id.RatingTired)

        val updateRatingButton = findViewById<Button>(R.id.ButtonUpdateRating)

        // Initialize the RatingBars and listOfRatings array
        initializeSymptomRatings(nauseaRatingBar, headacheRatingBar, diarrheaRatingBar, soarThroatRatingBar, feverRatingBar, muscleAcheRatingBar, smellRatingBar, coughRatingBar, breathRatingBar, tiredRatingBar)

        lifecycleScope.launch {
            try {
                db = AppDatabase.getInstance(applicationContext)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        updateRatingButton.setOnClickListener {
            // Update the data object with symptom ratings
            updateSymptomRatings()

            val isUploadSignsClicked = intent.getBooleanExtra("isUploadSignsClicked", false)
            Log.i("log", "Fetched untent Values" + isUploadSignsClicked)

            lifecycleScope.launch(Dispatchers.IO) {
                if (isUploadSignsClicked) {
                    try {
                        val latestData = db?.userDataDao()?.getRecentData()
                        data.HeartRate = latestData?.HeartRate ?: 0f
                        data.RespiratoryRate = latestData?.RespiratoryRate ?: 0f
                        data.id = latestData?.id ?: 0
                        db?.userDataDao()?.update(data)
                        Log.i("log", "Fetched updated Values" + db?.userDataDao()?.getRecentData())
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Handle the exception, e.g., display an error toast
                    }
                } else {
                    try {
                        db?.userDataDao()?.insert(data)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Handle the exception, e.g., display an error toast
                    }
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@UploadSymptoms, "Symptoms are updated in the database", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initializeSymptomRatings(vararg ratingBars: RatingBar) {
        for ((index, ratingBar) in ratingBars.withIndex()) {
            ratingBar.rating = listOfRatings[index]
            ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
                listOfRatings[index] = rating
            }
        }
    }

    private fun updateSymptomRatings() {
        data.timestamp = Date(System.currentTimeMillis())
        data.Nausea = listOfRatings[0]
        data.Headache = listOfRatings[1]
        data.Diarrhea = listOfRatings[2]
        data.SoarThroat = listOfRatings[3]
        data.Fever = listOfRatings[4]
        data.MuscleAche = listOfRatings[5]
        data.LossOfSmellOrTaste = listOfRatings[6]
        data.Cough = listOfRatings[7]
        data.ShortnessOfBreath = listOfRatings[8]
        data.FeelingTired = listOfRatings[9]
    }
}

//class UploadSymtoms : AppCompatActivity() {
//    var listOfRatings = FloatArray(10)
//    private var db: ContextDatabase? = null
//
//    private val data = UserData()
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_upload_symtoms)
//
//        val symptomSeverity = findViewById<RatingBar>(R.id.RatingSelectionBar)
//        val updateRatingButton = findViewById<Button>(R.id.ButtonUpdateRating)
//        val symptomsDropdown = findViewById<Spinner>(R.id.SymptomSelectionDropdown)
//
//        val arrAdapter = ArrayAdapter.createFromResource(
//            this,
//            R.array.ListOfSymptoms,
//            android.R.layout.simple_spinner_item
//        )
//        arrAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        symptomsDropdown.adapter = arrAdapter
//
//        lifecycleScope.launch {
//            try {
//                db = ContextDatabase.getInstance(applicationContext)
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//
//        symptomSeverity.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
//            val i = symptomsDropdown.selectedItemPosition
//            listOfRatings[i] = rating
//        }
//
//        updateRatingButton.setOnClickListener {
//            data.timestamp = Date(System.currentTimeMillis())
//            data.Nausea = listOfRatings[0]
//            data.Headache = listOfRatings[1]
//            data.Diarrhea = listOfRatings[2]
//            data.SoarThroat = listOfRatings[3]
//            data.Fever = listOfRatings[4]
//            data.MuscleAche = listOfRatings[5]
//            data.LossOfSmellOrTaste = listOfRatings[6]
//            data.Cough = listOfRatings[7]
//            data.ShortnessOfBreath = listOfRatings[8]
//            data.FeelingTired = listOfRatings[9]
//
//            val isUploadSignsClicked = intent.getBooleanExtra("is_Upload_Signs_Clicked", false)
//
//            lifecycleScope.launch(Dispatchers.IO) {
//                if (isUploadSignsClicked) {
//                    try {
//                        val latestData = db?.userDataDao()?.getRecentData()
//                        data.HeartRate = latestData?.HeartRate ?: 0f
//                        data.RespiratoryRate = latestData?.RespiratoryRate ?: 0f
//                        data.id = latestData?.id ?: 0
//                        db?.userDataDao()?.update(data)
//                        Log.i("log", "Fetched updated Values" +db?.userDataDao()?.getRecentData())
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                        // Handle the exception, e.g., display an error toast
//                    }
//                } else {
//                    try {
//                        db?.userDataDao()?.insert(data)
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                        // Handle the exception, e.g., display an error toast
//                    }
//                }
//
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(this@UploadSymtoms, "Symptoms are updated in database", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//
//
//        symptomsDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onNothingSelected(adapterView: AdapterView<*>?) {
//                // Do nothing
//            }
//
//            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, index: Int, l: Long) {
//                symptomSeverity.rating = listOfRatings[index]
//            }
//        }
//    }
//}