package com.ddl.unirides.domain.usecase.chat

import com.ddl.unirides.data.model.Chat
import com.ddl.unirides.data.repository.ChatRepository
import com.ddl.unirides.data.repository.UserRepository
import javax.inject.Inject

/**
 * Use case para obtener o crear un chat entre el usuario actual y otro usuario para una oferta
 */
class GetOrCreateChatUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(otherUserId: String, offerId: String): Result<Chat> {
        val currentUserId = userRepository.getCurrentUserId() ?: return Result.failure(
            Exception("Usuario no autenticado")
        )

        return chatRepository.getOrCreateChat(currentUserId, otherUserId, offerId)
    }
}

