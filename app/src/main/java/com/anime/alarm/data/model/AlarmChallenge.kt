package com.anime.alarm.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class AlarmChallenge : Parcelable {
    @Parcelize
    object None : AlarmChallenge()
    @Parcelize
    data class ShakeChallenge(val shakesRequired: Int) : AlarmChallenge()
    @Parcelize
    data class MathChallenge(val difficulty: MathDifficulty) : Parcelable // Add @Parcelize here
}

enum class MathDifficulty { // Remove Parcelable from enum class itself
    EASY, 
    MEDIUM, 
    HARD
}