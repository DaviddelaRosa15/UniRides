package com.ddl.unirides.domain.usecase.auth

import com.ddl.unirides.data.repository.AuthRepository
import com.ddl.unirides.domain.Resource
import javax.inject.Inject

/**
 * Caso de uso para enviar email de recuperación de contraseña
 */
class SendPasswordResetEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Resource<Unit> {
        if (email.isBlank()) {
            return Resource.Error("El email es requerido")
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Resource.Error("Formato de email inválido")
        }

        return authRepository.sendPasswordResetEmail(email)
    }
}

