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
class ProtectedRouteViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _userState = MutableStateFlow<Resource<User?>>(Resource.Loading)
    val userState: StateFlow<Resource<User?>> = _userState.asStateFlow()

    fun checkAuthentication() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { resource ->
                _userState.value = resource
            }
        }
    }
}

