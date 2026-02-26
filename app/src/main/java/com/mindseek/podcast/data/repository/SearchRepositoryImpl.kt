package com.mindseek.podcast.data.repository

import com.mindseek.podcast.data.local.dao.SearchHistoryDao
import com.mindseek.podcast.data.local.entity.SearchHistory
import com.mindseek.podcast.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepositoryImpl @Inject constructor(
    private val searchHistoryDao: SearchHistoryDao
) : SearchRepository {

    override fun getRecentSearchHistory(limit: Int): Flow<List<SearchHistory>> {
        return searchHistoryDao.getRecentSearchHistory(limit)
    }

    override fun searchHistory(query: String, limit: Int): Flow<List<SearchHistory>> {
        return searchHistoryDao.searchHistory(query, limit)
    }

    override suspend fun saveSearchHistory(query: String, resultCount: Int) {
        if (query.isBlank()) return
        
        val searchHistory = SearchHistory(
            id = UUID.randomUUID().toString(),
            query = query.trim(),
            timestamp = System.currentTimeMillis(),
            resultCount = resultCount
        )
        
        // Delete existing entry with same query first
        searchHistoryDao.deleteSearchHistory(query.trim())
        
        // Insert new entry
        searchHistoryDao.insertSearchHistory(searchHistory)
        
        // Keep only recent 50 entries to avoid database bloat
        searchHistoryDao.keepRecentHistory(50)
    }

    override suspend fun deleteSearchHistory(query: String) {
        searchHistoryDao.deleteSearchHistory(query)
    }

    override suspend fun clearAllSearchHistory() {
        searchHistoryDao.clearAllSearchHistory()
    }
}