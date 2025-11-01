package com.ddl.unirides.ui.signup

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ddl.unirides.domain.Resource
import com.ddl.unirides.domain.usecase.auth.SignUpUseCase
import com.ddl.unirides.util.ImageCompressor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    application: Application,
    private val signUpUseCase: SignUpUseCase
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(SignUpState())
    val state: StateFlow<SignUpState> = _state.asStateFlow()

    fun onNameChange(name: String) {
        _state.update { it.copy(name = name, nameError = null) }
    }

    fun onEmailChange(email: String) {
        _state.update { it.copy(email = email, emailError = null) }
    }

    fun onPasswordChange(password: String) {
        _state.update { it.copy(password = password, passwordError = null) }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _state.update { it.copy(confirmPassword = confirmPassword, confirmPasswordError = null) }
    }

    fun onProfileImageSelected(uri: Uri?) {
        _state.update { it.copy(profileImageUri = uri) }
    }

    fun removeProfileImage() {
        _state.update { it.copy(profileImageUri = null) }
    }

    fun togglePasswordVisibility() {
        _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun toggleConfirmPasswordVisibility() {
        _state.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    fun signUp(onSuccess: () -> Unit) {
        val currentState = _state.value

        // Validaciones básicas de UI
        if (!validateInputs(
                currentState.name,
                currentState.email,
                currentState.password,
                currentState.confirmPassword
            )
        ) {
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            // Comprimir la imagen si existe
            val imageUri = currentState.profileImageUri?.let { uri ->
                _state.update { it.copy(isUploadingImage = true) }
                ImageCompressor.compressImage(
                    context = getApplication(),
                    imageUri = uri,
                    maxWidth = 800,
                    maxHeight = 800,
                    quality = 85
                ) ?: uri // Si falla la compresión, usar la original
            }

            _state.update { it.copy(isUploadingImage = false) }

            when (val result = signUpUseCase(
                email = currentState.email,
                password = currentState.password,
                name = currentState.name,
                profileImageUri = imageUri
            )) {
                is Resource.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    onSuccess()
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

    private fun validateInputs(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        var isValid = true

        // Validar nombre
        if (name.isBlank()) {
            _state.update { it.copy(nameError = "Name is required") }
            isValid = false
        } else if (name.length < 2) {
            _state.update { it.copy(nameError = "Name must be at least 2 characters") }
            isValid = false
        }

        // Validar email
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

        // Validar contraseña
        if (password.isBlank()) {
            _state.update { it.copy(passwordError = "Password is required") }
            isValid = false
        } else if (password.length < 6) {
            _state.update { it.copy(passwordError = "Password must be at least 6 characters") }
            isValid = false
        }

        // Validar confirmación de contraseña
        if (confirmPassword.isBlank()) {
            _state.update { it.copy(confirmPasswordError = "Please confirm your password") }
            isValid = false
        } else if (password != confirmPassword) {
            _state.update { it.copy(confirmPasswordError = "Passwords do not match") }
            isValid = false
        }

        return isValid
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
