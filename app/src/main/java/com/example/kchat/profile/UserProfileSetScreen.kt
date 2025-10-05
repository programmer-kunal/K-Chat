package com.example.kchat.profile

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.example.kchat.R
import com.example.kchat.presentation.navigation.Routes
import com.example.kchat.presentation.viewmodels.EmailAuthViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun UserProfileSetScreen(
    emailAuthViewModel: EmailAuthViewModel = hiltViewModel(),
    navHostController: NavHostController
) {
    var name by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var bitmapImage by remember { mutableStateOf<Bitmap?>(null) }
    val firebaseAuth = Firebase.auth
    val email = firebaseAuth.currentUser?.email ?: ""
    val userId = firebaseAuth.currentUser?.uid ?: ""
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            profileImageUri = uri
            uri?.let {
                bitmapImage = if (Build.VERSION.SDK_INT < 28) {
                    @Suppress("DEPRECATION")
                    android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, it)
                    ImageDecoder.decodeBitmap(source)
                }
            }
        }
    )

    Column(
        modifier = Modifier
            .background(color = colorResource(id = R.color.dark_blue))
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(128.dp)
                .clip(CircleShape)
                .background(color = colorResource(id = R.color.dark_blue))
                .border(2.dp, color = colorResource(id = R.color.light_blue), shape = CircleShape)
                .clickable { imagePickerLauncher.launch("image/*") }
        ) {
            when {
                bitmapImage != null -> {
                    Image(
                        bitmap = bitmapImage!!.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                profileImageUri != null -> {
                    Image(
                        painter = rememberImagePainter(profileImageUri),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                else -> {
                    Image(
                        painter = painterResource(id = R.drawable.user_placeholder),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = email, color = colorResource(id = R.color.light_blue))

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(text = "Name", color = colorResource(R.color.light_blue)) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = colorResource(id = R.color.dark_blue),
                focusedContainerColor = colorResource(id = R.color.dark_blue),
                unfocusedIndicatorColor = colorResource(id = R.color.light_blue),
                focusedIndicatorColor = colorResource(id = R.color.light_blue),
                unfocusedTextColor = colorResource(id = R.color.light_blue),
                focusedTextColor = colorResource(id = R.color.light_blue)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = status,
            onValueChange = { status = it },
            label = { Text(text = "Status", color = colorResource(R.color.light_blue)) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
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
            onClick = {
                emailAuthViewModel.saveUserProfile(userId, name, status, bitmapImage)
                navHostController.navigate(Routes.HomeScreen)
            },
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.light_blue))
        ) {
            Text("Save")
        }
    }
}
