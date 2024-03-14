package com.example.bondoman

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.bondoman.databinding.ActivityScanBinding

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
        setupView()
        // Display the captured image
        binding?.imageViewPhoto?.setImageURI(currentPhotoUri)
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
}
