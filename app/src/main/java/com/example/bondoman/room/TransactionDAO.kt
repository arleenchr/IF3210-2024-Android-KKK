package com.example.bondoman.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.bondoman.models.TransactionStats

@Dao
interface TransactionDAO {
    @Insert
    fun insertTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM `transaction` ORDER BY createdAt DESC")
    fun getAllTransaction(): LiveData<List<TransactionEntity>>

    @Query("SELECT * FROM `transaction` ORDER BY createdAt DESC")
    fun getAllTransactionsDirect(): List<TransactionEntity>

    @Query("SELECT * FROM `transaction` ORDER BY createdAt DESC LIMIT 3")
    fun getTopTransaction(): LiveData<List<TransactionEntity>>

    @Query("SELECT * FROM `transaction` WHERE id = :id")
    fun getLiveTransaction(id: Long): LiveData<TransactionEntity>

    @Query("SELECT * FROM `transaction` WHERE id = :id")
    fun getTransaction(id: Long): TransactionEntity

    @Query("SELECT \n" +
            "    SUM(CASE WHEN category = 'Income' THEN amount ELSE 0 END) AS totalIncome,\n" +
            "    SUM(CASE WHEN category = 'Expense' THEN amount ELSE 0 END) AS totalExpense\n" +
            "FROM \n" +
            "    `transaction`\n" +
            "WHERE \n" +
            "    strftime('%Y-%m', createdAt / 1000, 'unixepoch', 'localtime') = strftime('%Y-%m', 'now', 'localtime')")
    fun getTransactionStats(): LiveData<TransactionStats>

    @Update
    fun updateTransaction(transaction: TransactionEntity)

    @Delete
    fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM `transaction`")
    fun deleteAllTransaction()
}