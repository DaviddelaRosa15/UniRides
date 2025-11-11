package com.ddl.unirides.ui.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ddl.unirides.data.repository.UserRepository
import com.ddl.unirides.domain.usecase.GetAllAvailableOffersUseCase
import com.ddl.unirides.domain.usecase.chat.GetOrCreateChatUseCase
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllAvailableOffersUseCase: GetAllAvailableOffersUseCase,
    private val getOrCreateChatUseCase: GetOrCreateChatUseCase,
    private val userRepository: UserRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadOffers()
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val userId = userRepository.getCurrentUserId()
            if (userId != null) {
                userRepository.getUserProfileFlow(userId).collect { result ->
                    result.onSuccess { user ->
                        _uiState.update { it.copy(currentUser = user) }
                    }
                }
            }
        }
    }

    private fun loadOffers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getAllAvailableOffersUseCase().collect { result ->
                result.fold(
                    onSuccess = { offersWithPublisher ->
                        // Calcular valores máximos dinámicamente
                        val maxPrice = offersWithPublisher
                            .maxOfOrNull { it.offer.price.toFloat() }
                            ?.coerceAtLeast(100f) ?: 100f

                        val maxSeats = offersWithPublisher
                            .maxOfOrNull { it.offer.availableSeats }
                            ?.coerceAtLeast(8) ?: 8

                        _uiState.update { currentState ->
                            // Siempre establecer el priceRange con el máximo real
                            val newPriceRange = 0f..maxPrice

                            currentState.copy(
                                offers = offersWithPublisher,
                                maxPrice = maxPrice,
                                maxSeats = maxSeats,
                                priceRange = newPriceRange,
                                filteredOffers = applyFilters(
                                    offersWithPublisher,
                                    currentState.searchQuery,
                                    currentState.fromDate,
                                    currentState.toDate,
                                    newPriceRange,
                                    currentState.minSeats
                                ),
                                isLoading = false,
                                error = null
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "Error al cargar ofertas"
                            )
                        }
                    }
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        savedStateHandle["searchQuery"] = query
        _uiState.update { currentState ->
            currentState.copy(
                searchQuery = query,
                filteredOffers = applyFilters(
                    currentState.offers,
                    query,
                    currentState.fromDate,
                    currentState.toDate,
                    currentState.priceRange,
                    currentState.minSeats
                )
            )
        }
    }

    fun onDateTimeRangeSelected(fromDate: Timestamp?, toDate: Timestamp?) {
        savedStateHandle["fromDate"] = fromDate
        savedStateHandle["toDate"] = toDate
        _uiState.update { currentState ->
            currentState.copy(
                fromDate = fromDate,
                toDate = toDate,
                filteredOffers = applyFilters(
                    currentState.offers,
                    currentState.searchQuery,
                    fromDate,
                    toDate,
                    currentState.priceRange,
                    currentState.minSeats
                ),
                showDateTimeDialog = false
            )
        }
    }

    fun onFiltersChanged(priceRange: ClosedFloatingPointRange<Float>, minSeats: Int) {
        savedStateHandle["priceRangeStart"] = priceRange.start
        savedStateHandle["priceRangeEnd"] = priceRange.endInclusive
        savedStateHandle["minSeats"] = minSeats
        _uiState.update { currentState ->
            currentState.copy(
                priceRange = priceRange,
                minSeats = minSeats,
                filteredOffers = applyFilters(
                    currentState.offers,
                    currentState.searchQuery,
                    currentState.fromDate,
                    currentState.toDate,
                    priceRange,
                    minSeats
                ),
                showFiltersDialog = false
            )
        }
    }

    fun showDateTimeDialog() {
        _uiState.update { it.copy(showDateTimeDialog = true) }
    }

    fun dismissDateTimeDialog() {
        _uiState.update { it.copy(showDateTimeDialog = false) }
    }

    fun showFiltersDialog() {
        _uiState.update { it.copy(showFiltersDialog = true) }
    }

    fun dismissFiltersDialog() {
        _uiState.update { it.copy(showFiltersDialog = false) }
    }

    fun clearDateTimeFilter() {
        onDateTimeRangeSelected(null, null)
    }

    fun clearAllFilters() {
        _uiState.update { currentState ->
            savedStateHandle["searchQuery"] = ""
            savedStateHandle["fromDate"] = null
            savedStateHandle["toDate"] = null
            savedStateHandle["priceRangeStart"] = 0f
            savedStateHandle["priceRangeEnd"] = currentState.maxPrice
            savedStateHandle["minSeats"] = 1

            currentState.copy(
                searchQuery = "",
                fromDate = null,
                toDate = null,
                priceRange = 0f..currentState.maxPrice,
                minSeats = 1,
                filteredOffers = currentState.offers
            )
        }
    }

    private fun applyFilters(
        offers: List<OfferWithPublisher>,
        searchQuery: String,
        fromDate: Timestamp?,
        toDate: Timestamp?,
        priceRange: ClosedFloatingPointRange<Float>,
        minSeats: Int
    ): List<OfferWithPublisher> {
        return offers.filter { offerWithPublisher ->
            val offer = offerWithPublisher.offer

            // Filtro por búsqueda (origen o destino)
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                offer.origin.contains(searchQuery, ignoreCase = true) ||
                        offer.destination.contains(searchQuery, ignoreCase = true)
            }

            // Filtro por rango de fechas
            val matchesDateRange = if (fromDate != null && toDate != null) {
                val offerDate = offer.dateTime.toDate()
                val from = fromDate.toDate()
                val to = toDate.toDate()
                offerDate.after(from) && offerDate.before(to) ||
                        offerDate == from || offerDate == to
            } else {
                true
            }

            // Filtro por precio
            val matchesPrice =
                offer.price in priceRange.start.toDouble()..priceRange.endInclusive.toDouble()

            // Filtro por asientos disponibles
            val matchesSeats = offer.availableSeats >= minSeats

            matchesSearch && matchesDateRange && matchesPrice && matchesSeats
        }
            .sortedByDescending { it.offer.dateTime.toDate() } // Ordenar por fecha (más recientes primero)
    }

    /**
     * Crea o obtiene un chat existente con el conductor
     * Retorna el chatId si tuvo éxito, null en caso contrario
     */
    suspend fun createOrGetChat(offerId: String, otherUserId: String): String? {
        return try {
            val result = getOrCreateChatUseCase(otherUserId, offerId)
            result.getOrNull()?.id
        } catch (_: Exception) {
            null
        }
    }
}

