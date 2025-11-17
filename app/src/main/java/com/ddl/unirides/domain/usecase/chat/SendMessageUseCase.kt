package com.ddl.unirides.domain.usecase.chat

import com.ddl.unirides.data.model.Message
import com.ddl.unirides.data.repository.ChatRepository
import com.ddl.unirides.data.repository.UserRepository
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(chatId: String, content: String): Result<Message> {
        val currentUserId = userRepository.getCurrentUserId()
            ?: return Result.failure(Exception("Usuario no autenticado"))

        return chatRepository.sendMessage(chatId, currentUserId, content)
    }
}

