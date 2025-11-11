package com.ddl.unirides.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ddl.unirides.domain.usecase.chat.GetUserChatsUseCase
import com.ddl.unirides.domain.usecase.chat.MarkChatAsReadUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val getUserChatsUseCase: GetUserChatsUseCase,
    private val markChatAsReadUseCase: MarkChatAsReadUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatsUiState())
    val uiState: StateFlow<ChatsUiState> = _uiState.asStateFlow()

    init {
        loadChats()
    }

    private fun loadChats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getUserChatsUseCase().collect { result ->
                result.fold(
                    onSuccess = { chatsWithDetails ->
                        val chatPreviews = chatsWithDetails.map { chatDetail ->
                            ChatPreview(
                                chatId = chatDetail.chat.id,
                                otherUserId = chatDetail.otherUser?.id ?: "",
                                otherUserName = chatDetail.otherUser?.name ?: "Usuario desconocido",
                                otherUserProfilePicture = chatDetail.otherUser?.profilePictureUrl,
                                origin = chatDetail.offer?.origin ?: "Origen desconocido",
                                destination = chatDetail.offer?.destination
                                    ?: "Destino desconocido",
                                lastMessage = chatDetail.lastMessage?.content ?: "No hay mensajes",
                                lastMessageSenderName = if (chatDetail.lastMessage?.senderId == chatDetail.otherUser?.id) {
                                    chatDetail.otherUser?.name
                                } else {
                                    null // El mensaje fue enviado por el usuario actual
                                },
                                lastMessageTimestamp = chatDetail.lastMessage?.timestamp?.toDate()?.time
                                    ?: chatDetail.chat.lastMessageTimestamp.toDate().time,
                                unreadCount = chatDetail.unreadCount
                            )
                        }

                        _uiState.update {
                            it.copy(
                                chats = chatPreviews,
                                isLoading = false,
                                error = null
                            )
                        }
                    },
                    onFailure = { exception ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = exception.message ?: "Error al cargar los chats"
                            )
                        }
                    }
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onChatClick(chatId: String) {
        viewModelScope.launch {
            // Marcar los mensajes como leídos cuando se abre el chat
            markChatAsReadUseCase(chatId)
        }
    }

    fun retryLoading() {
        loadChats()
    }
}

