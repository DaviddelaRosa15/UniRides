package com.ddl.unirides.domain.usecase.rating

import com.ddl.unirides.data.model.User
import com.ddl.unirides.data.repository.RatingRepository
import com.ddl.unirides.data.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

data class UserWithRating(
    val user: User,
    val currentRating: Int = 0,
    val hasRated: Boolean = false
)

class GetVerifiedUsersWithRatingsUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val ratingRepository: RatingRepository
) {
    operator fun invoke(): Flow<Result<List<UserWithRating>>> = flow {
        try {
            // Obtener el ID del usuario actual
            val currentUserId = userRepository.getCurrentUserId()

            if (currentUserId == null) {
                emit(Result.failure(Exception("Usuario no autenticado")))
                return@flow
            }

            // Obtener todos los usuarios verificados
            val usersResult = userRepository.getAllVerifiedUsers()

            if (usersResult.isFailure) {
                emit(
                    Result.failure(
                        usersResult.exceptionOrNull() ?: Exception("Error al obtener usuarios")
                    )
                )
                return@flow
            }

            val users = usersResult.getOrNull() ?: emptyList()

            // Para cada usuario verificado, obtener su calificación si existe
            val usersWithRatings = users
                .filter { it.id != currentUserId } // Excluir al usuario actual
                .map { user ->
                    // Verificar si el usuario actual ya calificó a este usuario
                    val hasRatedResult = ratingRepository.hasUserRatedUser(currentUserId, user.id)
                    val hasRated = hasRatedResult.getOrDefault(false)

                    // Si ya lo calificó, obtener la calificación
                    val currentRating = if (hasRated) {
                        val ratingsResult = ratingRepository.getUserRatings(user.id)
                        ratingsResult.getOrNull()
                            ?.firstOrNull { it.raterUserId == currentUserId }
                            ?.score ?: 0
                    } else {
                        0
                    }

                    UserWithRating(
                        user = user,
                        currentRating = currentRating,
                        hasRated = hasRated
                    )
                }

            emit(Result.success(usersWithRatings))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}

