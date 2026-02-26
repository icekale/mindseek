package com.mindseek.podcast.core.performance

import android.app.Application
import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Startup optimization utility for improving app launch time
 */
@Singleton
class StartupOptimizer @Inject constructor(
    private val performanceMonitor: PerformanceMonitor
) {
    
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var appStartTime: Long = 0
    private var isInitialized = false
    
    private val startupTasks = mutableListOf<StartupTask>()
    private val completedTasks = mutableSetOf<String>()
    
    /**
     * Initialize startup optimization
     */
    fun initialize(application: Application) {
        if (isInitialized) return
        
        appStartTime = SystemClock.elapsedRealtime()
        performanceMonitor.startTiming("app_startup")
        
        Log.d(TAG, "App startup optimization initialized")
        isInitialized = true
        
        // Register for app lifecycle callbacks
        application.registerActivityLifecycleCallbacks(StartupLifecycleCallbacks(this))
    }
    
    /**
     * Add a startup task
     */
    fun addStartupTask(task: StartupTask) {
        startupTasks.add(task)
        Log.d(TAG, "Added startup task: ${task.name} (Priority: ${task.priority})")
    }
    
    /**
     * Execute startup tasks based on priority
     */
    fun executeStartupTasks() {
        coroutineScope.launch {
            val sortedTasks = startupTasks.sortedBy { it.priority }
            
            // Execute critical tasks first (on main thread if needed)
            val criticalTasks = sortedTasks.filter { it.priority == StartupPriority.CRITICAL }
            executeTasks(criticalTasks, "Critical")
            
            // Execute high priority tasks
            val highPriorityTasks = sortedTasks.filter { it.priority == StartupPriority.HIGH }
            executeTasks(highPriorityTasks, "High Priority")
            
            // Execute medium priority tasks in background
            val mediumPriorityTasks = sortedTasks.filter { it.priority == StartupPriority.MEDIUM }
            launch(Dispatchers.IO) {
                executeTasks(mediumPriorityTasks, "Medium Priority")
            }
            
            // Execute low priority tasks in background with delay
            val lowPriorityTasks = sortedTasks.filter { it.priority == StartupPriority.LOW }
            launch(Dispatchers.IO) {
                kotlinx.coroutines.delay(1000) // Delay low priority tasks
                executeTasks(lowPriorityTasks, "Low Priority")
            }
        }
    }
    
    /**
     * Execute a list of tasks
     */
    private suspend fun executeTasks(tasks: List<StartupTask>, category: String) {
        Log.d(TAG, "Executing $category tasks (${tasks.size} tasks)")
        
        for (task in tasks) {
            if (completedTasks.contains(task.name)) {
                continue // Skip already completed tasks
            }
            
            try {
                performanceMonitor.measureTimeSuspend("startup_task_${task.name}") {
                    if (task.requiresMainThread) {
                        withContext(Dispatchers.Main) {
                            task.execute()
                        }
                    } else {
                        task.execute()
                    }
                }
                
                completedTasks.add(task.name)
                Log.d(TAG, "Completed startup task: ${task.name}")
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to execute startup task: ${task.name}", e)
            }
        }
        
        Log.d(TAG, "Completed $category tasks")
    }
    
    /**
     * Mark app as fully started
     */
    fun markAppStarted() {
        if (appStartTime > 0) {
            performanceMonitor.endTiming("app_startup")
            val startupTime = SystemClock.elapsedRealtime() - appStartTime
            
            Log.i(TAG, "App startup completed in ${startupTime}ms")
            
            // Log startup performance
            logStartupPerformance(startupTime)
            
            appStartTime = 0
        }
    }
    
    /**
     * Get startup statistics
     */
    fun getStartupStats(): StartupStats {
        return StartupStats(
            totalTasks = startupTasks.size,
            completedTasks = completedTasks.size,
            pendingTasks = startupTasks.size - completedTasks.size,
            criticalTasks = startupTasks.count { it.priority == StartupPriority.CRITICAL },
            highPriorityTasks = startupTasks.count { it.priority == StartupPriority.HIGH },
            mediumPriorityTasks = startupTasks.count { it.priority == StartupPriority.MEDIUM },
            lowPriorityTasks = startupTasks.count { it.priority == StartupPriority.LOW }
        )
    }
    
    /**
     * Log startup performance metrics
     */
    private fun logStartupPerformance(startupTime: Long) {
        val stats = getStartupStats()
        
        Log.i(TAG, "Startup Performance Summary:")
        Log.i(TAG, "  Total startup time: ${startupTime}ms")
        Log.i(TAG, "  Total tasks: ${stats.totalTasks}")
        Log.i(TAG, "  Completed tasks: ${stats.completedTasks}")
        Log.i(TAG, "  Pending tasks: ${stats.pendingTasks}")
        Log.i(TAG, "  Critical tasks: ${stats.criticalTasks}")
        Log.i(TAG, "  High priority tasks: ${stats.highPriorityTasks}")
        Log.i(TAG, "  Medium priority tasks: ${stats.mediumPriorityTasks}")
        Log.i(TAG, "  Low priority tasks: ${stats.lowPriorityTasks}")
        
        // Warn about slow startup
        if (startupTime > SLOW_STARTUP_THRESHOLD) {
            Log.w(TAG, "Slow app startup detected: ${startupTime}ms")
        }
    }
    
    /**
     * Create common startup tasks
     */
    fun createCommonStartupTasks(): List<StartupTask> {
        return listOf(
            StartupTask(
                name = "initialize_database",
                priority = StartupPriority.CRITICAL,
                requiresMainThread = false
            ) {
                // Database initialization would go here
                Log.d(TAG, "Database initialized")
            },
            
            StartupTask(
                name = "initialize_network",
                priority = StartupPriority.HIGH,
                requiresMainThread = false
            ) {
                // Network initialization would go here
                Log.d(TAG, "Network initialized")
            },
            
            StartupTask(
                name = "load_user_preferences",
                priority = StartupPriority.HIGH,
                requiresMainThread = false
            ) {
                // Load user preferences
                Log.d(TAG, "User preferences loaded")
            },
            
            StartupTask(
                name = "initialize_analytics",
                priority = StartupPriority.MEDIUM,
                requiresMainThread = false
            ) {
                // Analytics initialization
                Log.d(TAG, "Analytics initialized")
            },
            
            StartupTask(
                name = "preload_images",
                priority = StartupPriority.LOW,
                requiresMainThread = false
            ) {
                // Preload common images
                Log.d(TAG, "Images preloaded")
            }
        )
    }
    
    companion object {
        private const val TAG = "StartupOptimizer"
        private const val SLOW_STARTUP_THRESHOLD = 3000L // 3 seconds
    }
}

