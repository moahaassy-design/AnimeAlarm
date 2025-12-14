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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AnimeAlarmTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AnimeAlarmAppHost()
                }
            }
        }
    }
}

@Composable
fun AnimeAlarmAppHost() {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                navigateToEntry = { navController.navigate("entry") }
            )
        }
        composable("entry") {
            AlarmEntryScreen(
                navigateBack = { navController.popBackStack() }
            )
        }
    }
}