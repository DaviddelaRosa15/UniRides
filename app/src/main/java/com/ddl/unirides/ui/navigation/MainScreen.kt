package com.ddl.unirides.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
    val shouldShowBottomBar = currentRoute != Screen.Offer.route &&
            !(currentRoute?.startsWith("chat_detail/") ?: false) &&
            !(currentRoute?.startsWith("offer_detail/") ?: false)

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
                        navController.navigate(Screen.OfferDetail.createRoute(offerId))
                    },
                    onNavigateToChatDetail = { chatId ->
                        navController.navigate(Screen.ChatDetail.createRoute(chatId))
                    }
                )
            }

            composable(Screen.Search.route) {
                MyTripsScreen(
                    onOfferClick = { offerId ->
                        navController.navigate(Screen.OfferDetail.createRoute(offerId))
                    },
                    onPublishClick = {
                        navController.navigate(Screen.Offer.route)
                    }
                )
            }

            composable(Screen.ChatList.route) {
                com.ddl.unirides.ui.chat.ChatsScreen(
                    onChatClick = { chatId ->
                        // TODO: Navegar a detalle del chat
                        navController.navigate(Screen.ChatDetail.createRoute(chatId))
                    }
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

            // Pantalla de detalle de chat
            composable(
                route = Screen.ChatDetail.route,
                arguments = listOf(
                    navArgument("chatId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
                com.ddl.unirides.ui.chatdetail.ChatDetailScreen(
                    chatId = chatId,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToHome = {
                        viewModel.selectRoute(Screen.Home.route)
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
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
                    onNavigateToChats = {
                        viewModel.selectRoute(Screen.ChatList.route)
                        navController.navigate(Screen.ChatList.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToProfile = {
                        viewModel.selectRoute(Screen.Profile.route)
                        navController.navigate(Screen.Profile.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            // Pantalla de detalle de viaje
            composable(
                route = Screen.OfferDetail.route,
                arguments = listOf(
                    navArgument("offerId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val offerId = backStackEntry.arguments?.getString("offerId") ?: ""
                com.ddl.unirides.ui.tripdetail.TripDetailScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onDriverClick = { driverId ->
                        // TODO: Navegar al perfil del conductor cuando esté implementado
                        // navController.navigate(Screen.UserProfile.createRoute(driverId))
                    },
                    onChatClick = { chatId ->
                        navController.navigate(Screen.ChatDetail.createRoute(chatId))
                    }
                )
            }
        }
    }
}


