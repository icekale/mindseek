package com.mindseek.podcast.integration

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mindseek.podcast.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class PodcastAppIntegrationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun completeUserFlow_browseSearchSubscribeAndPlay() {
        // 1. App starts with home screen
        composeTestRule
            .onNodeWithText("推荐")
            .assertIsDisplayed()

        // 2. Navigate to search
        composeTestRule
            .onNodeWithText("搜索")
            .performClick()

        // 3. Verify search screen is displayed
        composeTestRule
            .onNodeWithText("搜索播客或节�?)
            .assertIsDisplayed()

        // 4. Perform a search
        composeTestRule
            .onNodeWithText("搜索播客或节�?)
            .performTextInput("technology")

        // 5. Wait for search results and click on first result
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule
                    .onNodeWithText("Tech Podcast")
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        composeTestRule
            .onNodeWithText("Tech Podcast")
            .performClick()

        // 6. Verify podcast detail screen is displayed
        composeTestRule
            .onNodeWithText("播客详情")
            .assertIsDisplayed()

        // 7. Subscribe to the podcast
        composeTestRule
            .onNodeWithText("订阅")
            .performClick()

        // 8. Verify subscription status changed
        composeTestRule
            .onNodeWithText("已订�?)
            .assertIsDisplayed()

        // 9. Click on an episode to play
        composeTestRule
            .onNodeWithText("Episode 1")
            .performClick()

        // 10. Verify player screen is displayed
        composeTestRule
            .onNodeWithContentDescription("播放")
            .assertIsDisplayed()

        // 11. Start playing
        composeTestRule
            .onNodeWithContentDescription("播放")
            .performClick()

        // 12. Verify pause button appears
        composeTestRule
            .onNodeWithContentDescription("暂停")
            .assertIsDisplayed()

        // 13. Navigate to subscriptions to verify podcast is there
        composeTestRule
            .onNodeWithText("订阅")
            .performClick()

        // 14. Verify subscribed podcast appears in subscriptions
        composeTestRule
            .onNodeWithText("Tech Podcast")
            .assertIsDisplayed()

        // 15. Navigate to profile
        composeTestRule
            .onNodeWithText("我的")
            .performClick()

        // 16. Check play history
        composeTestRule
            .onNodeWithText("播放历史")
            .performClick()

        // 17. Verify episode appears in history
        composeTestRule
            .onNodeWithText("Episode 1")
            .assertIsDisplayed()
    }

    @Test
    fun userFlow_favoriteEpisode() {
        // 1. Start at home screen
        composeTestRule
            .onNodeWithText("推荐")
            .assertIsDisplayed()

        // 2. Navigate to search and find an episode
        composeTestRule
            .onNodeWithText("搜索")
            .performClick()

        composeTestRule
            .onNodeWithText("节目")
            .performClick()

        composeTestRule
            .onNodeWithText("搜索播客或节�?)
            .performTextInput("tech episode")

        // 3. Click on episode result
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule
                    .onNodeWithText("Tech Episode 1")
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        composeTestRule
            .onNodeWithText("Tech Episode 1")
            .performClick()

        // 4. Add to favorites
        composeTestRule
            .onNodeWithContentDescription("收藏")
            .performClick()

        // 5. Navigate to favorites
        composeTestRule
            .onNodeWithText("我的")
            .performClick()

        composeTestRule
            .onNodeWithText("我的收藏")
            .performClick()

        // 6. Verify episode appears in favorites
        composeTestRule
            .onNodeWithText("Tech Episode 1")
            .assertIsDisplayed()
    }

    @Test
    fun userFlow_downloadEpisodeForOfflinePlayback() {
        // 1. Navigate to a podcast episode
        composeTestRule
            .onNodeWithText("搜索")
            .performClick()

        composeTestRule
            .onNodeWithText("搜索播客或节�?)
            .performTextInput("podcast")

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule
                    .onNodeWithText("Sample Podcast")
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        composeTestRule
            .onNodeWithText("Sample Podcast")
            .performClick()

        // 2. Download an episode
        composeTestRule
            .onNodeWithContentDescription("下载")
            .performClick()

        // 3. Verify download started
        composeTestRule
            .onNodeWithContentDescription("下载�?)
            .assertIsDisplayed()

        // 4. Navigate to profile and check downloads
        composeTestRule
            .onNodeWithText("我的")
            .performClick()

        composeTestRule
            .onNodeWithText("下载管理")
            .performClick()

        // 5. Verify episode appears in downloads
        composeTestRule
            .onNodeWithText("Sample Episode")
            .assertIsDisplayed()
    }

    @Test
    fun userFlow_commentOnEpisode() {
        // 1. Navigate to an episode
        composeTestRule
            .onNodeWithText("搜索")
            .performClick()

        composeTestRule
            .onNodeWithText("节目")
            .performClick()

        composeTestRule
            .onNodeWithText("搜索播客或节�?)
            .performTextInput("episode")

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule
                    .onNodeWithText("Sample Episode")
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        composeTestRule
            .onNodeWithText("Sample Episode")
            .performClick()

        // 2. Scroll to comments section
        composeTestRule
            .onNodeWithText("评论")
            .assertIsDisplayed()

        // 3. Add a comment
        composeTestRule
            .onNodeWithText("写评�?..")
            .performClick()

        composeTestRule
            .onNodeWithText("写评�?..")
            .performTextInput("Great episode!")

        composeTestRule
            .onNodeWithText("发布")
            .performClick()

        // 4. Verify comment appears
        composeTestRule
            .onNodeWithText("Great episode!")
            .assertIsDisplayed()
    }

    @Test
    fun userFlow_adjustPlaybackSettings() {
        // 1. Start playing an episode
        composeTestRule
            .onNodeWithText("推荐")
            .assertIsDisplayed()

        // Assume there's a recommended podcast with episodes
        composeTestRule
            .onNodeWithText("Recommended Podcast")
            .performClick()

        composeTestRule
            .onNodeWithText("Episode 1")
            .performClick()

        // 2. Open player controls
        composeTestRule
            .onNodeWithContentDescription("播放")
            .performClick()

        // 3. Adjust playback speed
        composeTestRule
            .onNodeWithText("1.0x")
            .performClick()

        composeTestRule
            .onNodeWithText("1.5x")
            .performClick()

        // 4. Verify speed changed
        composeTestRule
            .onNodeWithText("1.5x")
            .assertIsDisplayed()

        // 5. Adjust volume
        composeTestRule
            .onNodeWithContentDescription("音量控制")
            .assertIsDisplayed()
    }

    @Test
    fun userFlow_manageSubscriptions() {
        // 1. Subscribe to multiple podcasts
        composeTestRule
            .onNodeWithText("搜索")
            .performClick()

        // Subscribe to first podcast
        composeTestRule
            .onNodeWithText("搜索播客或节�?)
            .performTextInput("tech")

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule
                    .onNodeWithText("Tech Podcast")
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        composeTestRule
            .onNodeWithText("Tech Podcast")
            .performClick()

        composeTestRule
            .onNodeWithText("订阅")
            .performClick()

        // 2. Navigate to subscriptions
        composeTestRule
            .onNodeWithText("订阅")
            .performClick()

        // 3. Verify subscribed podcast appears
        composeTestRule
            .onNodeWithText("Tech Podcast")
            .assertIsDisplayed()

        // 4. Unsubscribe from podcast
        composeTestRule
            .onNodeWithText("Tech Podcast")
            .performClick()

        composeTestRule
            .onNodeWithText("取消订阅")
            .performClick()

        // 5. Verify podcast is removed from subscriptions
        composeTestRule
            .onNodeWithText("订阅")
            .performClick()

        // The podcast should no longer appear in the list
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            try {
                composeTestRule
                    .onNodeWithText("Tech Podcast")
                    .assertDoesNotExist()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }
}