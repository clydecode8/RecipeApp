package com.example.assignme.GUI.DailyTracker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.assignme.AndroidBar.AppBottomNavigation
import com.example.assignme.AndroidBar.AppTopBar
import com.example.assignme.ViewModel.UserViewModel
import com.google.firebase.database.FirebaseDatabase

import androidx.compose.runtime.livedata.observeAsState

@Composable
fun TrackerPage(navController: NavController, userViewModel: UserViewModel) {
    // Observe userId as state
    val userId by userViewModel.userId.observeAsState()

    // Check user data if userId is available
    LaunchedEffect(userId) {
        userId?.let { id ->
            userViewModel.fetchTrackerData(id) { trackerData ->
                // Navigate to SetUpInfo if any of the fields are blank or null
                if (trackerData == null || trackerData.weight.isBlank() || trackerData.height.isBlank() || trackerData.bodyImageUri == null) {
                    navController.navigate("setup_info_page")
                }
            }
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = "Daily Tracker", navController = navController) },
        bottomBar = { AppBottomNavigation(navController = navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WeightSection(navController)
            WaterIntakeSection()
            CaloriesSection()

            Button(
                onClick = { navController.navigate("daily_analysis") },
                modifier = Modifier.fillMaxWidth()
                    .padding(top = 5.dp) // Optional additional padding
            ) {
                Text("Daily Analysis")
            }
        }
    }
}


@Composable
fun WeightSection(navController: NavController) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Weight", fontWeight = FontWeight.Bold)
            }
            Text("0 kg", fontSize = 36.sp, fontWeight = FontWeight.Bold)
            Text("kg", color = Color.Gray)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    label = { Text("Today's weight") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Update")
                }
            }
        }
    }
}

@Composable
fun WaterIntakeSection() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Water Intake", fontWeight = FontWeight.Bold)
            }
            Text("0 ml water (0 Glass)")
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add water")
            }
        }
    }
}

@Composable
fun CaloriesSection() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Calories", fontWeight = FontWeight.Bold)
            }
            Text("0 kcal", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("kcal")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    label = { Text("Today's calories") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                ) {
                    Text("Add")
                }
            }
        }
    }
}
