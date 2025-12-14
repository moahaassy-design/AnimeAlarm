package com.anime.alarm.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anime.alarm.data.Alarm
import com.anime.alarm.data.AlarmRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(private val alarmRepository: AlarmRepository) : ViewModel() {
    
    val homeUiState: StateFlow<HomeUiState> = 
        alarmRepository.getAllAlarmsStream().map { HomeUiState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = HomeUiState()
            )

    fun toggleAlarm(alarm: Alarm, isActive: Boolean) {
        viewModelScope.launch {
            alarmRepository.updateAlarm(alarm.copy(isActive = isActive))
            // TODO: Call AlarmScheduler to schedule/cancel alarm logic here
        }
    }
    
    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            alarmRepository.deleteAlarm(alarm)
            // TODO: Cancel alarm
        }
    }
}

data class HomeUiState(val alarmList: List<Alarm> = listOf())
