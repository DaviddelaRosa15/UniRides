package com.ddl.unirides.ui.offer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ddl.unirides.ui.common.DatePickerField
import com.ddl.unirides.ui.common.SeatsCounter
import com.ddl.unirides.ui.common.TimePickerField
import com.ddl.unirides.ui.common.UniRidesPrimaryButton
import com.ddl.unirides.ui.common.UniRidesTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferRideScreen(
    onNavigateBack: () -> Unit,
    viewModel: OfferRideViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.clearSuccess()
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Publicar Viaje",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .imePadding()
        ) {
            // Sección de ubicaciones
            Text(
                text = "Ubicaciones",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            UniRidesTextField(
                value = uiState.origin,
                onValueChange = viewModel::onOriginChange,
                label = "Origen",
                placeholder = "¿Desde dónde sales?",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                isError = uiState.originError != null,
                errorMessage = uiState.originError,
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(12.dp))

            UniRidesTextField(
                value = uiState.destination,
                onValueChange = viewModel::onDestinationChange,
                label = "Destino",
                placeholder = "¿Hacia dónde vas?",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                isError = uiState.destinationError != null,
                errorMessage = uiState.destinationError,
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Sección de fecha y hora
            Text(
                text = "Fecha y Hora",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DatePickerField(
                    value = uiState.date,
                    onValueChange = viewModel::onDateChange,
                    label = "Fecha",
                    modifier = Modifier.weight(1f),
                    isError = uiState.dateError != null,
                    errorMessage = uiState.dateError
                )

                TimePickerField(
                    value = uiState.time,
                    onValueChange = viewModel::onTimeChange,
                    label = "Hora",
                    modifier = Modifier.weight(1f),
                    isError = uiState.timeError != null,
                    errorMessage = uiState.timeError
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sección de asientos y precio
            Text(
                text = "Detalles del Viaje",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = androidx.compose.ui.Alignment.Top
            ) {
                // Asientos disponibles
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Asientos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                    )
                    SeatsCounter(
                        value = uiState.availableSeats,
                        onValueChange = viewModel::onSeatsChange,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Precio por asiento
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Precio/asiento",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = uiState.price,
                        onValueChange = viewModel::onPriceChange,
                        placeholder = { Text("0.00") },
                        leadingIcon = {
                            Text(
                                text = "$",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        isError = uiState.priceError != null,
                        supportingText = if (uiState.priceError != null) {
                            { Text(uiState.priceError!!) }
                        } else null,
                        singleLine = true,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            errorBorderColor = MaterialTheme.colorScheme.error
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Notas adicionales
            Text(
                text = "Notas Adicionales (Opcional)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            UniRidesTextField(
                value = uiState.notes,
                onValueChange = viewModel::onNotesChange,
                label = "Notas",
                placeholder = "Ej: Tengo espacio para equipaje, acepto mascotas...",
                singleLine = false,
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Text
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón de publicar
            UniRidesPrimaryButton(
                text = "Publicar Viaje",
                onClick = viewModel::publishOffer,
                isLoading = uiState.isLoading,
                enabled = !uiState.isLoading && !uiState.isSuccess
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

