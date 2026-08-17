package com.HcmDz.ElecPilot.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [PlcEntity::class], version = 2, exportSchema = true)
abstract class PlcDatabase : RoomDatabase() {

    abstract fun plcDao(): PlcDao

    companion object {
        @Volatile
        private var INSTANCE: PlcDatabase? = null

        // ponytail: idempotent so it survives either v1 history (with or without the favorite column)
        internal val MIGRATION_1_2 = Migration(1, 2) { db ->
            val columns = mutableSetOf<String>()
            db.query("PRAGMA table_info(plc_io)").use { c ->
                val nameIdx = c.getColumnIndex("name")
                while (c.moveToNext()) columns.add(c.getString(nameIdx))
            }
            if ("favorite" !in columns) {
                db.execSQL("ALTER TABLE plc_io ADD COLUMN favorite INTEGER NOT NULL DEFAULT 0")
            }
            db.execSQL("CREATE INDEX IF NOT EXISTS index_plc_io_favorite_id ON plc_io (favorite, id)")
        }

        fun getInstance(context: Context): PlcDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlcDatabase::class.java,
                    "plc_database"
                ).setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
