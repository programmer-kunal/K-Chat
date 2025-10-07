package com.example.kchat.presentation.splashscreen.homescreen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.kchat.R
import com.example.kchat.presentation.navigation.Routes
import com.example.kchat.presentation.splashscreen.bottomnavigation.BottomNavigation
import com.example.kchat.presentation.viewmodels.BaseViewModel
import com.google.firebase.auth.FirebaseAuth
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun HomeScreen(navHostController: NavHostController, homeBaseViewModel: BaseViewModel) {

    val context = LocalContext.current
    var showPopup by remember { mutableStateOf(false) }
    val chatData by homeBaseViewModel.chatList.collectAsState()
    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email

    // ✅ Load chats automatically without lambda
    LaunchedEffect(currentUserEmail) {
        currentUserEmail?.let { homeBaseViewModel.loadChatList(it) }
    }

    Scaffold(
        containerColor = colorResource(id = R.color.dark_blue),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showPopup = true },
                containerColor = colorResource(id = R.color.light_blue),
                contentColor = Color.White,
                modifier = Modifier.size(65.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.add_chat_icon),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = Color.White
                )
            }
        },
        bottomBar = {
            BottomNavigation(navHostController, selectedItem = 0, onClick = { index ->
                when (index) {
                    0 -> navHostController.navigate(Routes.HomeScreen)
                    1 -> navHostController.navigate(Routes.UpdateScreen)
                    2 -> navHostController.navigate(Routes.CommunitiesScreen)
                    3 -> navHostController.navigate(Routes.CallScreen)
                }
            })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .background(color = colorResource(id = R.color.dark_blue))
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            TopBar(navHostController)
            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(12.dp))

            if (showPopup) {
                AddUserPopup(
                    onDismiss = { showPopup = false },
                    onUserAdd = { newUser ->
                        homeBaseViewModel.addChat(newUser)
                        currentUserEmail?.let { homeBaseViewModel.loadChatList(it) }
                    },
                    baseViewModel = homeBaseViewModel
                )
            }

            LazyColumn {
                items(chatData) { chat ->
                    ChatDesign(
                        chatDesignModel = chat,
                        onClick = {
                            chat.email?.let { email ->
                                // ✅ Encode email to prevent crashes
                                val encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8.toString())
                                navHostController.navigate("chat_screen/$encodedEmail")
                            } ?: run {
                                Toast.makeText(context, "User email not found", Toast.LENGTH_SHORT).show()
                            }
                        },
                        baseViewModel = homeBaseViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun TopBar(navHostController: NavHostController) {
    var isSearching by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        if (isSearching) {
            TextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text("Search", color = colorResource(id = R.color.light_blue)) },
                singleLine = true,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp)
                    .fillMaxWidth(0.8f),
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
        } else {
            Text(
                "KChat",
                fontSize = 28.sp,
                color = colorResource(id = R.color.light_blue),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp),
                fontWeight = FontWeight.Bold
            )

            Row(modifier = Modifier.align(Alignment.CenterEnd)) {
                IconButton(onClick = {}) {
                    Icon(
                        painter = painterResource(id = R.drawable.camera),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = colorResource(id = R.color.light_blue)
                    )
                }
                IconButton(onClick = { isSearching = !isSearching }) {
                    Icon(
                        painter = painterResource(id = R.drawable.search),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = colorResource(id = R.color.light_blue)
                    )
                }

                IconButton(onClick = { showMenu = !showMenu }) {
                    Icon(
                        painter = painterResource(id = R.drawable.more),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = colorResource(id = R.color.light_blue)
                    )

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(color = colorResource(id = R.color.light_blue))
                    ) {
                        DropdownMenuItem(
                            text = { Text("New Group", color = Color.DarkGray) },
                            onClick = { showMenu = false })
                        DropdownMenuItem(
                            text = { Text("New Broadcast", color = Color.DarkGray) },
                            onClick = { showMenu = false })
                        DropdownMenuItem(
                            text = { Text("Linked Devices", color = Color.DarkGray) },
                            onClick = { showMenu = false })
                        DropdownMenuItem(
                            text = { Text("Starred Messages", color = Color.DarkGray) },
                            onClick = { showMenu = false })
                        DropdownMenuItem(
                            text = { Text("Settings", color = Color.DarkGray) },
                            onClick = {
                                showMenu = false
                                navHostController.navigate(Routes.SettingScreen)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Logout", color = Color.Red, fontWeight = FontWeight.Bold) },
                            onClick = {
                                showMenu = false
                                FirebaseAuth.getInstance().signOut()
                                navHostController.navigate(Routes.WelcomeScreen) {
                                    popUpTo(Routes.HomeScreen) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddUserPopup(
    onDismiss: () -> Unit,
    onUserAdd: (ChatDesignModel) -> Unit,
    baseViewModel: BaseViewModel
) {
    var email by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var userFound by remember { mutableStateOf<ChatDesignModel?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Enter Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row {
                    Button(
                        onClick = {
                            isSearching = true
                            baseViewModel.searchUserByEmail(email) { user ->
                                isSearching = false
                                userFound = user
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Search")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                when {
                    isSearching -> Text("Searching...", color = Color.Gray)
                    userFound != null -> {
                        Text("User Found: ${userFound!!.name ?: "Unknown"}")
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(onClick = {
                            onUserAdd(userFound!!)
                            onDismiss()
                        }) {
                            Text("Add to Chats")
                        }
                    }
                    else -> Text("No user found", color = Color.Gray)
                }
            }
        }
    )
}
