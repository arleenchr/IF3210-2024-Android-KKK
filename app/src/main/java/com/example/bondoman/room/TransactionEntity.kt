package com.example.bondoman.room

import android.location.Location
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Timestamp

@Entity(tableName = "transaction", primaryKeys = ["title", "createdAt"])
data class TransactionEntity (
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        var id: Long = 0,

        @ColumnInfo(name = "title")
        var title: String = "",

        @ColumnInfo(name = "amount")
        var amount: Int = 0,

        @ColumnInfo(name = "category")
        var category: String = "",

        @ColumnInfo(name = "location")
        var location: Location,

        @ColumnInfo(name = "createdAt")
        var createdAt: Timestamp,
)