package com.mindseek.podcast.presentation.animation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry

/**
 * Page transition animations for navigation
 */
object PageTransitions {
    
    /**
     * Slide transition from right to left (forward navigation)
     */
    fun slideInFromRight(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(300, easing = EaseOutCubic)
        ) + fadeIn(animationSpec = tween(300))
    }
    
    /**
     * Slide transition from left to right (back navigation)
     */
    fun slideOutToRight(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(300, easing = EaseInCubic)
        ) + fadeOut(animationSpec = tween(300))
    }
    
    /**
     * Slide transition from left to right (back navigation enter)
     */
    fun slideInFromLeft(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = tween(300, easing = EaseOutCubic)
        ) + fadeIn(animationSpec = tween(300))
    }
    
    /**
     * Slide transition from right to left (forward navigation exit)
     */
    fun slideOutToLeft(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = tween(300, easing = EaseInCubic)
        ) + fadeOut(animationSpec = tween(300))
    }
    
    /**
     * Fade transition
     */
    fun fadeIn(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        fadeIn(
            animationSpec = tween(300, easing = EaseInOut)
        )
    }
    
    fun fadeOut(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        fadeOut(
            animationSpec = tween(300, easing = EaseInOut)
        )
    }
    
    /**
     * Scale transition (for modal-like screens)
     */
    fun scaleIn(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        scaleIn(
            initialScale = 0.9f,
            animationSpec = tween(300, easing = EaseOutBack)
        ) + fadeIn(animationSpec = tween(300))
    }
    
    fun scaleOut(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        scaleOut(
            targetScale = 0.9f,
            animationSpec = tween(300, easing = EaseInBack)
        ) + fadeOut(animationSpec = tween(300))
    }
    
    /**
     * Slide up transition (for bottom sheets or modal screens)
     */
    fun slideInFromBottom(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn(animationSpec = tween(300))
    }
    
    fun slideOutToBottom(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300, easing = EaseInCubic)
        ) + fadeOut(animationSpec = tween(300))
    }
    
    /**
     * Slide down transition
     */
    fun slideInFromTop(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn(animationSpec = tween(300))
    }
    
    fun slideOutToTop(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(300, easing = EaseInCubic)
        ) + fadeOut(animationSpec = tween(300))
    }
}

/**
 * Extension functions for common transition combinations
 */
fun AnimatedContentTransitionScope<NavBackStackEntry>.horizontalSlideTransition(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(300, easing = EaseOutCubic)
    ) + fadeIn(animationSpec = tween(300))
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.horizontalSlideExitTransition(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { -it },
        animationSpec = tween(300, easing = EaseInCubic)
    ) + fadeOut(animationSpec = tween(300))
}

/**
 * Shared element transition helpers
 */
@Composable
fun SharedElementTransition(
    visible: Boolean,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)) + scaleIn(
            initialScale = 0.8f,
            animationSpec = tween(300, easing = EaseOutBack)
        ),
        exit = fadeOut(animationSpec = tween(300)) + scaleOut(
            targetScale = 0.8f,
            animationSpec = tween(300, easing = EaseInBack)
        ),
        content = content
    )
}