package com.mindseek.podcast.domain.usecase

import com.mindseek.podcast.domain.repository.FavoriteRepository
import javax.inject.Inject

class AddToFavoritesUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    suspend operator fun invoke(episodeId: String): Boolean {
        return favoriteRepository.addToFavorites(episodeId)
    }
}