package com.ddl.unirides.domain.usecase.chat

import com.ddl.unirides.data.repository.ChatRepository
import com.ddl.unirides.data.repository.UserRepository
import javax.inject.Inject

/**
 * Use case para marcar los mensajes de un chat como leídos
 */
class MarkChatAsReadUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(chatId: String): Result<Unit> {
        val currentUserId = userRepository.getCurrentUserId() ?: return Result.failure(
            Exception("Usuario no autenticado")
        )

        return chatRepository.markMessagesAsRead(chatId, currentUserId)
    }
}

