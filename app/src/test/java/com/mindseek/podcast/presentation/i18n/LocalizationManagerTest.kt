package com.mindseek.podcast.presentation.i18n

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.*

@RunWith(RobolectricTestRunner::class)
class LocalizationManagerTest {
    
    private lateinit var context: Context
    private lateinit var localizationManager: LocalizationManager
    
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        localizationManager = LocalizationManager(context)
    }
    
    @Test
    fun `setLocale should update current locale`() {
        // Given
        val newLocale = Locale.ENGLISH
        
        // When
        localizationManager.setLocale(newLocale)
        
        // Then
        assertEquals(newLocale, localizationManager.currentLocale.value)
    }
    
    @Test
    fun `isLocaleSupported should return true for supported locales`() {
        // Given
        val supportedLocales = listOf(
            Locale.CHINESE,
            Locale.ENGLISH,
            Locale.US
        )
        
        // When & Then
        supportedLocales.forEach { locale ->
            assertTrue("Locale $locale should be supported", localizationManager.isLocaleSupported(locale))
        }
    }
    
    @Test
    fun `isLocaleSupported should return false for unsupported locales`() {
        // Given
        val unsupportedLocale = Locale.FRENCH
        
        // When
        val isSupported = localizationManager.isLocaleSupported(unsupportedLocale)
        
        // Then
        assertFalse("French locale should not be supported", isSupported)
    }
    
    @Test
    fun `formatDuration should format correctly for Chinese locale`() {
        // Given
        localizationManager.setLocale(Locale.CHINESE)
        val durationMs = 3661000L // 1 hour, 1 minute, 1 second
        
        // When
        val formatted = localizationManager.formatDuration(durationMs)
        
        // Then
        assertEquals("1小时1分钟", formatted)
    }
    
    @Test
    fun `formatDuration should format correctly for English locale`() {
        // Given
        localizationManager.setLocale(Locale.ENGLISH)
        val durationMs = 3661000L // 1 hour, 1 minute, 1 second
        
        // When
        val formatted = localizationManager.formatDuration(durationMs)
        
        // Then
        assertEquals("1h 1m", formatted)
    }
    
    @Test
    fun `formatFileSize should format correctly for Chinese locale`() {
        // Given
        localizationManager.setLocale(Locale.CHINESE)
        val sizeBytes = 1024L * 1024L * 5L // 5 MB
        
        // When
        val formatted = localizationManager.formatFileSize(sizeBytes)
        
        // Then
        assertEquals("5.0 MB", formatted)
    }
    
    @Test
    fun `formatFileSize should format bytes correctly for Chinese locale`() {
        // Given
        localizationManager.setLocale(Locale.CHINESE)
        val sizeBytes = 512L
        
        // When
        val formatted = localizationManager.formatFileSize(sizeBytes)
        
        // Then
        assertEquals("512 字节", formatted)
    }
    
    @Test
    fun `formatFileSize should format bytes correctly for English locale`() {
        // Given
        localizationManager.setLocale(Locale.ENGLISH)
        val sizeBytes = 512L
        
        // When
        val formatted = localizationManager.formatFileSize(sizeBytes)
        
        // Then
        assertEquals("512 bytes", formatted)
    }
    
    @Test
    fun `formatRelativeTime should format correctly for Chinese locale`() {
        // Given
        localizationManager.setLocale(Locale.CHINESE)
        val now = System.currentTimeMillis()
        val twoHoursAgo = now - (2 * 60 * 60 * 1000)
        
        // When
        val formatted = localizationManager.formatRelativeTime(twoHoursAgo)
        
        // Then
        assertEquals("2小时前", formatted)
    }
    
    @Test
    fun `formatRelativeTime should format correctly for English locale`() {
        // Given
        localizationManager.setLocale(Locale.ENGLISH)
        val now = System.currentTimeMillis()
        val twoHoursAgo = now - (2 * 60 * 60 * 1000)
        
        // When
        val formatted = localizationManager.formatRelativeTime(twoHoursAgo)
        
        // Then
        assertEquals("2 hours ago", formatted)
    }
    
    @Test
    fun `formatRelativeTime should return just now for recent timestamps`() {
        // Given
        localizationManager.setLocale(Locale.CHINESE)
        val now = System.currentTimeMillis()
        val fiveSecondsAgo = now - 5000
        
        // When
        val formatted = localizationManager.formatRelativeTime(fiveSecondsAgo)
        
        // Then
        assertEquals("刚刚", formatted)
    }
    
    @Test
    fun `formatDate should format date correctly`() {
        // Given
        localizationManager.setLocale(Locale.ENGLISH)
        val timestamp = 1640995200000L // January 1, 2022
        
        // When
        val formatted = localizationManager.formatDate(timestamp)
        
        // Then
        assertNotNull("Formatted date should not be null", formatted)
        assertTrue("Formatted date should contain year", formatted.contains("2022"))
    }
    
    @Test
    fun `getSupportedLocales should return non-empty list`() {
        // When
        val supportedLocales = localizationManager.getSupportedLocales()
        
        // Then
        assertFalse("Supported locales should not be empty", supportedLocales.isEmpty())
        assertTrue("Should support Chinese", supportedLocales.any { it.language == "zh" })
        assertTrue("Should support English", supportedLocales.any { it.language == "en" })
    }
    
    @Test
    fun `getLocaleDisplayName should return display name`() {
        // Given
        val locale = Locale.ENGLISH
        
        // When
        val displayName = localizationManager.getLocaleDisplayName(locale)
        
        // Then
        assertNotNull("Display name should not be null", displayName)
        assertFalse("Display name should not be empty", displayName.isEmpty())
    }
}