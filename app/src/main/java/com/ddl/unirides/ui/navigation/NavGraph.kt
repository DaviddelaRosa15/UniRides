package com.ddl.unirides.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ddl.unirides.ui.login.LoginScreen
import com.ddl.unirides.ui.profile.ProfileScreen
import com.ddl.unirides.ui.signup.SignUpScreen
import com.ddl.unirides.ui.verification.EmailVerificationScreen

/**
 * Grafo de navegación principal de la aplicación
 *
 * @param navController Controlador de navegación
 * @param startDestination Destino inicial (Login o Home según el estado de autenticación)
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ==================== AUTENTICACIÓN ====================
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route)
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToVerification = {
                    navController.navigate(Screen.EmailVerification.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToVerification = {
                    navController.navigate(Screen.EmailVerification.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.EmailVerification.route) {
            EmailVerificationScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.EmailVerification.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.EmailVerification.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            ProtectedRoute(
                onNotAuthenticated = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNotVerified = {
                    navController.navigate(Screen.EmailVerification.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) {
                MainScreen(
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        // ==================== BÚSQUEDA DE VIAJES ====================
        composable(Screen.Search.route) {
            ProtectedRoute(
                onNotAuthenticated = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNotVerified = {
                    navController.navigate(Screen.EmailVerification.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) {
                // TODO: Implementar SearchScreen
                PlaceholderScreen(
                    screenName = "Search",
                    onNavigate = {
                        navController.popBackStack()
                    }
                )
            }
        }


        // ==================== LISTA DE CHATS ====================
        composable(Screen.ChatList.route) {
            ProtectedRoute(
                onNotAuthenticated = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNotVerified = {
                    navController.navigate(Screen.EmailVerification.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) {
                // TODO: Implementar ChatListScreen
                PlaceholderScreen(
                    screenName = "Chat List",
                    onNavigate = {
                        navController.popBackStack()
                    }
                )
            }
        }

        // ==================== DETALLE DE CHAT ====================
        composable(
            route = Screen.ChatDetail.route,
            arguments = listOf(
                navArgument("chatId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""

            ProtectedRoute(
                onNotAuthenticated = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNotVerified = {
                    navController.navigate(Screen.EmailVerification.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) {
                // Usando placeholder temporal hasta implementar la pantalla final
                com.ddl.unirides.ui.chatdetail.ChatDetailScreenPlaceholder(
                    chatId = chatId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        // ==================== PERFIL DE USUARIO ====================
        composable(Screen.Profile.route) {
            ProtectedRoute(
                onNotAuthenticated = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNotVerified = {
                    navController.navigate(Screen.EmailVerification.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) {
                ProfileScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToSettings = {
                        // TODO: Implementar pantalla de configuración
                    },
                    onNavigateToMyTrips = {
                        // TODO: Navegar a mis viajes cuando esté implementado
                    },
                    onSignOut = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        // ==================== DETALLE DE OFERTA ====================
        composable(
            route = Screen.OfferDetail.route,
            arguments = listOf(
                navArgument("offerId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val offerId = backStackEntry.arguments?.getString("offerId") ?: ""
            // TODO: Implementar OfferDetailScreen
            PlaceholderScreen(
                screenName = "Offer Detail: $offerId",
                onNavigate = {
                    navController.popBackStack()
                }
            )
        }

        // ==================== PERFIL DE OTRO USUARIO ====================
        composable(
            route = Screen.UserProfile.route,
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            // TODO: Implementar UserProfileScreen
            PlaceholderScreen(
                screenName = "User Profile: $userId",
                onNavigate = {
                    navController.popBackStack()
                }
            )
        }
    }
}

/**
 * Pantalla temporal para probar la navegación
 */
@Composable
private fun PlaceholderScreen(
    screenName: String,
    onNavigate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = screenName,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigate) {
            Text("Navigate")
        }
    }
}
