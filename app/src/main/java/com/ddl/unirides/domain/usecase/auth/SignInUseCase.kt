package com.ddl.unirides.domain.usecase.auth

import com.ddl.unirides.data.model.User
import com.ddl.unirides.data.repository.AuthRepository
import com.ddl.unirides.domain.Resource
import javax.inject.Inject

/**
 * Caso de uso para iniciar sesión
 */
class SignInUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Resource<User> {
        // Validaciones de negocio
        if (email.isBlank()) {
            return Resource.Error("El email es requerido")
        }

        if (password.isBlank()) {
            return Resource.Error("La contraseña es requerida")
        }

        if (!email.contains(".edu")) {
            return Resource.Error("Solo se permiten correos universitarios (.edu)")
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Resource.Error("Formato de email inválido")
        }

        if (password.length < 6) {
            return Resource.Error("La contraseña debe tener al menos 6 caracteres")
        }

        // Ejecutar login
        return authRepository.signIn(email, password)
    }
}

