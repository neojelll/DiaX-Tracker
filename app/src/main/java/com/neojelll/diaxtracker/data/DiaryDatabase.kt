package com.neojelll.diaxtracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [DiaryEntry::class, MealPreset::class, MealPresetProduct::class],
    version = 8,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class DiaryDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao
    abstract fun mealPresetDao(): MealPresetDao

    companion object {
        @Volatile
        private var INSTANCE: DiaryDatabase? = null

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE diary_entries ADD COLUMN sugarSource TEXT")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE diary_entries ADD COLUMN breadUnits REAL")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE diary_entries ADD COLUMN photoPath TEXT")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS meal_presets (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "name TEXT NOT NULL, " +
                        "breadUnits REAL NOT NULL)"
                )
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS meal_preset_products (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "mealPresetId INTEGER NOT NULL, " +
                        "name TEXT NOT NULL, " +
                        "breadUnits REAL NOT NULL, " +
                        "sortOrder INTEGER NOT NULL, " +
                        "FOREIGN KEY(mealPresetId) REFERENCES meal_presets(id) ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_meal_preset_products_mealPresetId " +
                        "ON meal_preset_products(mealPresetId)"
                )
                db.execSQL(
                    "INSERT INTO meal_preset_products (mealPresetId, name, breadUnits, sortOrder) " +
                        "SELECT id, name, breadUnits, 0 FROM meal_presets"
                )

                db.execSQL(
                    "CREATE TABLE meal_presets_new (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "name TEXT NOT NULL, " +
                        "comment TEXT NOT NULL DEFAULT '')"
                )
                db.execSQL(
                    "INSERT INTO meal_presets_new (id, name, comment) " +
                        "SELECT id, name, '' FROM meal_presets"
                )
                db.execSQL("DROP TABLE meal_presets")
                db.execSQL("ALTER TABLE meal_presets_new RENAME TO meal_presets")
            }
        }

        fun getDatabase(context: Context): DiaryDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    DiaryDatabase::class.java,
                    "diary_database"
                ).addMigrations(
                    MIGRATION_3_4,
                    MIGRATION_4_5,
                    MIGRATION_5_6,
                    MIGRATION_6_7,
                    MIGRATION_7_8
                ).build().also { INSTANCE = it }
            }
        }
    }
}
