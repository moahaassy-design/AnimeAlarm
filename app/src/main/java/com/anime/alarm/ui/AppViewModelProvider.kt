package com.anime.alarm.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.anime.alarm.AnimeAlarmApp
import com.anime.alarm.ui.home.HomeViewModel

import com.anime.alarm.ui.entry.EntryViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val app = animeAlarmApplication()
            HomeViewModel(
                app.container.alarmRepository,
                app.container.alarmScheduler,
                app.container.characterRepository
            )
        }
        initializer {
            val app = animeAlarmApplication()
            EntryViewModel(
                app.container.alarmRepository,
                app.container.alarmScheduler
            )
        }
    }
}

fun CreationExtras.animeAlarmApplication(): AnimeAlarmApp =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AnimeAlarmApp)
