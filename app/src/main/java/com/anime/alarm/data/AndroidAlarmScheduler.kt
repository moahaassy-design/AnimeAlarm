package com.anime.alarm.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.anime.alarm.receiver.AlarmReceiver
import java.time.LocalDateTime
import java.time.ZoneId

class AndroidAlarmScheduler(
    private val context: Context
) : AlarmScheduler {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule(alarm: Alarm) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", alarm.id)
            putExtra("ALARM_LABEL", alarm.label)
            putExtra("ALARM_CHALLENGE", alarm.challenge) // Pass the challenge object
        }
        
        // Flag immutable wajib di Android 12+
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Hitung waktu alarm berikutnya
        val now = LocalDateTime.now()
        var alarmTime = now.withHour(alarm.hour).withMinute(alarm.minute).withSecond(0).withNano(0)

        // Jika waktu sudah lewat hari ini, jadwalkan untuk besok
        if (alarmTime.isBefore(now)) {
            alarmTime = alarmTime.plusDays(1)
        }
        
        // Konversi ke Millis
        val triggerAtMillis = alarmTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        Log.d("AnimeAlarm", "Scheduling alarm ${alarm.id} at $alarmTime")

        // Gunakan setExactAndAllowWhileIdle agar alarm bunyi meski HP doze mode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
             alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    override fun cancel(alarm: Alarm) {
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                alarm.id,
                Intent(context, AlarmReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        Log.d("AnimeAlarm", "Cancelled alarm ${alarm.id}")
    }
}
