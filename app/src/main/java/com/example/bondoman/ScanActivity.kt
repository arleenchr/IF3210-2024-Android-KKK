package com.example.bondoman

import android.Manifest
import android.content.ContentValues
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.bondoman.data.Result
import com.example.bondoman.databinding.ActivityScanBinding
import com.example.bondoman.repository.ScanRepository
import com.example.bondoman.service.RetrofitClient
import com.example.bondoman.ui.login.LoggedInUserView
import com.example.bondoman.ui.login.LoginResult
import kotlinx.coroutines.launch
import java.sql.Date
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class ScanActivity : AppCompatActivity() {

    private var binding: ActivityScanBinding? = null
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private var currentPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the permission request launcher
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                launchCamera()
            } else {
                Toast.makeText(this, "Camera permission is required to use the camera", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        // Initialize the take picture launcher
        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                setupViewAfterPhotoTaken()
            } else {
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (currentPhotoUri == null) { // Prevent relaunching if returning from background
            requestCameraPermissionAndLaunch()
        }
    }

    private fun requestCameraPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        currentPhotoUri = createImageUri()
        takePictureLauncher.launch(currentPhotoUri)
    }

    private fun setupViewAfterPhotoTaken() {
        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding?.root)

//        .launch {
//            val result = ScanRepository.scan
//
//            if (result is Result.Success) {
//                val editor: SharedPreferences.Editor = RetrofitClient.sharedPreferences.edit()
//                editor.putString("username", username)
//                editor.putString("token", result.data)
//                editor.apply()
//                _loginResult.value = LoginResult(success = LoggedInUserView(displayName = username))
//            } else if (result is Result.Error) {
//                val errorMessage = result.exception.message ?: "Unknown error"
//                _loginResult.value = LoginResult(error = R.string.login_failed)
//            }
//        }
//
//        binding?.apply {
//            amount.text = getString(R.string.rp, NumberFormat.getNumberInstance(Locale("in", "ID")).format(transaction.amount))
//            category.text = transaction.category
//            date.text = convertTimestampToDate(transaction.createdAt.time)
//            title.text = transaction.title
//            locationGmaps.text = transaction.location.address
//            time.text = convertTimestampToTime(transaction.createdAt.time)
//            total.text = getString(R.string.rp, NumberFormat.getNumberInstance(Locale("in", "ID")).format(transaction.amount))
//        }
        setupView()
    }

    private fun setupView() {
        binding?.buttonTakePhoto?.setOnClickListener {
            currentPhotoUri = createImageUri()
            takePictureLauncher.launch(currentPhotoUri)
        }

        binding?.buttonConfirm?.setOnClickListener {
            // Handle confirm action
            Toast.makeText(this, "Photo sent!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun createImageUri(): Uri {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "new_photo_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!
    }

    fun convertTimestampToDate(timestamp: Long): String {
        val date = Date(timestamp)
        val timeFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

        return timeFormat.format(date)
    }
}