/**
 * Represents a startup task
 */
data class StartupTask(
    val name: String,
    val priority: StartupPriority,
    val requiresMainThread: Boolean = false,
    val execute: suspend () -> Unit
)

/**
 * Startup task priorities
 */
enum class StartupPriority {
    CRITICAL,   // Must complete before app is usable
    HIGH,       // Should complete quickly for good UX
    MEDIUM,     // Can be delayed slightly
    LOW         // Can be delayed significantly
}

/**
 * Startup statistics
 */
data class StartupStats(
    val totalTasks: Int,
    val completedTasks: Int,
    val pendingTasks: Int,
    val criticalTasks: Int,
    val highPriorityTasks: Int,
    val mediumPriorityTasks: Int,
    val lowPriorityTasks: Int
)

/**
 * Activity lifecycle callbacks for startup optimization
 */
private class StartupLifecycleCallbacks(
    private val startupOptimizer: StartupOptimizer
) : Application.ActivityLifecycleCallbacks {
    
    private var isFirstActivity = true
    
    override fun onActivityCreated(activity: android.app.Activity, savedInstanceState: android.os.Bundle?) {
        if (isFirstActivity) {
            startupOptimizer.executeStartupTasks()
            isFirstActivity = false
        }
    }
    
    override fun onActivityStarted(activity: android.app.Activity) {}
    override fun onActivityResumed(activity: android.app.Activity) {
        // Mark app as started when first activity is resumed
        startupOptimizer.markAppStarted()
    }
    override fun onActivityPaused(activity: android.app.Activity) {}
    override fun onActivityStopped(activity: android.app.Activity) {}
    override fun onActivitySaveInstanceState(activity: android.app.Activity, outState: android.os.Bundle) {}
    override fun onActivityDestroyed(activity: android.app.Activity) {}
}