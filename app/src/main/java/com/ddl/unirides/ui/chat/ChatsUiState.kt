package com.ddl.unirides.ui.chat

data class ChatsUiState(
    val chats: List<ChatPreview> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = ""
)

data class ChatPreview(
    val chatId: String,
    val otherUserId: String,
    val otherUserName: String,
    val otherUserProfilePicture: String? = null,
    val origin: String,
    val destination: String,
    val lastMessage: String,
    val lastMessageSenderName: String?,
    val lastMessageTimestamp: Long,
    val unreadCount: Int = 0
)

