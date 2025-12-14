package com.anime.alarm.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize // Make sure you have the kotlin-parcelize plugin enabled in build.gradle.kts

sealed class AlarmChallenge : Parcelable {
    @Parcelize
    object None : AlarmChallenge()
    @Parcelize
    data class ShakeChallenge(val shakesRequired: Int) : AlarmChallenge()
    @Parcelize
    data class MathChallenge(val difficulty: MathDifficulty) : AlarmChallenge()
    // Anda bisa menambahkan lebih banyak jenis tantangan di sini
}

enum class MathDifficulty : Parcelable {
    @Parcelize EASY, 
    @Parcelize MEDIUM, 
    @Parcelize HARD
}
