package com.ddl.unirides.domain.usecase.chat

import com.ddl.unirides.data.model.Message
import com.ddl.unirides.data.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveChatMessagesUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    operator fun invoke(chatId: String): Flow<Result<List<Message>>> {
        return repository.getChatMessagesFlow(chatId)
    }
}

