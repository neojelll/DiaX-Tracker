package com.neojelll.diaxtracker.sensor

import com.neojelll.diaxtracker.data.SensorReadingLog
import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.abs

/** The logged reading closest in time to [reference], or null if [readings] is empty. */
internal fun nearestSensorReading(readings: List<SensorReadingLog>, reference: LocalDateTime): SensorReadingLog? =
    readings.minByOrNull { abs(Duration.between(reference, it.timestamp).toMillis()) }
