package com.HcmDz.ElecPilot.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MotorDao {

    @Query("SELECT * FROM motors ORDER BY favorite DESC, id ASC")
    fun getAllMotors(): Flow<List<MotorEntity>>

    @Query("SELECT * FROM motors ORDER BY favorite DESC, id ASC")
    suspend fun getAllMotorsOnce(): List<MotorEntity>

    @Query("SELECT * FROM motors WHERE id = :id")
    suspend fun getMotorById(id: Long): MotorEntity?

    @Query("SELECT * FROM motors WHERE id IN (:ids)")
    suspend fun getMotorsByIds(ids: List<Long>): List<MotorEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(motor: MotorEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(motors: List<MotorEntity>)

    @Update
    suspend fun update(motor: MotorEntity)

    @Query("UPDATE motors SET favorite = :favorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, favorite: Boolean)

    @Update
    suspend fun updateAll(motors: List<MotorEntity>)

    @Delete
    suspend fun delete(motor: MotorEntity)

    @Query("DELETE FROM motors WHERE id IN (:ids)")
    suspend fun deleteMotorsByIds(ids: List<Long>)

    @Query("DELETE FROM motors")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM motors")
    suspend fun count(): Int
}
