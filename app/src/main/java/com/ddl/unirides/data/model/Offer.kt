package com.ddl.unirides.data.model

import com.google.firebase.Timestamp

data class Offer(
    val id: String = "",
    val publisherUserId: String = "",
    val destination: String = "",
    val origin: String = "",
    val dateTime: Timestamp = Timestamp.now(),
    val price: Double = 0.0,
    val availableSeats: Int = 0,
    val details: String? = null
)