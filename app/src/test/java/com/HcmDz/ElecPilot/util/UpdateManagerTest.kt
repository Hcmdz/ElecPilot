package com.HcmDz.ElecPilot.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class UpdateManagerTest {

    @Test
    fun parseStandardSha256File() {
        val text = "75bf25abfe35f9caa8e090cda2912bb472201a3d41df50794ed599ac21ddfc03  ElecPilot-release.apk\n"
        assertEquals(
            "75bf25abfe35f9caa8e090cda2912bb472201a3d41df50794ed599ac21ddfc03",
            UpdateManager.parseSha256Asset(text, "ElecPilot-release.apk")
        )
    }

    @Test
    fun parsePrefersLineMentioningApk() {
        val text = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa  other.apk\n" +
            "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb  ElecPilot-release.apk\n"
        assertEquals(
            "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb",
            UpdateManager.parseSha256Asset(text, "ElecPilot-release.apk")
        )
    }

    @Test
    fun parseGarbageReturnsNull() {
        assertNull(UpdateManager.parseSha256Asset("not a checksum", "ElecPilot-release.apk"))
        assertNull(UpdateManager.parseSha256Asset("", "ElecPilot-release.apk"))
        assertNull(UpdateManager.parseSha256Asset("SHA-256: xyz", "ElecPilot-release.apk"))
    }

    @Test
    fun updateFileGuard() {
        val dir = File("/cache/updates")
        assertTrue(UpdateManager.isUpdateFile(dir, File("/cache/updates/ElecPilot-release.apk")))
        assertFalse(UpdateManager.isUpdateFile(dir, File("/cache/other.apk")))
        assertFalse(UpdateManager.isUpdateFile(dir, File("/cache/updates/../other.apk")))
        assertFalse(UpdateManager.isUpdateFile(dir, File("/cache/updates-evil/x.apk")))
    }
}
