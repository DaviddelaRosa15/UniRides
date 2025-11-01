package com.ddl.unirides.domain.usecase.auth

import com.ddl.unirides.data.repository.AuthRepository
import com.ddl.unirides.domain.Resource
import javax.inject.Inject

/**
 * Caso de uso para enviar correo de verificación
 */
class SendEmailVerificationUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Resource<Unit> {
        return authRepository.sendEmailVerification()
    }
}

