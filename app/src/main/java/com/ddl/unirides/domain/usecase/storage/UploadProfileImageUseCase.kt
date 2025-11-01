package com.ddl.unirides.domain.usecase.storage

import android.net.Uri
import com.ddl.unirides.data.repository.StorageRepository
import com.ddl.unirides.domain.Resource
import javax.inject.Inject

/**
 * Caso de uso para subir imagen de perfil
 */
class UploadProfileImageUseCase @Inject constructor(
    private val storageRepository: StorageRepository
) {
    suspend operator fun invoke(userId: String, imageUri: Uri): Resource<String> {
        // Validar que la URI no esté vacía
        if (imageUri.toString().isEmpty()) {
            return Resource.Error("La imagen no es válida")
        }

        return storageRepository.uploadProfileImage(userId, imageUri)
    }
}

