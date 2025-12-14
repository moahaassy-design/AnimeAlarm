package com.anime.alarm.ui.entry

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anime.alarm.ui.AppViewModelProvider

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