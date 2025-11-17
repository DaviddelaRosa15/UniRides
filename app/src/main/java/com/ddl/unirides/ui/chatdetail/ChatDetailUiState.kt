package com.ddl.unirides.ui.chatdetail

import com.ddl.unirides.data.model.Message
import com.ddl.unirides.data.model.Offer
import com.ddl.unirides.data.model.User

data class ChatDetailUiState(
    val isLoading: Boolean = true,
    val messages: List<Message> = emptyList(),
    val otherUser: User? = null,
    val offer: Offer? = null,
    val currentUserId: String = "",
    val currentMessage: String = "",
    val isSending: Boolean = false,
    val error: String? = null
)

