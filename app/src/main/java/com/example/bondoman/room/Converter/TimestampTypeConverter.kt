package com.example.bondoman.room.Converter

import androidx.room.TypeConverter
import java.sql.Timestamp

class TimestampTypeConverter {

    @TypeConverter
    fun fromTimestamp(value: Long?): Timestamp? {
        return value?.let { Timestamp(it) }
    }

    @TypeConverter
    fun toTimestamp(timestamp: Timestamp?): Long? {
        return timestamp?.time
    }
}