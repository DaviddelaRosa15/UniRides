package com.ddl.unirides.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ddl.unirides.ui.common.BottomNavigationBar
import com.ddl.unirides.ui.home.HomeScreen
import com.ddl.unirides.ui.mytrips.MyTripsScreen
import com.ddl.unirides.ui.offer.OfferRideScreen
import com.ddl.unirides.ui.profile.ProfileScreen

@Composable
fun MainScreen(
    onLogout: () -> Unit,
    viewModel: MainNavViewModel = hiltViewModel()
) {
    val navController = rememberNavController()

    // Observe selected tab from ViewModel (persists with SavedStateHandle)
    val selectedRoute by viewModel.selectedRoute.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route


    // Determinar si debemos mostrar el bottom bar
    val shouldShowBottomBar = currentRoute != Screen.Offer.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (shouldShowBottomBar) {
                BottomNavigationBar(
                    currentRoute = currentRoute ?: selectedRoute,
                    onNavigate = { route ->
                        if (route != currentRoute) {
                            viewModel.selectRoute(route)
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = selectedRoute,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onRateDriverClick = {
                        navController.navigate(Screen.RateDriver.route)
                    },
                    onMyTripsClick = {
                        viewModel.selectRoute(Screen.Search.route)
                        navController.navigate(Screen.Search.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onChatListClick = {
                        viewModel.selectRoute(Screen.ChatList.route)
                        navController.navigate(Screen.ChatList.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onOfferClick = { offerId ->
                        // TODO: Navegar a detalle de oferta
                    },
                    onChatWithUserClick = { offerId, userId ->
                        // TODO: Navegar a chat con usuario específico
                    }
                )
            }

            composable(Screen.Search.route) {
                MyTripsScreen(
                    onOfferClick = { offerId ->
                        // TODO: Navegar a detalle de oferta
                    },
                    onPublishClick = {
                        navController.navigate(Screen.Offer.route)
                    }
                )
            }

            composable(Screen.ChatList.route) {
                // TODO: Implementar pantalla de lista de chats
                PlaceholderScreenContent(
                    screenName = "Chats"
                )
            }

            composable(Screen.Offer.route) {
                OfferRideScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigateBack = null,
                    onNavigateToSettings = {
                        // TODO: Implementar navegación a configuración
                    },
                    onNavigateToMyTrips = {
                        viewModel.selectRoute(Screen.Search.route)
                        navController.navigate(Screen.Search.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onSignOut = onLogout,
                )
            }

            // Pantalla de calificar conductor
            composable(Screen.RateDriver.route) {
                com.ddl.unirides.ui.rating.RateDriverScreen(
                    onNavigateBack = {
                        navController.navigateUp()
                    }
                )
            }
        }
    }
}


@Composable
private fun PlaceholderScreenContent(screenName: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = screenName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Próximamente",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

