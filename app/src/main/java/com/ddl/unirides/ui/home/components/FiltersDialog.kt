package com.ddl.unirides.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlin.math.roundToInt

@Composable
fun FiltersDialog(
    currentPriceRange: ClosedFloatingPointRange<Float>,
    currentMinSeats: Int,
    maxPrice: Float,
    maxSeats: Int,
    onDismiss: () -> Unit,
    onApply: (priceRange: ClosedFloatingPointRange<Float>, minSeats: Int) -> Unit,
    onClearAll: () -> Unit
) {
    var priceRange by remember {
        mutableStateOf(currentPriceRange.start..currentPriceRange.endInclusive)
    }
    var minSeats by remember { mutableIntStateOf(currentMinSeats) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Filtros",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Filtro de precio
                Text(
                    text = "Rango de precio",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "$${priceRange.start.roundToInt()} - $${priceRange.endInclusive.roundToInt()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                RangeSlider(
                    value = priceRange,
                    onValueChange = { range ->
                        priceRange = range
                    },
                    valueRange = 0f..maxPrice,
                    steps = (maxPrice / 5).roundToInt().coerceAtLeast(1) - 1
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Filtro de asientos mínimos
                Text(
                    text = "Asientos mínimos",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "$minSeats ${if (minSeats == 1) "asiento" else "asientos"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                androidx.compose.material3.Slider(
                    value = minSeats.toFloat(),
                    onValueChange = { minSeats = it.roundToInt() },
                    valueRange = 1f..maxSeats.toFloat(),
                    steps = (maxSeats - 2).coerceAtLeast(0)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onClearAll()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Limpiar todo")
                    }

                    Button(
                        onClick = {
                            onApply(
                                priceRange,
                                minSeats
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Aplicar")
                    }
                }
            }
        }
    }
}

