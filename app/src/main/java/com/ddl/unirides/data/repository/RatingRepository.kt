package com.ddl.unirides.data.repository

import com.ddl.unirides.data.model.Rating
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RatingRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    /**
     * Obtiene todas las calificaciones de un usuario específico en tiempo real
     *
     * @param userId ID del usuario del cual se quieren obtener las calificaciones
     * @return Flow con Result de lista de calificaciones
     */
    fun getUserRatingsFlow(userId: String): Flow<Result<List<Rating>>> = callbackFlow {
        val listener = firestore.collection("ratings")
            .whereEqualTo("ratedUserId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val ratings = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Rating::class.java)?.copy(id = doc.id)
                    }
                    trySend(Result.success(ratings))
                } else {
                    trySend(Result.failure(Exception("Error al obtener calificaciones")))
                }
            }

        awaitClose { listener.remove() }
    }

    /**
     * Crea una nueva calificación
     *
     * @param rating Calificación a crear
     * @return Result con el ID de la calificación creada
     */
    suspend fun createRating(rating: Rating): Result<String> {
        return try {
            val docRef = firestore.collection("ratings")
                .add(rating)
                .await()

            val ratingId = docRef.id
            docRef.update("id", ratingId).await()

            Result.success(ratingId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza una calificación existente
     *
     * @param ratingId ID de la calificación a actualizar
     * @param rating Nueva información de la calificación
     * @return Result con Unit si fue exitoso
     */
    suspend fun updateRating(ratingId: String, rating: Rating): Result<Unit> {
        return try {
            firestore.collection("ratings")
                .document(ratingId)
                .set(rating)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina una calificación
     *
     * @param ratingId ID de la calificación a eliminar
     * @return Result con Unit si fue exitoso
     */
    suspend fun deleteRating(ratingId: String): Result<Unit> {
        return try {
            firestore.collection("ratings")
                .document(ratingId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Verifica si un usuario ya calificó a otro usuario
     *
     * @param raterUserId ID del usuario que califica
     * @param ratedUserId ID del usuario calificado
     * @return Result con true si ya existe una calificación, false si no
     */
    suspend fun hasUserRatedUser(raterUserId: String, ratedUserId: String): Result<Boolean> {
        return try {
            val snapshot = firestore.collection("ratings")
                .whereEqualTo("raterUserId", raterUserId)
                .whereEqualTo("ratedUserId", ratedUserId)
                .limit(1)
                .get()
                .await()

            Result.success(!snapshot.isEmpty)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

