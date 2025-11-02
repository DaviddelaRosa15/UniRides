package com.ddl.unirides.ui.verification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ddl.unirides.domain.Resource
import com.ddl.unirides.domain.usecase.auth.CheckEmailVerifiedUseCase
import com.ddl.unirides.domain.usecase.auth.SendEmailVerificationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmailVerificationViewModel @Inject constructor(
    private val checkEmailVerifiedUseCase: CheckEmailVerifiedUseCase,
    private val sendEmailVerificationUseCase: SendEmailVerificationUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(EmailVerificationState())
    val state: StateFlow<EmailVerificationState> = _state.asStateFlow()

    /**
     * Verificar si el correo ya está verificado
     */
    fun checkVerification(onVerified: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isCheckingVerification = true, error = null) }

            when (val result = checkEmailVerifiedUseCase()) {
                is Resource.Success -> {
                    if (result.data == true) {
                        _state.update {
                            it.copy(
                                isCheckingVerification = false,
                                isVerified = true,
                                successMessage = "¡Correo verificado exitosamente!"
                            )
                        }
                        onVerified()
                    } else {
                        _state.update {
                            it.copy(
                                isCheckingVerification = false,
                                error = "El correo aún no ha sido verificado."
                            )
                        }
                    }
                }

                is Resource.Error -> {
                    _state.update {
                        it.copy(
                            isCheckingVerification = false,
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

    /**
     * Reenviar correo de verificación
     */
    fun resendVerificationEmail() {
        viewModelScope.launch {
            _state.update { it.copy(isSendingEmail = true, error = null, successMessage = null) }

            when (val result = sendEmailVerificationUseCase()) {
                is Resource.Success -> {
                    _state.update {
                        it.copy(
                            isSendingEmail = false,
                            successMessage = "¡Correo de verificación enviado!"
                        )
                    }
                }

                is Resource.Error -> {
                    _state.update {
                        it.copy(
                            isSendingEmail = false,
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

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun clearSuccessMessage() {
        _state.update { it.copy(successMessage = null) }
    }
}
