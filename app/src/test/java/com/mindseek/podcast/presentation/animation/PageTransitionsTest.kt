package com.mindseek.podcast.presentation.animation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavBackStackEntry
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class PageTransitionsTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun `slideInFromRight should return valid EnterTransition`() {
        // Given
        val mockScope = mock(AnimatedContentTransitionScope::class.java) as AnimatedContentTransitionScope<NavBackStackEntry>
        
        // When
        val transition = PageTransitions.slideInFromRight()
        val result = mockScope.transition()
        
        // Then
        assert(result is EnterTransition)
    }
    
    @Test
    fun `slideOutToRight should return valid ExitTransition`() {
        // Given
        val mockScope = mock(AnimatedContentTransitionScope::class.java) as AnimatedContentTransitionScope<NavBackStackEntry>
        
        // When
        val transition = PageTransitions.slideOutToRight()
        val result = mockScope.transition()
        
        // Then
        assert(result is ExitTransition)
    }
    
    @Test
    fun `fadeIn should return valid EnterTransition`() {
        // Given
        val mockScope = mock(AnimatedContentTransitionScope::class.java) as AnimatedContentTransitionScope<NavBackStackEntry>
        
        // When
        val transition = PageTransitions.fadeIn()
        val result = mockScope.transition()
        
        // Then
        assert(result is EnterTransition)
    }
    
    @Test
    fun `fadeOut should return valid ExitTransition`() {
        // Given
        val mockScope = mock(AnimatedContentTransitionScope::class.java) as AnimatedContentTransitionScope<NavBackStackEntry>
        
        // When
        val transition = PageTransitions.fadeOut()
        val result = mockScope.transition()
        
        // Then
        assert(result is ExitTransition)
    }
    
    @Test
    fun `scaleIn should return valid EnterTransition`() {
        // Given
        val mockScope = mock(AnimatedContentTransitionScope::class.java) as AnimatedContentTransitionScope<NavBackStackEntry>
        
        // When
        val transition = PageTransitions.scaleIn()
        val result = mockScope.transition()
        
        // Then
        assert(result is EnterTransition)
    }
    
    @Test
    fun `slideInFromBottom should return valid EnterTransition`() {
        // Given
        val mockScope = mock(AnimatedContentTransitionScope::class.java) as AnimatedContentTransitionScope<NavBackStackEntry>
        
        // When
        val transition = PageTransitions.slideInFromBottom()
        val result = mockScope.transition()
        
        // Then
        assert(result is EnterTransition)
    }
}