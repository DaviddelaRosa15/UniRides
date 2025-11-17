package com.ddl.unirides.ui.chatdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ddl.unirides.domain.usecase.chat.GetChatByIdUseCase
import com.ddl.unirides.domain.usecase.chat.MarkChatAsReadUseCase
import com.ddl.unirides.domain.usecase.chat.ObserveChatMessagesUseCase
import com.ddl.unirides.domain.usecase.chat.SendMessageUseCase
import com.ddl.unirides.domain.usecase.offer.GetOfferByIdUseCase
import com.ddl.unirides.domain.usecase.user.GetCurrentUserIdUseCase
import com.ddl.unirides.domain.usecase.user.GetUserByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getChatByIdUseCase: GetChatByIdUseCase,
    private val observeChatMessagesUseCase: ObserveChatMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val markChatAsReadUseCase: MarkChatAsReadUseCase,
    private val getOfferByIdUseCase: GetOfferByIdUseCase,
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
) : ViewModel() {

    private val chatId: String = checkNotNull(savedStateHandle["chatId"])

    private val _uiState = MutableStateFlow(ChatDetailUiState())
    val uiState: StateFlow<ChatDetailUiState> = _uiState.asStateFlow()

    init {
        loadChatData()
        observeMessages()
        markMessagesAsRead()
    }

    private fun loadChatData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val currentUserId = getCurrentUserIdUseCase()
            if (currentUserId == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Usuario no autenticado"
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(currentUserId = currentUserId) }

            // Obtener chat
            getChatByIdUseCase(chatId)
                .onSuccess { chat ->
                    if (chat != null) {
                        // Determinar el otro usuario
                        val otherUserId = if (chat.user1Id == currentUserId) {
                            chat.user2Id
                        } else {
                            chat.user1Id
                        }

                        // Cargar datos del otro usuario
                        getUserByIdUseCase(otherUserId)
                            .onSuccess { user ->
                                _uiState.update { it.copy(otherUser = user) }
                            }
                            .onFailure { e ->
                                _uiState.update {
                                    it.copy(error = "Error al cargar usuario: ${e.message}")
                                }
                            }

                        // Cargar datos de la oferta
                        getOfferByIdUseCase(chat.offerId)
                            .onSuccess { offer ->
                                _uiState.update { it.copy(offer = offer) }
                            }
                            .onFailure { e ->
                                _uiState.update {
                                    it.copy(error = "Error al cargar oferta: ${e.message}")
                                }
                            }

                        _uiState.update { it.copy(isLoading = false) }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Chat no encontrado"
                            )
                        }
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Error al cargar chat: ${e.message}"
                        )
                    }
                }
        }
    }

    private fun observeMessages() {
        viewModelScope.launch {
            observeChatMessagesUseCase(chatId).collect { result ->
                result.onSuccess { messages ->
                    _uiState.update { it.copy(messages = messages) }
                }.onFailure { e ->
                    _uiState.update {
                        it.copy(error = "Error al cargar mensajes: ${e.message}")
                    }
                }
            }
        }
    }

    private fun markMessagesAsRead() {
        viewModelScope.launch {
            markChatAsReadUseCase(chatId)
        }
    }

    fun onMessageChanged(message: String) {
        _uiState.update { it.copy(currentMessage = message) }
    }

    fun sendMessage() {
        val message = _uiState.value.currentMessage.trim()
        if (message.isEmpty() || _uiState.value.isSending) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, error = null) }

            sendMessageUseCase(chatId, message)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            currentMessage = "",
                            isSending = false
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            error = "Error al enviar mensaje: ${e.message}"
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

