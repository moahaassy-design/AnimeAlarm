package com.anime.alarm.data

import kotlinx.coroutines.flow.Flow

interface AlarmRepository {
    fun getAllAlarmsStream(): Flow<List<Alarm>>
    suspend fun getAlarmStream(id: Int): Alarm?
    suspend fun insertAlarm(alarm: Alarm)
    suspend fun deleteAlarm(alarm: Alarm)
    suspend fun updateAlarm(alarm: Alarm)
}

class OfflineAlarmRepository(private val alarmDao: AlarmDao) : AlarmRepository {
    override fun getAllAlarmsStream(): Flow<List<Alarm>> = alarmDao.getAllAlarms()
    override suspend fun getAlarmStream(id: Int): Alarm? = alarmDao.getAlarmById(id)
    override suspend fun insertAlarm(alarm: Alarm) = alarmDao.insertAlarm(alarm)
    override suspend fun deleteAlarm(alarm: Alarm) = alarmDao.deleteAlarm(alarm)
    override suspend fun updateAlarm(alarm: Alarm) = alarmDao.updateAlarm(alarm)
}
