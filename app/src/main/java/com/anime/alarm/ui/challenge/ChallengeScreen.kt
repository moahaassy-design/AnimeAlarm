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
import android.util.Log
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anime.alarm.R
import com.anime.alarm.data.model.AlarmChallenge
import com.anime.alarm.data.model.MathDifficulty
import com.anime.alarm.service.AlarmRingService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sqrt
import kotlin.random.Random

// Reusing colors for consistency
val SakuraPink = Color(0xFFFFB7C5)
val SakuraDeep = Color(0xFFFF69B4)
val DeepPurple = Color(0xFF6A1B9A)
val ErrorRed = Color(0xFFD32F2F)

@Composable
fun ChallengeScreen(challenge: AlarmChallenge, onChallengeCompleted: () -> Unit) {
    val context = LocalContext.current
    
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
                        DeepPurple,
                        Color.Black
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Character Sprite Display
        var challengeCompletedAnim by remember { mutableStateOf(false) }

        // Change sprite to happy when challenge is completed
        val charNormal = painterResource(R.drawable.char_normal)
        val charHappy = painterResource(R.drawable.char_happy)
        val characterImage = if (challengeCompletedAnim) charHappy else charNormal
        
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top // Align content to top for character placement
        ) {
            // Animated Character Image
            AnimatedVisibility(
                visible = true, // Always visible on challenge screen
                enter = fadeIn(animationSpec = tween(durationMillis = 500)),
                exit = fadeOut(animationSpec = tween(durationMillis = 500))
            ) {
                Image(
                    painter = characterImage,
                    contentDescription = "Character",
                    modifier = Modifier
                        .size(200.dp)
                        .scale(if (challengeCompletedAnim) 1.2f else 1.0f) // Simple reaction on completion
                        .animateContentSize() // Animate size changes
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Main Challenge Content
            when (challenge) {
                is AlarmChallenge.ShakeChallenge -> ShakeChallengeContent(challenge) {
                    challengeCompletedAnim = true
                    completeAndStop()
                }
                is AlarmChallenge.MathChallenge -> MathChallengeContent(challenge) {
                    challengeCompletedAnim = true
                    completeAndStop()
                }
                is AlarmChallenge.MemoryChallenge -> MemoryChallengeContent(challenge) {
                    challengeCompletedAnim = true
                    completeAndStop()
                }
                AlarmChallenge.None -> {
                    Button(
                        onClick = {
                            challengeCompletedAnim = true
                            completeAndStop()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SakuraDeep),
                        modifier = Modifier.padding(16.dp).fillMaxWidth().height(60.dp)
                    ) {
                        Text("Dismiss Alarm", fontSize = 20.sp)
                    }
                }
            }
        }
    }
}

// --- SHAKE CHALLENGE ---
@Composable
fun ShakeChallengeContent(challenge: AlarmChallenge.ShakeChallenge, onComplete: () -> Unit) {
    val context = LocalContext.current
    var shakeCount by remember { mutableIntStateOf(0) }
    var sensorMissing by remember { mutableStateOf(false) }
    val requiredShakes = challenge.shakesRequired
    
    // Animation for the progress bar
    val progress by animateFloatAsState(
        targetValue = shakeCount.toFloat() / requiredShakes,
        label = "progress"
    )

    DisposableEffect(Unit) {
        var sensorManager: SensorManager? = null
        var accelerometer: Sensor? = null
        var vibrator: Vibrator? = null

        try {
            sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                 @Suppress("DEPRECATION") 
                 context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback if system services fail
        }

        if (sensorManager == null || accelerometer == null) {
            Log.e("AnimeAlarm", "Accelerometer not available! Auto-completing challenge.")
            sensorMissing = true
            onDispose { }
        } else {
            // Capture vibrator safely for use inside listener
            val safeVibrator = vibrator
            
            val sensorEventListener = object : SensorEventListener {
                private var lastShakeTime: Long = 0
                private val SHAKE_THRESHOLD = 12.0f 

                override fun onSensorChanged(event: SensorEvent?) {
                    if (event != null) {
                        val x = event.values[0]
                        val y = event.values[1]
                        val z = event.values[2]
                        val gForce = sqrt(x * x + y * y + z * z)

                        if (gForce > SHAKE_THRESHOLD && System.currentTimeMillis() - lastShakeTime > 500) {
                            shakeCount++
                            lastShakeTime = System.currentTimeMillis()
                            // Safe vibration call
                            try {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    safeVibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                                } else {
                                    @Suppress("DEPRECATION")
                                    safeVibrator?.vibrate(50)
                                }
                            } catch (e: Exception) {
                                // Ignore vibration errors
                            }
                        }
                    }
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI)
            onDispose { sensorManager.unregisterListener(sensorEventListener) }
        }
    }

    if (sensorMissing) {
        LaunchedEffect(Unit) {
            delay(2000)
            onComplete()
        }
        Text("No Accelerometer detected.\nAuto-completing...", color = ErrorRed, textAlign = TextAlign.Center)
    }

    if (shakeCount >= requiredShakes) {
        LaunchedEffect(Unit) { onComplete() }
    }

    if (!sensorMissing) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("SHAKE IT!", style = MaterialTheme.typography.displayLarge, color = SakuraPink, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(32.dp))
            
            // Circular Progress
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                CircularProgressIndicator(
                    progress = 1f,
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White.copy(alpha = 0.2f),
                    trackColor = Color.Transparent,
                )
                CircularProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxSize(),
                    color = SakuraDeep,
                    strokeWidth = 12.dp,
                )
                Text(
                    "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )
            }
        }
    }
}

