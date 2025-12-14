package com.anime.alarm

import android.app.Application
import android.content.Context
import com.anime.alarm.data.AlarmDatabase
import com.anime.alarm.data.OfflineAlarmRepository

class AnimeAlarmApp : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}

interface AppContainer {
    val alarmRepository: OfflineAlarmRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val alarmRepository: OfflineAlarmRepository by lazy {
        OfflineAlarmRepository(AlarmDatabase.getDatabase(context).alarmDao())
    }
}
