package com.anime.alarm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anime.alarm.ui.home.HomeScreen
import com.anime.alarm.ui.entry.AlarmEntryScreen
import com.anime.alarm.ui.theme.AnimeAlarmTheme
import com.anime.alarm.ui.challenge.ChallengeScreen
import com.anime.alarm.ui.shop.ShopScreen // Import ShopScreen
import com.anime.alarm.data.model.AlarmChallenge
import android.os.Build
import android.view.WindowManager
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.net.Uri
import com.anime.alarm.util.BatteryOptimizationHelper
import com.anime.alarm.util.BatteryOptimizationDialog
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import com.anime.alarm.data.model.MathDifficulty // Add this import

import android.util.Log // Add import

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request Exact Alarm Permission for Android 12+ (API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:$packageName")
                }
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Failed to open exact alarm settings", e)
                }
            }
        }

        // Make activity show over lock screen and turn screen on
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED) // Deprecated but good for older APIs


        setContent {
            AnimeAlarmTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Reconstruct challenge from primitives to avoid Parcelable crash with Sealed Classes
                    val challengeType = intent.getStringExtra("CHALLENGE_TYPE")
                    val challengeVal = intent.getIntExtra("CHALLENGE_VAL", 0)

                    val challenge = when (challengeType) {
                        "SHAKE" -> AlarmChallenge.ShakeChallenge(challengeVal)
                        "MATH" -> AlarmChallenge.MathChallenge(MathDifficulty.values().getOrElse(challengeVal) { MathDifficulty.EASY })
                        "MEMORY" -> AlarmChallenge.MemoryChallenge(challengeVal)
                        else -> AlarmChallenge.None
                    }

                    val alarmId = intent.getIntExtra("ALARM_ID", -1)

                    if (challenge != AlarmChallenge.None && alarmId != -1) {
                        ChallengeScreen(
                            challenge = challenge,
                            onChallengeCompleted = {
                                // Once challenge is completed, navigate back to home or finish activity
                                finish() // For now, just finish to dismiss the challenge
                            }
                        )
                    } else {
                        AnimeAlarmAppHost()
                    }
                }
            }
        }
    }
}

@Composable
fun AnimeAlarmAppHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val helper = remember { BatteryOptimizationHelper(context) }
    
    // Check if we already showed the warning
    val sharedPrefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    var showBatteryDialog by remember { 
        mutableStateOf(
            helper.isVendorSpecificOptimizationNeeded() && 
            !sharedPrefs.getBoolean("battery_opt_shown", false)
        ) 
    }

    if (showBatteryDialog) {
        BatteryOptimizationDialog(
            onDismiss = {
                showBatteryDialog = false
                sharedPrefs.edit().putBoolean("battery_opt_shown", true).apply()
            },
            onConfirm = {
                showBatteryDialog = false
                sharedPrefs.edit().putBoolean("battery_opt_shown", true).apply()
                try {
                    val intent = helper.getOptimizationIntent()
                    if (intent != null) {
                        context.startActivity(intent)
                    }
                } catch (e: Exception) {
                    // Fallback if specific intent fails
                    try {
                        context.startActivity(Intent(Settings.ACTION_SETTINGS))
                    } catch (e2: Exception) {
                        e2.printStackTrace()
                    }
                }
            }
        )
    }
    
    NavHost(navController = navController, startDestination = "home") {
        // ... existing routes ...
        composable("home") {
            HomeScreen(
                navigateToEntry = { navController.navigate("entry") },
                navigateToShop = { navController.navigate("shop") }
            )
        }
        composable("entry") {
            AlarmEntryScreen(
                navigateBack = { navController.popBackStack() }
            )
        }
        composable("shop") {
            ShopScreen(
                navigateBack = { navController.popBackStack() }
            )
        }
    }
}