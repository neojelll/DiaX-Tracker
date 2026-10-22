package com.neojelll.diaxtracker.screenshots

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.neojelll.diaxtracker.data.CarbDisplay
import com.neojelll.diaxtracker.data.CarbGoal
import com.neojelll.diaxtracker.data.CarbGoalStore
import com.neojelll.diaxtracker.data.CarbSettingsStore
import com.neojelll.diaxtracker.data.CarbUnit
import com.neojelll.diaxtracker.data.DiaryDatabase
import com.neojelll.diaxtracker.data.DiaryEntry
import com.neojelll.diaxtracker.data.DiaryEntryProduct
import com.neojelll.diaxtracker.data.DisclaimerStore
import com.neojelll.diaxtracker.data.InsulinSettingsStore
import com.neojelll.diaxtracker.data.MealPreset
import com.neojelll.diaxtracker.data.MealPresetProduct
import com.neojelll.diaxtracker.data.SugarSource
import com.neojelll.diaxtracker.sensor.SensorReadingStore
import com.neojelll.diaxtracker.time.AppClock
import java.io.File
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.runBlocking

/**
 * A made-up diary for the screenshot stand: every feature has something to show, no real data
 * anywhere. Time is stopped at Tue 21 Oct 2026, 14:30, so "Добрый день!", "только что" and
 * "замеры придут в 15:20" read the same on every run.
 *
 * Written straight through the DAOs and the stores of the app under test (`.debug`, its own
 * database) - the UI and the view model are untouched.
 */
object DemoFixtures {
    val zone: ZoneId = ZoneId.of("Europe/Moscow")
    val now: LocalDateTime = LocalDateTime.of(2026, 10, 21, 14, 30)

    /** How the sensor looks on the home screen. */
    enum class Sensor { FRESH, STALE, NONE }

    private const val GRAMS_PER_XE = 12f
    private fun xe(n: Double) = (n * GRAMS_PER_XE).toFloat()

    fun install(context: Context, sensor: Sensor = Sensor.FRESH) {
        AppClock.clock = Clock.fixed(now.atZone(zone).toInstant(), zone)
        DisclaimerStore(context).setAccepted()
        CarbSettingsStore(context).save(CarbDisplay(CarbUnit.XE, GRAMS_PER_XE))
        CarbGoalStore(context).save(CarbGoal(enabled = true, grams = 150f))
        InsulinSettingsStore(context).saveDurationHours(4f)
        val sensorStore = SensorReadingStore(context)
        when (sensor) {
            Sensor.FRESH -> sensorStore.save(6.9f, millis(now.minusMinutes(2)))
            Sensor.STALE -> sensorStore.save(7.4f, millis(now.minusMinutes(48)))
            // Older than the three days a sensor counts as "in use" - the tile shows no data.
            Sensor.NONE -> sensorStore.save(6.1f, millis(now.minusDays(5)))
        }

        val db = DiaryDatabase.getDatabase(context)
        runBlocking {
            db.clearAllTables()
            presets(db)
            diary(db, photo(context))
        }
    }

    private fun millis(at: LocalDateTime) = at.atZone(zone).toInstant().toEpochMilli()

    private suspend fun presets(db: DiaryDatabase) {
        val dao = db.mealPresetDao()
        suspend fun preset(name: String, comment: String, vararg products: Pair<String, Double>) {
            val id = dao.insert(MealPreset(name = name, comment = comment))
            dao.insertProducts(products.mapIndexed { i, (n, x) -> MealPresetProduct(mealPresetId = id, name = n, carbsGrams = xe(x), sortOrder = i) })
        }
        preset("Овсянка", "дома по утрам", "овсяные хлопья" to 2.5, "банан" to 0.5)
        preset("Гречка с курицей", "", "гречка" to 3.0, "курица" to 0.0, "лук, морковь" to 1.0, "соус" to 1.0)
        preset("Сырники", "со сметаной", "творог" to 0.5, "мука" to 2.0, "сметана" to 0.5)
        preset("Паста с соусом", "", "паста" to 4.0, "соус" to 1.0)
        preset("Йогурт", "перекус", "йогурт" to 1.5)
    }

