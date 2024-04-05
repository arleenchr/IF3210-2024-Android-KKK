package com.example.bondoman.ui.list_items

import com.example.bondoman.room.TransactionEntity

sealed class TransactionListItem {
    data class DateHeader(val date: String) : TransactionListItem()
    data class TransactionItem(val transaction: TransactionEntity) : TransactionListItem()
}