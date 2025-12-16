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
    
    @Parcelize
    data class MemoryChallenge(val numRounds: Int) : AlarmChallenge()
}

enum class MathDifficulty {
    EASY, 
    MEDIUM, 
    HARD
}