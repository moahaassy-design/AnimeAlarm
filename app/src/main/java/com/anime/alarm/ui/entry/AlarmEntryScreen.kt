package com.anime.alarm.ui.entry

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anime.alarm.ui.AppViewModelProvider
import com.anime.alarm.data.model.AlarmChallenge
import com.anime.alarm.data.model.MathDifficulty

// Anime Palette
val SakuraPink = Color(0xFFFFB7C5)
val SakuraDeep = Color(0xFFFF69B4)
val DeepPurple = Color(0xFF6A1B9A)
val SoftBackground = Color(0xFFFFF0F5) // Lavender Blush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEntryScreen(
    navigateBack: () -> Unit,
    viewModel: EntryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val alarmUiState = viewModel.alarmUiState
    
    val timePickerState = rememberTimePickerState(
        initialHour = 0,
        initialMinute = 0
    )
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = SoftBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "New Alarm", 
                        fontWeight = FontWeight.Bold, 
                        color = DeepPurple 
                    ) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Time Picker Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp).fillMaxWidth()
                ) {
                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = SoftBackground,
                            clockDialSelectedContentColor = Color.White,
                            clockDialUnselectedContentColor = DeepPurple,
                            selectorColor = SakuraDeep,
                            periodSelectorBorderColor = SakuraDeep,
                            periodSelectorSelectedContainerColor = SakuraPink.copy(alpha = 0.5f),
                            timeSelectorSelectedContainerColor = SakuraPink.copy(alpha = 0.5f),
                            timeSelectorUnselectedContainerColor = SoftBackground
                        )
                    )
                }
            }
            
            // 2. Label Input
            OutlinedTextField(
                value = alarmUiState.label,
                onValueChange = { viewModel.updateUiState(alarmUiState.copy(label = it)) },
                label = { Text("Alarm Label") },
                placeholder = { Text("Wake up, Senpai!") },
                leadingIcon = { Icon(Icons.Default.Create, contentDescription = null, tint = SakuraDeep) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SakuraDeep,
                    unfocusedBorderColor = SakuraPink,
                    focusedLabelColor = DeepPurple
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // 3. Challenge Selection Title
            Text(
                "Mission to Dismiss",
                style = MaterialTheme.typography.titleMedium,
                color = DeepPurple,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start)
            )

            // 4. Challenge Grid
            val currentChallengeType = when (alarmUiState.challenge) {
                is AlarmChallenge.None -> "None"
                is AlarmChallenge.ShakeChallenge -> "Shake"
                is AlarmChallenge.MathChallenge -> "Math"
                is AlarmChallenge.MemoryChallenge -> "Memory"
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ChallengeOptionCard(
                    title = "None",
                    icon = Icons.Default.NotificationsActive,
                    isSelected = currentChallengeType == "None",
                    color = Color.Gray,
                    onClick = { viewModel.updateUiState(alarmUiState.copy(challenge = AlarmChallenge.None)) }
                )
                ChallengeOptionCard(
                    title = "Shake",
                    icon = Icons.Default.Vibration,
                    isSelected = currentChallengeType == "Shake",
                    color = Color(0xFFE91E63),
                    onClick = { 
                        viewModel.updateUiState(alarmUiState.copy(challenge = AlarmChallenge.ShakeChallenge(20))) 
                    }
                )
                ChallengeOptionCard(
                    title = "Math",
                    icon = Icons.Default.Calculate,
                    isSelected = currentChallengeType == "Math",
                    color = Color(0xFF2196F3),
                    onClick = { 
                        viewModel.updateUiState(alarmUiState.copy(challenge = AlarmChallenge.MathChallenge(MathDifficulty.EASY))) 
                    }
                )
                 ChallengeOptionCard(
                    title = "Memory",
                    icon = Icons.Default.Visibility,
                    isSelected = currentChallengeType == "Memory",
                    color = Color(0xFF4CAF50),
                    onClick = { 
                        viewModel.updateUiState(alarmUiState.copy(challenge = AlarmChallenge.MemoryChallenge(3))) 
                    }
                )
            }

            // 5. Challenge Settings (Dynamic)
            AnimatedChallengeSettings(
                challenge = alarmUiState.challenge, 
                onUpdate = { updated -> viewModel.updateUiState(alarmUiState.copy(challenge = updated)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 6. Save Button
            Button(
                onClick = {
                    viewModel.updateUiState(
                        alarmUiState.copy(
                            hour = timePickerState.hour,
                            minute = timePickerState.minute
                        )
                    )
                    viewModel.saveAlarm()
                    navigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SakuraDeep
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Text(
                    "Set Alarm", 
                    fontSize = 18.sp, 
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.Check, contentDescription = null)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ChallengeOptionCard(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(75.dp)
            .clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(60.dp)
                .background(
                    color = if (isSelected) color.copy(alpha = 0.2f) else Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) color else Color.LightGray.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) color else Color.Gray,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) color else Color.Gray,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun AnimatedChallengeSettings(
    challenge: AlarmChallenge,
    onUpdate: (AlarmChallenge) -> Unit
) {
    if (challenge !is AlarmChallenge.None) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Mission Difficulty",
                    style = MaterialTheme.typography.labelLarge,
                    color = DeepPurple,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                when (challenge) {
                    is AlarmChallenge.ShakeChallenge -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Shakes: ${challenge.shakesRequired}", modifier = Modifier.weight(1f))
                            Slider(
                                value = challenge.shakesRequired.toFloat(),
                                onValueChange = { onUpdate(challenge.copy(shakesRequired = it.toInt())) },
                                valueRange = 5f..50f,
                                steps = 9,
                                colors = SliderDefaults.colors(thumbColor = SakuraDeep, activeTrackColor = SakuraDeep)
                            )
                        }
                    }
                    is AlarmChallenge.MathChallenge -> {
                        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                            MathDifficulty.values().forEach { diff ->
                                FilterChip(
                                    selected = challenge.difficulty == diff,
                                    onClick = { onUpdate(challenge.copy(difficulty = diff)) },
                                    label = { Text(diff.name) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SakuraPink,
                                        selectedLabelColor = DeepPurple
                                    )
                                )
                            }
                        }
                    }
                    is AlarmChallenge.MemoryChallenge -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Rounds: ${challenge.numRounds}", modifier = Modifier.weight(1f))
                            Slider(
                                value = challenge.numRounds.toFloat(),
                                onValueChange = { onUpdate(challenge.copy(numRounds = it.toInt())) },
                                valueRange = 1f..5f,
                                steps = 3,
                                colors = SliderDefaults.colors(thumbColor = SakuraDeep, activeTrackColor = SakuraDeep)
                            )
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}