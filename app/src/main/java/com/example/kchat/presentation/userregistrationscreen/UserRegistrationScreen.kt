package com.example.kchat.presentation.splashscreen.userregistrationscreen

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.kchat.R
import com.example.kchat.presentation.navigation.Routes
import com.example.kchat.presentation.viewmodels.AuthState
import com.example.kchat.presentation.viewmodels.PhoneAuthViewModel

@Composable
fun UserRegistrationScreen(
    navController: NavController,
    phoneAuthViewModel: PhoneAuthViewModel = hiltViewModel()
) {
    val authState by phoneAuthViewModel.authState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    var otp by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedCountry by remember { mutableStateOf("India") }
    var countryCode by remember { mutableStateOf("+91") }
    var phoneNumber by remember { mutableStateOf("") }

    // Country code mapping
    val countryCodeMap = mapOf(
        "India" to "+91",
        "USA" to "+1",
        "UK" to "+44",
        "Germany" to "+49",
        "France" to "+33"
        // Add more as needed
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.dark_blue))
            .padding(top = 40.dp, bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Enter your phone number",
            fontSize = 20.sp,
            color = colorResource(id = R.color.light_blue),
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Country selection
        TextButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.width(230.dp)) {
                Text(
                    text = selectedCountry,
                    color = colorResource(id = R.color.light_blue),
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 16.sp
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = colorResource(id = R.color.light_blue),
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            countryCodeMap.keys.forEach { country ->
                DropdownMenuItem(text = { Text(text = country) }, onClick = {
                    selectedCountry = country
                    countryCode = countryCodeMap[country] ?: "+91"
                    expanded = false
                })
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Phone input or OTP input
        when (authState) {
            is AuthState.Ideal, is AuthState.Loading, is AuthState.CodeSent -> {
                if (authState is AuthState.CodeSent) {
                    otpInputUI(otp, onOtpChange = { otp = it }, phoneAuthViewModel, context)
                } else {
                    phoneInputUI(phoneNumber, onPhoneChange = { phoneNumber = it }, countryCode, activity, context, phoneAuthViewModel)
                }

                if (authState is AuthState.Loading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                }
            }

            is AuthState.Success -> {
                LaunchedEffect(authState) {
                    phoneAuthViewModel.resetAuthState()
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
        }
    }
}

@Composable
fun phoneInputUI(
    phoneNumber: String,
    onPhoneChange: (String) -> Unit,
    countryCode: String,
    activity: Activity?,
    context: android.content.Context,
    viewModel: PhoneAuthViewModel
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            value = countryCode,
            onValueChange = {},
            modifier = Modifier.width(70.dp),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = colorResource(id = R.color.dark_blue),
                focusedContainerColor = colorResource(id = R.color.dark_blue),
                unfocusedIndicatorColor = colorResource(id = R.color.light_blue),
                focusedIndicatorColor = colorResource(id = R.color.light_blue),
                unfocusedTextColor = colorResource(id = R.color.light_blue),
                focusedTextColor = colorResource(id = R.color.light_blue)
            )
        )
        Spacer(modifier = Modifier.width(10.dp))
        TextField(
            value = phoneNumber,
            onValueChange = onPhoneChange,
            placeholder = { Text(text = "Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
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
    }
    Spacer(modifier = Modifier.height(26.dp))
    Button(
        onClick = {
            if (phoneNumber.isNotEmpty()) {
                activity?.let {
                    viewModel.sendVerificationCode("$countryCode$phoneNumber", it)
                } ?: Toast.makeText(context, "Activity not found", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
            }
        },
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.light_blue))
    ) {
        Text("Send OTP", fontSize = 16.sp, color = Color.White)
    }
}

@Composable
fun otpInputUI(
    otp: String,
    onOtpChange: (String) -> Unit,
    viewModel: PhoneAuthViewModel,
    context: android.content.Context
) {
    Text(
        text = "Enter the OTP sent to your mobile number",
        fontSize = 20.sp,
        color = colorResource(id = R.color.light_blue),
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(8.dp))
    TextField(
        value = otp,
        onValueChange = onOtpChange,
        placeholder = { Text(text = "OTP") },
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
    Spacer(modifier = Modifier.height(32.dp))
    Button(
        onClick = { viewModel.verifyCode(otp, context) },
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.light_blue))
    ) {
        Text(text = "Verify OTP", fontSize = 16.sp, color = Color.White)
    }
}
