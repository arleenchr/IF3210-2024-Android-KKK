package com.example.bondoman

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.bondoman.databinding.ActivityViewTransactionBinding
import com.example.bondoman.room.TransactionDAO
import com.example.bondoman.room.TransactionDatabase
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Locale

class ViewTransaction : AppCompatActivity() {
    private lateinit var transactionDAO: TransactionDAO
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityViewTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.title = "Transaction Details";

        transactionDAO = TransactionDatabase.getDatabase(applicationContext).transactionDAO

        val transactionId = intent.getLongExtra("id", -1)
        if (transactionId != -1L) {
            // Observe the LiveData object returned by getTransaction
            transactionDAO.getLiveTransaction(transactionId).observe(this) { transaction ->
                transaction?.let {
                    binding.apply {
                        title.text = transaction.title
                        category.text = transaction.category
                        date.text = convertTimestampToDate(transaction.createdAt.time)
                        title.text = transaction.title
                        locationGmaps.text = transaction.location.address
                        time.text = convertTimestampToTime(transaction.createdAt.time)
                        total.text = getString(R.string.rp, transaction.amount.toString())

                        // Set click listener for locationGmaps TextView
                        locationGmaps.setOnClickListener {
                            transaction.location.name?.let { it1 -> openLocationURL(it1) }
                        }

                        // Set click listener for delete button
                        deleteButton.setOnClickListener {
                            transactionDAO.deleteTransaction(transaction)
                            Toast.makeText(applicationContext, "Successfully deleted transaction", Toast.LENGTH_SHORT).show()
                            finish()
                        }

                        // Set click listener for edit button
                        editButton.setOnClickListener {
                            val intent = Intent(applicationContext, EditTransaction::class.java)
                            intent.putExtra("id", transactionId)
                            startActivity(intent)
                        }
                    }
                }
            }
        }
    }

    fun convertTimestampToTime(timestamp: Long): String {
        val date = Date(timestamp)
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        return timeFormat.format(date)
    }

    fun convertTimestampToDate(timestamp: Long): String {
        val date = Date(timestamp)
        val timeFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

        return timeFormat.format(date)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun openLocationURL(name: String) {
        val locationUrl = "https://www.google.com/maps/search/?api=1&query=$name"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(locationUrl))
        startActivity(intent)
    }
}