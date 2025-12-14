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

import com.anime.alarm.data.AlarmScheduler

class HomeViewModel(
    private val alarmRepository: AlarmRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {
    
    val homeUiState: StateFlow<HomeUiState> = 
        alarmRepository.getAllAlarmsStream().map { HomeUiState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = HomeUiState()
            )

    fun toggleAlarm(alarm: Alarm, isActive: Boolean) {
        viewModelScope.launch {
            val updatedAlarm = alarm.copy(isActive = isActive)
            alarmRepository.updateAlarm(updatedAlarm)
            
            if (isActive) {
                alarmScheduler.schedule(updatedAlarm)
            } else {
                alarmScheduler.cancel(updatedAlarm)
            }
        }
    }
    
    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            alarmScheduler.cancel(alarm)
            alarmRepository.deleteAlarm(alarm)
        }
    }
}

data class HomeUiState(val alarmList: List<Alarm> = listOf())
