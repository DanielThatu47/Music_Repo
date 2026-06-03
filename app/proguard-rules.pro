-keep class com.danielthatu.musicplayer.models.** { *; }
-keep class com.danielthatu.musicplayer.database.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase { *; }

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { <init>(...); }
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** { **[] $VALUES; public *; }

# Media3 / ExoPlayer
-keep class com.google.android.exoplayer2.** { *; }
-keep class androidx.media3.** { *; }

# Kotlin serialization
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
