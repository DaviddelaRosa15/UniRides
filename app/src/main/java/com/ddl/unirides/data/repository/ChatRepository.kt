package com.ddl.unirides.data.repository

import com.ddl.unirides.data.model.Chat
import com.ddl.unirides.data.model.Message
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    companion object {
        private const val CHATS_COLLECTION = "chats"
        private const val MESSAGES_COLLECTION = "messages"
    }

    /**
     * Obtiene todos los chats del usuario actual en tiempo real
     */
    fun getUserChatsFlow(currentUserId: String): Flow<Result<List<Chat>>> = callbackFlow {
        val listener = firestore.collection(CHATS_COLLECTION)
            .whereEqualTo("user1Id", currentUserId)
            .addSnapshotListener { snapshot1, error1 ->
                if (error1 != null) {
                    trySend(Result.failure(error1))
                    return@addSnapshotListener
                }

                // También buscar donde el usuario es user2
                firestore.collection(CHATS_COLLECTION)
                    .whereEqualTo("user2Id", currentUserId)
                    .addSnapshotListener { snapshot2, error2 ->
                        if (error2 != null) {
                            trySend(Result.failure(error2))
                            return@addSnapshotListener
                        }

                        val chats1 = snapshot1?.documents?.mapNotNull {
                            it.toObject(Chat::class.java)?.copy(id = it.id)
                        } ?: emptyList()
                        val chats2 = snapshot2?.documents?.mapNotNull {
                            it.toObject(Chat::class.java)?.copy(id = it.id)
                        } ?: emptyList()

                        val allChats =
                            (chats1 + chats2).sortedByDescending { it.lastMessageTimestamp }
                        trySend(Result.success(allChats))
                    }
            }

        awaitClose { listener.remove() }
    }

    /**
     * Obtiene un chat específico por ID
     */
    suspend fun getChatById(chatId: String): Result<Chat?> {
        return try {
            val document = firestore.collection(CHATS_COLLECTION)
                .document(chatId)
                .get()
                .await()

            val chat = document.toObject(Chat::class.java)?.copy(id = document.id)
            Result.success(chat)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene o crea un chat entre dos usuarios para una oferta específica
     */
    suspend fun getOrCreateChat(
        currentUserId: String,
        otherUserId: String,
        offerId: String
    ): Result<Chat> {
        return try {
            // Buscar chats donde el usuario actual es user1Id y el otro es user2Id
            val chats1 = firestore.collection(CHATS_COLLECTION)
                .whereEqualTo("user1Id", currentUserId)
                .whereEqualTo("user2Id", otherUserId)
                .whereEqualTo("offerId", offerId)
                .get()
                .await()

            // Buscar chats donde el usuario actual es user2Id y el otro es user1Id
            val chats2 = firestore.collection(CHATS_COLLECTION)
                .whereEqualTo("user1Id", otherUserId)
                .whereEqualTo("user2Id", currentUserId)
                .whereEqualTo("offerId", offerId)
                .get()
                .await()

            // Combinar resultados
            val allChats = chats1.documents + chats2.documents
            val existingChat = allChats
                .mapNotNull { it.toObject(Chat::class.java)?.copy(id = it.id) }
                .firstOrNull()

            if (existingChat != null) {
                Result.success(existingChat)
            } else {
                // Crear nuevo chat
                val newChat = Chat(
                    user1Id = currentUserId,
                    user2Id = otherUserId,
                    offerId = offerId,
                    lastMessageTimestamp = Timestamp.now()
                )

                val documentRef = firestore.collection(CHATS_COLLECTION)
                    .add(newChat)
                    .await()

                // Actualizar el documento con su propio ID
                val chatId = documentRef.id
                documentRef.update("id", chatId).await()

                Result.success(newChat.copy(id = documentRef.id))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene los mensajes de un chat en tiempo real
     */
    fun getChatMessagesFlow(chatId: String): Flow<Result<List<Message>>> = callbackFlow {
        val listener = firestore.collection(CHATS_COLLECTION)
            .document(chatId)
            .collection(MESSAGES_COLLECTION)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull {
                    it.toObject(Message::class.java)?.copy(id = it.id)
                } ?: emptyList()

                trySend(Result.success(messages))
            }

        awaitClose { listener.remove() }
    }

    /**
     * Envía un mensaje a un chat
     */
    suspend fun sendMessage(chatId: String, senderId: String, content: String): Result<Message> {
        return try {
            val message = Message(
                senderId = senderId,
                content = content,
                timestamp = Timestamp.now(),
                isRead = false
            )

            // Agregar mensaje a la subcolección
            val documentRef = firestore.collection(CHATS_COLLECTION)
                .document(chatId)
                .collection(MESSAGES_COLLECTION)
                .add(message)
                .await()

            // Actualizar el timestamp del último mensaje en el chat
            firestore.collection(CHATS_COLLECTION)
                .document(chatId)
                .update("lastMessageTimestamp", message.timestamp)
                .await()

            Result.success(message.copy(id = documentRef.id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Marca los mensajes de un chat como leídos
     */
    suspend fun markMessagesAsRead(chatId: String, currentUserId: String): Result<Unit> {
        return try {
            // Obtener mensajes no leídos que no fueron enviados por el usuario actual
            val unreadMessages = firestore.collection(CHATS_COLLECTION)
                .document(chatId)
                .collection(MESSAGES_COLLECTION)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            // Actualizar cada mensaje
            val batch = firestore.batch()
            unreadMessages.documents
                .filter {
                    val message = it.toObject(Message::class.java)
                    message?.senderId != currentUserId
                }
                .forEach { document ->
                    batch.update(document.reference, "isRead", true)
                }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene el conteo de mensajes no leídos en un chat para el usuario actual
     */
    suspend fun getUnreadMessageCount(chatId: String, currentUserId: String): Result<Int> {
        return try {
            val unreadMessages = firestore.collection(CHATS_COLLECTION)
                .document(chatId)
                .collection(MESSAGES_COLLECTION)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            // Contar solo los mensajes que no fueron enviados por el usuario actual
            val count = unreadMessages.documents
                .mapNotNull { it.toObject(Message::class.java) }
                .count { it.senderId != currentUserId }

            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene el último mensaje de un chat
     */
    suspend fun getLastMessage(chatId: String): Result<Message?> {
        return try {
            val lastMessage = firestore.collection(CHATS_COLLECTION)
                .document(chatId)
                .collection(MESSAGES_COLLECTION)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            val message = lastMessage.documents.firstOrNull()?.toObject(Message::class.java)
                ?.copy(id = lastMessage.documents.firstOrNull()?.id ?: "")

            Result.success(message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina un chat y todos sus mensajes
     */
    suspend fun deleteChat(chatId: String): Result<Unit> {
        return try {
            // Primero eliminar todos los mensajes
            val messages = firestore.collection(CHATS_COLLECTION)
                .document(chatId)
                .collection(MESSAGES_COLLECTION)
                .get()
                .await()

            val batch = firestore.batch()
            messages.documents.forEach { document ->
                batch.delete(document.reference)
            }

            // Eliminar el chat
            batch.delete(firestore.collection(CHATS_COLLECTION).document(chatId))
            batch.commit().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}