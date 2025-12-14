# ProGuard rules for AnimeAlarm

# Room
-keep class androidx.room.RoomDatabase { *; }
-keep class androidx.room.RoomDatabase$Builder { *; }
-keep interface androidx.room.TypeConverter

# Retrofit (if added later)
#-keep class retrofit2.** { *; }

# Parcelable
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
