package com.example.bondoman.ui.pie_chart

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.bondoman.room.TransactionDAO

class PieChartViewModel(private val transactionDAO: TransactionDAO) : ViewModel() {

    fun calculateMonthlyGrowth(category: String): LiveData<Double> {
        return transactionDAO.calculateMonthlyGrowth(category)
    }

    fun hadIncomeTransactionsLastMonth(): LiveData<Boolean> =
        Transformations.map(transactionDAO.hadTransactionsLastMonth("Income")) {
            it == 1
        }

    fun hadExpenseTransactionsLastMonth(): LiveData<Boolean> =
        Transformations.map(transactionDAO.hadTransactionsLastMonth("Expense")) {
            it == 1
        }

}
