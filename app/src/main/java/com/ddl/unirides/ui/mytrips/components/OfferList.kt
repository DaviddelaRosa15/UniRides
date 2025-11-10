package com.ddl.unirides.ui.mytrips.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ddl.unirides.data.model.Offer

@Composable
fun OfferList(
    offers: List<Offer>,
    onOfferClick: (Offer) -> Unit,
    onPublishClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Lista de ofertas
        items(offers) { offer ->
            OfferCard(
                offer = offer,
                onClick = { onOfferClick(offer) }
            )
        }

        // Card CTA para publicar
        item {
            Spacer(modifier = Modifier.height(8.dp))
            PublishOfferCard(onPublishClick = onPublishClick)
        }
    }
}


