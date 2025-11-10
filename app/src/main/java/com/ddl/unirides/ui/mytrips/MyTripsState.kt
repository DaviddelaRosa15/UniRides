package com.ddl.unirides.ui.mytrips

import com.ddl.unirides.data.model.Offer

data class MyTripsState(
    val offers: List<Offer> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

