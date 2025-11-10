package com.ddl.unirides.domain.usecase

import com.ddl.unirides.data.repository.OfferRepository
import com.ddl.unirides.data.repository.UserRepository
import com.ddl.unirides.ui.home.OfferWithPublisher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetAllAvailableOffersUseCase @Inject constructor(
    private val offerRepository: OfferRepository,
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<Result<List<OfferWithPublisher>>> {
        // Obtener el ID del usuario actual
        val currentUserId = userRepository.getCurrentUserId() ?: ""

        // Pasar el currentUserId al repositorio para filtrar en la consulta
        return offerRepository.getAllOffersFlow(currentUserId)
            .map { result ->
                result.mapCatching { offers ->
                    // Para cada oferta, obtener la información del publicador
                    offers.map { offer ->
                        val publisherResult = userRepository.getUserProfile(offer.publisherUserId)
                        OfferWithPublisher(
                            offer = offer,
                            publisher = publisherResult.getOrNull()
                        )
                    }
                }
            }
            .catch { e ->
                emit(Result.failure(e))
            }
    }
}

