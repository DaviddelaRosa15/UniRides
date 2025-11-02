package com.ddl.unirides.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ddl.unirides.data.model.User
import com.ddl.unirides.domain.Resource
import com.ddl.unirides.domain.usecase.auth.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkAuthenticationStatus()
    }

    private fun checkAuthenticationStatus() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val user = resource.data
                        _authState.value = when {
                            user == null -> AuthState.NotAuthenticated
                            !user.verified -> AuthState.NotVerified
                            else -> AuthState.Authenticated(user)
                        }
                    }

                    is Resource.Error -> {
                        _authState.value = AuthState.NotAuthenticated
                    }

                    is Resource.Loading -> {
                        _authState.value = AuthState.Loading
                    }
                }
            }
        }
    }
}

sealed class AuthState {
    object Loading : AuthState()
    object NotAuthenticated : AuthState()
    object NotVerified : AuthState()
    data class Authenticated(val user: User) : AuthState()
}

