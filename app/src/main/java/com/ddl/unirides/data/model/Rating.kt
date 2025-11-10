package com.ddl.unirides.data.model

import com.google.firebase.Timestamp

data class Rating(
    val id: String = "",
    val raterUserId: String = "",      // Usuario que califica
    val ratedUserId: String = "",      // Usuario que es calificado
    val score: Int = 0,                 // 1-5
    val comment: String? = null,
    val timestamp: Timestamp = Timestamp.now()
)