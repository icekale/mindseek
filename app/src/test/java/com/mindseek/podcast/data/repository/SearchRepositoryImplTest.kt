package com.mindseek.podcast.data.repository

import com.mindseek.podcast.data.local.dao.SearchHistoryDao
import com.mindseek.podcast.data.local.entity.SearchHistory
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*

class SearchRepositoryImplTest {

    @Mock
    private lateinit var searchHistoryDao: SearchHistoryDao

    private lateinit var searchRepository: SearchRepositoryImpl

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        searchRepository = SearchRepositoryImpl(searchHistoryDao)
    }

    @Test
    fun `getRecentSearchHistory should return flow from dao`() = runTest {
        val mockHistory = listOf(
            SearchHistory("1", "query1", System.currentTimeMillis(), 5),
            SearchHistory("2", "query2", System.currentTimeMillis(), 3)
        )
        val limit = 10

        whenever(searchHistoryDao.getRecentSearchHistory(limit))
            .thenReturn(flowOf(mockHistory))

        searchRepository.getRecentSearchHistory(limit)

        verify(searchHistoryDao).getRecentSearchHistory(limit)
    }

    @Test
    fun `searchHistory should return flow from dao`() = runTest {
        val query = "test"
        val limit = 5
        val mockHistory = listOf(
            SearchHistory("1", "test query", System.currentTimeMillis(), 2)
        )

        whenever(searchHistoryDao.searchHistory(query, limit))
            .thenReturn(flowOf(mockHistory))

        searchRepository.searchHistory(query, limit)

        verify(searchHistoryDao).searchHistory(query, limit)
    }

    @Test
    fun `saveSearchHistory should delete existing and insert new entry`() = runTest {
        val query = "test query"
        val resultCount = 5

        searchRepository.saveSearchHistory(query, resultCount)

        verify(searchHistoryDao).deleteSearchHistory(query)
        verify(searchHistoryDao).insertSearchHistory(any())
        verify(searchHistoryDao).keepRecentHistory(50)
    }

    @Test
    fun `saveSearchHistory should not save blank query`() = runTest {
        searchRepository.saveSearchHistory("", 5)

        verify(searchHistoryDao, never()).deleteSearchHistory(any())
        verify(searchHistoryDao, never()).insertSearchHistory(any())
        verify(searchHistoryDao, never()).keepRecentHistory(any())
    }

    @Test
    fun `saveSearchHistory should trim query before saving`() = runTest {
        val query = "  test query  "
        val trimmedQuery = "test query"
        val resultCount = 5

        searchRepository.saveSearchHistory(query, resultCount)

        verify(searchHistoryDao).deleteSearchHistory(trimmedQuery)
        verify(searchHistoryDao).insertSearchHistory(argThat { 
            this.query == trimmedQuery 
        })
    }

    @Test
    fun `deleteSearchHistory should call dao delete method`() = runTest {
        val query = "test query"

        searchRepository.deleteSearchHistory(query)

        verify(searchHistoryDao).deleteSearchHistory(query)
    }

    @Test
    fun `clearAllSearchHistory should call dao clear method`() = runTest {
        searchRepository.clearAllSearchHistory()

        verify(searchHistoryDao).clearAllSearchHistory()
    }
}