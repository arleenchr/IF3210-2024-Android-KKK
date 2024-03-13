package com.example.bondoman.room

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TransactionDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transaction")
    fun getAllTransaction(): LiveData<List<TransactionEntity>>

    @Query("SELECT * FROM transaction WHERE item = :item")
    fun getTransaction(item: String): TransactionEntity

    @Update()
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete()
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transaction")
    suspend fun deleteAllTransaction()
}