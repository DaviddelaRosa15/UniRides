package com.ddl.unirides.ui.offer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ddl.unirides.data.model.Offer
import com.ddl.unirides.domain.usecase.offer.CreateOfferUseCase
import com.ddl.unirides.domain.usecase.user.GetCurrentUserIdUseCase
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject


@HiltViewModel
class OfferRideViewModel @Inject constructor(
    private val createOfferUseCase: CreateOfferUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OfferRideUiState())
    val uiState: StateFlow<OfferRideUiState> = _uiState.asStateFlow()

    fun onOriginChange(origin: String) {
        _uiState.update { it.copy(origin = origin, originError = null) }
    }

    fun onDestinationChange(destination: String) {
        _uiState.update { it.copy(destination = destination, destinationError = null) }
    }

    fun onDateChange(date: String) {
        _uiState.update { it.copy(date = date, dateError = null) }
    }

    fun onTimeChange(time: String) {
        _uiState.update { it.copy(time = time, timeError = null) }
    }

    fun onSeatsChange(seats: Int) {
        _uiState.update { it.copy(availableSeats = seats) }
    }

    fun onPriceChange(price: String) {
        // Solo permitir números y punto decimal
        val filteredPrice = price.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(price = filteredPrice, priceError = null) }
    }

    fun onNotesChange(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun publishOffer() {
        if (!validateForm()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val currentUserId = getCurrentUserIdUseCase() ?: run {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Usuario no autenticado"
                        )
                    }
                    return@launch
                }

                // Convertir fecha y hora a Timestamp combinado
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val dateTimeString = "${_uiState.value.date} ${_uiState.value.time}"
                val dateTime = dateFormat.parse(dateTimeString)
                val timestamp = if (dateTime != null) {
                    Timestamp(dateTime)
                } else {
                    Timestamp.now()
                }

                val offer = Offer(
                    publisherUserId = currentUserId,
                    origin = _uiState.value.origin.trim(),
                    destination = _uiState.value.destination.trim(),
                    dateTime = timestamp,
                    availableSeats = _uiState.value.availableSeats,
                    price = _uiState.value.price.toDoubleOrNull() ?: 0.0,
                    details = _uiState.value.notes.trim().ifEmpty { null }
                )

                val result = createOfferUseCase(offer)

                result.onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true
                        )
                    }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Error al publicar el viaje"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error inesperado"
                    )
                }
            }
        }
    }

    private fun validateForm(): Boolean {
        val state = _uiState.value
        var isValid = true

        if (state.origin.isBlank()) {
            _uiState.update { it.copy(originError = "El origen es requerido") }
            isValid = false
        }

        if (state.destination.isBlank()) {
            _uiState.update { it.copy(destinationError = "El destino es requerido") }
            isValid = false
        }

        if (state.date.isBlank()) {
            _uiState.update { it.copy(dateError = "La fecha es requerida") }
            isValid = false
        }

        if (state.time.isBlank()) {
            _uiState.update { it.copy(timeError = "La hora es requerida") }
            isValid = false
        }

        if (state.price.isBlank()) {
            _uiState.update { it.copy(priceError = "El precio es requerido") }
            isValid = false
        } else {
            val priceValue = state.price.toDoubleOrNull()
            if (priceValue == null || priceValue <= 0) {
                _uiState.update { it.copy(priceError = "El precio debe ser mayor a 0") }
                isValid = false
            }
        }

        return isValid
    }

    fun clearSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

