package com.ddl.unirides.ui.offer

data class OfferRideUiState(
    val origin: String = "",
    val destination: String = "",
    val date: String = "",
    val time: String = "",
    val availableSeats: Int = 1,
    val price: String = "",
    val notes: String = "",
    val originError: String? = null,
    val destinationError: String? = null,
    val dateError: String? = null,
    val timeError: String? = null,
    val priceError: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

