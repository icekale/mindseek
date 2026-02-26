package com.mindseek.podcast.core.performance

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.util.LruCache
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Memory optimization utility for managing app memory usage
 */
@Singleton
class MemoryOptimizer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    // Image cache with memory-aware sizing
    private val imageCache: LruCache<String, Bitmap> by lazy {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8 // Use 1/8th of available memory for image cache
        
        object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                return bitmap.byteCount / 1024
            }
            
            override fun entryRemoved(evicted: Boolean, key: String, oldValue: Bitmap, newValue: Bitmap?) {
                if (evicted && !oldValue.isRecycled) {
                    oldValue.recycle()
                }
            }
        }
    }
    
    // Weak reference cache for objects that can be recreated
    private val weakReferenceCache = mutableMapOf<String, WeakReference<Any>>()
    
    /**
     * Get memory information
     */
    fun getMemoryInfo(): MemoryInfo {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        val availableMemory = maxMemory - usedMemory
        
        return MemoryInfo(
            totalMemory = runtime.totalMemory(),
            freeMemory = runtime.freeMemory(),
            maxMemory = maxMemory,
            usedMemory = usedMemory,
            availableMemory = availableMemory,
            isLowMemory = memoryInfo.lowMemory,
            threshold = memoryInfo.threshold
        )
    }
    
    /**
     * Check if device is in low memory state
     */
    fun isLowMemory(): Boolean {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.lowMemory
    }
    
    /**
     * Get memory usage percentage
     */
    fun getMemoryUsagePercentage(): Float {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        return (usedMemory.toFloat() / maxMemory.toFloat()) * 100
    }
    
    /**
     * Perform memory cleanup
     */
    fun performMemoryCleanup() {
        coroutineScope.launch {
            Log.d(TAG, "Performing memory cleanup...")
            
            // Clear image cache if memory is low
            if (isLowMemory()) {
                imageCache.evictAll()
                Log.d(TAG, "Cleared image cache due to low memory")
            }
            
            // Clean up weak references
            cleanupWeakReferences()
            
            // Suggest garbage collection
            System.gc()
            
            Log.d(TAG, "Memory cleanup completed")
        }
    }
    
    /**
     * Add bitmap to cache
     */
    fun cacheBitmap(key: String, bitmap: Bitmap) {
        if (!bitmap.isRecycled) {
            imageCache.put(key, bitmap)
        }
    }
    
    /**
     * Get bitmap from cache
     */
    fun getCachedBitmap(key: String): Bitmap? {
        return imageCache.get(key)
    }
    
    /**
     * Remove bitmap from cache
     */
    fun removeCachedBitmap(key: String) {
        imageCache.remove(key)
    }
    
    /**
     * Store object in weak reference cache
     */
    fun <T : Any> cacheWeakReference(key: String, obj: T) {
        weakReferenceCache[key] = WeakReference(obj)
    }
    
    /**
     * Get object from weak reference cache
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getWeakReference(key: String): T? {
        return weakReferenceCache[key]?.get() as? T
    }
    
    /**
     * Clean up null weak references
     */
    private fun cleanupWeakReferences() {
        val iterator = weakReferenceCache.iterator()
        var cleanedCount = 0
        
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.value.get() == null) {
                iterator.remove()
                cleanedCount++
            }
        }
        
        if (cleanedCount > 0) {
            Log.d(TAG, "Cleaned up $cleanedCount null weak references")
        }
    }
    
    /**
     * Get cache statistics
     */
    fun getCacheStats(): CacheStats {
        return CacheStats(
            imageCacheSize = imageCache.size().toLong(),
            imageCacheMaxSize = imageCache.maxSize().toLong(),
            imageCacheHitCount = imageCache.hitCount().toLong(),
            imageCacheMissCount = imageCache.missCount().toLong(),
            weakReferenceCacheSize = weakReferenceCache.size
        )
    }
    
    /**
     * Monitor memory usage and perform cleanup if needed
     */
    fun monitorAndOptimize() {
        coroutineScope.launch {
            val memoryUsage = getMemoryUsagePercentage()
            
            Log.d(TAG, "Current memory usage: ${memoryUsage.toInt()}%")
            
            when {
                memoryUsage > CRITICAL_MEMORY_THRESHOLD -> {
                    Log.w(TAG, "Critical memory usage detected, performing aggressive cleanup")
                    performAggressiveCleanup()
                }
                memoryUsage > HIGH_MEMORY_THRESHOLD -> {
                    Log.w(TAG, "High memory usage detected, performing cleanup")
                    performMemoryCleanup()
                }
                memoryUsage > MODERATE_MEMORY_THRESHOLD -> {
                    Log.d(TAG, "Moderate memory usage, performing light cleanup")
                    performLightCleanup()
                }
            }
        }
    }
    
    /**
     * Perform aggressive memory cleanup
     */
    private fun performAggressiveCleanup() {
        // Clear all caches
        imageCache.evictAll()
        weakReferenceCache.clear()
        
        // Force garbage collection
        System.gc()
        
        Log.d(TAG, "Aggressive memory cleanup completed")
    }
    
    /**
     * Perform light memory cleanup
     */
    private fun performLightCleanup() {
        // Only clean up weak references
        cleanupWeakReferences()
        
        // Trim image cache by 25%
        val currentSize = imageCache.size()
        val targetSize = (currentSize * 0.75).toInt()
        imageCache.trimToSize(targetSize)
        
        Log.d(TAG, "Light memory cleanup completed")
    }
    
    companion object {
        private const val TAG = "MemoryOptimizer"
        private const val MODERATE_MEMORY_THRESHOLD = 60f // 60%
        private const val HIGH_MEMORY_THRESHOLD = 75f // 75%
        private const val CRITICAL_MEMORY_THRESHOLD = 90f // 90%
    }
}

/**
 * Data class representing memory information
 */
data class MemoryInfo(
    val totalMemory: Long,
    val freeMemory: Long,
    val maxMemory: Long,
    val usedMemory: Long,
    val availableMemory: Long,
    val isLowMemory: Boolean,
    val threshold: Long
)

/**
 * Data class representing cache statistics
 */
data class CacheStats(
    val imageCacheSize: Long,
    val imageCacheMaxSize: Long,
    val imageCacheHitCount: Long,
    val imageCacheMissCount: Long,
    val weakReferenceCacheSize: Int
) {
    val imageCacheHitRate: Float
        get() = if (imageCacheHitCount + imageCacheMissCount > 0) {
            imageCacheHitCount.toFloat() / (imageCacheHitCount + imageCacheMissCount).toFloat()
        } else 0f
}