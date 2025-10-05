package com.example.kchat.presentation.splashscreen.homescreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.kchat.R
import com.example.kchat.presentation.navigation.Routes
import com.example.kchat.presentation.splashscreen.bottomnavigation.BottomNavigation
import com.example.kchat.presentation.viewmodels.BaseViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HomeScreen(navHostController: NavHostController,homeBaseViewModel: BaseViewModel) {
    var showPopup by remember {
        mutableStateOf(false)
    }
    val chatData by homeBaseViewModel.chatList.collectAsState()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    if (userId != null) {
        LaunchedEffect(userId) {
            homeBaseViewModel.getChatForUser(userId) { chats ->

            }

        }
    }

    var showMenu by remember { mutableStateOf(false) }




    Scaffold(
        containerColor = colorResource(id = R.color.dark_blue),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showPopup=true

                },
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
            BottomNavigation(navHostController,selectedItem = 0, onClick = { index ->
            when(index){
                0->{
                    navHostController.navigate(Routes.HomeScreen)
                }
                1->{
                    navHostController.navigate(Routes.UpdateScreen)
                }
                2->{
                    navHostController.navigate(Routes.CommunitiesScreen)
                }
                3->{
                    navHostController.navigate(Routes.CallScreen)
                }
            }
            })
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .background(color = colorResource(id = R.color.dark_blue))
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                var isSearching by remember { mutableStateOf(false) }
                var searchText by remember { mutableStateOf("") }
                var showMenu by remember { mutableStateOf(false) }
                if (isSearching) {
                    TextField(
                        value = searchText,
                        onValueChange = {
                            searchText = it
                        },
                        placeholder = {
                            Text(text = "Search", color = colorResource(id = R.color.light_blue))
                        },
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
                        if (isSearching) {
                            IconButton(onClick = {
                                isSearching = false
                                searchText = ""
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.cross),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = colorResource(id = R.color.light_blue)
                                )
                            }
                        }else{
                            IconButton(onClick = {
                                isSearching = true
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.search),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = colorResource(id = R.color.light_blue)
                                )
                            }


                        }

                        IconButton(onClick = {
                            showMenu = !showMenu
                        }) {
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
                            ){
                                DropdownMenuItem(text={ Text(text = "New Group",color=Color.DarkGray)}, onClick = {showMenu=false})

                                DropdownMenuItem(text={ Text(text = "New Broadcast",color=Color.DarkGray)}, onClick = {showMenu=false})

                                DropdownMenuItem(text={ Text(text = "Linked Devices",color=Color.DarkGray)}, onClick = {showMenu=false})

                                DropdownMenuItem(text={ Text(text = "Starred Messages",color=Color.DarkGray)}, onClick = {showMenu=false})

                                DropdownMenuItem(text={ Text(text = "Settings",color=Color.DarkGray)}, onClick = {showMenu=false

                                navHostController.navigate(Routes.SettingScreen)

                                })

                            }

                        }
                    }

                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()

            Spacer(modifier = Modifier.height(12.dp))
            if(showPopup){
                AddUserPopup(onDismiss ={showPopup=false}, onUserAdd = {newUser->
                    homeBaseViewModel.addChat(newUser)
                },baseViewModel = homeBaseViewModel)
            }

            LazyColumn {
                items(chatData) { chat->
                    ChatDesign(chatDesignModel = chat, onClick = {
                        navHostController.navigate(Routes.ChatScreen.createRoutes(phoneNumber = chat.phoneNumber?:"ok"))

                    },baseViewModel=homeBaseViewModel)
                    }
                }
            }

                }
            }

@Composable
fun AddUserPopup(
    onDismiss: () -> Unit,
    onUserAdd:(ChatDesignModel)->Unit,
    baseViewModel: BaseViewModel
) {
    var phoneNumber by remember { mutableStateOf("") }

    var isSearching by remember { mutableStateOf(false) }

    var userFound by remember { mutableStateOf<ChatDesignModel?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(color = colorResource(id = R.color.dark_blue))
    ) {
      TextField(
          value = phoneNumber,
          onValueChange = { phoneNumber=it},
          label = {Text("Enter Phone Number")},
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          colors = TextFieldDefaults.colors(
              unfocusedPlaceholderColor =colorResource(id=R.color.light_blue),
              focusedPlaceholderColor =colorResource(id=R.color.light_blue),
              unfocusedContainerColor =colorResource(id=R.color.dark_blue),
              focusedContainerColor =colorResource(id=R.color.dark_blue),
              unfocusedIndicatorColor=colorResource(id=R.color.light_blue),
              focusedIndicatorColor=colorResource(id=R.color.light_blue),
              unfocusedTextColor=colorResource(id=R.color.light_blue),
              focusedTextColor=colorResource(id=R.color.light_blue)
      )
      )
        Row{
            Button(onClick = {
                isSearching=true
                baseViewModel.searchUserByPhoneNumber(phoneNumber) { user ->
                    isSearching = false

                    if (user != null) {
                        userFound = user
                    } else {
                        userFound = null
                    }
                }
            }, modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(colorResource(R.color.light_blue))
            ){
                Text(text = "Search")
            }
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(colorResource(R.color.light_blue))
            )
                {
                    Text(text = "Cancel")
                }
            }
        if(isSearching){
            Text(text = "Searching...",color=Color.Gray)
        }
        userFound?.let{
            Column {
                Text(text = "User Found ${it.name}")
                Button(onClick = {
                    onUserAdd(it)
                    onDismiss()
                }, colors = ButtonDefaults.buttonColors(colorResource(R.color.light_blue))) {
                    Text(text = "Add to Chats")
                }

            }
        }?:run{
            if(!isSearching){
                Text(text = "No User Found With This Number",color=Color.Gray)
            }
        }

        }


    }







