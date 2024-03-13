package com.example.bondoman.models

import android.location.Location
import java.sql.Timestamp

data class Transaction (
    val title: String,
    val amount: Int,
    val category: String,
    val location: Location,
    val createdAt: Timestamp,
)