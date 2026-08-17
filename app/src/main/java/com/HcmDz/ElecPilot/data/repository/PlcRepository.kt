package com.HcmDz.ElecPilot.data.repository

import com.HcmDz.ElecPilot.data.db.PlcDao
import com.HcmDz.ElecPilot.data.db.PlcEntity
import kotlinx.coroutines.flow.Flow

class PlcRepository(private val plcDao: PlcDao) {

    fun getAllPlc(): Flow<List<PlcEntity>> = plcDao.getAllPlc()

    suspend fun getAllPlcOnce(): List<PlcEntity> = plcDao.getAllPlcOnce()

    suspend fun insert(plc: PlcEntity) = plcDao.insert(plc)

    suspend fun insertAll(plcList: List<PlcEntity>) = plcDao.insertAll(plcList)

    suspend fun update(plc: PlcEntity) = plcDao.update(plc)

    suspend fun updateFavorite(id: Long, favorite: Boolean) = plcDao.updateFavorite(id, favorite)

    suspend fun updateAll(plcList: List<PlcEntity>) = plcDao.updateAll(plcList)

    suspend fun delete(plc: PlcEntity) = plcDao.delete(plc)

    suspend fun deletePlcByIds(ids: List<Long>) = plcDao.deletePlcByIds(ids)

    suspend fun deleteAll() = plcDao.deleteAll()

    suspend fun count(): Int = plcDao.count()
}