    private suspend fun diary(db: DiaryDatabase, photoPath: String) {
        val dao = db.diaryDao()
        val today = now.toLocalDate()
        val yesterday = today.minusDays(1)
        fun at(day: java.time.LocalDate, h: Int, m: Int) = day.atTime(h, m)

        suspend fun entry(
            createdAt: LocalDateTime,
            sugar: Float? = null,
            source: SugarSource? = sugar?.let { SugarSource.MANUAL },
            carbsXe: Double? = null,
            preset: String? = null,
            products: List<Pair<String, Double>> = emptyList(),
            short: Float? = null,
            long: Float? = null,
            notes: String = "",
            photo: String? = null
        ): Long = dao.insertWithProducts(
            DiaryEntry(
                bloodSugar = sugar, sugarSource = source, carbsGrams = carbsXe?.let { xe(it) }, mealLabel = preset,
                shortInsulinDose = short, longInsulinDose = long, notes = notes, photoPath = photo, createdAt = createdAt
            ),
            products.mapIndexed { i, (n, x) -> DiaryEntryProduct(diaryEntryId = 0, name = n, carbsGrams = xe(x), sortOrder = i) }
        )

        /** The automatic sugar checks of a meal, +1h..+4h; null = not taken yet. */
        suspend fun checks(mealId: Long, mealAt: LocalDateTime, vararg sugars: Float?) {
            sugars.forEachIndexed { i, sugar ->
                if (sugar != null) {
                    dao.insert(
                        DiaryEntry(
                            bloodSugar = sugar, sugarSource = SugarSource.SENSOR, shortInsulinDose = null, longInsulinDose = null,
                            notes = "", createdAt = mealAt.plusHours(i + 1L), sourceEntryId = mealId, sourceHour = i + 1
                        )
                    )
                }
            }
        }

        // Yesterday: the carbohydrate goal (12.5 XE) reached, a low at night with a note.
        entry(at(yesterday, 3, 15), sugar = 3.6f, source = SugarSource.SENSOR, notes = "Проснулся ночью, съел 1 ХЕ")
        entry(at(yesterday, 8, 10), sugar = 5.8f, carbsXe = 3.0, preset = "Овсянка", products = listOf("овсяные хлопья" to 2.5, "банан" to 0.5), short = 4f)
        // 13:50 - within the hour around "now" a day later, so the notifications sheet has it.
        entry(at(yesterday, 13, 50), sugar = 6.0f, source = SugarSource.SENSOR, carbsXe = 4.0, short = 5f)
        entry(at(yesterday, 16, 40), sugar = 5.2f, carbsXe = 1.5, preset = "Йогурт", products = listOf("йогурт" to 1.5))
        entry(at(yesterday, 19, 30), sugar = 6.6f, source = SugarSource.SENSOR, carbsXe = 5.0, preset = "Гречка с курицей",
            products = listOf("гречка" to 3.0, "курица" to 0.0, "лук, морковь" to 1.0, "соус" to 1.0), short = 6f)
        entry(at(yesterday, 22, 0), sugar = 7.1f, source = SugarSource.SENSOR, long = 18f)

        // Today: long insulin, a breakfast with everything inside, a lunch whose checks are still coming,
        // a correction for a high sugar.
        entry(at(today, 7, 50), sugar = 6.2f, source = SugarSource.SENSOR, long = 18f)
        val breakfastAt = at(today, 8, 20)
        val breakfast = entry(breakfastAt, sugar = 10.6f, carbsXe = 3.0, preset = "Овсянка",
            products = listOf("овсяные хлопья" to 2.5, "банан" to 0.5), short = 4f, notes = "Половину банана оставил на потом", photo = photoPath)
        checks(breakfast, breakfastAt, 9.1f, 8.0f, 6.9f, 6.4f)
        val lunchAt = at(today, 12, 20)
        val lunch = entry(lunchAt, sugar = 7.4f, source = SugarSource.SENSOR, carbsXe = 4.0, short = 5f, notes = "Гулял после обеда минут 40")
        checks(lunch, lunchAt, 9.6f, 8.4f, null, null)
        entry(at(today, 13, 35), sugar = 10.9f, short = 2f)
    }

    /** A drawn plate standing in for a photo of food - no real picture in the repo. */
    private fun photo(context: Context): String {
        val size = 600
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawColor(Color.rgb(140, 130, 115))
        paint.color = Color.rgb(201, 183, 156); canvas.drawCircle(300f, 300f, 270f, paint)
        paint.color = Color.rgb(239, 227, 207); canvas.drawCircle(300f, 300f, 235f, paint)
        paint.color = Color.rgb(217, 160, 102); canvas.drawCircle(235f, 250f, 95f, paint)
        paint.color = Color.rgb(123, 155, 74); canvas.drawCircle(375f, 360f, 80f, paint)
        paint.color = Color.rgb(245, 214, 120); canvas.drawCircle(360f, 205f, 45f, paint)
        val file = File(context.filesDir, "photos/demo-breakfast.jpg").apply { parentFile?.mkdirs() }
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        return file.absolutePath
    }
}
