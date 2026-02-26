package com.mindseek.podcast.core.debug

import android.content.Context
import android.os.Build
import android.util.Log
import com.mindseek.podcast.BuildConfig
import com.mindseek.podcast.core.performance.MemoryOptimizer
import com.mindseek.podcast.core.performance.PerformanceMonitor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Debug utilities for development and testing
 */
@Singleton
class DebugUtils @Inject constructor(
    @ApplicationContext private val context: Context,
    private val performanceMonitor: PerformanceMonitor,
    private val memoryOptimizer: MemoryOptimizer
) {
    
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    /**
     * Check if debug mode is enabled
     */
    fun isDebugMode(): Boolean {
        return BuildConfig.DEBUG
    }
    
    /**
     * Log debug information
     */
    fun logDebugInfo(tag: String, message: String) {
        if (isDebugMode()) {
            Log.d(tag, message)
        }
    }
    
    /**
     * Log verbose information
     */
    fun logVerbose(tag: String, message: String) {
        if (isDebugMode()) {
            Log.v(tag, message)
        }
    }
    
    /**
     * Log error with stack trace
     */
    fun logError(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
        
        // Write to debug log file in debug mode
        if (isDebugMode()) {
            writeToDebugLog("ERROR", tag, message, throwable)
        }
    }
    
    /**
     * Generate debug report
     */
    fun generateDebugReport(): DebugReport {
        val memoryInfo = memoryOptimizer.getMemoryInfo()
        val cacheStats = memoryOptimizer.getCacheStats()
        val performanceMetrics = performanceMonitor.getMetrics()
        
        return DebugReport(
            timestamp = System.currentTimeMillis(),
            appVersion = BuildConfig.VERSION_NAME,
            buildType = BuildConfig.BUILD_TYPE,
            deviceInfo = getDeviceInfo(),
            memoryInfo = memoryInfo,
            cacheStats = cacheStats,
            performanceMetrics = performanceMetrics.values.toList(),
            logEntries = getRecentLogEntries()
        )
    }
    
    /**
     * Export debug report to file
     */
    fun exportDebugReport() {
        if (!isDebugMode()) return
        
        coroutineScope.launch {
            try {
                val report = generateDebugReport()
                val fileName = "debug_report_${System.currentTimeMillis()}.txt"
                val file = File(context.getExternalFilesDir("debug"), fileName)
                
                file.parentFile?.mkdirs()
                
                FileWriter(file).use { writer ->
                    writer.write(formatDebugReport(report))
                }
                
                Log.i(TAG, "Debug report exported to: ${file.absolutePath}")
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to export debug report", e)
            }
        }
    }
    
    /**
     * Get device information
     */
    private fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            androidVersion = Build.VERSION.RELEASE,
            apiLevel = Build.VERSION.SDK_INT,
            brand = Build.BRAND,
            device = Build.DEVICE,
            hardware = Build.HARDWARE,
            board = Build.BOARD
        )
    }
    
    /**
     * Format debug report as text
     */
    private fun formatDebugReport(report: DebugReport): String {
        val sb = StringBuilder()
        
        sb.appendLine("=== DEBUG REPORT ===")
        sb.appendLine("Timestamp: ${dateFormat.format(Date(report.timestamp))}")
        sb.appendLine("App Version: ${report.appVersion}")
        sb.appendLine("Build Type: ${report.buildType}")
        sb.appendLine()
        
        sb.appendLine("=== DEVICE INFO ===")
        sb.appendLine("Manufacturer: ${report.deviceInfo.manufacturer}")
        sb.appendLine("Model: ${report.deviceInfo.model}")
        sb.appendLine("Android Version: ${report.deviceInfo.androidVersion}")
        sb.appendLine("API Level: ${report.deviceInfo.apiLevel}")
        sb.appendLine("Brand: ${report.deviceInfo.brand}")
        sb.appendLine("Device: ${report.deviceInfo.device}")
        sb.appendLine("Hardware: ${report.deviceInfo.hardware}")
        sb.appendLine("Board: ${report.deviceInfo.board}")
        sb.appendLine()
        
        sb.appendLine("=== MEMORY INFO ===")
        sb.appendLine("Total Memory: ${report.memoryInfo.totalMemory / 1024 / 1024} MB")
        sb.appendLine("Free Memory: ${report.memoryInfo.freeMemory / 1024 / 1024} MB")
        sb.appendLine("Max Memory: ${report.memoryInfo.maxMemory / 1024 / 1024} MB")
        sb.appendLine("Used Memory: ${report.memoryInfo.usedMemory / 1024 / 1024} MB")
        sb.appendLine("Available Memory: ${report.memoryInfo.availableMemory / 1024 / 1024} MB")
        sb.appendLine("Is Low Memory: ${report.memoryInfo.isLowMemory}")
        sb.appendLine()
        
        sb.appendLine("=== CACHE STATS ===")
        sb.appendLine("Image Cache Size: ${report.cacheStats.imageCacheSize}")
        sb.appendLine("Image Cache Max Size: ${report.cacheStats.imageCacheMaxSize}")
        sb.appendLine("Image Cache Hit Rate: ${(report.cacheStats.imageCacheHitRate * 100).toInt()}%")
        sb.appendLine("Weak Reference Cache Size: ${report.cacheStats.weakReferenceCacheSize}")
        sb.appendLine()
        
        sb.appendLine("=== PERFORMANCE METRICS ===")
        report.performanceMetrics.forEach { metric ->
            sb.appendLine("${metric.key}: ${metric.duration ?: "N/A"}ms")
        }
        sb.appendLine()
        
        sb.appendLine("=== RECENT LOG ENTRIES ===")
        report.logEntries.forEach { entry ->
            sb.appendLine("${entry.timestamp} [${entry.level}] ${entry.tag}: ${entry.message}")
        }
        
        return sb.toString()
    }
    
    /**
     * Write to debug log file
     */
    private fun writeToDebugLog(level: String, tag: String, message: String, throwable: Throwable?) {
        coroutineScope.launch {
            try {
                val logFile = File(context.getExternalFilesDir("debug"), "debug.log")
                logFile.parentFile?.mkdirs()
                
                FileWriter(logFile, true).use { writer ->
                    val timestamp = dateFormat.format(Date())
                    writer.appendLine("$timestamp [$level] $tag: $message")
                    
                    throwable?.let {
                        writer.appendLine("Stack trace:")
                        writer.appendLine(it.stackTraceToString())
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to write to debug log", e)
            }
        }
    }
    
    /**
     * Get recent log entries (mock implementation)
     */
    private fun getRecentLogEntries(): List<LogEntry> {
        // In a real implementation, this would read from a log buffer
        // For now, return empty list
        return emptyList()
    }
    
    /**
     * Clear debug logs
     */
    fun clearDebugLogs() {
        if (!isDebugMode()) return
        
        coroutineScope.launch {
            try {
                val debugDir = File(context.getExternalFilesDir("debug"), "")
                if (debugDir.exists()) {
                    debugDir.listFiles()?.forEach { file ->
                        file.delete()
                    }
                }
                Log.i(TAG, "Debug logs cleared")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear debug logs", e)
            }
        }
    }
    
    /**
     * Dump current state for debugging
     */
    fun dumpCurrentState() {
        if (!isDebugMode()) return
        
        Log.d(TAG, "=== CURRENT STATE DUMP ===")
        
        // Memory state
        val memoryInfo = memoryOptimizer.getMemoryInfo()
        Log.d(TAG, "Memory Usage: ${memoryInfo.usedMemory / 1024 / 1024}MB / ${memoryInfo.maxMemory / 1024 / 1024}MB")
        
        // Cache state
        val cacheStats = memoryOptimizer.getCacheStats()
        Log.d(TAG, "Image Cache: ${cacheStats.imageCacheSize}/${cacheStats.imageCacheMaxSize} (${(cacheStats.imageCacheHitRate * 100).toInt()}% hit rate)")
        
        // Performance metrics
        val metrics = performanceMonitor.getMetrics()
        Log.d(TAG, "Performance Metrics: ${metrics.size} tracked")
        
        Log.d(TAG, "=== END STATE DUMP ===")
    }
    
    companion object {
        private const val TAG = "DebugUtils"
    }
}

/**
 * Debug report data class
 */
data class DebugReport(
    val timestamp: Long,
    val appVersion: String,
    val buildType: String,
    val deviceInfo: DeviceInfo,
    val memoryInfo: com.mindseek.podcast.core.performance.MemoryInfo,
    val cacheStats: com.mindseek.podcast.core.performance.CacheStats,
    val performanceMetrics: List<com.mindseek.podcast.core.performance.PerformanceMetric>,
    val logEntries: List<LogEntry>
)

/**
 * Device information data class
 */
data class DeviceInfo(
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val apiLevel: Int,
    val brand: String,
    val device: String,
    val hardware: String,
    val board: String
)

/**
 * Log entry data class
 */
data class LogEntry(
    val timestamp: String,
    val level: String,
    val tag: String,
    val message: String
)