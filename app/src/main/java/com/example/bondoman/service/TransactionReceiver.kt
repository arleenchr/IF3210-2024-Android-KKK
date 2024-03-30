package com.example.bondoman.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.bondoman.AddTransaction

class TransactionReceiver : BroadcastReceiver() {
    private companion object {
        private const val RANDOMIZE_TRANSACTIONS_ACTION = "com.example.bondoman.ACTION_RANDOMIZE_TRANSACTIONS"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == RANDOMIZE_TRANSACTIONS_ACTION) {
            val randomAmount = intent.getIntExtra("amount", 0)
            val randomTitle = intent.getStringExtra("title")
            val randomCategory = intent.getIntExtra("category", 0)
            val addTransactionIntent = Intent(context, AddTransaction::class.java)
            addTransactionIntent.putExtra("random_amount", randomAmount)
            addTransactionIntent.putExtra("random_title", randomTitle)
            addTransactionIntent.putExtra("random_category", randomCategory)
            addTransactionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context?.startActivity(addTransactionIntent)
        }
    }
}