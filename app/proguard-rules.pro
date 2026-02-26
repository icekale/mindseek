# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Retrofit interfaces
-keep interface com.mindseek.podcast.data.remote.api.** { *; }

# Keep data classes used with Gson
-keep class com.mindseek.podcast.data.remote.dto.** { *; }
-keep class com.mindseek.podcast.data.local.entity.** { *; }

# Keep Room entities and DAOs
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel

# Keep ExoPlayer classes
-keep class com.google.android.exoplayer2.** { *; }
-dontwarn com.google.android.exoplayer2.**

# Keep Compose classes
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**