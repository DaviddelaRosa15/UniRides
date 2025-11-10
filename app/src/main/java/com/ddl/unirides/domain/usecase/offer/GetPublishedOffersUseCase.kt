package com.ddl.unirides.domain.usecase.offer

import com.ddl.unirides.data.model.Offer
import com.ddl.unirides.data.repository.OfferRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPublishedOffersUseCase @Inject constructor(
    private val offerRepository: OfferRepository
) {
    operator fun invoke(userId: String): Flow<Result<List<Offer>>> {
        return offerRepository.getPublishedOffersFlow(userId)
    }
}

