package com.anime.alarm.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.anime.alarm.data.model.AlarmChallenge
import com.anime.alarm.data.model.MathDifficulty

class AlarmTypeConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromAlarmChallenge(challenge: AlarmChallenge): String {
        return gson.toJson(challenge)
    }

    @TypeConverter
    fun toAlarmChallenge(challengeString: String): AlarmChallenge {
        // This is a simple conversion. For sealed classes, you might need to
        // include type information in the JSON or use a custom deserializer
        // that checks for properties to determine the concrete type.
        // For now, we'll try to deserialize based on a simple check or default.
        if (challengeString.contains("ShakeChallenge")) {
            return gson.fromJson(challengeString, AlarmChallenge.ShakeChallenge::class.java)
        } else if (challengeString.contains("MathChallenge")) {
            return gson.fromJson(challengeString, AlarmChallenge.MathChallenge::class.java)
        }
        return AlarmChallenge.None
    }

    @TypeConverter
    fun fromMathDifficulty(difficulty: MathDifficulty): String {
        return difficulty.name
    }

    @TypeConverter
    fun toMathDifficulty(difficultyString: String): MathDifficulty {
        return MathDifficulty.valueOf(difficultyString)
    }
}
