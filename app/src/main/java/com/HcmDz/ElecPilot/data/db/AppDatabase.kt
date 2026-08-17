package com.HcmDz.ElecPilot.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [MotorEntity::class], version = 3, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {

    abstract fun motorDao(): MotorDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = Migration(1, 2) { db ->
            db.execSQL("ALTER TABLE motors ADD COLUMN favorite INTEGER NOT NULL DEFAULT 0")
        }

        // ponytail: v2 and v3 schemas are identical (pure version bump), so the migration is a no-op
        internal val MIGRATION_2_3 = Migration(2, 3) { db -> }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "motor_database"
                ).setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }

    }
}
