package com.example.assignme.GUI.AccountProfile

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.darkColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.lightColors
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.assignme.R
import com.example.assignme.ViewModel.MockUserViewModel
import com.example.assignme.ViewModel.UserProfileProvider
import com.example.assignme.ViewModel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(navController: NavController, userViewModel: UserProfileProvider) {

    val colors = if (isSystemInDarkTheme()) {
        darkColors()
    } else {
        lightColors()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { },
                actions = {
                    IconButton(onClick = { /* Handle action */ }) {
                        Icon(
                            imageVector = Icons.Rounded.Favorite,
                            contentDescription = "Favorite",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding()
                .statusBarsPadding()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ProfileSection()
            AdminActions(navController)

            Text(text = "Menu Button End", color = Color.Black)
        }
    }
}

@Composable
fun ProfileSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.profile),
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Text(
            text = "Welcome Admin Ocelot,",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

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

    AdminDashboard(
        navController = rememberNavController(),
        userViewModel = MockUserViewModel()
    )
}