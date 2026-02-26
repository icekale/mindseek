package com.mindseek.podcast.presentation.player

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mindseek.podcast.domain.model.EpisodeDomain
import com.mindseek.podcast.ui.theme.PodcastTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlayerScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleEpisode = EpisodeDomain(
        id = "episode1",
        podcastId = "podcast1",
        title = "Episode 1: Introduction to Tech",
        description = "First episode about technology trends and innovations",
        audioUrl = "https://example.com/audio1.mp3",
        duration = 3600000L, // 1 hour
        publishDate = System.currentTimeMillis(),
        imageUrl = "https://example.com/image1.jpg",
        isDownloaded = false,
        localPath = null,
        isFavorite = false
    )

    @Test
    fun playerScreen_displaysEpisodeInfo() {
        composeTestRule.setContent {
            PodcastTheme {
                PlayerScreen(
                    uiState = PlayerUiState(
                        currentEpisode = sampleEpisode,
                        isPlaying = false,
                        currentPosition = 0L,
                        duration = 3600000L
                    ),
                    onPlayPause = {},
                    onSeek = {},
                    onSpeedChange = {},
                    onVolumeChange = {},
                    onNext = {},
                    onPrevious = {},
                    onToggleFavorite = {},
                    onShare = {},
                    onClose = {}
                )
            }
        }

        // Verify episode title is displayed
        composeTestRule
            .onNodeWithText("Episode 1: Introduction to Tech")
            .assertIsDisplayed()

        // Verify episode description is displayed
        composeTestRule
            .onNodeWithText("First episode about technology trends and innovations")
            .assertIsDisplayed()
    }

    @Test
    fun playerScreen_displaysPlayPauseButton() {
        composeTestRule.setContent {
            PodcastTheme {
                PlayerScreen(
                    uiState = PlayerUiState(
                        currentEpisode = sampleEpisode,
                        isPlaying = false,
                        currentPosition = 0L,
                        duration = 3600000L
                    ),
                    onPlayPause = {},
                    onSeek = {},
                    onSpeedChange = {},
                    onVolumeChange = {},
                    onNext = {},
                    onPrevious = {},
                    onToggleFavorite = {},
                    onShare = {},
                    onClose = {}
                )
            }
        }

        // Verify play button is displayed when not playing
        composeTestRule
            .onNodeWithContentDescription("播放")
            .assertIsDisplayed()
    }

    @Test
    fun playerScreen_displaysPauseButtonWhenPlaying() {
        composeTestRule.setContent {
            PodcastTheme {
                PlayerScreen(
                    uiState = PlayerUiState(
                        currentEpisode = sampleEpisode,
                        isPlaying = true,
                        currentPosition = 1800000L, // 30 minutes
                        duration = 3600000L
                    ),
                    onPlayPause = {},
                    onSeek = {},
                    onSpeedChange = {},
                    onVolumeChange = {},
                    onNext = {},
                    onPrevious = {},
                    onToggleFavorite = {},
                    onShare = {},
                    onClose = {}
                )
            }
        }

        // Verify pause button is displayed when playing
        composeTestRule
            .onNodeWithContentDescription("暂停")
            .assertIsDisplayed()
    }

    @Test
    fun playerScreen_displaysProgressBar() {
        composeTestRule.setContent {
            PodcastTheme {
                PlayerScreen(
                    uiState = PlayerUiState(
                        currentEpisode = sampleEpisode,
                        isPlaying = true,
                        currentPosition = 1800000L, // 30 minutes
                        duration = 3600000L // 1 hour
                    ),
                    onPlayPause = {},
                    onSeek = {},
                    onSpeedChange = {},
                    onVolumeChange = {},
                    onNext = {},
                    onPrevious = {},
                    onToggleFavorite = {},
                    onShare = {},
                    onClose = {}
                )
            }
        }

        // Verify progress bar is displayed
        composeTestRule
            .onNodeWithContentDescription("播放进度")
            .assertIsDisplayed()

        // Verify time displays
        composeTestRule
            .onNodeWithText("30:00")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("60:00")
            .assertIsDisplayed()
    }

    @Test
    fun playerScreen_playPauseButtonTriggersCallback() {
        var playPauseCalled = false
        
        composeTestRule.setContent {
            PodcastTheme {
                PlayerScreen(
                    uiState = PlayerUiState(
                        currentEpisode = sampleEpisode,
                        isPlaying = false,
                        currentPosition = 0L,
                        duration = 3600000L
                    ),
                    onPlayPause = { playPauseCalled = true },
                    onSeek = {},
                    onSpeedChange = {},
                    onVolumeChange = {},
                    onNext = {},
                    onPrevious = {},
                    onToggleFavorite = {},
                    onShare = {},
                    onClose = {}
                )
            }
        }

        // Click play button
        composeTestRule
            .onNodeWithContentDescription("播放")
            .performClick()

        // Verify callback was triggered
        assert(playPauseCalled)
    }

    @Test
    fun playerScreen_displaysNavigationButtons() {
        composeTestRule.setContent {
            PodcastTheme {
                PlayerScreen(
                    uiState = PlayerUiState(
                        currentEpisode = sampleEpisode,
                        isPlaying = false,
                        currentPosition = 0L,
                        duration = 3600000L
                    ),
                    onPlayPause = {},
                    onSeek = {},
                    onSpeedChange = {},
                    onVolumeChange = {},
                    onNext = {},
                    onPrevious = {},
                    onToggleFavorite = {},
                    onShare = {},
                    onClose = {}
                )
            }
        }

        // Verify previous button is displayed
        composeTestRule
            .onNodeWithContentDescription("上一�?)
            .assertIsDisplayed()

        // Verify next button is displayed
        composeTestRule
            .onNodeWithContentDescription("下一�?)
            .assertIsDisplayed()
    }

    @Test
    fun playerScreen_nextButtonTriggersCallback() {
        var nextCalled = false
        
        composeTestRule.setContent {
            PodcastTheme {
                PlayerScreen(
                    uiState = PlayerUiState(
                        currentEpisode = sampleEpisode,
                        isPlaying = false,
                        currentPosition = 0L,
                        duration = 3600000L
                    ),
                    onPlayPause = {},
                    onSeek = {},
                    onSpeedChange = {},
                    onVolumeChange = {},
                    onNext = { nextCalled = true },
                    onPrevious = {},
                    onToggleFavorite = {},
                    onShare = {},
                    onClose = {}
                )
            }
        }

        // Click next button
        composeTestRule
            .onNodeWithContentDescription("下一�?)
            .performClick()

        // Verify callback was triggered
        assert(nextCalled)
    }

    @Test
    fun playerScreen_previousButtonTriggersCallback() {
        var previousCalled = false
        
        composeTestRule.setContent {
            PodcastTheme {
                PlayerScreen(
                    uiState = PlayerUiState(
                        currentEpisode = sampleEpisode,
                        isPlaying = false,
                        currentPosition = 0L,
                        duration = 3600000L
                    ),
                    onPlayPause = {},
                    onSeek = {},
                    onSpeedChange = {},
                    onVolumeChange = {},
                    onNext = {},
                    onPrevious = { previousCalled = true },
                    onToggleFavorite = {},
                    onShare = {},
                    onClose = {}
                )
            }
        }

        // Click previous button
        composeTestRule
            .onNodeWithContentDescription("上一�?)
            .performClick()

        // Verify callback was triggered
        assert(previousCalled)
    }

    @Test
    fun playerScreen_displaysPlaybackSpeedControl() {
        composeTestRule.setContent {
            PodcastTheme {
                PlayerScreen(
                    uiState = PlayerUiState(
                        currentEpisode = sampleEpisode,
                        isPlaying = false,
                        currentPosition = 0L,
                        duration = 3600000L,
                        playbackSpeed = 1.0f
                    ),
                    onPlayPause = {},
                    onSeek = {},
                    onSpeedChange = {},
                    onVolumeChange = {},
                    onNext = {},
                    onPrevious = {},
                    onToggleFavorite = {},
                    onShare = {},
                    onClose = {}
                )
            }
        }

        // Verify playback speed control is displayed
        composeTestRule
            .onNodeWithText("1.0x")
            .assertIsDisplayed()
    }

    @Test
    fun playerScreen_displaysFavoriteButton() {
        composeTestRule.setContent {
            PodcastTheme {
                PlayerScreen(
                    uiState = PlayerUiState(
                        currentEpisode = sampleEpisode,
                        isPlaying = false,
                        currentPosition = 0L,
                        duration = 3600000L
                    ),
                    onPlayPause = {},
                    onSeek = {},
                    onSpeedChange = {},
                    onVolumeChange = {},
                    onNext = {},
                    onPrevious = {},
                    onToggleFavorite = {},
                    onShare = {},
                    onClose = {}
                )
            }
        }

        // Verify favorite button is displayed
        composeTestRule
            .onNodeWithContentDescription("收藏")
            .assertIsDisplayed()
    }

    @Test
    fun playerScreen_favoriteButtonTriggersCallback() {
        var favoriteCalled = false
        
        composeTestRule.setContent {
            PodcastTheme {
                PlayerScreen(
                    uiState = PlayerUiState(
                        currentEpisode = sampleEpisode,
                        isPlaying = false,
                        currentPosition = 0L,
                        duration = 3600000L
                    ),
                    onPlayPause = {},
                    onSeek = {},
                    onSpeedChange = {},
                    onVolumeChange = {},
                    onNext = {},
                    onPrevious = {},
                    onToggleFavorite = { favoriteCalled = true },
                    onShare = {},
                    onClose = {}
                )
            }
        }

        // Click favorite button
        composeTestRule
            .onNodeWithContentDescription("收藏")
            .performClick()

        // Verify callback was triggered
        assert(favoriteCalled)
    }

    @Test
    fun playerScreen_displaysShareButton() {
        composeTestRule.setContent {
            PodcastTheme {
                PlayerScreen(
                    uiState = PlayerUiState(
                        currentEpisode = sampleEpisode,
                        isPlaying = false,
                        currentPosition = 0L,
                        duration = 3600000L
                    ),
                    onPlayPause = {},
                    onSeek = {},
                    onSpeedChange = {},
                    onVolumeChange = {},
                    onNext = {},
                    onPrevious = {},
                    onToggleFavorite = {},
                    onShare = {},
                    onClose = {}
                )
            }
        }

        // Verify share button is displayed
        composeTestRule
            .onNodeWithContentDescription("分享")
            .assertIsDisplayed()
    }

    @Test
    fun playerScreen_shareButtonTriggersCallback() {
        var shareCalled = false
        
        composeTestRule.setContent {
            PodcastTheme {
                PlayerScreen(
                    uiState = PlayerUiState(
                        currentEpisode = sampleEpisode,
                        isPlaying = false,
                        currentPosition = 0L,
                        duration = 3600000L
                    ),
                    onPlayPause = {},
                    onSeek = {},
                    onSpeedChange = {},
                    onVolumeChange = {},
                    onNext = {},
                    onPrevious = {},
                    onToggleFavorite = {},
                    onShare = { shareCalled = true },
                    onClose = {}
                )
            }
        }

        // Click share button
        composeTestRule
            .onNodeWithContentDescription("分享")
            .performClick()

        // Verify callback was triggered
        assert(shareCalled)
    }

    @Test
    fun playerScreen_displaysCloseButton() {
        composeTestRule.setContent {
            PodcastTheme {
                PlayerScreen(
                    uiState = PlayerUiState(
                        currentEpisode = sampleEpisode,
                        isPlaying = false,
                        currentPosition = 0L,
                        duration = 3600000L
                    ),
                    onPlayPause = {},
                    onSeek = {},
                    onSpeedChange = {},
                    onVolumeChange = {},
                    onNext = {},
                    onPrevious = {},
                    onToggleFavorite = {},
                    onShare = {},
                    onClose = {}
                )
            }
        }

        // Verify close button is displayed
        composeTestRule
            .onNodeWithContentDescription("关闭")
            .assertIsDisplayed()
    }

    @Test
    fun playerScreen_closeButtonTriggersCallback() {
        var closeCalled = false
        
        composeTestRule.setContent {
            PodcastTheme {
                PlayerScreen(
                    uiState = PlayerUiState(
                        currentEpisode = sampleEpisode,
                        isPlaying = false,
                        currentPosition = 0L,
                        duration = 3600000L
                    ),
                    onPlayPause = {},
                    onSeek = {},
                    onSpeedChange = {},
                    onVolumeChange = {},
                    onNext = {},
                    onPrevious = {},
                    onToggleFavorite = {},
                    onShare = {},
                    onClose = { closeCalled = true }
                )
            }
        }

        // Click close button
        composeTestRule
            .onNodeWithContentDescription("关闭")
            .performClick()

        // Verify callback was triggered
        assert(closeCalled)
    }
}