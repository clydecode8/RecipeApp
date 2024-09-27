package com.example.assignme.GUI.DailyTracker

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.assignme.AndroidBar.AppBottomNavigation
import com.example.assignme.AndroidBar.AppTopBar
import com.example.assignme.ViewModel.TrackerViewModel
import com.example.assignme.ViewModel.UserViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Transformation(
    navController: NavController,
    userViewModel: UserViewModel,
    trackerViewModel: TrackerViewModel
) {
    Scaffold(
        topBar = { AppTopBar(title = "Transformations", navController = navController) },
        bottomBar = { AppBottomNavigation(navController = navController) }
    ) { paddingValues ->
        // Main content goes here (currently empty)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // You can add any specific content here if needed
            Text("Transformation Content", modifier = Modifier.align(Alignment.Center))
        }
    }
}
