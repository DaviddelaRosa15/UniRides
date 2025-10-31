package com.ddl.unirides.domain.usecase.auth

import com.ddl.unirides.data.model.User
import com.ddl.unirides.data.repository.AuthRepository
import com.ddl.unirides.domain.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para obtener el usuario actual
 */
class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<Resource<User?>> {
        return authRepository.getCurrentUser()
    }
}

