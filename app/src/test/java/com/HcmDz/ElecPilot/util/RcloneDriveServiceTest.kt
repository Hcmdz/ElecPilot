package com.HcmDz.ElecPilot.util

import org.junit.Assert.assertFalse
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
}
