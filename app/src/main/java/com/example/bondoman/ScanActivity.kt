package com.example.bondoman

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.example.bondoman.data.Result
import com.example.bondoman.data.ScanDataSource
import com.example.bondoman.databinding.ActivityScanBinding
import com.example.bondoman.models.ScanResponse
import com.example.bondoman.models.Transaction
import com.example.bondoman.repository.ScanRepository
import com.example.bondoman.room.TransactionDAO
import com.example.bondoman.room.TransactionDatabase
import com.example.bondoman.room.TransactionEntity
import com.google.android.filament.View
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Date
import java.sql.Timestamp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale


class ScanActivity : AppCompatActivity() {

    private var binding: ActivityScanBinding? = null
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private var currentPhotoUri: Uri? = null
    private lateinit var transaction: Transaction
    private lateinit var transactionDAO: TransactionDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the permission request launcher
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.all { it.value }

            if (allGranted) {
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

        transactionDAO = TransactionDatabase.getDatabase(applicationContext).transactionDAO
    }

    override fun onResume() {
        super.onResume()
        if (currentPhotoUri == null) { // Prevent relaunching if returning from background
            requestCameraAndStoragePermissions()
        }
    }

    private fun requestCameraAndStoragePermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            // All permissions are granted, proceed with launching the camera
            launchCamera()
        }
    }

    private fun launchCamera() {
        currentPhotoUri = createImageUri()
        takePictureLauncher.launch(currentPhotoUri)
    }

    private fun setupViewAfterPhotoTaken() {
        binding = ActivityScanBinding.inflate(layoutInflater)

        showLoading()
        setContentView(binding?.root)

        var response: ScanResponse?

        // Initialize createdAt with the current timestamp representing today
        val todayTimestamp = Timestamp(System.currentTimeMillis())

        lifecycleScope.launch {
            val result = currentPhotoUri?.let { getPath(it)?.toUri()?.let { ScanRepository(ScanDataSource()).scan(it) } }

            if (result is Result.Success) {
                response = result.data
                val items = response?.items?.items
                val latitude = 37.7749
                val longitude = -122.4194
                val placeName = "Dummy Place"
                val place = Place.builder()
                    .setLatLng(LatLng(latitude, longitude))
                    .setName(placeName)
                    .setAddress(placeName)
                    .build()
                var amount = 0
                items?.forEach { item ->
                    amount += item.qty
                }
                transaction = Transaction("Scan", amount, "Expense", place, todayTimestamp)
            } else if (result is Result.Error) {
                val errorMessage = result.exception.message ?: "Unknown error"
                Toast.makeText(this@ScanActivity, errorMessage, Toast.LENGTH_SHORT).show()
            }

            binding?.apply {
                transaction.let { txn ->
                    amount.text = getString(
                        R.string.rp,
                        NumberFormat.getNumberInstance(Locale("in", "ID")).format(txn.amount)
                    )
                    category.text = txn.category
                    date.text = convertTimestampToDate(txn.createdAt.time)
                    title.text = txn.title
                    locationGmaps.text = txn.location.address
                    time.text = convertTimestampToTime(txn.createdAt.time)
                    total.text = getString(
                        R.string.rp,
                        NumberFormat.getNumberInstance(Locale("in", "ID")).format(txn.amount)
                    )
                }
            }
            setupView()
            hideLoading()
        }
    }

    private fun showLoading() {
        // Show loading indicator, e.g., progress bar
        binding?.mainDetail?.visibility = android.view.View.GONE
        binding?.progressBar?.visibility = android.view.View.VISIBLE
    }

    private fun hideLoading() {
        // Hide loading indicator, e.g., progress bar
        binding?.progressBar?.visibility = android.view.View.GONE
        binding?.mainDetail?.visibility = android.view.View.VISIBLE
    }

    private fun setupView() {
        binding?.buttonTakePhoto?.setOnClickListener {
            currentPhotoUri = createImageUri()
            takePictureLauncher.launch(currentPhotoUri)
        }

        binding?.buttonConfirm?.setOnClickListener {
            onSaveButtonClicked()
            Toast.makeText(this, "Successfully created transaction", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun onSaveButtonClicked() {
        // Create a new TransactionEntity object
        val transaction = TransactionEntity(
            title = transaction.title,
            amount = transaction.amount,
            category = transaction.category,
            location = transaction.location,
            createdAt = transaction.createdAt
        )

        // Insert the transaction into the database
        insertTransaction(transaction)
    }

    private fun insertTransaction(transaction: TransactionEntity) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                transactionDAO.insertTransaction(transaction)
            }
        }
    }

    private fun createImageUri(): Uri {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "new_photo_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!
    }

    private fun convertTimestampToDate(timestamp: Long): String {
        val date = Date(timestamp)
        val timeFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

        return timeFormat.format(date)
    }

    private fun convertTimestampToTime(timestamp: Long): String {
        val date = Date(timestamp)
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        return timeFormat.format(date)
    }

    private fun getPath(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor? = applicationContext.contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            val columnIndex: Int = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            it.moveToFirst()
            return it.getString(columnIndex)
        }
        return null
    }
}
