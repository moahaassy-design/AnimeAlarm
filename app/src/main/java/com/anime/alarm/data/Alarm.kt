package com.anime.alarm.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val hour: Int,
    val minute: Int,
    val label: String = "Alarm",
    val isActive: Boolean = true,
    val isVibrate: Boolean = true,
    // Format: "Mon,Tue,Wed" or simple codes "1,2,3"
    val daysOfWeek: String = "" 
)
