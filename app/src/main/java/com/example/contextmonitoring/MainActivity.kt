package com.example.contextmonitoring

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.Date
import kotlin.math.abs
import kotlin.math.sqrt
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.contextmonitoring.databinding.MainActivityBinding


class MainActivity : AppCompatActivity() {
    private var db: AppDatabase? = null

    private var HRCalculationInProgress = false
    private var RRCalculationInProgress = false

    private var respiratoryRate = 0.0
    private var heartRate = 0.0

    // Declare and initialize isUploadSignsClicked
    private var isUploadSignsClicked = false
    private lateinit var binding: MainActivityBinding

    private suspend fun calculateHeartRate(videoPath: String): String {
        return withContext(Dispatchers.Default) {
            val frameList = ArrayList<Bitmap>()
            val retriever = MediaMetadataRetriever()

            try {
                // Set the data source to the video file
                retriever.setDataSource(videoPath)

                // Get the total number of frames in the video
                val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT)
                val duration = durationStr!!.toInt() / 12 // Assuming 12 frames per second

                System.gc()

                // Extract frames at regular intervals
                var interval = 10
                while (interval < duration) {
                    val bitmap = retriever.getFrameAtIndex(interval)
                    frameList.add(bitmap!!)
                    interval += 5
                }

            } catch (exception: Exception) {
                Log.i("log", "Exception: $exception")
                // Handle or log the exception
            } finally {
                retriever.release()
            }

            // Calculate the heart rate from the extracted frames
            var pixelCount: Long = 0
            var redBucket: Long = 0
            val redBucketList = mutableListOf<Long>()

            for (frame in frameList) {
                redBucket = 0
                for (xCoordinate in 0 until frame.width) {
                    for (yCoordinate in 0 until frame.height) {
                        val color: Int = frame.getPixel(xCoordinate, yCoordinate)
                        redBucket += Color.red(color) + Color.blue(color) + Color.green(color)
                        pixelCount++
                    }
                }
                redBucketList.add(redBucket)
            }

            // Calculate the average pixel values
            val avgBucketList = mutableListOf<Long>()
            for (index in 0 until redBucketList.lastIndex - 5) {
                val temp = (redBucketList[index] + redBucketList[index + 1] + redBucketList[index + 2] + redBucketList[index + 3] + redBucketList[index + 4]) / 4
                avgBucketList.add(temp)
            }

            // Calculate heart rate by counting significant changes in pixel values
            var count = 0
            var previousBucketValue = avgBucketList[0]
            for (index in 1 until avgBucketList.lastIndex) {
                val currentBucketValue = avgBucketList[index]
                if ((currentBucketValue - previousBucketValue) > 3500) {
                    count++
                }
                previousBucketValue = avgBucketList[index]
            }

            // Convert the count to heart rate (assumed 12 frames per second)
            val heartRate = ((count.toFloat() * 12 / 45) * 60).toInt()
            Log.i("log", "Heart rate: $heartRate")
            (heartRate / 2).toString() // Return the calculated heart rate
        }
    }

    private fun calculateRespiratoryRate(displayRR: TextView) {
        // Checks if there is an existing respiratory rate detection process running
        if (RRCalculationInProgress) {
            showToast("Respiratory rate calculation in-progress. Please wait...")
        } else {
            RRCalculationInProgress = true
            //displayRR.text = "Processing..."
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    // Replace with your actual CSV resource ID
                    val resourceId = R.raw.csvbreathe27v1
                    val (accelValuesZ, accelValuesY, accelValuesX) = extractAndDivideCSVData(
                        this@MainActivity,
                        resourceId
                    )

                    // Process the accelerometer data
                    val respiratoryRate = calculateRespiratoryRateFromAccelerometerData(accelValuesZ, accelValuesY, accelValuesX)

                    // Update the UI with the calculated respiratory rate
                    updateRespiratoryRateUI(displayRR, respiratoryRate)
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Handle any exceptions
                    RRCalculationInProgress = false
                }
            }
        }
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun calculateRespiratoryRateFromAccelerometerData(accelerometerValuesZ: FloatArray, accelerometerValuesY: FloatArray, accelerometerValuesX: FloatArray): Double {
        var res = 0
        var currentValue = 0f
        var previousValue = 0f
        previousValue = 10f

        for (index in 11 until 450) {
            currentValue = sqrt(
                Math.pow(accelerometerValuesZ[index].toDouble(), 2.0) + Math.pow(
                    accelerometerValuesX[index].toDouble(),
                    2.0
                ) + Math.pow(accelerometerValuesY[index].toDouble(), 2.0)
            ).toFloat()

            if (abs(previousValue - currentValue) > 0.085) {
                res++
            }

            previousValue = currentValue
        }

        return res.toDouble() / 45.0 * 30.0
    }

    private fun updateRespiratoryRateUI(displayRR: TextView, rate: Double) {
        runOnUiThread {
            displayRR.text = "Respiratory Rate: ${rate.toInt()}"
            RRCalculationInProgress = false
            respiratoryRate = rate
            binding.ButtonMRR.hideProgress {
                textResourceId = R.string.ButtonMRR
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        //setContentView(R.layout.main_activity)

        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Context Monitoring"

        requestStorageAndCameraPermissions(this)

        val recordVideoButton = findViewById<Button>(R.id.ButtonRecordVideo)
        val videoView = findViewById<VideoView>(R.id.videoView)
        var videoUploaded = false

        recordVideoButton.setOnClickListener {

            // Assuming your video file name in res/raw is "sample_video.mp4"
            val videoUri = Uri.parse("android.resource://" + packageName + "/" + R.raw.heartrate)
            videoView.setVideoURI(videoUri)
            videoView.seekTo(1)
            videoView.pause()
            videoUploaded = true
            Toast.makeText(this@MainActivity, "Video uploaded successfully", Toast.LENGTH_SHORT).show()
            videoView.visibility = View.VISIBLE
        }

        val measureHeartRateButton = findViewById<Button>(R.id.ButtonMHR)
        val displayHeartRate = findViewById<TextView>(R.id.DisplayHearRate)

        measureHeartRateButton.setOnClickListener {
            binding.ButtonMHR.showProgress{
                textResourceId = R.string.ButtonProcessing
            }
            val videoUri = Uri.parse("android.resource://${packageName}/raw/${R.raw.heartrate}")
            Log.i("log", "Fetched Values " + videoUri)
            val videoFile = copyRawResourceToFile(this, R.raw.heartrate, "temp_media_file.mp4")

            lifecycleScope.launch {
                if(!videoUploaded) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Please upload the video", Toast.LENGTH_SHORT).show()
                    }
                }
                else {
                    //displayHeartRate.text = "Processing..."
                    val HRate = calculateHeartRate(videoFile.path)
                    displayHeartRate.text = "Heart Rate: $HRate"
                    heartRate = HRate.toDouble()
                    binding.ButtonMHR.hideProgress{
                        textResourceId = R.string.ButtonMHR
                    }
                }
                videoFile.delete()
            }
        }

        val displayRR = findViewById<TextView>(R.id.DisplayRespiratoryRate)
        val mrrButton = findViewById<Button>(R.id.ButtonMRR)

        // Set an onClickListener for the respiratory rate calculation button
        mrrButton.setOnClickListener {
            binding.ButtonMRR.showProgress{
                textResourceId = R.string.ButtonProcessing
            }
            calculateRespiratoryRate(displayRR)
        }

        // Find and set up the symptoms upload button
        val symptomsUploadButton = findViewById<Button>(R.id.ButtonSymptomsUpload)
        symptomsUploadButton.setOnClickListener {
            // Launch UploadSymptoms activity and pass a flag
            val intent = Intent(this@MainActivity, UploadSymptoms::class.java)
            intent.putExtra("isUploadSignsClicked", isUploadSignsClicked)
            startActivity(intent)
        }

// Initialize the database in a coroutine
        lifecycleScope.launch {
            try {
                Log.i("log", "Fetching context: " + applicationContext)
                db = AppDatabase.getInstance(applicationContext)
            } catch (e: Exception) {
                Log.i("log", "Error fetching context: " + e.printStackTrace())
                e.printStackTrace()
            }
        }

// Find and set up the signs upload button
        val signsUploadButton = findViewById<Button>(R.id.ButtonSignsUpload)
        signsUploadButton.setOnClickListener {
            // Set a flag indicating that signs are being uploaded
            isUploadSignsClicked = true

            // Perform database operations in a background thread
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val userData = UserData().apply {
                        HeartRate = heartRate.toFloat()
                        RespiratoryRate = respiratoryRate.toFloat()
                        timestamp = Date(System.currentTimeMillis())
                    }
                    Log.i("log", "Fetched heart rate: " + displayHeartRate.text)

                    // Insert the data into the database
                    db?.userDataDao()?.insert(userData)

                    // Log and display a success message on the main thread
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Signs uploaded to the database", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Handle the exception, e.g., display an error message
                }
            }
        }
    }

    fun requestStorageAndCameraPermissions(activity: Activity) {
        val requestCode = 1
        val writeStoragePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )

        if (writeStoragePermission != PackageManager.PERMISSION_GRANTED) {
            Log.i("PermissionLog", "Storage and camera permissions are required.")
        }

        ActivityCompat.requestPermissions(activity, permissions, requestCode)
        Log.i("PermissionLog", "Requested required permissions.")
    }

    fun copyRawResourceToFile(context: Context, resourceId: Int, destinationFileName: String): File {
        val resourceInputStream = context.resources.openRawResource(resourceId)
        val destinationFile = File(context.cacheDir, destinationFileName)

        try {
            destinationFile.outputStream().use { fileOutputStream ->
                val buffer = ByteArray(1024)
                var bytesRead: Int
                while (resourceInputStream.read(buffer).also { bytesRead = it } != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead)
                }
            }
        } catch (ioException: IOException) {
            // Handle the exception (e.g., log or throw)
        } finally {
            try {
                resourceInputStream.close()
            } catch (ioException: IOException) {
                // Handle the exception (e.g., log or throw)
            }
        }

        return destinationFile
    }

    fun extractAndDivideCSVData(context: Context, resourceId: Int): Triple<FloatArray, FloatArray, FloatArray> {
        val xValues = mutableListOf<Float>()
        val yValues = mutableListOf<Float>()
        val zValues = mutableListOf<Float>()

        var currentArray: MutableList<Float>? = null

        try {
            val inputStream = context.resources.openRawResource(resourceId)
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            var line: String?

            for (line in bufferedReader.readLines()) {
                val floatValue = line.toFloatOrNull()

                if (floatValue != null && floatValue.toDouble() != 0.0) {
                    // Non-null value, add it to the current array
                    currentArray?.add(floatValue)
                } else {
                    // Found a delimiter, switch to the next array
                    currentArray = when (currentArray) {
                        zValues -> yValues
                        yValues -> xValues
                        else -> zValues
                    }
                }
            }
        } catch (exception: Exception) {
            Log.i("log", exception.printStackTrace().toString())
            exception.printStackTrace()
        }

        return Triple(zValues.toFloatArray(), yValues.toFloatArray(), xValues.toFloatArray())
    }
}

