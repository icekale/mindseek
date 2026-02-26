package com.mindseek.podcast.presentation.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mindseek.podcast.domain.model.PodcastDomain
import com.mindseek.podcast.ui.theme.PodcastTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val samplePodcasts = listOf(
        PodcastDomain(
            id = "1",
            title = "Tech Podcast",
            description = "A podcast about technology",
            imageUrl = "https://example.com/image1.jpg",
            author = "Tech Author",
            category = "Technology",
            isSubscribed = false,
            lastUpdated = System.currentTimeMillis()
        ),
        PodcastDomain(
            id = "2",
            title = "Science Podcast",
            description = "A podcast about science",
            imageUrl = "https://example.com/image2.jpg",
            author = "Science Author",
            category = "Science",
            isSubscribed = true,
            lastUpdated = System.currentTimeMillis()
        )
    )

    @Test
    fun homeScreen_displaysLoadingState() {
        composeTestRule.setContent {
            PodcastTheme {
                HomeScreen(
                    uiState = HomeUiState(isLoading = true),
                    onPodcastClick = {},
                    onRefresh = {}
                )
            }
        }

        // Verify loading indicator is displayed
        composeTestRule
            .onNodeWithContentDescription("Loading")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_displaysPodcastList() {
        composeTestRule.setContent {
            PodcastTheme {
                HomeScreen(
                    uiState = HomeUiState(
                        recommendedPodcasts = samplePodcasts,
                        isLoading = false
                    ),
                    onPodcastClick = {},
                    onRefresh = {}
                )
            }
        }

        // Verify podcast titles are displayed
        composeTestRule
            .onNodeWithText("Tech Podcast")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Science Podcast")
            .assertIsDisplayed()

        // Verify authors are displayed
        composeTestRule
            .onNodeWithText("Tech Author")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Science Author")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_displaysErrorState() {
        val errorMessage = "Network error occurred"
        
        composeTestRule.setContent {
            PodcastTheme {
                HomeScreen(
                    uiState = HomeUiState(
                        isLoading = false,
                        errorMessage = errorMessage
                    ),
                    onPodcastClick = {},
                    onRefresh = {}
                )
            }
        }

        // Verify error message is displayed
        composeTestRule
            .onNodeWithText(errorMessage)
            .assertIsDisplayed()

        // Verify retry button is displayed
        composeTestRule
            .onNodeWithText("重试")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_podcastClickTriggersCallback() {
        var clickedPodcastId: String? = null
        
        composeTestRule.setContent {
            PodcastTheme {
                HomeScreen(
                    uiState = HomeUiState(
                        recommendedPodcasts = samplePodcasts,
                        isLoading = false
                    ),
                    onPodcastClick = { podcastId -> clickedPodcastId = podcastId },
                    onRefresh = {}
                )
            }
        }

        // Click on first podcast
        composeTestRule
            .onNodeWithText("Tech Podcast")
            .performClick()

        // Verify callback was triggered with correct ID
        assert(clickedPodcastId == "1")
    }

    @Test
    fun homeScreen_refreshTriggersCallback() {
        var refreshCalled = false
        
        composeTestRule.setContent {
            PodcastTheme {
                HomeScreen(
                    uiState = HomeUiState(
                        recommendedPodcasts = samplePodcasts,
                        isLoading = false
                    ),
                    onPodcastClick = {},
                    onRefresh = { refreshCalled = true }
                )
            }
        }

        // Perform pull to refresh
        composeTestRule
            .onNodeWithContentDescription("刷新")
            .performClick()

        // Verify refresh callback was triggered
        assert(refreshCalled)
    }

    @Test
    fun homeScreen_displaysEmptyState() {
        composeTestRule.setContent {
            PodcastTheme {
                HomeScreen(
                    uiState = HomeUiState(
                        recommendedPodcasts = emptyList(),
                        isLoading = false
                    ),
                    onPodcastClick = {},
                    onRefresh = {}
                )
            }
        }

        // Verify empty state message is displayed
        composeTestRule
            .onNodeWithText("暂无推荐播客")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_scrollableList() {
        val manyPodcasts = (1..20).map { index ->
            PodcastDomain(
                id = index.toString(),
                title = "Podcast $index",
                description = "Description $index",
                imageUrl = "https://example.com/image$index.jpg",
                author = "Author $index",
                category = "Category",
                isSubscribed = false,
                lastUpdated = System.currentTimeMillis()
            )
        }
        
        composeTestRule.setContent {
            PodcastTheme {
                HomeScreen(
                    uiState = HomeUiState(
                        recommendedPodcasts = manyPodcasts,
                        isLoading = false
                    ),
                    onPodcastClick = {},
                    onRefresh = {}
                )
            }
        }

        // Verify first podcast is visible
        composeTestRule
            .onNodeWithText("Podcast 1")
            .assertIsDisplayed()

        // Scroll to make last podcast visible
        composeTestRule
            .onNodeWithText("Podcast 20")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_subscriptionStatusDisplayed() {
        composeTestRule.setContent {
            PodcastTheme {
                HomeScreen(
                    uiState = HomeUiState(
                        recommendedPodcasts = samplePodcasts,
                        isLoading = false
                    ),
                    onPodcastClick = {},
                    onRefresh = {}
                )
            }
        }

        // Verify subscription status is shown for subscribed podcast
        composeTestRule
            .onNodeWithContentDescription("已订�?)
            .assertIsDisplayed()
    }
}