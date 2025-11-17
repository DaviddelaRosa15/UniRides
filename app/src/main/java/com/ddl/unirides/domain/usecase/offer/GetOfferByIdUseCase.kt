package com.ddl.unirides.domain.usecase.offer

import com.ddl.unirides.data.model.Offer
import com.ddl.unirides.data.repository.OfferRepository
import javax.inject.Inject

class GetOfferByIdUseCase @Inject constructor(
    private val offerRepository: OfferRepository
) {
    suspend operator fun invoke(offerId: String): Result<Offer> {
        return offerRepository.getOfferById(offerId)
    }
}

