package com.anime.alarm.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anime.alarm.data.Alarm
import com.anime.alarm.ui.AppViewModelProvider
import java.util.Locale
import com.anime.alarm.ui.components.WaguriMascot
import com.anime.alarm.ui.components.MascotEmotion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateToEntry: () -> Unit = {} // Placeholder for navigation
) {
    val homeUiState by viewModel.homeUiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Anime Alarm", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = navigateToEntry,
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Alarm")
            }
        }
    ) { innerPadding ->
        HomeBody(
            alarmList = homeUiState.alarmList,
            currentCharacter = homeUiState.currentCharacter,
            onDelete = viewModel::deleteAlarm,
            onToggle = viewModel::toggleAlarm,
            modifier = modifier.padding(innerPadding)
        )
    }
}

import com.anime.alarm.data.model.Character

@Composable
fun HomeBody(
    alarmList: List<Alarm>,
    currentCharacter: Character? = null,
    onDelete: (Alarm) -> Unit,
    onToggle: (Alarm, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    if (alarmList.isEmpty()) {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WaguriMascot(
                modifier = Modifier.size(200.dp),
                emotion = MascotEmotion.SLEEPY,
                assets = currentCharacter?.assets
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Alarms set.\nGanbatte! Add one!",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(alarmList, key = { it.id }) { alarm ->
                AlarmItem(alarm = alarm, onDelete = onDelete, onToggle = onToggle)
            }
        }
    }
}

@Composable
fun AlarmItem(
    alarm: Alarm,
    onDelete: (Alarm) -> Unit,
    onToggle: (Alarm, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = String.format(Locale.getDefault(), "%02d:%02d", alarm.hour, alarm.minute),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = alarm.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = alarm.isActive,
                    onCheckedChange = { onToggle(alarm, it) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { onDelete(alarm) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}