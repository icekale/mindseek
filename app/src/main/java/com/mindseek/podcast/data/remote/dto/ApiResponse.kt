package com.mindseek.podcast.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Generic API response wrapper
 */
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("data")
    val data: T? = null,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("error_code")
    val errorCode: String? = null,
    
    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Paginated response wrapper
 */
data class PaginatedResponse<T>(
    @SerializedName("items")
    val items: List<T>,
    
    @SerializedName("page")
    val page: Int,
    
    @SerializedName("limit")
    val limit: Int,
    
    @SerializedName("total_count")
    val totalCount: Int,
    
    @SerializedName("total_pages")
    val totalPages: Int,
    
    @SerializedName("has_next")
    val hasNext: Boolean,
    
    @SerializedName("has_previous")
    val hasPrevious: Boolean
)

/**
 * Search response with additional metadata
 */
data class SearchResponse<T>(
    @SerializedName("query")
    val query: String,
    
    @SerializedName("results")
    val results: List<T>,
    
    @SerializedName("total_results")
    val totalResults: Int,
    
    @SerializedName("search_time_ms")
    val searchTimeMs: Long,
    
    @SerializedName("suggestions")
    val suggestions: List<String> = emptyList(),
    
    @SerializedName("page")
    val page: Int,
    
    @SerializedName("limit")
    val limit: Int
)