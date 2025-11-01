package com.ddl.unirides.domain.usecase.auth

import com.ddl.unirides.data.repository.AuthRepository
import com.ddl.unirides.domain.Resource
import javax.inject.Inject

/**
 * Caso de uso para verificar si el correo está verificado
 */
class CheckEmailVerifiedUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Resource<Boolean> {
        return authRepository.checkEmailVerified()
    }
}

