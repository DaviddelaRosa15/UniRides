package com.ddl.unirides.ui.profile

import com.ddl.unirides.data.model.Rating
import com.ddl.unirides.data.model.User

data class ProfileState(
    val user: User? = null,
    val ratings: List<Rating> = emptyList(),
    val ratingStats: RatingStats = RatingStats(),
    val isLoading: Boolean = true,
    val error: String? = null
)

data class RatingStats(
    val totalRatings: Int = 0,
    val averageRating: Float = 0f,
    val ratingDistribution: Map<Int, Int> = emptyMap(),
    val percentageDistribution: Map<Int, Float> = emptyMap()
)

