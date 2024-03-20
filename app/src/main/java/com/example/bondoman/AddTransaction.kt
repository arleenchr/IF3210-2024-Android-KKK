package com.example.bondoman

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
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.bondoman.room.TransactionDAO
import com.example.bondoman.room.TransactionDatabase
import com.example.bondoman.room.TransactionEntity
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
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
        val categories = arrayOf("Income", "Expense")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        transactionDAO = TransactionDatabase.getDatabase(applicationContext).transactionDAO
        val spinner: Spinner = findViewById(R.id.category)
        val saveButton = findViewById<Button>(R.id.saveButton)
        var adapter = ArrayAdapter(this, R.layout.transaction_category_item, categories)
        adapter.setDropDownViewResource(R.layout.transaction_category_popup_item)
        spinner.adapter = adapter

        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.title = "Add Transaction";

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

        // Set background color
        autocompleteFragment.view?.setBackgroundColor(Color.parseColor("#313131"))

        // Set text color for the input field
        val autoCompleteTextView = autocompleteFragment.view?.findViewById<EditText>(com.google.android.libraries.places.R.id.places_autocomplete_search_input)
        autoCompleteTextView?.setTextColor(Color.WHITE)

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
            onSaveButtonClicked()
            Toast.makeText(applicationContext, "Successfully created transaction", Toast.LENGTH_SHORT).show()
            finish()
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
}