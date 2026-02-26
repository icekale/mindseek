package com.mindseek.podcast.domain.usecase

import com.mindseek.podcast.domain.model.EpisodeDomain
import com.mindseek.podcast.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllFavoritesUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    operator fun invoke(): Flow<List<EpisodeDomain>> {
        return favoriteRepository.getAllFavorites()
    }
    
    fun getRecent(limit: Int = 20): Flow<List<EpisodeDomain>> {
        return favoriteRepository.getRecentFavorites(limit)
    }
}