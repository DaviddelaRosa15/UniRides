package com.ddl.unirides.domain.usecase.user

import com.ddl.unirides.data.model.User
import com.ddl.unirides.data.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(userId: String): Flow<Result<User>> {
        return userRepository.getUserProfileFlow(userId)
    }
}
