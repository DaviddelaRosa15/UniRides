package com.ddl.unirides.data.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val profilePictureUrl: String? = null,
    val verified: Boolean = false
)