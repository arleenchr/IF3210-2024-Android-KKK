package com.example.bondoman

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class TwibbonActivity : AppCompatActivity() {
    private lateinit var viewFinder: PreviewView
    private lateinit var overlayView: ImageView
    private lateinit var imageCapture: ImageCapture

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_twibbon)

        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.title = "Twibbon";
        supportActionBar?.setBackgroundDrawable(AppCompatResources.getDrawable(this, R.color.gray_800))

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        viewFinder = findViewById(R.id.previewView)
        overlayView = findViewById(R.id.overlayView)

        onBackPressedDispatcher.addCallback(this) {
            onRetake()
        }

        findViewById<MaterialButton>(R.id.buttonRetake).setOnClickListener {
            onRetake()
        }

        findViewById<MaterialButton>(R.id.buttonSave).setOnClickListener {
            val drawable = overlayView.drawable
            if (drawable is BitmapDrawable) {
                val bitmap = drawable.bitmap
                saveImageToGallery(bitmap)
            }
        }

        findViewById<FloatingActionButton>(R.id.twibbonFab).setOnClickListener {
            takePhoto()
        }
    }

    private fun takePhoto() {

        // Create temporary file to hold the image
        val photoFile = File(
            externalMediaDirs.firstOrNull(),
            "JPEG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())}.jpg"
        )

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has been taken
        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(baseContext, "Photo capture failed: ${exc.message}", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                    val source = ImageDecoder.createSource(contentResolver, savedUri)
                    val capturedBitmap = ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }

                    val overlayDrawable = ContextCompat.getDrawable(baseContext, R.drawable.twibbon_overlay) as BitmapDrawable
                    val overlayBitmap = overlayDrawable.bitmap

                    val combinedImage = combineImages(capturedBitmap, overlayBitmap)

                    runOnUiThread {
                        // Display the combined image
                        overlayView.setImageBitmap(combinedImage)

                        viewFinder.visibility = View.GONE
                        findViewById<FloatingActionButton>(R.id.twibbonFab).visibility = View.GONE
                        findViewById<View>(R.id.captureFab).visibility = View.GONE

                        findViewById<LinearLayout>(R.id.twibbonActions).visibility = View.VISIBLE
                    }
                }
            })
    }

    private fun combineImages(capturedBitmap: Bitmap, overlay: Bitmap): Bitmap {
        val mutableBitmap = if (capturedBitmap.isMutable) {
            capturedBitmap
        } else {
            capturedBitmap.copy(Bitmap.Config.ARGB_8888, true)
        }

        val canvas = Canvas(mutableBitmap)
        val overlayScaledWidth = capturedBitmap.width
        val overlayScaledHeight = capturedBitmap.height

        val overlayResized = Bitmap.createScaledBitmap(overlay, overlayScaledWidth, overlayScaledHeight, true)
        canvas.drawBitmap(overlayResized, 0f, 0f, null)
        return mutableBitmap
    }


    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            // CameraProvider
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Initialize the Preview use case
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            // Initialize the ImageCapture use case here and use it later
            imageCapture = ImageCapture.Builder().build()

            try {
                // Unbind all use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
            } catch(exc: Exception) {
                Toast.makeText(this, "Failed to start camera", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun onRetake() {
        if (viewFinder.visibility == View.GONE) {
            viewFinder.visibility = View.VISIBLE
            findViewById<LinearLayout>(R.id.twibbonActions).visibility = View.GONE
            findViewById<FloatingActionButton>(R.id.twibbonFab).visibility = View.VISIBLE
            findViewById<View>(R.id.captureFab).visibility = View.VISIBLE
            overlayView.setImageResource(R.drawable.twibbon_overlay)
        } else {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun saveImageToGallery(bitmap: Bitmap) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "Twibbon")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val contentResolver = contentResolver
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            contentResolver.openOutputStream(it).use { outputStream ->
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
            }

            Toast.makeText(this, "Image saved to gallery", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
