package com.ddl.unirides.domain.usecase.offer

import com.ddl.unirides.data.model.Offer
import com.ddl.unirides.data.model.User
import com.ddl.unirides.data.repository.OfferRepository
import com.ddl.unirides.data.repository.UserRepository
import javax.inject.Inject

data class TripDetailData(
    val offer: Offer,
    val driver: User
)

class GetTripDetailUseCase @Inject constructor(
    private val offerRepository: OfferRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(offerId: String): Result<TripDetailData> {
        return try {
            // Obtener la oferta
            val offerResult = offerRepository.getOfferById(offerId)

            if (offerResult.isFailure) {
                return Result.failure(
                    offerResult.exceptionOrNull() ?: Exception("Error al obtener la oferta")
                )
            }

            val offer =
                offerResult.getOrNull() ?: return Result.failure(Exception("Oferta no encontrada"))

            // Obtener los datos del conductor
            val driverResult = userRepository.getUserProfile(offer.publisherUserId)

            if (driverResult.isFailure) {
                return Result.failure(
                    driverResult.exceptionOrNull()
                        ?: Exception("Error al obtener datos del conductor")
                )
            }

            val driver = driverResult.getOrNull()
                ?: return Result.failure(Exception("Conductor no encontrado"))

            Result.success(TripDetailData(offer = offer, driver = driver))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

