package com.example.kchat.presentation.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlinx.serialization.Serializable

sealed class Routes {

    @Serializable
    data object SplashScreen: Routes()

    @Serializable
    data object WelcomeScreen: Routes()

    @Serializable
    data object UserRegistrationScreen: Routes()

    @Serializable
    data object HomeScreen: Routes()

    @Serializable
    data object UpdateScreen: Routes()

    @Serializable
    data object CommunitiesScreen: Routes()

    @Serializable
    data object CallScreen: Routes()

    @Serializable
    data object UserProfileSetScreen: Routes()

    @Serializable
    data object SettingScreen: Routes()

    @Serializable
    data object ChatScreen: Routes() {
        const val route = "chat_screen/{email}"

        fun createRoute(email: String): String {
            val encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8.toString())
            return "chat_screen/$encodedEmail"
        }
    }
}
