package com.mindseek.podcast

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltAndroidApp
class PodcastApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Global crash logger — write stack trace to file for debugging
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            val stackTrace = sw.toString()

            Log.e("NioPodcast", "FATAL CRASH in thread ${thread.name}", throwable)

            try {
                val crashDir = File(filesDir, "crashes")
                crashDir.mkdirs()
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val crashFile = File(crashDir, "crash_$timestamp.txt")
                crashFile.writeText("""
                    Time: ${Date()}
                    Thread: ${thread.name}
                    ${stackTrace}
                """.trimIndent())
            } catch (_: Exception) { }

            // Forward to default handler (which will kill the app)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
