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

    @Query("""
    WITH MonthlyTotals AS (
        SELECT 
            strftime('%Y-%m', createdAt / 1000, 'unixepoch', 'localtime') AS month,
            SUM(CASE WHEN category = :category THEN amount ELSE 0 END) AS total
        FROM 
            `transaction`
        GROUP BY 
            month
    ), CurrentMonth AS (
        SELECT 
            total AS current_month_total
        FROM 
            MonthlyTotals
        WHERE 
            month = strftime('%Y-%m', 'now', 'localtime')
    ), PreviousMonth AS (
        SELECT 
            total AS previous_month_total
        FROM 
            MonthlyTotals
        WHERE 
            month = strftime('%Y-%m', 'now', '-1 month', 'localtime')
    )
    SELECT 
        (IFNULL((SELECT current_month_total FROM CurrentMonth), 0) - IFNULL((SELECT previous_month_total FROM PreviousMonth), 0)) * 1.0 / 
        IFNULL((SELECT previous_month_total FROM PreviousMonth), 1) * 100 AS percentage_growth
""")
    fun calculateMonthlyGrowth(category: String): LiveData<Double>

    @Update
    fun updateTransaction(transaction: TransactionEntity)

    @Delete
    fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM `transaction`")
    fun deleteAllTransaction()
}