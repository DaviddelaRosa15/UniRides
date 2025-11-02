package com.ddl.unirides.domain.usecase.user

import com.ddl.unirides.data.model.Rating
import com.ddl.unirides.data.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserRatingsUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(userId: String): Flow<Result<List<Rating>>> {
        return userRepository.getRatingsFlow(userId)
    }
}