// --- MATH CHALLENGE ---
@Composable
fun MathChallengeContent(challenge: AlarmChallenge.MathChallenge, onComplete: () -> Unit) {
    var question by remember { mutableStateOf(generateMathQuestion(challenge.difficulty)) }
    var input by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("SOLVE THIS!", color = SakuraPink, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        
        // Display Question
        Text(
            question.text,
            color = Color.White,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Display Input
        Text(
            input.ifEmpty { "?" },
            color = if (isError) ErrorRed else SakuraDeep,
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                .padding(horizontal = 32.dp, vertical = 12.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Numpad
        val buttons = listOf(
            "1", "2", "3",
            "4", "5", "6",
            "7", "8", "9",
            "C", "0", "OK"
        )
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            buttons.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { btn ->
                        Button(
                            onClick = {
                                when (btn) {
                                    "C" -> { input = ""; isError = false }
                                    "OK" -> {
                                        Log.d("AnimeAlarm", "Math Input: $input, Expected: ${question.answer}")
                                        if (input == question.answer.toString()) {
                                            onComplete()
                                        } else {
                                            isError = true
                                            input = ""
                                        }
                                    }
                                    else -> {
                                        if (input.length < 5) input += btn
                                        isError = false
                                    }
                                }
                            },
                            modifier = Modifier.size(80.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (btn == "OK") SakuraDeep else Color.White.copy(alpha = 0.2f)
                            )
                        ) {
                            Text(btn, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

data class MathQuestion(val text: String, val answer: Int)

fun generateMathQuestion(difficulty: MathDifficulty): MathQuestion {
    val r = Random
    return when (difficulty) {
        MathDifficulty.EASY -> {
            val a = r.nextInt(1, 10)
            val b = r.nextInt(1, 10)
            MathQuestion("$a + $b", a + b)
        }
        MathDifficulty.MEDIUM -> {
            val a = r.nextInt(10, 50)
            val b = r.nextInt(1, 10)
            MathQuestion("$a - $b", a - b) // Changed to subtraction for variety but simple
        }
        MathDifficulty.HARD -> {
            val a = r.nextInt(2, 10)
            val b = r.nextInt(2, 10)
            val c = r.nextInt(1, 20)
            MathQuestion("$a x $b + $c", (a * b) + c)
        }
    }
}


// --- MEMORY CHALLENGE ---
@Composable
fun MemoryChallengeContent(challenge: AlarmChallenge.MemoryChallenge, onComplete: () -> Unit) {
    val roundsTotal = challenge.numRounds
    var currentRound by remember { mutableIntStateOf(1) }
    var pattern by remember { mutableStateOf(generatePattern(3)) } // Start with 3 items
    val userPattern = remember { mutableStateListOf<Int>() }
    var showingPattern by remember { mutableStateOf(true) }
    var flashIndex by remember { mutableIntStateOf(-1) } // Which index is currently glowing
    var message by remember { mutableStateOf("Watch carefully!") }

    // Game Logic Loop
    LaunchedEffect(currentRound, showingPattern) {
        if (showingPattern) {
            userPattern.clear()
            message = "Watch..."
            delay(1000)
            pattern.forEach { index ->
                flashIndex = index
                delay(600) // Glow time
                flashIndex = -1
                delay(200) // Gap
            }
            showingPattern = false
            message = "Repeat!"
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Round $currentRound / $roundsTotal", color = SakuraPink, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text(message, color = Color.White, style = MaterialTheme.typography.headlineMedium)
        
        Spacer(modifier = Modifier.height(32.dp))

        // Grid 3x3
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (row in 0 until 3) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (col in 0 until 3) {
                        val index = row * 3 + col
                        val isGlowing = flashIndex == index
                        
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isGlowing) SakuraDeep else Color.White.copy(alpha = 0.2f)
                                )
                                .clickable(enabled = !showingPattern) {
                                    // User tap logic
                                    launchSoundEffect() // Placeholder
                                    userPattern.add(index)
                                    
                                    // Check immediately if wrong
                                    if (userPattern[userPattern.lastIndex] != pattern[userPattern.lastIndex]) {
                                        // Wrong tap! Reset pattern and restart round
                                        Log.d("AnimeAlarm", "Memory: Wrong tap. Resetting pattern.")
                                        message = "Wrong! Try Again."
                                        userPattern.clear()
                                        pattern = generatePattern(3 + currentRound - 1) // Generate new pattern for same level
                                        showingPattern = true
                                        return@clickable
                                    }

                                    // Check if complete
                                    if (userPattern.size == pattern.size) {
                                        Log.d("AnimeAlarm", "Memory: Pattern complete.")
                                        if (currentRound >= roundsTotal) {
                                            onComplete()
                                        } else {
                                            currentRound++
                                            pattern = generatePattern(3 + currentRound) // Harder
                                            showingPattern = true
                                        }
                                    }
                                }
                        )
                    }
                }
            }
        }
    }
}

fun generatePattern(length: Int): List<Int> {
    return List(length) { Random.nextInt(0, 9) }
}

fun launchSoundEffect() {
    // Placeholder for sound
}
