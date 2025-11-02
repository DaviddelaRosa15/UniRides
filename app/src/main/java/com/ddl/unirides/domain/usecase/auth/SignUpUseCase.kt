package com.ddl.unirides.domain.usecase.auth

import android.net.Uri
import com.ddl.unirides.data.model.User
import com.ddl.unirides.data.repository.AuthRepository
import com.ddl.unirides.domain.Resource
import com.ddl.unirides.domain.usecase.storage.UploadProfileImageUseCase
import javax.inject.Inject

/**
 * Caso de uso para registrar nuevos usuarios
 */
class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val uploadProfileImageUseCase: UploadProfileImageUseCase
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        name: String,
        profileImageUri: Uri? = null
    ): Resource<User> {
        // Validaciones de negocio
        if (name.isBlank()) {
            return Resource.Error("El nombre es requerido")
        }

        if (name.length < 2) {
            return Resource.Error("El nombre debe tener al menos 2 caracteres")
        }

        if (email.isBlank()) {
            return Resource.Error("El correo es requerido")
        }

        if (!email.contains(".edu")) {
            return Resource.Error("Solo se permiten correos universitarios (.edu)")
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Resource.Error("Formato de correo inválido")
        }

        if (password.isBlank()) {
            return Resource.Error("La contraseña es requerida")
        }

        if (password.length < 6) {
            return Resource.Error("La contraseña debe tener al menos 6 caracteres")
        }

        // Primero crear el usuario en Auth para obtener el UID
        val signUpResult = authRepository.signUp(email, password, name, null)

        if (signUpResult is Resource.Error) {
            return signUpResult
        }

        val user = (signUpResult as Resource.Success).data

        // Si hay imagen, subirla y actualizar el perfil
        if (profileImageUri != null) {
            when (val uploadResult = uploadProfileImageUseCase(user.id, profileImageUri)) {
                is Resource.Success -> {
                    val photoUrl = uploadResult.data
                    // Actualizar el perfil con la URL de la foto
                    authRepository.updateUserProfile(
                        user.id,
                        mapOf("profilePictureUrl" to photoUrl)
                    )
                    return Resource.Success(user.copy(profilePictureUrl = photoUrl))
                }

                is Resource.Error -> {
                    // Aunque falle la subida de imagen, el usuario ya fue creado
                    // Retornamos éxito pero sin foto
                    return Resource.Success(user)
                }

                is Resource.Loading -> {
                    // No debería llegar aquí
                }
            }
        }

        return Resource.Success(user)
    }
}
