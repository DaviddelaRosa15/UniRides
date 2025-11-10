package com.ddl.unirides.domain.usecase.user

import com.ddl.unirides.data.repository.UserRepository
import javax.inject.Inject

/**
 * Caso de uso para obtener solo el ID del usuario actual
 */
class GetCurrentUserIdUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): String? {
        return userRepository.getCurrentUserId()
    }
}

