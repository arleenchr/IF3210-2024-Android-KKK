package com.example.bondoman

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.bondoman.databinding.ActivityAddTransactionBinding
import com.example.bondoman.databinding.ActivityEditTransactionBinding
import com.example.bondoman.databinding.ActivityViewTransactionBinding
import com.example.bondoman.models.Transaction
import com.example.bondoman.room.TransactionDAO
import com.example.bondoman.room.TransactionDatabase
import com.example.bondoman.room.TransactionEntity
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
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

class EditTransaction : AppCompatActivity() {
    private lateinit var transactionDAO: TransactionDAO
    private lateinit var autocompleteFragment: AutocompleteSupportFragment
    private lateinit var selectedPlace: Place
    private lateinit var currentTransaction: TransactionEntity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val categories = arrayOf("Income", "Expense")

        val binding = ActivityEditTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        transactionDAO = TransactionDatabase.getDatabase(applicationContext).transactionDAO
        val spinner: Spinner = findViewById(R.id.category)
        val saveButton = findViewById<Button>(R.id.saveButton)
        var adapter = ArrayAdapter(this, R.layout.transaction_category_item, categories)
        adapter.setDropDownViewResource(R.layout.transaction_category_popup_item)
        spinner.adapter = adapter

        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.title = "Edit Transaction";

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

        val transactionId = intent.getLongExtra("id", -1)
        if (transactionId != -1L) {
            currentTransaction = transactionDAO.getTransaction(transactionId)
            binding.apply {
                title.setText(currentTransaction.title)
                amount.setText(currentTransaction.amount.toString())
                category.setSelection(if (currentTransaction.category == "Income") 0 else 1)
                autocompleteFragment.setText(currentTransaction.location.name)

                val place = Place.builder()
                    .setLatLng(currentTransaction.location.latLng)
                    .setName(currentTransaction.location.name)
                    .setAddress(currentTransaction.location.address)
                    .build()

                // Assign the Place object to selectedPlace
                selectedPlace = place
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
            id = currentTransaction.id,
            title = title,
            amount = amount,
            category = category,
            location = selectedPlace,
            createdAt = Timestamp(System.currentTimeMillis())
        )

        // Insert the transaction into the database
        updateTransaction(transaction)
    }

    private fun updateTransaction(transaction: TransactionEntity) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                transactionDAO.updateTransaction(transaction)
            }
        }
    }
}