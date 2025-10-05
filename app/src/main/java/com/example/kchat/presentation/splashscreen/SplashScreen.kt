package com.example.kchat.presentation.splashscreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.kchat.R
import com.example.kchat.presentation.navigation.Routes
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navHostController: NavHostController) {

    LaunchedEffect(Unit) {
        delay(2000)
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // ✅ User is already logged in → go directly to Home
            navHostController.navigate(Routes.HomeScreen) {
                popUpTo<Routes.SplashScreen> {
                    inclusive = true
                }
            }
        } else {
            // ❌ User not logged in → go to Welcome
            navHostController.navigate(Routes.WelcomeScreen) {
                popUpTo<Routes.SplashScreen> {
                    inclusive = true
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.dark_blue))
    ) {
        // App logo in center
        Image(
            painter = painterResource(id = R.drawable.logokchat),
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.Center)
        )

        // "From KAVTORS" text at bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "From",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )

            Row {
                Image(
                    painter = painterResource(id = R.drawable.logo2),
                    contentDescription = null,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = "KAVTORS",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.light_blue),
                    modifier = Modifier.padding(top = 5.dp)
                )
            }
        }
    }
}
