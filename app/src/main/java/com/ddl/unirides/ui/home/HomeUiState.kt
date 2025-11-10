package com.ddl.unirides.ui.home

import com.ddl.unirides.data.model.Offer
import com.ddl.unirides.data.model.User
import com.google.firebase.Timestamp

data class HomeUiState(
    val searchQuery: String = "",
    val fromDate: Timestamp? = null,
    val toDate: Timestamp? = null,
    val priceRange: ClosedFloatingPointRange<Float> = 0f..Float.MAX_VALUE,
    val minSeats: Int = 1,
    val maxPrice: Float = 100f,
    val maxSeats: Int = 8,
    val offers: List<OfferWithPublisher> = emptyList(),
    val filteredOffers: List<OfferWithPublisher> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showDateTimeDialog: Boolean = false,
    val showFiltersDialog: Boolean = false
)

data class OfferWithPublisher(
    val offer: Offer,
    val publisher: User? = null
)

