package com.HcmDz.ElecPilot.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlcDao {

    @Query("SELECT * FROM plc_io ORDER BY favorite DESC, id ASC")
    fun getAllPlc(): Flow<List<PlcEntity>>

    @Query("SELECT * FROM plc_io ORDER BY favorite DESC, id ASC")
    suspend fun getAllPlcOnce(): List<PlcEntity>

    @Query("SELECT * FROM plc_io WHERE id = :id")
    suspend fun getPlcById(id: Long): PlcEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plc: PlcEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(plcList: List<PlcEntity>)

    @Update
    suspend fun update(plc: PlcEntity)

    @Query("UPDATE plc_io SET favorite = :favorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, favorite: Boolean)

    @Update
    suspend fun updateAll(plcList: List<PlcEntity>)

    @Delete
    suspend fun delete(plc: PlcEntity)

    @Query("DELETE FROM plc_io WHERE id IN (:ids)")
    suspend fun deletePlcByIds(ids: List<Long>)

    @Query("DELETE FROM plc_io")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM plc_io")
    suspend fun count(): Int
}
