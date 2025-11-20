package com.ddl.unirides.ui.tripdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ddl.unirides.domain.usecase.chat.GetOrCreateChatUseCase
import com.ddl.unirides.domain.usecase.offer.GetTripDetailUseCase
import com.ddl.unirides.domain.usecase.user.GetCurrentUserIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TripDetailViewModel @Inject constructor(
    private val getTripDetailUseCase: GetTripDetailUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val getOrCreateChatUseCase: GetOrCreateChatUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(TripDetailUiState())
    val uiState: StateFlow<TripDetailUiState> = _uiState.asStateFlow()

    private val offerId: String = checkNotNull(savedStateHandle["offerId"])

    init {
        loadTripDetail()
    }

    private fun loadTripDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Obtener el ID del usuario actual
                val currentUserId = getCurrentUserIdUseCase()

                if (currentUserId == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Error al obtener usuario actual"
                        )
                    }
                    return@launch
                }

                // Obtener los detalles del viaje
                val result = getTripDetailUseCase(offerId)

                if (result.isSuccess) {
                    val tripDetail = result.getOrNull()
                    if (tripDetail != null) {
                        val isTripPassed =
                            tripDetail.offer.dateTime.toDate().before(java.util.Date())
                        val isCurrentUserTheDriver =
                            currentUserId == tripDetail.offer.publisherUserId

                        _uiState.update {
                            it.copy(
                                offer = tripDetail.offer,
                                driver = tripDetail.driver,
                                currentUserId = currentUserId,
                                isLoading = false,
                                error = null,
                                isTripPassed = isTripPassed,
                                isCurrentUserTheDriver = isCurrentUserTheDriver
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "No se encontró el viaje"
                            )
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.exceptionOrNull()?.message ?: "Error al cargar el viaje"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error desconocido"
                    )
                }
            }
        }
    }

    fun retry() {
        loadTripDetail()
    }

    /**
     * Crea o recupera un chat con el conductor del viaje
     */
    suspend fun createOrGetChat(offerId: String, driverId: String): String? {
        return try {
            val result = getOrCreateChatUseCase(driverId, offerId)
            result.getOrNull()?.id
        } catch (_: Exception) {
            null
        }
    }
}

