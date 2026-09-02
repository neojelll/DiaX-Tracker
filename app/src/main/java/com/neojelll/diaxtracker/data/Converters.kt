package com.neojelll.diaxtracker.data

import androidx.room.TypeConverter
import java.time.LocalDateTime

class Converters {
    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? = value?.toString()

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? =
        value?.let { LocalDateTime.parse(it) }

    @TypeConverter
    fun fromSugarSource(value: SugarSource?): String? = value?.name

    @TypeConverter
    fun toSugarSource(value: String?): SugarSource? = value?.let { SugarSource.valueOf(it) }
}
