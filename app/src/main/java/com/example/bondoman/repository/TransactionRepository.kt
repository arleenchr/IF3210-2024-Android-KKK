package com.example.bondoman.repository

import androidx.lifecycle.LiveData
import com.example.bondoman.room.*

class TransactionRepository (private val database: TransactionDatabase) {
    val listTransaction: LiveData<List<TransactionEntity>> = database.transactionDAO.getAllTransaction()

    suspend fun insertTransaction(transaction: TransactionEntity) {
        database.transactionDAO.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        database.transactionDAO.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        database.transactionDAO.deleteTransaction(transaction)
    }

//    suspend fun deleteAllTransaction() {
//        database.transactionDAO.deleteAllTransaction()
//    }
}