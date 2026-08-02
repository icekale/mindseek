package com.mindseek.podcast.data.repository

import com.mindseek.podcast.data.local.dao.EpisodeDao
import com.mindseek.podcast.data.local.dao.PodcastDao
import com.mindseek.podcast.data.local.entity.Episode
import com.mindseek.podcast.data.local.entity.Podcast
import com.mindseek.podcast.data.remote.api.PodcastApiService
import com.mindseek.podcast.data.remote.api.NioRadioApi
import com.mindseek.podcast.data.remote.dto.EpisodeDto
import com.mindseek.podcast.data.remote.dto.PodcastDto
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PodcastRepositoryImplTest {

    @Mock
    private lateinit var podcastDao: PodcastDao

    @Mock
    private lateinit var episodeDao: EpisodeDao

    @Mock
    private lateinit var podcastApiService: PodcastApiService

    @Mock
    private lateinit var nioRadioApi: NioRadioApi

    private lateinit var repository: PodcastRepositoryImpl

    private val samplePodcast = Podcast(
        id = "1",
        title = "Test Podcast",
        description = "Test Description",
        imageUrl = "https://example.com/image.jpg",
        author = "Test Author",
        category = "Technology",
        isSubscribed = false,
        lastUpdated = System.currentTimeMillis()
    )

    private val samplePodcastDto = PodcastDto(
        id = "1",
        title = "Test Podcast",
        description = "Test Description",
        imageUrl = "https://example.com/image.jpg",
        author = "Test Author",
        category = "Technology",
        lastUpdated = System.currentTimeMillis()
    )

    private val sampleEpisode = Episode(
        id = "1",
        podcastId = "1",
        title = "Test Episode",
        description = "Test Episode Description",
        audioUrl = "https://example.com/audio.mp3",
        duration = 3600000L,
        publishDate = System.currentTimeMillis(),
        isDownloaded = false,
        localPath = null
    )

    private val sampleEpisodeDto = EpisodeDto(
        id = "1",
        podcastId = "1",
        title = "Test Episode",
        description = "Test Episode Description",
        audioUrl = "https://example.com/audio.mp3",
        duration = 3600000L,
        publishDate = System.currentTimeMillis()
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = PodcastRepositoryImpl(podcastDao, episodeDao, podcastApiService, nioRadioApi)
    }

    @Test
    fun `getAllPodcasts returns flow from dao`() = runTest {
        // Given
        val podcasts = listOf(samplePodcast)
        whenever(podcastDao.getAllPodcasts()).thenReturn(flowOf(podcasts))

        // When
        val result = repository.getAllPodcasts().first()

        // Then
        assertEquals(podcasts, result)
        verify(podcastDao).getAllPodcasts()
    }

    @Test
    fun `getSubscribedPodcasts returns flow from dao`() = runTest {
        // Given
        val subscribedPodcasts = listOf(samplePodcast.copy(isSubscribed = true))
        whenever(podcastDao.getSubscribedPodcasts()).thenReturn(flowOf(subscribedPodcasts))

        // When
        val result = repository.getSubscribedPodcasts().first()

        // Then
        assertEquals(subscribedPodcasts, result)
        verify(podcastDao).getSubscribedPodcasts()
    }

    @Test
    fun `getPodcastById returns podcast from dao`() = runTest {
        // Given
        whenever(podcastDao.getPodcastById("1")).thenReturn(samplePodcast)

        // When
        val result = repository.getPodcastById("1")

        // Then
        assertEquals(samplePodcast, result)
        verify(podcastDao).getPodcastById("1")
    }

    @Test
    fun `getRecommendedPodcasts fetches from API and caches locally`() = runTest {
        // Given
        val remotePodcasts = listOf(samplePodcastDto)
        whenever(podcastApiService.getRecommendedPodcasts(1)).thenReturn(remotePodcasts)

        // When
        val result = repository.getRecommendedPodcasts(1)

        // Then
        assertEquals(1, result.size)
        assertEquals(samplePodcast.title, result[0].title)
        verify(podcastApiService).getRecommendedPodcasts(1)
        verify(podcastDao).insertPodcasts(any())
    }

    @Test
    fun `getRecommendedPodcasts falls back to local data on network error`() = runTest {
        // Given
        whenever(podcastApiService.getRecommendedPodcasts(1)).thenThrow(RuntimeException("Network error"))
        whenever(podcastDao.getAllPodcasts()).thenReturn(flowOf(listOf(samplePodcast)))

        // When
        val result = repository.getRecommendedPodcasts(1)

        // Then
        assertEquals(1, result.size)
        assertEquals(samplePodcast, result[0])
        verify(podcastApiService).getRecommendedPodcasts(1)
        verify(podcastDao).getAllPodcasts()
    }

    @Test
    fun `subscribeToPodcast updates subscription status`() = runTest {
        // When
        repository.subscribeToPodcast("1")

        // Then
        verify(podcastDao).updateSubscriptionStatus("1", true)
    }

    @Test
    fun `unsubscribeFromPodcast updates subscription status`() = runTest {
        // When
        repository.unsubscribeFromPodcast("1")

        // Then
        verify(podcastDao).updateSubscriptionStatus("1", false)
    }

    @Test
    fun `getEpisodesByPodcastId fetches from API and returns flow from dao`() = runTest {
        // Given
        val remoteEpisodes = listOf(sampleEpisodeDto)
        whenever(podcastApiService.getEpisodesByPodcastId("1")).thenReturn(remoteEpisodes)
        whenever(episodeDao.getEpisodesByPodcastId("1")).thenReturn(flowOf(listOf(sampleEpisode)))

        // When
        val result = repository.getEpisodesByPodcastId("1").first()

        // Then
        assertEquals(1, result.size)
        assertEquals(sampleEpisode, result[0])
        verify(podcastApiService).getEpisodesByPodcastId("1")
        verify(episodeDao).insertEpisodes(any())
        verify(episodeDao).getEpisodesByPodcastId("1")
    }

    @Test
    fun `refreshPodcastData updates podcast and episodes from API`() = runTest {
        // Given
        whenever(podcastApiService.getPodcastById("1")).thenReturn(samplePodcastDto)
        whenever(podcastApiService.getEpisodesByPodcastId("1")).thenReturn(listOf(sampleEpisodeDto))
        whenever(podcastDao.getPodcastById("1")).thenReturn(samplePodcast)

        // When
        repository.refreshPodcastData("1")

        // Then
        verify(podcastApiService).getPodcastById("1")
        verify(podcastApiService).getEpisodesByPodcastId("1")
        verify(podcastDao).insertPodcast(any())
        verify(episodeDao).insertEpisodes(any())
    }
}