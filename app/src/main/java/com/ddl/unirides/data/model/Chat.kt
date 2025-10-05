package com.ddl.unirides.data.model

import com.google.firebase.Timestamp

data class Chat(
    val id: String = "",
    val user1Id: String = "",
    val user2Id: String = "",
    val offerId: String = "",
    val lastMessageTimestamp: Timestamp = Timestamp.now()
)