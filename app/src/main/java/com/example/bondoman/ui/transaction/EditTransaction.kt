package com.example.bondoman.ui.transaction

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import com.example.bondoman.R
import com.example.bondoman.databinding.ActivityEditTransactionBinding
import com.example.bondoman.room.TransactionDAO
import com.example.bondoman.room.TransactionDatabase
import com.example.bondoman.room.TransactionEntity
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        val adapter = ArrayAdapter(this, R.layout.transaction_category_item, categories)
        adapter.setDropDownViewResource(R.layout.transaction_category_popup_item)
        spinner.adapter = adapter

        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.title = "Edit Transaction";
        supportActionBar?.setBackgroundDrawable(AppCompatResources.getDrawable(this,
            R.color.gray_800
        ))

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

        autocompleteFragment.view?.findViewById<EditText>(com.google.android.libraries.places.R.id.places_autocomplete_search_input)?.addTextChangedListener(object:
            TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Do nothing
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    selectedPlace = Place.builder().build()
                }
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

        saveButton.setOnClickListener {
            if (validateInputs()) {
                onSaveButtonClicked()
                Toast.makeText(this, "Successfully edited transaction", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
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
            createdAt = currentTransaction.createdAt
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