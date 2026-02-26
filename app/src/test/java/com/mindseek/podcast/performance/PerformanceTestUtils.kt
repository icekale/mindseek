package com.mindseek.podcast.performance

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import kotlin.system.measureTimeMillis

/**
 * Utility class for performance testing
 */
object PerformanceTestUtils {
    
    /**
     * Measure execution time of a block and assert it's within threshold
     */
    fun assertExecutionTime(
        description: String,
        maxTimeMs: Long,
        block: () -> Unit
    ) {
        val executionTime = measureTimeMillis {
            block()
        }
        
        println("Performance Test [$description]: ${executionTime}ms (max: ${maxTimeMs}ms)")
        
        assertTrue(
            "Performance test failed: $description took ${executionTime}ms, expected max ${maxTimeMs}ms",
            executionTime <= maxTimeMs
        )
    }
    
    /**
     * Measure execution time of a suspend block and assert it's within threshold
     */
    suspend fun assertSuspendExecutionTime(
        description: String,
        maxTimeMs: Long,
        block: suspend () -> Unit
    ) {
        val executionTime = measureTimeMillis {
            block()
        }
        
        println("Performance Test [$description]: ${executionTime}ms (max: ${maxTimeMs}ms)")
        
        assertTrue(
            "Performance test failed: $description took ${executionTime}ms, expected max ${maxTimeMs}ms",
            executionTime <= maxTimeMs
        )
    }
    
    /**
     * Run performance test with multiple iterations and assert average time
     */
    fun assertAverageExecutionTime(
        description: String,
        iterations: Int,
        maxAverageTimeMs: Long,
        block: () -> Unit
    ) {
        val times = mutableListOf<Long>()
        
        repeat(iterations) {
            val time = measureTimeMillis {
                block()
            }
            times.add(time)
        }
        
        val averageTime = times.average().toLong()
        val minTime = times.minOrNull() ?: 0
        val maxTime = times.maxOrNull() ?: 0
        
        println("Performance Test [$description] over $iterations iterations:")
        println("  Average: ${averageTime}ms")
        println("  Min: ${minTime}ms")
        println("  Max: ${maxTime}ms")
        println("  Expected max average: ${maxAverageTimeMs}ms")
        
        assertTrue(
            "Performance test failed: $description average time ${averageTime}ms exceeds max ${maxAverageTimeMs}ms",
            averageTime <= maxAverageTimeMs
        )
    }
    
    /**
     * Benchmark memory usage of a block
     */
    fun benchmarkMemoryUsage(
        description: String,
        block: () -> Unit
    ): MemoryBenchmark {
        val runtime = Runtime.getRuntime()
        
        // Force garbage collection before measurement
        System.gc()
        Thread.sleep(100)
        
        val memoryBefore = runtime.totalMemory() - runtime.freeMemory()
        
        block()
        
        val memoryAfter = runtime.totalMemory() - runtime.freeMemory()
        val memoryUsed = memoryAfter - memoryBefore
        
        val benchmark = MemoryBenchmark(
            description = description,
            memoryBefore = memoryBefore,
            memoryAfter = memoryAfter,
            memoryUsed = memoryUsed
        )
        
        println("Memory Benchmark [$description]:")
        println("  Memory before: ${memoryBefore / 1024}KB")
        println("  Memory after: ${memoryAfter / 1024}KB")
        println("  Memory used: ${memoryUsed / 1024}KB")
        
        return benchmark
    }
    
    /**
     * Assert memory usage is within threshold
     */
    fun assertMemoryUsage(
        description: String,
        maxMemoryUsageKB: Long,
        block: () -> Unit
    ) {
        val benchmark = benchmarkMemoryUsage(description, block)
        val memoryUsedKB = benchmark.memoryUsed / 1024
        
        assertTrue(
            "Memory usage test failed: $description used ${memoryUsedKB}KB, expected max ${maxMemoryUsageKB}KB",
            memoryUsedKB <= maxMemoryUsageKB
        )
    }
    
    /**
     * Run stress test with increasing load
     */
    fun runStressTest(
        description: String,
        startLoad: Int,
        maxLoad: Int,
        loadIncrement: Int,
        maxTimePerLoadMs: Long,
        block: (load: Int) -> Unit
    ) {
        println("Stress Test [$description]:")
        
        var currentLoad = startLoad
        while (currentLoad <= maxLoad) {
            val executionTime = measureTimeMillis {
                block(currentLoad)
            }
            
            println("  Load $currentLoad: ${executionTime}ms")
            
            assertTrue(
                "Stress test failed at load $currentLoad: took ${executionTime}ms, expected max ${maxTimePerLoadMs}ms",
                executionTime <= maxTimePerLoadMs
            )
            
            currentLoad += loadIncrement
        }
        
        println("Stress test completed successfully")
    }
    
    /**
     * Test concurrent execution performance
     */
    fun testConcurrentPerformance(
        description: String,
        concurrencyLevel: Int,
        maxTotalTimeMs: Long,
        block: suspend () -> Unit
    ) = runTest {
        val jobs = (1..concurrencyLevel).map {
            kotlinx.coroutines.async {
                block()
            }
        }
        
        val totalTime = measureTimeMillis {
            jobs.forEach { it.await() }
        }
        
        println("Concurrent Performance Test [$description]:")
        println("  Concurrency level: $concurrencyLevel")
        println("  Total time: ${totalTime}ms")
        println("  Expected max: ${maxTotalTimeMs}ms")
        
        assertTrue(
            "Concurrent performance test failed: $description took ${totalTime}ms with $concurrencyLevel concurrent operations, expected max ${maxTotalTimeMs}ms",
            totalTime <= maxTotalTimeMs
        )
    }
    
    /**
     * Profile method execution and return detailed metrics
     */
    fun profileExecution(
        description: String,
        warmupIterations: Int = 5,
        measurementIterations: Int = 10,
        block: () -> Unit
    ): ExecutionProfile {
        // Warmup
        repeat(warmupIterations) {
            block()
        }
        
        // Measurement
        val times = mutableListOf<Long>()
        repeat(measurementIterations) {
            val time = measureTimeMillis {
                block()
            }
            times.add(time)
        }
        
        val profile = ExecutionProfile(
            description = description,
            iterations = measurementIterations,
            times = times,
            averageTime = times.average(),
            minTime = times.minOrNull() ?: 0,
            maxTime = times.maxOrNull() ?: 0,
            standardDeviation = calculateStandardDeviation(times)
        )
        
        println("Execution Profile [$description]:")
        println("  Iterations: ${profile.iterations}")
        println("  Average: ${profile.averageTime.toLong()}ms")
        println("  Min: ${profile.minTime}ms")
        println("  Max: ${profile.maxTime}ms")
        println("  Std Dev: ${profile.standardDeviation.toLong()}ms")
        
        return profile
    }
    
    /**
     * Calculate standard deviation
     */
    private fun calculateStandardDeviation(values: List<Long>): Double {
        val mean = values.average()
        val variance = values.map { (it - mean) * (it - mean) }.average()
        return kotlin.math.sqrt(variance)
    }
}

/**
 * Memory benchmark result
 */
data class MemoryBenchmark(
    val description: String,
    val memoryBefore: Long,
    val memoryAfter: Long,
    val memoryUsed: Long
)

/**
 * Execution profile result
 */
data class ExecutionProfile(
    val description: String,
    val iterations: Int,
    val times: List<Long>,
    val averageTime: Double,
    val minTime: Long,
    val maxTime: Long,
    val standardDeviation: Double
)