package com.sravanthi.quickcart.core.database

import androidx.room.TypeConverter

internal class QuickCartConverters {
    @TypeConverter
    fun urlsToString(urls: List<String>): String = urls.joinToString(SEPARATOR)

    @TypeConverter
    fun stringToUrls(value: String): List<String> = if (value.isEmpty()) emptyList() else value.split(SEPARATOR)

    private companion object {
        // URLs never contain a raw newline.
        const val SEPARATOR = "\n"
    }
}
