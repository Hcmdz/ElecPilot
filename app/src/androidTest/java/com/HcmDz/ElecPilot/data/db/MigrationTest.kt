package com.HcmDz.ElecPilot.data.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        PlcDatabase::class.java
    )

    @get:Rule
    val motorHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun count(table: String, db: SupportSQLiteDatabase): Int =
        db.query("SELECT COUNT(*) FROM $table").use { c ->
            c.moveToFirst()
            c.getInt(0)
        }

    private fun plcRowSql() =
        "INSERT INTO plc_io (atelier, dp, carte, position, item, designation, favorite) " +
            "VALUES ('A1','DP1','C1','P1','ITEM1','DES1',0)"

    private fun motorRowSql() =
        "INSERT INTO motors (atelier, positionTGBT, item, designation, puissanceKW, types, typesDeparts, cable, typeCable, tgbt, favorite) " +
            "VALUES ('A1','P1','ITEM1','DES1','5.5','T1','TD1','C1','TC1','TGBT1',0)"

    @Test
    fun plcUpgradeFromV1_doesNotDestroyData() {
        context.deleteDatabase("plc_database")
        helper.createDatabase("plc_database", 1).apply {
            execSQL(plcRowSql())
            close()
        }
        val plcDb = PlcDatabase.getInstance(context)
        assertEquals(1, count("plc_io", plcDb.openHelper.readableDatabase))
    }

    @Test
    fun motorsUpgradeFromV2_doesNotDestroyData() {
        context.deleteDatabase("motor_database")
        motorHelper.createDatabase("motor_database", 2).apply {
            execSQL(motorRowSql())
            close()
        }
        val motorDb = AppDatabase.getInstance(context)
        assertEquals(1, count("motors", motorDb.openHelper.readableDatabase))
    }

    @Test
    fun plcUpgradeWithoutFallback_runsRegisteredMigration() {
        context.deleteDatabase("plc_test_no_fallback")
        helper.createDatabase("plc_test_no_fallback", 1).apply {
            execSQL(plcRowSql())
            close()
        }
        val plcDb = Room.databaseBuilder(context, PlcDatabase::class.java, "plc_test_no_fallback")
            .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
            .addMigrations(PlcDatabase.MIGRATION_1_2)
            .build()
        assertEquals(1, count("plc_io", plcDb.openHelper.readableDatabase))
        plcDb.close()
    }

    @Test
    fun motorsUpgradeWithoutFallback_runsRegisteredMigration() {
        context.deleteDatabase("motor_test_no_fallback")
        motorHelper.createDatabase("motor_test_no_fallback", 2).apply {
            execSQL(motorRowSql())
            close()
        }
        val motorDb = Room.databaseBuilder(context, AppDatabase::class.java, "motor_test_no_fallback")
            .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
            .addMigrations(AppDatabase.MIGRATION_2_3)
            .build()
        assertEquals(1, count("motors", motorDb.openHelper.readableDatabase))
        motorDb.close()
    }

    @Test
    fun plcUpgradeFromLegacyV1WithoutFavorite_addsColumnAndPreservesData() {
        context.deleteDatabase("plc_legacy_test")
        android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(
            context.getDatabasePath("plc_legacy_test"), null
        ).use { raw ->
            raw.version = 1
            raw.execSQL(
                "CREATE TABLE plc_io (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "atelier TEXT NOT NULL, dp TEXT NOT NULL, carte TEXT NOT NULL, " +
                    "position TEXT NOT NULL, item TEXT NOT NULL, designation TEXT NOT NULL)"
            )
            raw.execSQL(
                "INSERT INTO plc_io (atelier, dp, carte, position, item, designation) " +
                    "VALUES ('A1','DP1','C1','P1','ITEM1','DES1')"
            )
        }
        val plcDb = Room.databaseBuilder(context, PlcDatabase::class.java, "plc_legacy_test")
            .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
            .addMigrations(PlcDatabase.MIGRATION_1_2)
            .build()
        assertEquals(1, count("plc_io", plcDb.openHelper.readableDatabase))
        val hasFavorite = plcDb.openHelper.readableDatabase.query("PRAGMA table_info(plc_io)").use { c ->
            val nameIdx = c.getColumnIndex("name")
            var has = false
            while (c.moveToNext()) {
                if (c.getString(nameIdx) == "favorite") has = true
            }
            has
        }
        assertTrue(hasFavorite)
        plcDb.close()
    }
}
