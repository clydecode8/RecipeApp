package com.example.assignme.GUI.AccountProfile

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.example.assignme.R
import com.example.assignme.ViewModel.AdminProfile
import com.example.assignme.ViewModel.MockThemeViewModel
import com.example.assignme.ViewModel.MockUserViewModel
import com.example.assignme.ViewModel.ThemeInterface
import com.example.assignme.ViewModel.ThemeViewModel
import com.example.assignme.ViewModel.UserProfile
import com.example.assignme.ViewModel.UserProfileProvider
import com.example.assignme.ViewModel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(userViewModel: UserProfileProvider, navController: NavController, themeViewModel: ThemeViewModel) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Admin Page") }, // Add a title
                actions = {
                    IconButton(onClick = { /* Handle settings action */ }) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding()
                .statusBarsPadding()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                ProfileSection(navController, userViewModel)
            }
            item {
                //AdminActions(navController)
            }
            item {
                //Text(text = "Menu Button End", color = Color.Black)
                val userId by userViewModel.userId.observeAsState()
                // Ensure you observe the LiveData correctly
                val prof by userViewModel.adminProfile.observeAsState(initial = AdminProfile())
                userId?.let {

                    Log.d("AdminPage", "Current profile name: ${prof.name}")

// Check if the profile name is null and fetch if needed
                    if (prof.name == null) {
                        println("Fetching admin profile...")
                        userViewModel.fetchAdminProfile(it)
                    }

// Column for profile picture and name
                    Column(
                        modifier = Modifier
                             // Add space between Column and button
                    ) {
                        // Display profile picture if available
                        if (!prof.profilePictureUrl.isNullOrBlank()) {
                            Image(
                                painter = rememberImagePainter(prof.profilePictureUrl),
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Display default image if profilePictureUrl is null
                            Image(
                                painter = painterResource(id = R.drawable.google),
                                contentDescription = "Default Profile Picture",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }

                        // Display the name
                        Text(
                            text = "No Name Available", // Fallback text if name is null
                            fontSize = 18.sp,
                            modifier = Modifier.padding(top = 8.dp) // Add space between picture and name
                        )
                    }
                    Text(
                        text = "No Name Available", // Fallback text if name is null
                        fontSize = 18.sp,
                        modifier = Modifier.padding(top = 8.dp) // Add space between picture and name
                    )

                    AdminActions(navController = navController)
                }
            }
        }
    }
}

@Composable
fun AdminActions(navController: NavController) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ActionButton(text = "Manage Post", onClick = { navController.navigate("manage_post") })
        ActionButton(text = "Approve Recipe", onClick = { navController.navigate("approve_recipe") })
        ActionButton(text = "Add Admin", onClick = { navController.navigate("add_admin") })
    }
}

@Composable
fun ProfileSection(navController: NavController, userViewModel: UserProfileProvider) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {



    }

    Spacer(modifier = Modifier.height(16.dp)) // Space below the button

    // Divider section with "or login with"
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        HorizontalDivider(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp),
            thickness = 1.dp,
            color = Color.Gray
        )
    }

}

@Composable
fun ActionButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = text, style = MaterialTheme.typography.titleMedium)
        }
    }

    // Divider section
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        HorizontalDivider(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 2.dp),
            thickness = 1.dp,
            color = Color.Gray
        )
    }
}



@Preview(showBackground = true)
@Composable
fun PreviewAdminDashboard() {

    val mockThemeViewModel = MockThemeViewModel()
    AdminDashboard(
        navController = rememberNavController(),
        userViewModel = MockUserViewModel(),
        themeViewModel = mockThemeViewModel
    )
}