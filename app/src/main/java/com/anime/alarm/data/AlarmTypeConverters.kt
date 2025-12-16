package com.anime.alarm.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.anime.alarm.data.model.AlarmChallenge
import com.anime.alarm.data.model.MathDifficulty

class AlarmTypeConverters {

    @TypeConverter
    fun fromAlarmChallenge(challenge: AlarmChallenge): String {
        return when (challenge) {
            is AlarmChallenge.None -> "None"
            is AlarmChallenge.ShakeChallenge -> "Shake|${challenge.shakesRequired}"
            is AlarmChallenge.MathChallenge -> "Math|${challenge.difficulty.name}"
        }
    }

    @TypeConverter
    fun toAlarmChallenge(value: String): AlarmChallenge {
        val parts = value.split("|")
        return when (parts[0]) {
            "Shake" -> AlarmChallenge.ShakeChallenge(parts.getOrNull(1)?.toIntOrNull() ?: 10)
            "Math" -> {
                val difficulty = try {
                    MathDifficulty.valueOf(parts.getOrNull(1) ?: "EASY")
                } catch (e: IllegalArgumentException) {
                    MathDifficulty.EASY
                }
                AlarmChallenge.MathChallenge(difficulty)
            }
            else -> AlarmChallenge.None
        }
    }

    @TypeConverter
    fun fromMathDifficulty(difficulty: MathDifficulty): String {
        return difficulty.name
    }

    @TypeConverter
    fun toMathDifficulty(difficultyString: String): MathDifficulty {
        return try {
            MathDifficulty.valueOf(difficultyString)
        } catch (e: IllegalArgumentException) {
            MathDifficulty.EASY
        }
    }
}
