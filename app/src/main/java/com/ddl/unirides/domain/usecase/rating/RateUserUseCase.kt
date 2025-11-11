package com.ddl.unirides.domain.usecase.rating

import com.ddl.unirides.data.model.Rating
import com.ddl.unirides.data.repository.RatingRepository
import com.ddl.unirides.data.repository.UserRepository
import com.google.firebase.Timestamp
import javax.inject.Inject

class RateUserUseCase @Inject constructor(
    private val ratingRepository: RatingRepository,
    private val userRepository: UserRepository
) {
    /**
     * Califica o actualiza la calificación de un usuario
     *
     * @param ratedUserId ID del usuario que será calificado
     * @param score Puntuación de 1 a 5
     * @return Result con Unit si fue exitoso, o Exception si falló
     */
    suspend operator fun invoke(ratedUserId: String, score: Int): Result<Unit> {
        return try {
            // Validar score
            if (score !in 1..5) {
                return Result.failure(Exception("La calificación debe estar entre 1 y 5"))
            }

            // Obtener ID del usuario actual
            val currentUserId = userRepository.getCurrentUserId()
            if (currentUserId == null) {
                return Result.failure(Exception("Usuario no autenticado"))
            }

            // No permitir auto-calificación
            if (currentUserId == ratedUserId) {
                return Result.failure(Exception("No puedes calificarte a ti mismo"))
            }

            // Verificar si ya existe una calificación
            val hasRatedResult = ratingRepository.hasUserRatedUser(currentUserId, ratedUserId)
            val hasRated = hasRatedResult.getOrDefault(false)

            if (hasRated) {
                // Actualizar calificación existente
                val ratingsResult = ratingRepository.getUserRatings(ratedUserId)
                val existingRating = ratingsResult.getOrNull()
                    ?.firstOrNull { it.raterUserId == currentUserId }

                if (existingRating != null) {
                    val updatedRating = existingRating.copy(
                        score = score,
                        timestamp = Timestamp.now()
                    )
                    ratingRepository.updateRating(existingRating.id, updatedRating)
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Error al obtener la calificación existente"))
                }
            } else {
                // Crear nueva calificación
                val newRating = Rating(
                    raterUserId = currentUserId,
                    ratedUserId = ratedUserId,
                    score = score,
                    comment = null,
                    timestamp = Timestamp.now()
                )
                ratingRepository.createRating(newRating)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

