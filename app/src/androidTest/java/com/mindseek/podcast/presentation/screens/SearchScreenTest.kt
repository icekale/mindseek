package com.mindseek.podcast.presentation.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mindseek.podcast.domain.model.EpisodeDomain
import com.mindseek.podcast.domain.model.PodcastDomain
import com.mindseek.podcast.ui.theme.PodcastTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchScreenTest {

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
        )
    )

    private val sampleEpisodes = listOf(
        EpisodeDomain(
            id = "episode1",
            podcastId = "1",
            title = "Episode 1: Introduction to Tech",
            description = "First episode about technology",
            audioUrl = "https://example.com/audio1.mp3",
            duration = 3600000L,
            publishDate = System.currentTimeMillis(),
            imageUrl = "https://example.com/image1.jpg",
            isDownloaded = false,
            localPath = null,
            isFavorite = false
        )
    )

    @Test
    fun searchScreen_displaysInitialState() {
        composeTestRule.setContent {
            PodcastTheme {
                SearchScreen(
                    uiState = SearchUiState(),
                    onSearchQueryChange = {},
                    onSearchTypeChange = {},
                    onPodcastClick = {},
                    onEpisodeClick = {},
                    onClearHistory = {}
                )
            }
        }

        // Verify search input field is displayed
        composeTestRule
            .onNodeWithText("搜索播客或节�?)
            .assertIsDisplayed()

        // Verify search type tabs are displayed
        composeTestRule
            .onNodeWithText("播客")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("节目")
            .assertIsDisplayed()
    }

    @Test
    fun searchScreen_searchInputTriggersCallback() {
        var searchQuery = ""
        
        composeTestRule.setContent {
            PodcastTheme {
                SearchScreen(
                    uiState = SearchUiState(),
                    onSearchQueryChange = { query -> searchQuery = query },
                    onSearchTypeChange = {},
                    onPodcastClick = {},
                    onEpisodeClick = {},
                    onClearHistory = {}
                )
            }
        }

        // Type in search field
        composeTestRule
            .onNodeWithText("搜索播客或节�?)
            .performTextInput("technology")

        // Verify callback was triggered
        assert(searchQuery == "technology")
    }

    @Test
    fun searchScreen_displaysSearchResults() {
        composeTestRule.setContent {
            PodcastTheme {
                SearchScreen(
                    uiState = SearchUiState(
                        searchQuery = "tech",
                        searchType = SearchType.PODCASTS,
                        podcastResults = samplePodcasts,
                        isSearching = false
                    ),
                    onSearchQueryChange = {},
                    onSearchTypeChange = {},
                    onPodcastClick = {},
                    onEpisodeClick = {},
                    onClearHistory = {}
                )
            }
        }

        // Verify search results are displayed
        composeTestRule
            .onNodeWithText("Tech Podcast")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Tech Author")
            .assertIsDisplayed()
    }

    @Test
    fun searchScreen_displaysEpisodeResults() {
        composeTestRule.setContent {
            PodcastTheme {
                SearchScreen(
                    uiState = SearchUiState(
                        searchQuery = "tech",
                        searchType = SearchType.EPISODES,
                        episodeResults = sampleEpisodes,
                        isSearching = false
                    ),
                    onSearchQueryChange = {},
                    onSearchTypeChange = {},
                    onPodcastClick = {},
                    onEpisodeClick = {},
                    onClearHistory = {}
                )
            }
        }

        // Verify episode results are displayed
        composeTestRule
            .onNodeWithText("Episode 1: Introduction to Tech")
            .assertIsDisplayed()
    }

    @Test
    fun searchScreen_displaysLoadingState() {
        composeTestRule.setContent {
            PodcastTheme {
                SearchScreen(
                    uiState = SearchUiState(
                        searchQuery = "tech",
                        isSearching = true
                    ),
                    onSearchQueryChange = {},
                    onSearchTypeChange = {},
                    onPodcastClick = {},
                    onEpisodeClick = {},
                    onClearHistory = {}
                )
            }
        }

        // Verify loading indicator is displayed
        composeTestRule
            .onNodeWithContentDescription("搜索框)
            .assertIsDisplayed()
    }

    @Test
    fun searchScreen_displaysNoResultsState() {
        composeTestRule.setContent {
            PodcastTheme {
                SearchScreen(
                    uiState = SearchUiState(
                        searchQuery = "nonexistent",
                        searchType = SearchType.PODCASTS,
                        podcastResults = emptyList(),
                        isSearching = false
                    ),
                    onSearchQueryChange = {},
                    onSearchTypeChange = {},
                    onPodcastClick = {},
                    onEpisodeClick = {},
                    onClearHistory = {}
                )
            }
        }

        // Verify no results message is displayed
        composeTestRule
            .onNodeWithText("未找到相关结�?)
            .assertIsDisplayed()
    }

    @Test
    fun searchScreen_searchTypeTabsWork() {
        var selectedSearchType = SearchType.PODCASTS
        
        composeTestRule.setContent {
            PodcastTheme {
                SearchScreen(
                    uiState = SearchUiState(searchType = selectedSearchType),
                    onSearchQueryChange = {},
                    onSearchTypeChange = { type -> selectedSearchType = type },
                    onPodcastClick = {},
                    onEpisodeClick = {},
                    onClearHistory = {}
                )
            }
        }

        // Click on episodes tab
        composeTestRule
            .onNodeWithText("节目")
            .performClick()

        // Verify callback was triggered
        assert(selectedSearchType == SearchType.EPISODES)
    }

    @Test
    fun searchScreen_podcastClickTriggersCallback() {
        var clickedPodcastId: String? = null
        
        composeTestRule.setContent {
            PodcastTheme {
                SearchScreen(
                    uiState = SearchUiState(
                        searchQuery = "tech",
                        searchType = SearchType.PODCASTS,
                        podcastResults = samplePodcasts,
                        isSearching = false
                    ),
                    onSearchQueryChange = {},
                    onSearchTypeChange = {},
                    onPodcastClick = { podcastId -> clickedPodcastId = podcastId },
                    onEpisodeClick = {},
                    onClearHistory = {}
                )
            }
        }

        // Click on podcast result
        composeTestRule
            .onNodeWithText("Tech Podcast")
            .performClick()

        // Verify callback was triggered
        assert(clickedPodcastId == "1")
    }

    @Test
    fun searchScreen_episodeClickTriggersCallback() {
        var clickedEpisodeId: String? = null
        
        composeTestRule.setContent {
            PodcastTheme {
                SearchScreen(
                    uiState = SearchUiState(
                        searchQuery = "tech",
                        searchType = SearchType.EPISODES,
                        episodeResults = sampleEpisodes,
                        isSearching = false
                    ),
                    onSearchQueryChange = {},
                    onSearchTypeChange = {},
                    onPodcastClick = {},
                    onEpisodeClick = { episodeId -> clickedEpisodeId = episodeId },
                    onClearHistory = {}
                )
            }
        }

        // Click on episode result
        composeTestRule
            .onNodeWithText("Episode 1: Introduction to Tech")
            .performClick()

        // Verify callback was triggered
        assert(clickedEpisodeId == "episode1")
    }

    @Test
    fun searchScreen_displaysSearchHistory() {
        val searchHistory = listOf("technology", "science", "business")
        
        composeTestRule.setContent {
            PodcastTheme {
                SearchScreen(
                    uiState = SearchUiState(
                        searchHistory = searchHistory
                    ),
                    onSearchQueryChange = {},
                    onSearchTypeChange = {},
                    onPodcastClick = {},
                    onEpisodeClick = {},
                    onClearHistory = {}
                )
            }
        }

        // Verify search history items are displayed
        composeTestRule
            .onNodeWithText("technology")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("science")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("business")
            .assertIsDisplayed()

        // Verify clear history button is displayed
        composeTestRule
            .onNodeWithText("清除历史")
            .assertIsDisplayed()
    }

    @Test
    fun searchScreen_clearHistoryTriggersCallback() {
        var historyClearCalled = false
        val searchHistory = listOf("technology", "science")
        
        composeTestRule.setContent {
            PodcastTheme {
                SearchScreen(
                    uiState = SearchUiState(
                        searchHistory = searchHistory
                    ),
                    onSearchQueryChange = {},
                    onSearchTypeChange = {},
                    onPodcastClick = {},
                    onEpisodeClick = {},
                    onClearHistory = { historyClearCalled = true }
                )
            }
        }

        // Click clear history button
        composeTestRule
            .onNodeWithText("清除历史")
            .performClick()

        // Verify callback was triggered
        assert(historyClearCalled)
    }
}