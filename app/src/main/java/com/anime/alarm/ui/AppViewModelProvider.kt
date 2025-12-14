package com.anime.alarm.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.anime.alarm.AnimeAlarmApp
import com.anime.alarm.ui.home.HomeViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            HomeViewModel(animeAlarmApplication().container.alarmRepository)
        }
    }
}

fun CreationExtras.animeAlarmApplication(): AnimeAlarmApp =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AnimeAlarmApp)
