package com.ddl.unirides.ui.rating

import com.ddl.unirides.data.model.User

data class RateDriverUiState(
    val searchQuery: String = "",
    val drivers: List<DriverToRate> = emptyList(),
    val filteredDrivers: List<DriverToRate> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class DriverToRate(
    val driver: User,
    val currentRating: Int = 0,
    val hasRated: Boolean = false
)

