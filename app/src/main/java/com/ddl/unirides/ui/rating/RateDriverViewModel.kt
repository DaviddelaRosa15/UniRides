package com.ddl.unirides.ui.rating

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ddl.unirides.domain.usecase.rating.GetVerifiedUsersWithRatingsUseCase
import com.ddl.unirides.domain.usecase.rating.RateUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RateDriverViewModel @Inject constructor(
    private val getVerifiedUsersWithRatingsUseCase: GetVerifiedUsersWithRatingsUseCase,
    private val rateUserUseCase: RateUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RateDriverUiState())
    val uiState: StateFlow<RateDriverUiState> = _uiState.asStateFlow()

    init {
        loadDrivers()
    }

    private fun loadDrivers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getVerifiedUsersWithRatingsUseCase().collect { result ->
                result.fold(
                    onSuccess = { usersWithRatings ->
                        val driversToRate = usersWithRatings.map { userWithRating ->
                            DriverToRate(
                                driver = userWithRating.user,
                                currentRating = userWithRating.currentRating,
                                hasRated = userWithRating.hasRated
                            )
                        }

                        _uiState.update {
                            it.copy(
                                drivers = driversToRate,
                                filteredDrivers = driversToRate,
                                isLoading = false,
                                error = null
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "Error al cargar conductores"
                            )
                        }
                    }
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { currentState ->
            val filtered = if (query.isBlank()) {
                currentState.drivers
            } else {
                currentState.drivers.filter { driverToRate ->
                    driverToRate.driver.name.contains(query, ignoreCase = true)
                }
            }

            currentState.copy(
                searchQuery = query,
                filteredDrivers = filtered
            )
        }
    }

    fun onRatingChanged(driverId: String, rating: Int) {
        viewModelScope.launch {
            // Llamar al UseCase para crear/actualizar la calificación
            val result = rateUserUseCase(driverId, rating)

            result.fold(
                onSuccess = {
                    // Actualizar el estado local
                    _uiState.update { currentState ->
                        val updatedDrivers = currentState.drivers.map { driver ->
                            if (driver.driver.id == driverId) {
                                driver.copy(
                                    currentRating = rating,
                                    hasRated = true
                                )
                            } else {
                                driver
                            }
                        }

                        val updatedFiltered = currentState.filteredDrivers.map { driver ->
                            if (driver.driver.id == driverId) {
                                driver.copy(
                                    currentRating = rating,
                                    hasRated = true
                                )
                            } else {
                                driver
                            }
                        }

                        currentState.copy(
                            drivers = updatedDrivers,
                            filteredDrivers = updatedFiltered
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(error = error.message ?: "Error al calificar usuario")
                    }
                }
            )
        }
    }

    fun getUniversity(email: String): String {
        return when {
            email.endsWith("@pucmm.edu.do", ignoreCase = true) -> "PUCMM"
            email.endsWith("@intec.edu.do", ignoreCase = true) -> "INTEC"
            email.endsWith("@unphu.edu.do", ignoreCase = true) -> "UNPHU"
            email.endsWith("@uasd.edu.do", ignoreCase = true) -> "UASD"
            email.endsWith("@unibe.edu.do", ignoreCase = true) -> "UNIBE"
            email.endsWith("@ucsd.edu.do", ignoreCase = true) -> "UCSD"
            email.endsWith("@utesa.edu", ignoreCase = true) -> "UTESA"
            email.endsWith("@ufhec.edu.do", ignoreCase = true) -> "UFHEC"
            email.endsWith("@ucne.edu", ignoreCase = true) -> "UCNE"
            email.endsWith("@unicda.edu.do", ignoreCase = true) -> "UNICDA"
            email.endsWith("@itla.edu.do", ignoreCase = true) -> "ITLA"
            email.endsWith("@o-m.edu.do", ignoreCase = true) -> "O&M"
            else -> "Universidad"
        }
    }
}

