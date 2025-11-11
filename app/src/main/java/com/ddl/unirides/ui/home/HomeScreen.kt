package com.ddl.unirides.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ddl.unirides.ui.home.components.DateTimeRangeDialog
import com.ddl.unirides.ui.home.components.EmptyStateCard
import com.ddl.unirides.ui.home.components.FilterChips
import com.ddl.unirides.ui.home.components.FiltersDialog
import com.ddl.unirides.ui.home.components.HomeDrawerContent
import com.ddl.unirides.ui.home.components.RideOfferCard
import com.ddl.unirides.ui.home.components.SearchBar
import com.ddl.unirides.ui.home.components.TopBarHome
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onRateDriverClick: () -> Unit = {},
    onMyTripsClick: () -> Unit = {},
    onChatListClick: () -> Unit = {},
    onOfferClick: (String) -> Unit = {},
    onNavigateToChatDetail: (String) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            HomeDrawerContent(
                currentUser = uiState.currentUser,
                onHomeClick = {
                    scope.launch {
                        drawerState.close()
                    }
                },
                onRateDriverClick = {
                    scope.launch {
                        drawerState.close()
                        onRateDriverClick()
                    }
                },
                onMyTripsClick = {
                    scope.launch {
                        drawerState.close()
                        onMyTripsClick()
                    }
                }
            )
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopBarHome(
                    onMenuClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    },
                    onChatClick = onChatListClick
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Barra de búsqueda
                    SearchBar(
                        query = uiState.searchQuery,
                        onQueryChange = viewModel::onSearchQueryChanged
                    )

                    // Chips de filtros
                    FilterChips(
                        fromDate = uiState.fromDate,
                        toDate = uiState.toDate,
                        hasActiveFilters = uiState.priceRange != 0f..uiState.maxPrice || uiState.minSeats > 1,
                        onDateTimeClick = viewModel::showDateTimeDialog,
                        onFiltersClick = viewModel::showFiltersDialog,
                        onClearDateTime = viewModel::clearDateTimeFilter
                    )

                    // Título de sección
                    Text(
                        text = "Viajes disponibles",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )

                    // Lista de ofertas o estados especiales
                    when {
                        uiState.isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        uiState.error != null -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = uiState.error ?: "Error desconocido",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }

                        uiState.filteredOffers.isEmpty() -> {
                            EmptyStateCard(
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(
                                    items = uiState.filteredOffers,
                                    key = { it.offer.id }
                                ) { offerWithPublisher ->
                                    RideOfferCard(
                                        offerWithPublisher = offerWithPublisher,
                                        onCardClick = {
                                            onOfferClick(offerWithPublisher.offer.id)
                                        },
                                        onChatClick = {
                                            scope.launch {
                                                val chatId = viewModel.createOrGetChat(
                                                    offerId = offerWithPublisher.offer.id,
                                                    otherUserId = offerWithPublisher.offer.publisherUserId
                                                )
                                                chatId?.let { onNavigateToChatDetail(it) }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Diálogos
                if (uiState.showDateTimeDialog) {
                    DateTimeRangeDialog(
                        currentFromDate = uiState.fromDate,
                        currentToDate = uiState.toDate,
                        onDismiss = viewModel::dismissDateTimeDialog,
                        onConfirm = viewModel::onDateTimeRangeSelected
                    )
                }

                if (uiState.showFiltersDialog) {
                    FiltersDialog(
                        currentPriceRange = uiState.priceRange,
                        currentMinSeats = uiState.minSeats,
                        maxPrice = uiState.maxPrice,
                        maxSeats = uiState.maxSeats,
                        onDismiss = viewModel::dismissFiltersDialog,
                        onApply = viewModel::onFiltersChanged,
                        onClearAll = viewModel::clearAllFilters
                    )
                }
            }
        }
    }
}

