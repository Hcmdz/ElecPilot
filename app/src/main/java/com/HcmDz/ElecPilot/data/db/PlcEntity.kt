package com.HcmDz.ElecPilot.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.compose.runtime.Immutable

@Immutable
@Entity(tableName = "plc_io", indices = [Index(value = ["favorite", "id"])])
data class PlcEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val atelier: String = "",
    val dp: String = "",
    val carte: String = "",
    val position: String = "",
    val item: String = "",
    val designation: String = "",
    val favorite: Boolean = false
)
