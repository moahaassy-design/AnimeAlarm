package com.anime.alarm.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class AlarmChallenge : Parcelable {
    @Parcelize
    object None : AlarmChallenge()
    @Parcelize
    data class ShakeChallenge(val shakesRequired: Int) : AlarmChallenge()
    @Parcelize
    data class MathChallenge(val difficulty: MathDifficulty) : AlarmChallenge()
}

// @Parcelize is for the class, not individual enum entries.
enum class MathDifficulty : Parcelable {
    EASY, 
    MEDIUM, 
    HARD
}