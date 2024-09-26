package com.example.assignme.DataClass

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration
import android.content.Context

@Database(entities = [TrackerRecord::class], version = 2, exportSchema = false)
@TypeConverters(LocalDateConverter::class) // Keep this line for type conversion
abstract class TrackerDatabase : RoomDatabase() {
    abstract fun trackerRecordDao(): TrackerRecordDao

    companion object {
        @Volatile
        private var INSTANCE: TrackerDatabase? = null

        fun getDatabase(context: Context): TrackerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TrackerDatabase::class.java,
                    "tracker_database" // Change this to your desired database name
                )
                    .addMigrations(MIGRATION_1_2) // Add migration here
                    .fallbackToDestructiveMigration() // Optionally allow destructive migration
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Define migration from version 1 to version 2
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Example: Add a new column
                // Modify this line based on your actual schema change
                database.execSQL("ALTER TABLE TrackerRecord ADD COLUMN new_column_name INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
