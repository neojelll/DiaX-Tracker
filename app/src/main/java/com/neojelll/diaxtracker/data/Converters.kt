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
    fun fromInsulinType(value: InsulinType?): String? = value?.name

    @TypeConverter
    fun toInsulinType(value: String?): InsulinType? = value?.let { InsulinType.valueOf(it) }
}
