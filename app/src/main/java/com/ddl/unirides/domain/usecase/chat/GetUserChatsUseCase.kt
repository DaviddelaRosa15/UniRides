package com.ddl.unirides.domain.usecase.chat

import com.ddl.unirides.data.model.Chat
import com.ddl.unirides.data.model.Message
import com.ddl.unirides.data.model.Offer
import com.ddl.unirides.data.model.User
import com.ddl.unirides.data.repository.ChatRepository
import com.ddl.unirides.data.repository.OfferRepository
import com.ddl.unirides.data.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Use case para obtener todos los chats del usuario actual con información enriquecida
 */
class GetUserChatsUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val offerRepository: OfferRepository
) {
    /**
     * Retorna un Flow con los chats enriquecidos con información del otro usuario,
     * la oferta, el último mensaje y el conteo de mensajes no leídos
     */
    operator fun invoke(): Flow<Result<List<ChatWithDetails>>> = flow {
        val currentUserId = userRepository.getCurrentUserId() ?: run {
            emit(Result.failure(Exception("Usuario no autenticado")))
            return@flow
        }

        chatRepository.getUserChatsFlow(currentUserId).collect { result ->
            result.fold(
                onSuccess = { chats ->
                    try {
                        val chatsWithDetails = chats.map { chat ->
                            // Determinar el ID del otro usuario
                            val otherUserId = if (chat.user1Id == currentUserId) {
                                chat.user2Id
                            } else {
                                chat.user1Id
                            }

                            // Obtener información del otro usuario desde UserRepository
                            val otherUserResult = userRepository.getUserProfile(otherUserId)
                            val otherUser = otherUserResult.getOrNull()

                            // Obtener información de la oferta desde OfferRepository
                            val offerResult = offerRepository.getOfferById(chat.offerId)
                            val offer = offerResult.getOrNull()

                            // Obtener el último mensaje
                            val lastMessageResult = chatRepository.getLastMessage(chat.id)
                            val lastMessage = lastMessageResult.getOrNull()

                            // Obtener conteo de mensajes no leídos
                            val unreadCountResult =
                                chatRepository.getUnreadMessageCount(chat.id, currentUserId)
                            val unreadCount = unreadCountResult.getOrNull() ?: 0

                            ChatWithDetails(
                                chat = chat,
                                otherUser = otherUser,
                                offer = offer,
                                lastMessage = lastMessage,
                                unreadCount = unreadCount
                            )
                        }

                        emit(Result.success(chatsWithDetails))
                    } catch (e: Exception) {
                        emit(Result.failure(e))
                    }
                },
                onFailure = { exception ->
                    emit(Result.failure(exception))
                }
            )
        }
    }
}

/**
 * Clase de datos que contiene un chat con toda la información necesaria para mostrarlo en la UI
 */
data class ChatWithDetails(
    val chat: Chat,
    val otherUser: User?,
    val offer: Offer?,
    val lastMessage: Message?,
    val unreadCount: Int
)

