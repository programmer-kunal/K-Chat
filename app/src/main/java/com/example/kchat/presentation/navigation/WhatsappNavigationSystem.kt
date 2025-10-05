package com.example.kchat.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import androidx.navigation.compose.rememberNavController
import com.example.kchat.presentation.callscreen.CallScreen
import com.example.kchat.presentation.communitiesscreen.CommunitiesScreen
import com.example.kchat.presentation.splashscreen.SplashScreen
import com.example.kchat.presentation.splashscreen.homescreen.HomeScreen
import com.example.kchat.presentation.splashscreen.userregistrationscreen.UserRegistrationScreen
import com.example.kchat.presentation.splashscreen.welcomescreen.WelcomeScreen
import com.example.kchat.presentation.updatescreen.UpdateScreen
import com.example.kchat.presentation.viewmodels.BaseViewModel
import com.example.kchat.profile.UserProfileSetScreen

@Composable
fun WhatsappNavigationSystem() {
    val navController = rememberNavController()

    NavHost(startDestination =Routes.SplashScreen, navController = navController){

        composable<Routes.SplashScreen>{
            SplashScreen(navController)
        }
        composable<Routes.WelcomeScreen>{
            WelcomeScreen(navController)
        }
        composable<Routes.UserRegistrationScreen>{
            UserRegistrationScreen(navController)
        }
        composable<Routes.HomeScreen>{
            val baseViewModel:BaseViewModel = hiltViewModel()
            HomeScreen(navController,baseViewModel)
        }
        composable<Routes.UpdateScreen>{
            UpdateScreen(navController)
        }
        composable<Routes.CommunitiesScreen>{
            CommunitiesScreen(navController)
        }
        composable<Routes.CallScreen>{
            CallScreen(navController)
        }
        composable<Routes.UserProfileSetScreen>{
            UserProfileSetScreen(navHostController = navController)
        }



    }

}