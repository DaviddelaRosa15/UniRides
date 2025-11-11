package com.ddl.unirides.ui.navigation

/**
 * Sealed class que define todas las rutas de navegación de la app
 */
sealed class Screen(val route: String) {
    // Pantallas de autenticación
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object EmailVerification : Screen("email_verification")

    // Pantalla principal
    object Home : Screen("home")

    // Pantallas de funcionalidad
    object Search : Screen("search")
    object Offer : Screen("offer")
    object ChatList : Screen("chat_list")
    object Profile : Screen("profile")
    object RateDriver : Screen("rate_driver")

    // Pantallas con parámetros
    object ChatDetail : Screen("chat_detail/{chatId}") {
        fun createRoute(chatId: String) = "chat_detail/$chatId"
    }

    object OfferDetail : Screen("offer_detail/{offerId}") {
        fun createRoute(offerId: String) = "offer_detail/$offerId"
    }

    object UserProfile : Screen("user_profile/{userId}") {
        fun createRoute(userId: String) = "user_profile/$userId"
    }
}
