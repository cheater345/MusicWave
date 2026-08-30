# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keep class com.musicwave.data.model.** { *; }
-keep class com.musicwave.data.api.** { *; }
-keep class com.musicwave.playback.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public static ** INSTANCE;
}
-dontwarn io.ktor.**
-dontwarn kotlinx.serialization.**
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keep class io.ktor.** { *; }
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-keep class okio.** { *; }
-dontwarn okio.**
-keep class com.google.android.exoplayer2.** { *; }
-keep class androidx.media3.** { *; }