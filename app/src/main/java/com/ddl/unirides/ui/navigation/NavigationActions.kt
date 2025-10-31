package com.ddl.unirides.ui.navigation

import androidx.navigation.NavHostController

/**
 * Objeto que centraliza las acciones de navegación
 * Facilita la navegación desde ViewModels y Composables
 */
object NavigationActions {

    /**
     * Navega a la pantalla de login limpiando el back stack
     */
    fun navigateToLogin(navController: NavHostController) {
        navController.navigate(Screen.Login.route) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    /**
     * Navega a la pantalla de registro
     */
    fun navigateToSignUp(navController: NavHostController) {
        navController.navigate(Screen.SignUp.route)
    }

    /**
     * Navega a la pantalla principal (Home) limpiando el back stack
     */
    fun navigateToHome(navController: NavHostController) {
        navController.navigate(Screen.Home.route) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    /**
     * Navega a la pantalla de búsqueda de viajes
     */
    fun navigateToSearch(navController: NavHostController) {
        navController.navigate(Screen.Search.route)
    }

    /**
     * Navega a la pantalla para ofrecer un viaje
     */
    fun navigateToOffer(navController: NavHostController) {
        navController.navigate(Screen.Offer.route)
    }

    /**
     * Navega a la lista de chats
     */
    fun navigateToChatList(navController: NavHostController) {
        navController.navigate(Screen.ChatList.route)
    }

    /**
     * Navega al detalle de un chat específico
     */
    fun navigateToChatDetail(navController: NavHostController, chatId: String) {
        navController.navigate(Screen.ChatDetail.createRoute(chatId))
    }

    /**
     * Navega al perfil del usuario actual
     */
    fun navigateToProfile(navController: NavHostController) {
        navController.navigate(Screen.Profile.route)
    }

    /**
     * Navega al detalle de una oferta específica
     */
    fun navigateToOfferDetail(navController: NavHostController, offerId: String) {
        navController.navigate(Screen.OfferDetail.createRoute(offerId))
    }

    /**
     * Navega al perfil de otro usuario
     */
    fun navigateToUserProfile(navController: NavHostController, userId: String) {
        navController.navigate(Screen.UserProfile.createRoute(userId))
    }

    /**
     * Navega hacia atrás
     */
    fun navigateBack(navController: NavHostController) {
        navController.popBackStack()
    }
}

