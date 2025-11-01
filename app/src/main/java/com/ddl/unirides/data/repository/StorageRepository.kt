package com.ddl.unirides.data.repository

import android.net.Uri
import com.ddl.unirides.domain.Resource
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepository @Inject constructor(
    private val storage: FirebaseStorage
) {
    /**
     * Sube una imagen de perfil a Firebase Storage
     * @param userId ID del usuario
     * @param imageUri URI de la imagen a subir
     * @return URL de descarga de la imagen o error
     */
    suspend fun uploadProfileImage(userId: String, imageUri: Uri): Resource<String> {
        return try {
            // El nombre del archivo debe coincidir con {userId} en las reglas de Storage
            // Las reglas esperan: /profile_images/{userId}
            // Entonces el archivo debe nombrarse exactamente como el userId
            val storageRef = storage.reference
                .child("profile_images")
                .child(userId)  // Cambiar de "profile_$userId.jpg" a solo userId

            // Subir la imagen
            storageRef.putFile(imageUri).await()

            // Obtener la URL de descarga
            val downloadUrl = storageRef.downloadUrl.await()
            Resource.Success(downloadUrl.toString())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al subir la imagen")
        }
    }

    /**
     * Elimina una imagen de perfil de Firebase Storage
     * @param imageUrl URL de la imagen a eliminar
     */
    suspend fun deleteProfileImage(imageUrl: String): Resource<Unit> {
        return try {
            val storageRef = storage.getReferenceFromUrl(imageUrl)
            storageRef.delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al eliminar la imagen")
        }
    }

    /**
     * Sube una imagen genérica a Firebase Storage
     * @param path Ruta donde se guardará la imagen
     * @param imageUri URI de la imagen
     * @return URL de descarga
     */
    suspend fun uploadImage(path: String, imageUri: Uri): Resource<String> {
        return try {
            val fileName = "${UUID.randomUUID()}.jpg"
            val storageRef = storage.reference
                .child(path)
                .child(fileName)

            storageRef.putFile(imageUri).await()
            val downloadUrl = storageRef.downloadUrl.await()
            Resource.Success(downloadUrl.toString())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al subir la imagen")
        }
    }
}