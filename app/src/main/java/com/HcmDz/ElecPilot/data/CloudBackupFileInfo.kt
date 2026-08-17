package com.HcmDz.ElecPilot.data

import androidx.compose.runtime.Immutable

@Immutable
data class CloudBackupFileInfo(
    val id: String,
    val name: String,
    val size: Long,
    val modifiedTime: Long,
    val mimeType: String,
    val webViewLink: String? = null
)
