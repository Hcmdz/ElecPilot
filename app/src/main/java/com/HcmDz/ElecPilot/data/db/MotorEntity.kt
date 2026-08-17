package com.HcmDz.ElecPilot.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.compose.runtime.Immutable

@Immutable
@Entity(
    tableName = "motors",
    indices = [Index(value = ["favorite", "id"])]
)
data class MotorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val atelier: String = "",
    val positionTGBT: String = "",
    val item: String = "",
    val designation: String = "",
    val puissanceKW: String = "",
    val types: String = "",
    val typesDeparts: String = "",
    val cable: String = "",
    val typeCable: String = "",
    val tgbt: String = "",
    val favorite: Boolean = false
)
