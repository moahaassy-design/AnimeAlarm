package com.anime.alarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.extras == null) {
            Log.w("AnimeAlarm", "AlarmReceiver received intent with no extras! Using defaults.")
        }
        
        val message = intent.getStringExtra("ALARM_LABEL") ?: "Alarm!"
        val alarmId = intent.getIntExtra("ALARM_ID", -1)
        
        Log.d("AnimeAlarm", "Alarm Triggered: $message (ID: $alarmId)")
        
        // Tampilkan Toast sebagai indikator awal
        Toast.makeText(context, "ANIME ALARM: $message", Toast.LENGTH_LONG).show()
        
        // Start Foreground Service
        val serviceIntent = Intent(context, com.anime.alarm.service.AlarmRingService::class.java).apply {
            putExtras(intent) // Forward all extras (ID, Label, Challenge)
        }
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
