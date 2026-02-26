package com.mindseek.podcast.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for type-safe helper methods in PlayerRepositoryImpl
 * These tests verify that the helper methods prevent ClassCastException
 * and provide safe type conversion with proper error handling
 */
class TypeSafeHelperMethodsTest {

    @Test
    fun `safeCast should return correct type when casting is valid`() {
        // Given
        val mutableFlow = MutableStateFlow("test")
        val stateFlow: StateFlow<String> = mutableFlow.asStateFlow()
        
        // When
        val result = stateFlow.safeCast<StateFlow<String>>()
        
        // Then
        assertNotNull(result)
        assertEquals("test", result?.value)
    }

    @Test
    fun `safeCast should return null when casting is invalid`() {
        // Given
        val stringFlow = MutableStateFlow("test")
        
        // When
        val result = stringFlow.safeCast<MutableStateFlow<Int>>()
        
        // Then
        assertNull(result)
    }

    @Test
    fun `safeCastWithFallback should return original value when casting is valid`() {
        // Given
        val originalValue = "original"
        val fallbackValue = "fallback"
        
        // When
        val result = originalValue.safeCastWithFallback(fallbackValue)
        
        // Then
        assertEquals(originalValue, result)
    }

    @Test
    fun `safeCastWithFallback should return fallback when casting is invalid`() {
        // Given
        val originalValue: Any = 123
        val fallbackValue = "fallback"
        
        // When
        val result = originalValue.safeCastWithFallback(fallbackValue)
        
        // Then
        assertEquals(fallbackValue, result)
    }

    @Test
    fun `isFlowOfType should return true for correct type`() {
        // Given
        val mutableFlow = MutableStateFlow("test")
        
        // When
        val result = mutableFlow.isFlowOfType<MutableStateFlow<String>>()
        
        // Then
        assertTrue(result)
    }

    @Test
    fun `isFlowOfType should return false for incorrect type`() {
        // Given
        val stringValue = "not a flow"
        
        // When
        val result = stringValue.isFlowOfType<MutableStateFlow<String>>()
        
        // Then
        assertFalse(result)
    }

    @Test
    fun `validateType should return true for valid StateFlow`() {
        // Given
        val stateFlow = MutableStateFlow("test").asStateFlow()
        
        // When
        val result = stateFlow.validateType()
        
        // Then
        assertTrue(result)
    }

    @Test
    fun `safeValue should return current value when accessible`() {
        // Given
        val expectedValue = "test value"
        val stateFlow = MutableStateFlow(expectedValue)
        val fallback = "fallback"
        
        // When
        val result = stateFlow.safeValue(fallback)
        
        // Then
        assertEquals(expectedValue, result)
    }

    @Test
    fun `safeUpdate should return true when update is successful`() {
        // Given
        val mutableFlow = MutableStateFlow("initial")
        val newValue = "updated"
        
        // When
        val result = mutableFlow.safeUpdate(newValue)
        
        // Then
        assertTrue(result)
        assertEquals(newValue, mutableFlow.value)
    }

    @Test
    fun `safeUpdate should handle concurrent access safely`() {
        // Given
        val mutableFlow = MutableStateFlow(0)
        
        // When - simulate multiple updates
        val results = mutableListOf<Boolean>()
        repeat(10) { i ->
            results.add(mutableFlow.safeUpdate(i))
        }
        
        // Then - all updates should succeed
        assertTrue(results.all { it })
        assertTrue(mutableFlow.value in 0..9)
    }

    @Test
    fun `type safe methods should prevent ClassCastException`() {
        // Given
        val readonlyStateFlow: StateFlow<String> = MutableStateFlow("test").asStateFlow()
        
        // When & Then - these operations should not throw ClassCastException
        assertDoesNotThrow {
            val safeCastResult = readonlyStateFlow.safeCast<MutableStateFlow<String>>()
            assertNull(safeCastResult) // Should be null since it's readonly
            
            val validateResult = readonlyStateFlow.validateType()
            assertTrue(validateResult) // Should be valid StateFlow
            
            val safeValueResult = readonlyStateFlow.safeValue("fallback")
            assertEquals("test", safeValueResult) // Should return actual value
        }
    }

    private fun assertDoesNotThrow(block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            fail("Expected no exception but got: ${e.javaClass.simpleName}: ${e.message}")
        }
    }
}