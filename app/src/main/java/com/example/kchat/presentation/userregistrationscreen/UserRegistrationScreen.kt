package com.example.kchat.presentation.splashscreen.userregistrationscreen

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.kchat.R
import com.example.kchat.presentation.navigation.Routes
import com.example.kchat.presentation.viewmodels.AuthState
import com.example.kchat.presentation.viewmodels.EmailAuthViewModel

@Composable
fun UserRegistrationScreen(
    navController: NavController,
    emailAuthViewModel: EmailAuthViewModel = hiltViewModel()
) {
    val authState by emailAuthViewModel.authState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.dark_blue)),
        contentAlignment = Alignment.Center // ✅ Centers everything
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Login with Email",
                fontSize = 22.sp,
                color = colorResource(id = R.color.light_blue),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Email Input
            TextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Email Address") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    unfocusedPlaceholderColor = colorResource(id = R.color.light_blue),
                    focusedPlaceholderColor = colorResource(id = R.color.light_blue),
                    unfocusedContainerColor = colorResource(id = R.color.dark_blue),
                    focusedContainerColor = colorResource(id = R.color.dark_blue),
                    unfocusedIndicatorColor = colorResource(id = R.color.light_blue),
                    focusedIndicatorColor = colorResource(id = R.color.light_blue),
                    unfocusedTextColor = colorResource(id = R.color.light_blue),
                    focusedTextColor = colorResource(id = R.color.light_blue)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Input
            TextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    unfocusedPlaceholderColor = colorResource(id = R.color.light_blue),
                    focusedPlaceholderColor = colorResource(id = R.color.light_blue),
                    unfocusedContainerColor = colorResource(id = R.color.dark_blue),
                    focusedContainerColor = colorResource(id = R.color.dark_blue),
                    unfocusedIndicatorColor = colorResource(id = R.color.light_blue),
                    focusedIndicatorColor = colorResource(id = R.color.light_blue),
                    unfocusedTextColor = colorResource(id = R.color.light_blue),
                    focusedTextColor = colorResource(id = R.color.light_blue)
                )
            )

            Spacer(modifier = Modifier.height(26.dp))

            // ✅ Only Login Button (Register removed)
            Button(
                onClick = {
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        emailAuthViewModel.loginUser(email, password, context)
                    } else {
                        Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
                    }
                },
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.light_blue))
            ) {
                Text("Login", fontSize = 16.sp, color = Color.White)
            }

            if (authState is AuthState.Loading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(color = colorResource(id = R.color.light_blue))
            }

            when (authState) {
                is AuthState.Success -> {
                    LaunchedEffect(authState) {
                        emailAuthViewModel.resetAuthState()
                        navController.navigate(Routes.UserProfileSetScreen) {
                            popUpTo(Routes.UserRegistrationScreen) { inclusive = true }
                        }
                    }
                }

                is AuthState.Error -> {
                    LaunchedEffect(authState) {
                        Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_SHORT).show()
                    }
                }

                else -> {}
            }
        }
    }
}
