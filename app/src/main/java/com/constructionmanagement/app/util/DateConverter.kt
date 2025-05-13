package com.constructionmanagement.app.util

import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Type converter for Room database to convert between String and Date
 */
class DateConverter {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    @TypeConverter
    fun fromString(value: String?): Date? {
        return value?.let { dateFormat.parse(it) }
    }

    @TypeConverter
    fun dateToString(date: Date?): String? {
        return date?.let { dateFormat.format(it) }
    }
}
