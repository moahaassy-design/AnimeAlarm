package com.anime.alarm.ui.entry

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anime.alarm.data.Alarm
import com.anime.alarm.data.AlarmRepository
import com.anime.alarm.data.AlarmScheduler
import kotlinx.coroutines.launch

class EntryViewModel(
    private val alarmRepository: AlarmRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    var alarmUiState by mutableStateOf(AlarmUiState())
        private set

    fun updateUiState(newAlarmUiState: AlarmUiState) {
        alarmUiState = newAlarmUiState
    }

    fun saveAlarm() {
        if (isValidEntry()) {
            viewModelScope.launch {
                val alarm = alarmUiState.toAlarm()
                val newId = alarmRepository.insertAlarm(alarm)
                val newAlarmWithId = alarm.copy(id = newId.toInt())
                alarmScheduler.schedule(newAlarmWithId)
            }
        }
    }
    
    private fun isValidEntry(): Boolean {
        return true // Add validation logic if needed
    }
}

data class AlarmUiState(
    val hour: Int = 0,
    val minute: Int = 0,
    val label: String = "",
    val isActive: Boolean = true
)

fun AlarmUiState.toAlarm(): Alarm = Alarm(
    hour = hour,
    minute = minute,
    label = label,
    isActive = isActive
)
