package com.example.pawtrack2.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@Composable
fun PawTrackApp() {

    var currentScreen by rememberSaveable {
        mutableStateOf("login")
    }

    when (currentScreen) {

        "login" -> {
            LoginScreen(
                onLoginSuccess = { currentScreen = "dashboard" },
                onGoToSignUp = { currentScreen = "signup" },
                onGoToForgotPassword = { currentScreen = "forgot" }
            )
        }

        "signup" -> {
            SignUpScreen(
                onSignUpSuccess = { currentScreen = "login" },
                onGoToLogin = { currentScreen = "login" }
            )
        }

        "forgot" -> {
            ForgotPasswordScreen(
                onResetSuccess = { currentScreen = "login" },
                onGoToLogin = { currentScreen = "login" }
            )
        }

        "dashboard" -> {
            DashboardScreen(
                onLogout = { currentScreen = "login" },
                onOpenPetLocation = { currentScreen = "pet-location" }
            )
        }

        "pet-location" -> {
            PetLocationScreen(onBack = { currentScreen = "dashboard" })
        }
    }
}