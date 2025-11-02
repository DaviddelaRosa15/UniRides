package com.ddl.unirides.data.repository

import com.ddl.unirides.data.model.Rating
import com.ddl.unirides.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    suspend fun getUserProfile(userId: String): Result<User> {
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            if (document.exists()) {
                val user = document.toObject(User::class.java)
                if (user != null) {
                    Result.success(user.copy(id = document.id))
                } else {
                    Result.failure(Exception("Error al convertir usuario"))
                }
            } else {
                Result.failure(Exception("Usuario no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserProfileFlow(userId: String): Flow<Result<User>> = callbackFlow {
        val listener = firestore.collection("users")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject(User::class.java)
                    if (user != null) {
                        trySend(Result.success(user.copy(id = snapshot.id)))
                    } else {
                        trySend(Result.failure(Exception("Error al convertir usuario")))
                    }
                } else {
                    trySend(Result.failure(Exception("Usuario no encontrado")))
                }
            }

        awaitClose { listener.remove() }
    }

    suspend fun getRatings(userId: String): Result<List<Rating>> {
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("ratings")
                .get()
                .await()

            val ratings = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Rating::class.java)?.copy(id = doc.id)
            }

            Result.success(ratings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getRatingsFlow(userId: String): Flow<Result<List<Rating>>> = callbackFlow {
        val listener = firestore.collection("users")
            .document(userId)
            .collection("ratings")
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
                    trySend(Result.failure(Exception("Error al obtener ratings")))
                }
            }

        awaitClose { listener.remove() }
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid
}
