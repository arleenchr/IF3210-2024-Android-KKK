package com.example.bondoman.ui.transaction

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.bondoman.R
import com.example.bondoman.databinding.ActivityAddTransactionBinding
import com.example.bondoman.room.TransactionDAO
import com.example.bondoman.room.TransactionDatabase
import com.example.bondoman.room.TransactionEntity
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Timestamp

class AddTransaction : AppCompatActivity() {
    private lateinit var transactionDAO: TransactionDAO
    private lateinit var autocompleteFragment: AutocompleteSupportFragment
    private lateinit var selectedPlace: Place
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val categories = arrayOf("Income", "Expense")
        transactionDAO = TransactionDatabase.getDatabase(applicationContext).transactionDAO
        val spinner: Spinner = findViewById(R.id.category)
        val saveButton = findViewById<Button>(R.id.saveButton)
        var adapter = ArrayAdapter(this, R.layout.transaction_category_item, categories)
        adapter.setDropDownViewResource(R.layout.transaction_category_popup_item)
        spinner.adapter = adapter

        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.title = "Add Transaction";
        supportActionBar?.setBackgroundDrawable(AppCompatResources.getDrawable(this,
            R.color.gray_800
        ))

        // Initialize the fused location provider client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment

        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG))

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onError(status: Status) {
                // Do nothing
            }

            override fun onPlaceSelected(place: Place) {
                selectedPlace = place
            }
        })

        // Set corner radius for the input field
        autocompleteFragment.view?.setBackgroundResource(R.drawable.input_container)

        // Set text color for the input field
        val autoCompleteTextView = autocompleteFragment.view?.findViewById<EditText>(com.google.android.libraries.places.R.id.places_autocomplete_search_input)
        autoCompleteTextView?.setTextColor(Color.WHITE)

        // Set the text size for the input field
        autoCompleteTextView?.textSize = 16f

        // Set the font for the input field
        autoCompleteTextView?.typeface = resources.getFont(R.font.urbanist_medium)

        fetchCurrentLocation { location ->
            val origin = LatLng(location.latitude, location.longitude)

            // Use Geocoder to get the address from LatLng
            val geocoder = Geocoder(this)
            val addresses = geocoder.getFromLocation(origin.latitude, origin.longitude, 1)

            // Check if address is found
            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    val placeName = addresses[0]?.getAddressLine(0) // Get the first line of the address
                    autocompleteFragment.setText(placeName)

                    // Build the Place object with the retrieved LatLng
                    val place = Place.builder()
                        .setLatLng(origin)
                        .setName(placeName)
                        .setAddress(placeName)
                        .build()

                    // Assign the Place object to selectedPlace
                    selectedPlace = place
                }
            }
        }


        saveButton.setOnClickListener {
            if (validateInputs()) {
                onSaveButtonClicked()
                Toast.makeText(this, "Successfully created transaction", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Retrieve the random_amount extra from the intent
        val randomAmount = intent.getIntExtra("random_amount", 0)
        val randomTitle = intent.getStringExtra("random_title")
        val randomCategory = intent.getIntExtra("random_category", 0)

        if (randomTitle != null) {
            binding.amount.setText(randomAmount.toString())
            binding.category.setSelection(randomCategory)
            binding.title.setText(randomTitle.toString())
        }
    }

    private fun fetchCurrentLocation(callback: (LatLng) -> Unit) {
        // Check if location permissions are granted
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request location permissions
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1001
            )
            return
        }

        // Use fused location provider client to get the current location
        fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    callback.invoke(LatLng(it.latitude, it.longitude))
                }
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun onSaveButtonClicked() {
        // Get all inputs from UI elements
        val titleEditText = findViewById<EditText>(R.id.title)
        val amountEditText = findViewById<EditText>(R.id.amount)
        val categorySpinner = findViewById<Spinner>(R.id.category)

        val title = titleEditText.text.toString()
        val amount = amountEditText.text.toString().toIntOrNull() ?: 0
        val category = categorySpinner.selectedItem.toString()

        // Create a new TransactionEntity object
        val transaction = TransactionEntity(
            title = title,
            amount = amount,
            category = category,
            location = selectedPlace,
            createdAt = Timestamp(System.currentTimeMillis())
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation { location ->
                    val origin = LatLng(location.latitude, location.longitude)

                    // Use Geocoder to get the address from LatLng
                    val geocoder = Geocoder(this)
                    val addresses = geocoder.getFromLocation(origin.latitude, origin.longitude, 1)

                    // Check if address is found
                    if (addresses != null) {
                        if (addresses.isNotEmpty()) {
                            val placeName = addresses[0]?.getAddressLine(0) // Get the first line of the address
                            autocompleteFragment.setText(placeName)

                            // Build the Place object with the retrieved LatLng
                            val place = Place.builder()
                                .setLatLng(origin)
                                .setName(placeName)
                                .setAddress(placeName)
                                .build()

                            // Assign the Place object to selectedPlace
                            selectedPlace = place

                        }
                    }
                }
            }
        }
    }

    private fun validateInputs(): Boolean {
        val titleEditText = findViewById<EditText>(R.id.title)
        val amountEditText = findViewById<EditText>(R.id.amount)

        val title = titleEditText.text.toString().trim()
        val amountText = amountEditText.text.toString().trim()

        // Validate title and amount
        val isTitleValid = validateTitle(title)
        val isAmountValid = validateAmount(amountText)

        return isTitleValid && isAmountValid && !selectedPlace.name.isNullOrEmpty()
    }

    private fun validateTitle(title: String): Boolean {
        val maxLength = 50
        return title.isNotEmpty() && title.length <= maxLength && !containsSpecialCharacters(title)
    }

    private fun validateAmount(amountText: String): Boolean {
        val amount = amountText.toIntOrNull() ?: 0
        val minAmount = 1
        val maxAmount = 1000000
        return amount in minAmount..maxAmount
    }

    private fun containsSpecialCharacters(input: String): Boolean {
        val regex = Regex("[^A-Za-z0-9\\s]")
        return regex.containsMatchIn(input)
    }
}