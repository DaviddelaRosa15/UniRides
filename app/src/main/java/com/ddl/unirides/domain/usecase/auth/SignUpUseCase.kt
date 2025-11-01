package com.ddl.unirides.domain.usecase.auth

import com.ddl.unirides.data.model.User
import com.ddl.unirides.data.repository.AuthRepository
import com.ddl.unirides.domain.Resource
import javax.inject.Inject

/**
 * Caso de uso para registrar nuevos usuarios
 */
class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String, name: String): Resource<User> {
        // Validaciones de negocio
        if (name.isBlank()) {
            return Resource.Error("El nombre es requerido")
        }

        if (name.length < 2) {
            return Resource.Error("El nombre debe tener al menos 2 caracteres")
        }

        if (email.isBlank()) {
            return Resource.Error("El email es requerido")
        }

        if (!email.contains(".edu")) {
            return Resource.Error("Solo se permiten correos universitarios (.edu)")
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Resource.Error("Formato de email inválido")
        }

        if (password.isBlank()) {
            return Resource.Error("La contraseña es requerida")
        }

        if (password.length < 6) {
            return Resource.Error("La contraseña debe tener al menos 6 caracteres")
        }

        // Ejecutar registro
        return authRepository.signUp(email, password, name)
    }
}

