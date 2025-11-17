package com.ddl.unirides.domain.usecase.chat

import com.ddl.unirides.data.model.Chat
import com.ddl.unirides.data.repository.ChatRepository
import javax.inject.Inject

class GetChatByIdUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(chatId: String): Result<Chat?> {
        return chatRepository.getChatById(chatId)
    }
}

