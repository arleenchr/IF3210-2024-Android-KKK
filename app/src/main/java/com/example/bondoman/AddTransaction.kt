package com.example.bondoman

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.content.ContentProviderCompat.requireContext

class AddTransaction : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val categories = arrayOf("Income", "Outcome")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        val spinner: Spinner = findViewById(R.id.category)
        var adapter = ArrayAdapter(this, R.layout.transaction_category_item, categories)
        adapter.setDropDownViewResource(R.layout.transaction_category_popup_item)
//        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }
}