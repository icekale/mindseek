package com.mindseek.podcast.core.performance

import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Performance monitoring utility for tracking app performance metrics
 */
@Singleton
class PerformanceMonitor @Inject constructor() {
    
    private val performanceMetrics = ConcurrentHashMap<String, PerformanceMetric>()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    /**
     * Start timing a performance metric
     */
    fun startTiming(key: String) {
        val startTime = SystemClock.elapsedRealtime()
        performanceMetrics[key] = PerformanceMetric(
            key = key,
            startTime = startTime,
            endTime = null
        )
    }
    
    /**
     * End timing a performance metric and log the result
     */
    fun endTiming(key: String) {
        val endTime = SystemClock.elapsedRealtime()
        performanceMetrics[key]?.let { metric ->
            val updatedMetric = metric.copy(endTime = endTime)
            performanceMetrics[key] = updatedMetric
            
            val duration = endTime - metric.startTime
            Log.d(TAG, "Performance: $key took ${duration}ms")
            
            // Log warning for slow operations
            if (duration > SLOW_OPERATION_THRESHOLD) {
                Log.w(TAG, "Slow operation detected: $key took ${duration}ms")
            }
            
            // Store metric for analysis
            coroutineScope.launch {
                storeMetric(updatedMetric)
            }
        }
    }
    
    /**
     * Measure the execution time of a block of code
     */
    inline fun <T> measureTime(key: String, block: () -> T): T {
        startTiming(key)
        return try {
            block()
        } finally {
            endTiming(key)
        }
    }
    
    /**
     * Measure the execution time of a suspend block of code
     */
    suspend inline fun <T> measureTimeSuspend(key: String, crossinline block: suspend () -> T): T {
        startTiming(key)
        return try {
            block()
        } finally {
            endTiming(key)
        }
    }
    
    /**
     * Get all performance metrics
     */
    fun getMetrics(): Map<String, PerformanceMetric> {
        return performanceMetrics.toMap()
    }
    
    /**
     * Get a specific performance metric
     */
    fun getMetric(key: String): PerformanceMetric? {
        return performanceMetrics[key]
    }
    
    /**
     * Clear all performance metrics
     */
    fun clearMetrics() {
        performanceMetrics.clear()
    }
    
    /**
     * Get average duration for a specific metric key
     */
    fun getAverageDuration(key: String): Long? {
        val metrics = performanceMetrics.values.filter { it.key == key && it.endTime != null }
        if (metrics.isEmpty()) return null
        
        val totalDuration = metrics.sumOf { it.duration ?: 0 }
        return totalDuration / metrics.size
    }
    
    /**
     * Log memory usage
     */
    fun logMemoryUsage(context: String) {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        val availableMemory = maxMemory - usedMemory
        
        Log.d(TAG, "Memory Usage [$context]: " +
                "Used: ${usedMemory / 1024 / 1024}MB, " +
                "Available: ${availableMemory / 1024 / 1024}MB, " +
                "Max: ${maxMemory / 1024 / 1024}MB")
        
        // Warn if memory usage is high
        val memoryUsagePercentage = (usedMemory.toFloat() / maxMemory.toFloat()) * 100
        if (memoryUsagePercentage > HIGH_MEMORY_USAGE_THRESHOLD) {
            Log.w(TAG, "High memory usage detected: ${memoryUsagePercentage.toInt()}%")
        }
    }
    
    /**
     * Store metric for later analysis (could be sent to analytics service)
     */
    private suspend fun storeMetric(metric: PerformanceMetric) {
        // In a real app, this could send data to Firebase Performance or other analytics
        // For now, we just log it
        metric.duration?.let { duration ->
            Log.v(TAG, "Stored metric: ${metric.key} - ${duration}ms")
        }
    }
    
    companion object {
        private const val TAG = "PerformanceMonitor"
        private const val SLOW_OPERATION_THRESHOLD = 1000L // 1 second
        private const val HIGH_MEMORY_USAGE_THRESHOLD = 80f // 80%
    }
}

/**
 * Data class representing a performance metric
 */
data class PerformanceMetric(
    val key: String,
    val startTime: Long,
    val endTime: Long?
) {
    val duration: Long?
        get() = endTime?.let { it - startTime }
}

/**
 * Extension function for easy performance monitoring
 */
inline fun <T> PerformanceMonitor.measure(key: String, block: () -> T): T {
    return measureTime(key, block)
}

/**
 * Extension function for easy suspend performance monitoring
 */
suspend inline fun <T> PerformanceMonitor.measureSuspend(key: String, crossinline block: suspend () -> T): T {
    return measureTimeSuspend(key, block)
}