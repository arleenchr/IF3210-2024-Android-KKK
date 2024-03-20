package com.example.bondoman.ui.pie_chart

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.bondoman.room.TransactionDAO

class PieChartViewModel(private val transactionDAO: TransactionDAO) : ViewModel() {

    fun calculateMonthlyGrowth(category: String): LiveData<Double> {
        return transactionDAO.calculateMonthlyGrowth(category)
    }
}
