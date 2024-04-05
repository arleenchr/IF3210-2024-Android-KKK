package com.example.bondoman.ui.scan

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.example.bondoman.R
import com.example.bondoman.data.Result
import com.example.bondoman.data.ScanDataSource
import com.example.bondoman.databinding.ActivityScanBinding
import com.example.bondoman.models.ScanResponse
import com.example.bondoman.models.Transaction
import com.example.bondoman.repository.ScanRepository
import com.example.bondoman.room.TransactionDAO
import com.example.bondoman.room.TransactionDatabase
import com.example.bondoman.room.TransactionEntity
import com.example.bondoman.utils.NetworkUtils
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
    private lateinit var selectImageLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        if (checkInternetConnection()) {
            // Initialize the permission request launcher
            requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                val allGranted = permissions.all { it.value }

                if (!allGranted) {
                    Toast.makeText(this, "Camera permission is required to use the camera", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val selectedImageUri: Uri? = result.data?.data
                    if (selectedImageUri != null) {
                        // Handle the selected image URI
                        handleSelectedImage(selectedImageUri)
                        setupViewAfterPhotoTaken()
                    } else {
                        Toast.makeText(this, "Failed to retrieve selected image", Toast.LENGTH_SHORT).show()
                    }
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

            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setBackgroundDrawable(AppCompatResources.getDrawable(this,
                R.color.gray_800
            ))

            transactionDAO = TransactionDatabase.getDatabase(applicationContext).transactionDAO
        } else {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        if (currentPhotoUri == null) { // Prevent relaunching if returning from background
            requestCameraAndStoragePermissions()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun requestCameraAndStoragePermissions() {
        val permissionsToRequest = mutableListOf<String>()

        // Check if Android version is greater than 33
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) {
            // For Android versions higher than 33, only request CAMERA permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.CAMERA)
            }
        } else {
            // For Android version 33 or lower, request all permissions
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.CAMERA)
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        // Request permissions if needed
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            showImageSourceDialog()
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Take Photo", "Select from File")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose Image Source")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> launchCamera()
                1 -> launchImageSelection()
            }
            dialog.dismiss()
        }
        builder.setOnCancelListener {
            finish()
        }
        builder.show()
    }

    private fun handleSelectedImage(imageUri: Uri) {
        currentPhotoUri = imageUri
    }

    private fun launchImageSelection() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        selectImageLauncher.launch(intent)
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
                var amountValue = 0
                items?.forEach { item ->
                    amountValue += item.qty
                }
                amountValue *= 1000
                transaction = Transaction("Scan", amountValue, "Expense", place, todayTimestamp)

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
            } else if (result is Result.Error) {
                val errorMessage = result.exception.message ?: "Unknown error"
                Toast.makeText(this@ScanActivity, errorMessage, Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }
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
            showImageSourceDialog()
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

    private fun checkInternetConnection(): Boolean {
        val networkUtils = NetworkUtils(this)
        return if (networkUtils.isOnline()) {
            true
        } else {
            Toast.makeText(this, "No internet connection found.", Toast.LENGTH_SHORT).show()
            false
        }
    }


}
