package com.mindseek.podcast.domain.usecase

import com.mindseek.podcast.domain.model.EpisodeDomain
import com.mindseek.podcast.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SearchFavoritesUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    operator fun invoke(query: String): Flow<List<EpisodeDomain>> {
        return favoriteRepository.getAllFavorites().map { favorites ->
            if (query.isBlank()) {
                favorites
            } else {
                favorites.filter { episode ->
                    episode.title.contains(query, ignoreCase = true) ||
                    episode.description.contains(query, ignoreCase = true)
                }
            }
        }
    }
}