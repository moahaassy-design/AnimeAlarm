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
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (challenge) {
            is AlarmChallenge.ShakeChallenge -> ShakeChallengeContent(challenge, onChallengeCompleted)
            is AlarmChallenge.MathChallenge -> Text("Math Challenge (Coming Soon!)", style = MaterialTheme.typography.headlineMedium)
            AlarmChallenge.None -> {
                Text("No Challenge. Just dismiss.", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onChallengeCompleted) {
                    Text("Dismiss Alarm")
                }
            }
        }
    }

    // Stop the alarm service when ChallengeScreen is active.
    // This assumes the service is started by the AlarmReceiver and needs to be stopped here.
    // The service might be ringing, so we send a stop intent.
    DisposableEffect(Unit) {
        val stopAlarmIntent = Intent(context, AlarmRingService::class.java).apply {
            action = AlarmRingService.ACTION_STOP
        }
        context.startService(stopAlarmIntent)
        onDispose { }
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
            private val SHAKE_THRESHOLD = 800 // Adjust as needed
            private val SHAKE_INTERVAL = 500 // Min time between shakes in ms

            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null && event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]

                    val gForce = sqrt(x * x + y * y + z * z)

                    if (gForce > SHAKE_THRESHOLD && System.currentTimeMillis() - lastShakeTime > SHAKE_INTERVAL) {
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
        modifier = Modifier.fillMaxSize()
    ) {
        Text("Shake to Dismiss!", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Shakes: $shakeCount / $requiredShakes",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(32.dp))
        if (shakeCount >= requiredShakes) {
            Text("Challenge Complete!", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            // Automatically complete challenge after a short delay
            LaunchedEffect(Unit) {
                delay(1000) // Small delay for user to see "Complete"
                onChallengeCompleted()
            }
        }
    }
}
