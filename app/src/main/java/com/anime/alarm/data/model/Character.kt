package com.anime.alarm.data.model

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes

data class Character(
    val id: String,
    val name: String,
    val description: String,
    val isPremium: Boolean = false,
    val isUnlocked: Boolean = false,
    val assets: CharacterAssets
)

data class CharacterAssets(
    @DrawableRes val normalImage: Int,
    @DrawableRes val sleepyImage: Int,
    @DrawableRes val happyImage: Int,
    // Kita akan gunakan RawRes untuk suara nanti, saat ini placeholder 0
    @RawRes val alarmSound: Int = 0
)
