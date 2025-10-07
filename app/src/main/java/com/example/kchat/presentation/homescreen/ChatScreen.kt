@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.kchat.presentation.homescreen

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.kchat.R
import com.example.kchat.models.Message
import com.example.kchat.presentation.viewmodels.BaseViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ChatScreen(
    navHostController: NavHostController,
    baseViewModel: BaseViewModel,
    chatUserEmail: String?
) {
    val context = LocalContext.current
    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email

    // Safety checks
    if (currentUserEmail.isNullOrBlank()) {
        Toast.makeText(context, "Current user not found", Toast.LENGTH_SHORT).show()
        return
    }
    if (chatUserEmail.isNullOrBlank()) {
        Toast.makeText(context, "Chat user not found", Toast.LENGTH_SHORT).show()
        return
    }

    // ✅ Decode email safely
    val decodedEmail = Uri.decode(chatUserEmail)

    var messageText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<Message>() }

    // ✅ Listen for messages in real-time
    LaunchedEffect(decodedEmail) {
        baseViewModel.getMessage(currentUserEmail, decodedEmail) { newMessage ->
            messages.add(newMessage)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(decodedEmail) },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = colorResource(id = R.color.dark_blue),
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(id = R.color.dark_blue))
                    .padding(8.dp)
            ) {
                BasicTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White, shape = MaterialTheme.shapes.small)
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            baseViewModel.sendMessage(currentUserEmail, decodedEmail, messageText)
                            messageText = ""
                        }
                    }
                ) {
                    Text("Send")
                }
            }
        },
        containerColor = colorResource(id = R.color.dark_blue)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(colorResource(id = R.color.dark_blue)),
            reverseLayout = true // latest message at bottom
        ) {
            items(messages.reversed()) { message ->
                MessageItem(
                    message = message,
                    isCurrentUser = message.senderPhoneNumber == currentUserEmail
                )
            }
        }
    }
}

@Composable
fun MessageItem(message: Message, isCurrentUser: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Text(
            text = message.message,
            color = Color.White,
            modifier = Modifier
                .background(
                    if (isCurrentUser) Color(0xFF0F9D58) else Color.Gray,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(8.dp)
        )
    }
}
