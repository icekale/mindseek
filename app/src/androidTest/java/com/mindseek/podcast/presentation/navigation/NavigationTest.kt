package com.mindseek.podcast.presentation.navigation

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mindseek.podcast.presentation.PodcastApp
import com.mindseek.podcast.ui.theme.PodcastTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun navHost_verifyStartDestination() {
        composeTestRule.setContent {
            val navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            
            PodcastTheme {
                PodcastNavGraph(navController = navController)
            }
        }

        // Verify that the start destination is the home screen
        composeTestRule
            .onNodeWithText("推荐")
            .assertIsDisplayed()
    }

    @Test
    fun navHost_navigateToSearch() {
        composeTestRule.setContent {
            val navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            
            PodcastTheme {
                PodcastNavGraph(navController = navController)
            }
        }

        // Navigate to search screen
        composeTestRule
            .onNodeWithText("搜索")
            .performClick()

        // Verify search screen is displayed
        composeTestRule
            .onNodeWithText("搜索播客或节�?)
            .assertIsDisplayed()
    }

    @Test
    fun navHost_navigateToSubscriptions() {
        composeTestRule.setContent {
            val navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            
            PodcastTheme {
                PodcastNavGraph(navController = navController)
            }
        }

        // Navigate to subscriptions screen
        composeTestRule
            .onNodeWithText("订阅")
            .performClick()

        // Verify subscriptions screen is displayed
        composeTestRule
            .onNodeWithText("我的订阅")
            .assertIsDisplayed()
    }

    @Test
    fun navHost_navigateToProfile() {
        composeTestRule.setContent {
            val navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            
            PodcastTheme {
                PodcastNavGraph(navController = navController)
            }
        }

        // Navigate to profile screen
        composeTestRule
            .onNodeWithText("我的")
            .performClick()

        // Verify profile screen is displayed
        composeTestRule
            .onNodeWithText("个人中心")
            .assertIsDisplayed()
    }

    @Test
    fun navHost_navigateFromHomeToProfile() {
        lateinit var navController: TestNavHostController
        
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            
            PodcastTheme {
                PodcastNavGraph(navController = navController)
            }
        }

        // Start at home screen
        assertEquals("home", navController.currentBackStackEntry?.destination?.route)

        // Navigate to profile
        composeTestRule
            .onNodeWithText("我的")
            .performClick()

        // Verify navigation occurred
        assertEquals("profile", navController.currentBackStackEntry?.destination?.route)
    }

    @Test
    fun navHost_bottomNavigationPersistence() {
        composeTestRule.setContent {
            val navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            
            PodcastTheme {
                PodcastNavGraph(navController = navController)
            }
        }

        // Navigate to search
        composeTestRule
            .onNodeWithText("搜索")
            .performClick()

        // Verify bottom navigation is still visible
        composeTestRule
            .onNodeWithText("推荐")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("搜索")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("订阅")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("我的")
            .assertIsDisplayed()
    }

    @Test
    fun navHost_navigateToFavorites() {
        composeTestRule.setContent {
            val navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            
            PodcastTheme {
                PodcastNavGraph(navController = navController)
            }
        }

        // Navigate to profile first
        composeTestRule
            .onNodeWithText("我的")
            .performClick()

        // Navigate to favorites from profile
        composeTestRule
            .onNodeWithText("我的收藏")
            .performClick()

        // Verify favorites screen is displayed
        composeTestRule
            .onNodeWithText("收藏列表")
            .assertIsDisplayed()
    }

    @Test
    fun navHost_navigateToHistory() {
        composeTestRule.setContent {
            val navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            
            PodcastTheme {
                PodcastNavGraph(navController = navController)
            }
        }

        // Navigate to profile first
        composeTestRule
            .onNodeWithText("我的")
            .performClick()

        // Navigate to history from profile
        composeTestRule
            .onNodeWithText("播放历史")
            .performClick()

        // Verify history screen is displayed
        composeTestRule
            .onNodeWithText("播放记录")
            .assertIsDisplayed()
    }

    @Test
    fun navHost_backNavigationWorks() {
        lateinit var navController: TestNavHostController
        
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            
            PodcastTheme {
                PodcastNavGraph(navController = navController)
            }
        }

        // Navigate to search
        composeTestRule
            .onNodeWithText("搜索")
            .performClick()

        // Navigate back
        navController.popBackStack()

        // Verify we're back at home
        assertEquals("home", navController.currentBackStackEntry?.destination?.route)
    }

    @Test
    fun navHost_deepLinkNavigation() {
        lateinit var navController: TestNavHostController
        
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            
            PodcastTheme {
                PodcastNavGraph(navController = navController)
            }
        }

        // Navigate directly to podcast detail with ID
        navController.navigate("podcast_detail/test_podcast_id")

        // Verify navigation occurred
        assertEquals("podcast_detail/{podcastId}", navController.currentBackStackEntry?.destination?.route)
        assertEquals("test_podcast_id", navController.currentBackStackEntry?.arguments?.getString("podcastId"))
    }
}