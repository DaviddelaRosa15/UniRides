package com.ddl.unirides.domain.usecase.offer

import com.ddl.unirides.data.model.Offer
import com.ddl.unirides.data.repository.OfferRepository
import javax.inject.Inject

class CreateOfferUseCase @Inject constructor(
    private val offerRepository: OfferRepository
) {
    suspend operator fun invoke(offer: Offer): Result<String> {
        return offerRepository.createOffer(offer)
    }
}

