package com.HcmDz.ElecPilot.data.repository

import com.HcmDz.ElecPilot.data.db.MotorDao
import com.HcmDz.ElecPilot.data.db.MotorEntity
import kotlinx.coroutines.flow.Flow

class MotorRepository(private val motorDao: MotorDao) {

    fun getAllMotors(): Flow<List<MotorEntity>> = motorDao.getAllMotors()

    suspend fun getMotorById(id: Long): MotorEntity? = motorDao.getMotorById(id)

    suspend fun insert(motor: MotorEntity): Long = motorDao.insert(motor)

    suspend fun update(motor: MotorEntity) = motorDao.update(motor)

    suspend fun updateFavorite(id: Long, favorite: Boolean) = motorDao.updateFavorite(id, favorite)

    suspend fun updateAll(motors: List<MotorEntity>) = motorDao.updateAll(motors)

    suspend fun delete(motor: MotorEntity) = motorDao.delete(motor)

    suspend fun deleteMotorsByIds(ids: List<Long>) = motorDao.deleteMotorsByIds(ids)

    suspend fun deleteAll() = motorDao.deleteAll()

    suspend fun insertAll(motors: List<MotorEntity>) = motorDao.insertAll(motors)

    suspend fun count(): Int = motorDao.count()

    suspend fun getAllMotorsOnce(): List<MotorEntity> = motorDao.getAllMotorsOnce()

    suspend fun getMotorsByIds(ids: List<Long>): List<MotorEntity> = motorDao.getMotorsByIds(ids)
}
