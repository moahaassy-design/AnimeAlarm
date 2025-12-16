package com.anime.alarm.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.anime.alarm.MainActivity
import com.anime.alarm.R
import com.anime.alarm.data.model.AlarmChallenge
import com.anime.alarm.data.model.MathDifficulty

class AlarmRingService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AnimeAlarm::RingServiceWakelock")
        wakeLock?.acquire(10 * 60 * 1000L /*10 minutes*/)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        val label = intent?.getStringExtra("ALARM_LABEL") ?: "Alarm"
        val alarmId = intent?.getIntExtra("ALARM_ID", -1) ?: -1
        
        // Reconstruct challenge from primitives
        val currentChallenge = intent?.let { extractChallenge(it) } ?: AlarmChallenge.None
        
        val notification = buildNotification(label, alarmId, currentChallenge)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        
        startRinging()
        startVibrating()

        return START_STICKY
    }

    private fun extractChallenge(intent: Intent): AlarmChallenge {
        val type = intent.getStringExtra("CHALLENGE_TYPE") ?: "NONE"
        val value = intent.getIntExtra("CHALLENGE_VAL", 0)
        
        return when (type) {
            "SHAKE" -> AlarmChallenge.ShakeChallenge(value)
            "MATH" -> AlarmChallenge.MathChallenge(MathDifficulty.values().getOrElse(value) { MathDifficulty.EASY })
            "MEMORY" -> AlarmChallenge.MemoryChallenge(value)
            else -> AlarmChallenge.None
        }
    }

    private fun startRinging() {
        try {
            // Priority 1: Play Anime Character Voice (Ara Ara~)
            mediaPlayer = MediaPlayer.create(applicationContext, R.raw.char_getup).apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback: System Alarm Sound
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
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private fun startVibrating() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibrator = vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            val pattern = longArrayOf(0, 1000, 1000)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
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
                setSound(null, null)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_LABEL", label)
            
            // Pass Primitives to MainActivity
            when (challenge) {
                is AlarmChallenge.ShakeChallenge -> {
                    putExtra("CHALLENGE_TYPE", "SHAKE")
                    putExtra("CHALLENGE_VAL", challenge.shakesRequired)
                }
                is AlarmChallenge.MathChallenge -> {
                    putExtra("CHALLENGE_TYPE", "MATH")
                    putExtra("CHALLENGE_VAL", challenge.difficulty.ordinal)
                }
                is AlarmChallenge.MemoryChallenge -> {
                    putExtra("CHALLENGE_TYPE", "MEMORY")
                    putExtra("CHALLENGE_VAL", challenge.numRounds)
                }
                else -> {
                    putExtra("CHALLENGE_TYPE", "NONE")
                }
            }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, alarmId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Intent to stop alarm
        val stopIntent = Intent(this, AlarmRingService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Anime Alarm Triggered!")
            .setContentText(label)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .addAction(R.drawable.ic_launcher_foreground, "Dismiss", stopPendingIntent)
            .build()
    }

    companion object {
        const val NOTIFICATION_ID = 1
        const val ACTION_STOP = "STOP_ALARM"
    }
}
