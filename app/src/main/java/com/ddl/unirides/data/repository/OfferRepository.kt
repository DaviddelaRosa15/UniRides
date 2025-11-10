package com.ddl.unirides.data.repository

import com.ddl.unirides.data.model.Offer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfferRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    /**
     * Obtiene todas las ofertas publicadas por un usuario específico.
     * NOTA: Requiere crear índice compuesto en Firestore (ver FIRESTORE_INDICES.md)
     */
    fun getPublishedOffersFlow(userId: String): Flow<Result<List<Offer>>> = callbackFlow {
        val listener = firestore.collection("offers")
            .whereEqualTo("publisherUserId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val offers = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Offer::class.java)?.copy(id = doc.id)
                    }
                    // Ordenar por fecha en el cliente (descendente - más recientes primero)
                    val sortedOffers = offers.sortedByDescending { it.date.toDate() }
                    trySend(Result.success(sortedOffers))
                } else {
                    trySend(Result.failure(Exception("Error al obtener ofertas")))
                }
            }

        awaitClose { listener.remove() }
    }

    /**
     * Obtiene una oferta específica por ID
     */
    suspend fun getOfferById(offerId: String): Result<Offer> {
        return try {
            val document = firestore.collection("offers")
                .document(offerId)
                .get()
                .await()

            if (document.exists()) {
                val offer = document.toObject(Offer::class.java)
                if (offer != null) {
                    Result.success(offer.copy(id = document.id))
                } else {
                    Result.failure(Exception("Error al convertir oferta"))
                }
            } else {
                Result.failure(Exception("Oferta no encontrada"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Crea una nueva oferta
     */
    suspend fun createOffer(offer: Offer): Result<String> {
        return try {
            val docRef = firestore.collection("offers")
                .add(offer)
                .await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza una oferta existente
     */
    suspend fun updateOffer(offerId: String, offer: Offer): Result<Unit> {
        return try {
            firestore.collection("offers")
                .document(offerId)
                .set(offer)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina una oferta
     */
    suspend fun deleteOffer(offerId: String): Result<Unit> {
        return try {
            firestore.collection("offers")
                .document(offerId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

