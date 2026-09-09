package com.neojelll.diaxtracker.data

import android.util.Log
import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

class Converters {
    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? = value?.toString()

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? =
        value?.let {
            try {
                LocalDateTime.parse(it)
            } catch (e: DateTimeParseException) {
                // A malformed timestamp would otherwise crash every query that reads this row.
                Log.e(TAG, "Corrupt timestamp in database, substituting now(): $it", e)
                LocalDateTime.now()
            }
        }

    @TypeConverter
    fun fromSugarSource(value: SugarSource?): String? = value?.name

    @TypeConverter
    fun toSugarSource(value: String?): SugarSource? =
        value?.let {
            try {
                SugarSource.valueOf(it)
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Unknown sugar source in database, substituting null: $it", e)
                null
            }
        }

    private companion object {
        const val TAG = "Converters"
    }
}
