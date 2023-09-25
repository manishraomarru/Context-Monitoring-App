package com.example.contextmonitoring

import androidx.room.TypeConverter
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class Converters {
    var date_format: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    init {
        date_format.timeZone = TimeZone.getTimeZone("America/Phoenix")
    }

    @TypeConverter
    fun dateToTimestamp(value: String?): Date? {
        return if (value == null) null else {
            try {
                return date_format.parse(value)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            null
        }
    }

    @TypeConverter
    fun fromTimestamp(date: Date?): String? {
        return if (date == null) null else date_format.format(date)
    }
}