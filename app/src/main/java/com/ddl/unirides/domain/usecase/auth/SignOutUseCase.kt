package com.ddl.unirides.domain.usecase.auth

import com.ddl.unirides.data.repository.AuthRepository
import javax.inject.Inject

/**
 * Caso de uso para cerrar sesión
 */
class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke() {
        authRepository.signOut()
    }
}

