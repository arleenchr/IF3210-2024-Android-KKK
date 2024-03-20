package com.example.bondoman.ui.pie_chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bondoman.room.TransactionDAO

class PieChartViewModelFactory(private val transactionDAO: TransactionDAO) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PieChartViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PieChartViewModel(transactionDAO) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
