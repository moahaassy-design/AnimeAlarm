package com.anime.alarm

import android.app.Application
import android.content.Context
import com.anime.alarm.data.AlarmDatabase
import com.anime.alarm.data.OfflineAlarmRepository
import com.anime.alarm.data.AndroidAlarmScheduler
import com.anime.alarm.data.AlarmScheduler
import com.anime.alarm.data.repository.CharacterRepository
import com.anime.alarm.data.billing.BillingClientWrapper // Import Billing
import com.google.android.gms.ads.MobileAds

class AnimeAlarmApp : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
        MobileAds.initialize(this) {}
    }
}

interface AppContainer {
    val alarmRepository: OfflineAlarmRepository
    val alarmScheduler: AlarmScheduler
    val characterRepository: CharacterRepository
    val billingClient: BillingClientWrapper // Add to interface
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val alarmRepository: OfflineAlarmRepository by lazy {
        OfflineAlarmRepository(AlarmDatabase.getDatabase(context).alarmDao())
    }
    
    override val alarmScheduler: AlarmScheduler by lazy {
        AndroidAlarmScheduler(context)
    }

    override val characterRepository: CharacterRepository by lazy {
        CharacterRepository()
    }

    override val billingClient: BillingClientWrapper by lazy {
        BillingClientWrapper(context)
    }
}