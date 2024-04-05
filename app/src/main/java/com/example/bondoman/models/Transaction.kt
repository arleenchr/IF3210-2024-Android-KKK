package com.example.bondoman.models

import com.google.android.libraries.places.api.model.Place
import java.sql.Timestamp

data class Transaction(
    val title: String,
    val amount: Int,
    val category: String,
    val location: Place,
    val createdAt: Timestamp,
)