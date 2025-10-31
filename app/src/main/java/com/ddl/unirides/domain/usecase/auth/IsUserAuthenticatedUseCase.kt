package com.ddl.unirides.domain.usecase.auth

import com.ddl.unirides.data.repository.AuthRepository
import javax.inject.Inject

/**
 * Caso de uso para verificar si el usuario está autenticado
 */
class IsUserAuthenticatedUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Boolean {
        return authRepository.isUserAuthenticated
    }
}

