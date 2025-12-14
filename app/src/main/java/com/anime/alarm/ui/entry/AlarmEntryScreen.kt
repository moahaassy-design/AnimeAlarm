package com.anime.alarm.ui.entry

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType // Add this import
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anime.alarm.ui.AppViewModelProvider
import com.anime.alarm.data.model.AlarmChallenge
import com.anime.alarm.data.model.MathDifficulty

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

    // Challenge selection state
    var expanded by remember { mutableStateOf(false) }
    val challengeOptions = listOf("None", "Shake Challenge", "Math Challenge")
    var selectedChallengeText by remember { 
        mutableStateOf(
            when(alarmUiState.challenge) {
                AlarmChallenge.None -> "None"
                is AlarmChallenge.ShakeChallenge -> "Shake Challenge"
                is AlarmChallenge.MathChallenge -> "Math Challenge"
            }
        ) 
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("New Alarm") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TimePicker(state = timePickerState)
            
            OutlinedTextField(
                value = alarmUiState.label,
                onValueChange = { viewModel.updateUiState(alarmUiState.copy(label = it)) },
                label = { Text("Label (e.g. Wake up senpai!)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Challenge Selector
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedChallengeText,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Challenge Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    challengeOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectedChallengeText = option
                                val newChallenge = when (option) {
                                    "Shake Challenge" -> AlarmChallenge.ShakeChallenge(shakesRequired = 10) // Default
                                    "Math Challenge" -> AlarmChallenge.MathChallenge(difficulty = MathDifficulty.EASY) // Default
                                    else -> AlarmChallenge.None
                                }
                                viewModel.updateUiState(alarmUiState.copy(challenge = newChallenge))
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            // Challenge specific inputs
            when (val challenge = alarmUiState.challenge) {
                is AlarmChallenge.ShakeChallenge -> {
                    OutlinedTextField(
                        value = challenge.shakesRequired.toString(),
                        onValueChange = {
                            val shakes = it.toIntOrNull() ?: 0
                            viewModel.updateUiState(
                                alarmUiState.copy(
                                    challenge = AlarmChallenge.ShakeChallenge(shakesRequired = shakes.coerceAtLeast(1))
                                )
                            )
                        },
                        label = { Text("Shakes Required") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                is AlarmChallenge.MathChallenge -> {
                    // Math difficulty selector (basic for now)
                    var mathExpanded by remember { mutableStateOf(false) }
                    val mathDifficultyOptions = MathDifficulty.entries.map { it.name }
                    
                    ExposedDropdownMenuBox(
                        expanded = mathExpanded,
                        onExpandedChange = { mathExpanded = !mathExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = challenge.difficulty.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Math Difficulty") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = mathExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = mathExpanded,
                            onDismissRequest = { mathExpanded = false }
                        ) {
                            mathDifficultyOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        viewModel.updateUiState(
                                            alarmUiState.copy(
                                                challenge = AlarmChallenge.MathChallenge(difficulty = MathDifficulty.valueOf(option))
                                            )
                                        )
                                        mathExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                AlarmChallenge.None -> { /* No additional UI */ }
            }

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
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Alarm")
            }
        }
    }
}
