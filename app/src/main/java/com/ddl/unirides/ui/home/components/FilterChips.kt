package com.ddl.unirides.ui.home.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun FilterChips(
    fromDate: Timestamp?,
    toDate: Timestamp?,
    hasActiveFilters: Boolean,
    onDateTimeClick: () -> Unit,
    onFiltersClick: () -> Unit,
    onClearDateTime: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Chip de Date & Time
        FilterChip(
            selected = fromDate != null && toDate != null,
            onClick = onDateTimeClick,
            label = {
                Text(
                    text = if (fromDate != null && toDate != null) {
                        formatDateTimeRange(fromDate, toDate)
                    } else {
                        "Fecha y hora"
                    }
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null
                )
            },
            trailingIcon = if (fromDate != null && toDate != null) {
                {
                    IconButton(
                        onClick = onClearDateTime,
                        modifier = Modifier.size(18.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Limpiar",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else null,
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )

        // Chip de Filters
        FilterChip(
            selected = hasActiveFilters,
            onClick = onFiltersClick,
            label = { Text("Filtros") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )
    }
}

private fun formatDateTimeRange(fromDate: Timestamp, toDate: Timestamp): String {
    val dateFormat = SimpleDateFormat("d 'de' MMM", Locale.forLanguageTag("es"))
    val timeFormat = SimpleDateFormat("h:mm a", Locale.forLanguageTag("es"))

    val fromDateStr = dateFormat.format(fromDate.toDate())
    val fromTimeStr = timeFormat.format(fromDate.toDate())
    val toDateStr = dateFormat.format(toDate.toDate())
    val toTimeStr = timeFormat.format(toDate.toDate())

    return "$fromDateStr, $fromTimeStr — $toDateStr, $toTimeStr"
}

