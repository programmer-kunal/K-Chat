package com.example.kchat.presentation.communitiesscreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.kchat.R
import com.example.kchat.presentation.components.TopBar
import com.example.kchat.presentation.navigation.Routes
import com.example.kchat.presentation.splashscreen.bottomnavigation.BottomNavigation

@Composable
fun CommunitiesScreen(navHostController: NavHostController) {
    val sampleCommunities = listOf(
        Communities(image = R.drawable.img, name = "Tech Enthusiasts", members = "123 members"),
        Communities(image = R.drawable.img, name = "Photography Lovers", members = "223 members"),
        Communities(image = R.drawable.img, name = "Software Developers", members = "13 members"),
        Communities(image = R.drawable.img, name = "Hackers", members = "122 members"),
        Communities(image = R.drawable.img, name = "Life Savers", members = "99 members"),
        Communities(image = R.drawable.img, name = "Indie Developers", members = "173 members"),
        Communities(image = R.drawable.img, name = "Music Lovers", members = "133 members"),
        Communities(image = R.drawable.img, name = "UI/UX Designers", members = "132 members"),
        Communities(image = R.drawable.img, name = "Travel Explorers", members = "155 members"),
    )

    Scaffold(
        containerColor = colorResource(id = R.color.dark_blue),
        topBar = { TopBar("Communities") }, // ✅ Use reusable TopBar
        bottomBar = {
            BottomNavigation(
                navHostController,
                selectedItem = 2, // ✅ highlight Communities tab
                onClick = { index ->
                    when (index) {
                        0 -> navHostController.navigate(Routes.HomeScreen)
                        1 -> navHostController.navigate(Routes.UpdateScreen)
                        2 -> navHostController.navigate(Routes.CommunitiesScreen)
                        3 -> navHostController.navigate(Routes.CallScreen)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            Button(
                onClick = { /*TODO*/ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.light_blue)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Start a new Community",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your Communities",
                fontSize = 20.sp,
                color = colorResource(id = R.color.light_blue),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp)
            )

            LazyColumn {
                items(sampleCommunities) {
                    CommunityItemDesign(communities = it)
                }
            }
        }
    }
}
