package com.anime.alarm.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.anime.alarm.MainActivity // Explicitly import MainActivity
import com.anime.alarm.R
import com.anime.alarm.data.model.AlarmChallenge

class AlarmRingService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        val label = intent?.getStringExtra("ALARM_LABEL") ?: "Alarm"
        val alarmId = intent?.getIntExtra("ALARM_ID", -1) ?: -1
        
        val challenge = intent?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getParcelableExtra("ALARM_CHALLENGE", AlarmChallenge::class.java)
            } else {
                @Suppress("DEPRECATION")
                it.getParcelableExtra("ALARM_CHALLENGE") as? AlarmChallenge
            }
        } ?: AlarmChallenge.None
        
        startForeground(NOTIFICATION_ID, buildNotification(label, alarmId, challenge))
        startRinging()
        startVibrating()

        // Launch the ChallengeScreen/MainActivity
        val fullScreenIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_LABEL", label)
            putExtra("ALARM_CHALLENGE", challenge)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(fullScreenIntent)

        return START_STICKY
    }

    private fun startRinging() {
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, alarmUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startVibrating() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibrator = vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        val pattern = longArrayOf(0, 1000, 1000)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    private fun stopRinging() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        vibrator?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRinging()
    }

    private fun buildNotification(label: String, alarmId: Int, challenge: AlarmChallenge): Notification {
        val channelId = "ALARM_SERVICE_CHANNEL"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alarm Service",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(null, null) // Service manages sound manually
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_LABEL", label)
            putExtra("ALARM_CHALLENGE", challenge)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, alarmId, intent, // Use alarmId for request code
            PendingIntent.FLAG_IMMUTABLE
        )
        
        // Intent to stop alarm
        val stopIntent = Intent(this, AlarmRingService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Anime Alarm Triggered!")
            .setContentText(label)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .addAction(R.drawable.ic_launcher_foreground, "Dismiss", stopPendingIntent)
            .build()
    }

    companion object {
        const val NOTIFICATION_ID = 1
        const val ACTION_STOP = "STOP_ALARM"
    }
}