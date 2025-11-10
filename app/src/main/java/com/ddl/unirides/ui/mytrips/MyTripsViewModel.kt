package com.ddl.unirides.ui.mytrips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ddl.unirides.domain.usecase.offer.GetPublishedOffersUseCase
import com.ddl.unirides.domain.usecase.user.GetCurrentUserIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyTripsViewModel @Inject constructor(
    private val getPublishedOffersUseCase: GetPublishedOffersUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyTripsState())
    val uiState: StateFlow<MyTripsState> = _uiState.asStateFlow()

    init {
        loadPublishedOffers()
    }

    private fun loadPublishedOffers() {
        val userId = getCurrentUserIdUseCase()
        if (userId == null) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Usuario no autenticado"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            getPublishedOffersUseCase(userId).collect { result ->
                result.fold(
                    onSuccess = { offers ->
                        _uiState.value = _uiState.value.copy(
                            offers = offers,
                            isLoading = false,
                            error = null
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message ?: "Error al cargar ofertas"
                        )
                    }
                )
            }
        }
    }

    fun refresh() {
        loadPublishedOffers()
    }
}

