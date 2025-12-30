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
import androidx.compose.material.icons.filled.ShoppingCart // Import ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import com.anime.alarm.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anime.alarm.data.Alarm
import com.anime.alarm.data.model.Character
import com.anime.alarm.ui.AppViewModelProvider
import com.anime.alarm.ui.components.AdBanner
import com.anime.alarm.ui.components.MascotEmotion
import com.anime.alarm.ui.components.WaguriMascot
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateToEntry: () -> Unit = {},
    navigateToShop: () -> Unit = {} // Add new callback
) {
    val homeUiState by viewModel.homeUiState.collectAsState()
    var alarmToDelete by remember { mutableStateOf<Alarm?>(null) }

    alarmToDelete?.let { alarm ->
        AlertDialog(
            onDismissRequest = { alarmToDelete = null },
            title = { Text(text = stringResource(R.string.delete_alarm_dialog_title)) },
            text = { Text(text = stringResource(R.string.delete_alarm_dialog_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAlarm(alarm)
                        alarmToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.delete_alarm_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { alarmToDelete = null }
                ) {
                    Text(stringResource(R.string.delete_alarm_dialog_cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Anime Alarm", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                actions = {
                    IconButton(onClick = navigateToShop) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Shop",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
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
        },
        bottomBar = {
            AdBanner()
        }
    ) { innerPadding ->
        HomeBody(
            alarmList = homeUiState.alarmList,
            currentCharacter = homeUiState.currentCharacter,
            onDelete = { alarmToDelete = it },
            onToggle = viewModel::toggleAlarm,
            modifier = modifier.padding(innerPadding)
        )
    }
}

@Composable
fun HomeBody(
    alarmList: List<Alarm>,
    currentCharacter: Character? = null,
    onDelete: (Alarm) -> Unit,
    onToggle: (Alarm, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        if (alarmList.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
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
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(alarmList, key = { it.id }) { alarm ->
                    AlarmItem(alarm = alarm, onDelete = onDelete, onToggle = onToggle)
                }
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = alarm.isActive,
                    onCheckedChange = { onToggle(alarm, it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
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
