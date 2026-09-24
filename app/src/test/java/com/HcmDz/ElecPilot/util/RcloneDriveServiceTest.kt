package com.HcmDz.ElecPilot.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RcloneDriveServiceTest {

    @Test
    fun recognizesStaleTempFiles() {
        assertTrue(isStaleTempFile("rclone_conf_ab12cd34.tmp"))
        assertTrue(isStaleTempFile("cloud_restore_xyz.tmp"))
        assertTrue(isStaleTempFile("download_abc"))
        assertTrue(isStaleTempFile("upload_123"))
    }

    @Test
    fun ignoresNonTempFiles() {
        assertFalse(isStaleTempFile("rclone.conf.enc"))
        assertFalse(isStaleTempFile("settings.txt"))
        assertFalse(isStaleTempFile("cloud_file_list_cache.json"))
        assertFalse(isStaleTempFile(""))
    }

    // Contract goldens (tools/rclone/contract.md): lsf --format tsp timestamps.
    @Test
    fun parseTime_acceptsLsfAndIsoFormats() {
        assertTrue(RcloneDriveService.parseTime("2026-09-01 10:20:30") > 0)
        assertTrue(RcloneDriveService.parseTime("2026-09-01T10:20:30.000Z") > 0)
        assertTrue(RcloneDriveService.parseTime("2026-09-01T10:20:30Z") > 0)
    }

    @Test
    fun parseTime_rejectsBlankAndGarbage() {
        assertEquals(0L, RcloneDriveService.parseTime(""))
        assertEquals(0L, RcloneDriveService.parseTime("not-a-date"))
    }

    // Contract goldens: --use-json-log progress lines.
    @Test
    fun parseProgressJson_readsStatsObject() {
        val line = """{"stats":{"bytes":50.0,"totalBytes":100.0,"speed":1024,"eta":5}}"""
        val (percent, speed, eta) = RcloneDriveService.parseProgressJson(line)!!
        assertEquals(50f, percent)
        assertEquals(1024L, speed)
        assertEquals(5L, eta)
    }

    @Test
    fun parseProgressJson_rejectsNonStatsLines() {
        assertNull(RcloneDriveService.parseProgressJson("not json"))
        assertNull(RcloneDriveService.parseProgressJson("""{"level":"notice"}"""))
        assertNotNull(RcloneDriveService.parseProgressJson("""{"stats":{}}"""))
    }

    // Pin guard: intermediate primary + root backup, both scoped to the host.
    @Test
    fun graphPinner_pinsMicrosoftGraphWithBackup() {
        val pins = RcloneDriveService.graphPinner.pins
        assertEquals(2, pins.size)
        assertTrue(pins.all { it.matchesHostname("graph.microsoft.com") })
        assertTrue(pins.all { it.hashAlgorithm == "sha256" })
    }
}
