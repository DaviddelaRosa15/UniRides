package com.ddl.unirides.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ddl.unirides.data.model.Rating
import com.ddl.unirides.data.repository.UserRepository
import com.ddl.unirides.domain.usecase.auth.SignOutUseCase
import com.ddl.unirides.domain.usecase.user.GetUserProfileUseCase
import com.ddl.unirides.domain.usecase.user.GetUserRatingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getUserRatingsUseCase: GetUserRatingsUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileState())
    val uiState: StateFlow<ProfileState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val userId = userRepository.getCurrentUserId()
        if (userId == null) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Usuario no autenticado"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Observar perfil del usuario
            launch {
                getUserProfileUseCase(userId).collect { result ->
                    result.fold(
                        onSuccess = { user ->
                            _uiState.value = _uiState.value.copy(
                                user = user,
                                isLoading = false,
                                error = null
                            )
                        },
                        onFailure = { exception ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = exception.message ?: "Error al cargar perfil"
                            )
                        }
                    )
                }
            }

            // Observar ratings del usuario
            launch {
                getUserRatingsUseCase(userId).collect { result ->
                    result.fold(
                        onSuccess = { ratings ->
                            val stats = calculateRatingStats(ratings)
                            _uiState.value = _uiState.value.copy(
                                ratings = ratings,
                                ratingStats = stats
                            )
                        },
                        onFailure = { exception ->
                            // No bloqueamos la UI si fallan los ratings
                            _uiState.value = _uiState.value.copy(
                                ratings = emptyList(),
                                ratingStats = RatingStats()
                            )
                        }
                    )
                }
            }
        }
    }

    private fun calculateRatingStats(ratings: List<Rating>): RatingStats {
        if (ratings.isEmpty()) {
            return RatingStats()
        }

        val total = ratings.size
        val scoreCount = ratings.groupBy { it.score }
            .mapValues { it.value.size }

        return RatingStats(
            totalRatings = total,
            averageRating = ratings.map { it.score }.average().toFloat(),
            ratingDistribution = mapOf(
                5 to (scoreCount[5] ?: 0),
                4 to (scoreCount[4] ?: 0),
                3 to (scoreCount[3] ?: 0),
                2 to (scoreCount[2] ?: 0),
                1 to (scoreCount[1] ?: 0)
            ),
            percentageDistribution = mapOf(
                5 to ((scoreCount[5] ?: 0) * 100f / total),
                4 to ((scoreCount[4] ?: 0) * 100f / total),
                3 to ((scoreCount[3] ?: 0) * 100f / total),
                2 to ((scoreCount[2] ?: 0) * 100f / total),
                1 to ((scoreCount[1] ?: 0) * 100f / total)
            )
        )
    }

    fun signOut() {
        viewModelScope.launch {
            signOutUseCase()
            _uiState.value = ProfileState()
        }
    }

    fun getUniversity(email: String): String {
        return try {
            val domain = email.substringAfter("@").substringBefore(".edu")
            domain.split(".").joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercase() }
            }
        } catch (_: Exception) {
            "Universidad"
        }
    }
}
