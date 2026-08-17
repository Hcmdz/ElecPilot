package com.HcmDz.ElecPilot.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CloudBackupManagerTest {

    @Test
    fun allowsReasonableSize() {
        assertTrue(CloudBackupManager.isRestoreSizeAllowed(100L))
        assertTrue(CloudBackupManager.isRestoreSizeAllowed(50L * 1024 * 1024))
    }

    @Test
    fun rejectsEmptyAndOversized() {
        assertFalse(CloudBackupManager.isRestoreSizeAllowed(0L))
        assertFalse(CloudBackupManager.isRestoreSizeAllowed(50L * 1024 * 1024 + 1))
    }
}
