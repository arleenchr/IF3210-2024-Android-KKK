package com.example.bondoman.room

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TransactionDAO {
    @Insert
    fun insertTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM `transaction` ORDER BY createdAt DESC")
    fun getAllTransaction(): LiveData<List<TransactionEntity>>

    @Query("SELECT * FROM `transaction` ORDER BY createdAt DESC LIMIT 5")
    fun getTopTransaction(): LiveData<List<TransactionEntity>>

    @Query("SELECT * FROM `transaction` WHERE id = :id")
    fun getTransaction(id: Long): TransactionEntity

    @Update
    fun updateTransaction(transaction: TransactionEntity)

    @Delete
    fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM `transaction`")
    fun deleteAllTransaction()
}