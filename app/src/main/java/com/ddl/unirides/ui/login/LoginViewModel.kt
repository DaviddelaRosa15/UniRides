package com.ddl.unirides.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ddl.unirides.domain.Resource
import com.ddl.unirides.domain.usecase.auth.SendEmailVerificationUseCase
import com.ddl.unirides.domain.usecase.auth.SignInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val sendEmailVerificationUseCase: SendEmailVerificationUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun onEmailChange(email: String) {
        _state.update { it.copy(email = email, emailError = null) }
    }

    fun onPasswordChange(password: String) {
        _state.update { it.copy(password = password, passwordError = null) }
    }

    fun togglePasswordVisibility() {
        _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun login(onSuccess: () -> Unit, onNotVerified: () -> Unit) {
        val currentState = _state.value

        // Validaciones básicas de UI
        if (!validateInputs(currentState.email, currentState.password)) {
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            // Usar el Use Case en lugar del repositorio directamente
            when (val result = signInUseCase(currentState.email, currentState.password)) {
                is Resource.Success -> {
                    val user = result.data

                    // Verificar si el correo está verificado
                    if (user != null && user.verified) {
                        _state.update { it.copy(isLoading = false) }
                        onSuccess()
                    } else {
                        // Usuario no verificado, enviar correo de verificación
                        sendEmailVerificationUseCase()
                        _state.update { it.copy(isLoading = false) }
                        onNotVerified()
                    }
                }

                is Resource.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }

                is Resource.Loading -> {
                    // Ya está en loading
                }
            }
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true

        if (email.isBlank()) {
            _state.update { it.copy(emailError = "Email is required") }
            isValid = false
        } else if (!email.contains(".edu")) {
            _state.update { it.copy(emailError = "Only .edu emails are allowed") }
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _state.update { it.copy(emailError = "Invalid email format") }
            isValid = false
        }

        if (password.isBlank()) {
            _state.update { it.copy(passwordError = "Password is required") }
            isValid = false
        } else if (password.length < 6) {
            _state.update { it.copy(passwordError = "Password must be at least 6 characters") }
            isValid = false
        }

        return isValid
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
