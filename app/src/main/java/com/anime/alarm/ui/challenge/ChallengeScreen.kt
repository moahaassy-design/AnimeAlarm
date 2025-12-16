package com.anime.alarm.ui.challenge

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.anime.alarm.data.model.AlarmChallenge
import com.anime.alarm.service.AlarmRingService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sqrt

@Composable
fun ChallengeScreen(challenge: AlarmChallenge, onChallengeCompleted: () -> Unit) {
    val context = LocalContext.current
    
    // Create a completion handler that stops the service and calls the callback
    val completeAndStop = {
        val stopAlarmIntent = Intent(context, AlarmRingService::class.java).apply {
            action = AlarmRingService.ACTION_STOP
        }
        context.startService(stopAlarmIntent)
        onChallengeCompleted()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.errorContainer, // Alarm context -> Reddish/Urgent
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        when (challenge) {
            is AlarmChallenge.ShakeChallenge -> ShakeChallengeContent(challenge, completeAndStop)
            is AlarmChallenge.MathChallenge -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Math Challenge (Coming Soon!)", style = MaterialTheme.typography.headlineMedium)
                    Button(onClick = completeAndStop) { Text("Skip") }
                }
            }
            AlarmChallenge.None -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No Challenge. Just dismiss.", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = completeAndStop) {
                        Text("Dismiss Alarm")
                    }
                }
            }
        }
    }
}

@Composable
fun ShakeChallengeContent(challenge: AlarmChallenge.ShakeChallenge, onChallengeCompleted: () -> Unit) {
    val context = LocalContext.current
    var shakeCount by remember { mutableStateOf(0) }
    val requiredShakes = challenge.shakesRequired
    val coroutineScope = rememberCoroutineScope()

    // Vibrator setup
    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val sensorEventListener = object : SensorEventListener {
            private var lastShakeTime: Long = 0
            private val SHAKE_THRESHOLD = 15.0f // ~1.5g
            private val SHAKE_INTERVAL = 300 // ms

            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null && event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]

                    val gForce = sqrt(x * x + y * y + z * z)

                    if (gForce > SHAKE_THRESHOLD && System.currentTimeMillis() - lastShakeTime > SHAKE_INTERVAL) {
                        // Ensure it's not just gravity (9.8), but significantly more
                        shakeCount++
                        lastShakeTime = System.currentTimeMillis()
                        
                        // Vibrate briefly on shake
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(100)
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Not used
            }
        }

        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI)

        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
            vibrator.cancel() // Stop any ongoing vibration
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().padding(32.dp)
    ) {
        Text("Shake to Dismiss!", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(32.dp))
        
        val progress = if (requiredShakes > 0) shakeCount.toFloat() / requiredShakes else 0f
        androidx.compose.material3.LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(24.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "$shakeCount / $requiredShakes",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))
        if (shakeCount >= requiredShakes) {
            Text("Challenge Complete!", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            // Automatically complete challenge after a short delay
            LaunchedEffect(Unit) {
                delay(500)
                onChallengeCompleted()
            }
        }
    }
}
