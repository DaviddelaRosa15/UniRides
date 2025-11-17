package com.ddl.unirides.domain.usecase.user

import com.ddl.unirides.data.model.User
import com.ddl.unirides.data.repository.UserRepository
import javax.inject.Inject

class GetUserByIdUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): Result<User> {
        return userRepository.getUserProfile(userId)
    }
}

