package com.mindseek.podcast.domain.repository

import com.mindseek.podcast.data.local.entity.SearchHistory
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    fun getRecentSearchHistory(limit: Int = 10): Flow<List<SearchHistory>>
    fun searchHistory(query: String, limit: Int = 5): Flow<List<SearchHistory>>
    suspend fun saveSearchHistory(query: String, resultCount: Int)
    suspend fun deleteSearchHistory(query: String)
    suspend fun clearAllSearchHistory()
}