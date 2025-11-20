package com.ddl.unirides.ui.tripdetail

import com.ddl.unirides.data.model.Offer
import com.ddl.unirides.data.model.User

data class TripDetailUiState(
    val offer: Offer? = null,
    val driver: User? = null,
    val currentUserId: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
    val isTripPassed: Boolean = false,
    val isCurrentUserTheDriver: Boolean = false
)

