package com.example.assignme.GUI.DailyTracker

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.assignme.AndroidBar.AppBottomNavigation
import com.example.assignme.AndroidBar.AppTopBar
import com.example.assignme.ViewModel.TrackerViewModel
import com.example.assignme.ViewModel.UserViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BarChart(
    navController: NavController,
    trackerViewModel: TrackerViewModel
) {
    // Fetch calorie history here or pass it from the previous screen if needed
    val calorieHistory by trackerViewModel.calorieHistory.observeAsState(emptyList())

    Scaffold(
        topBar = { AppTopBar(title = "Calories Chart", navController = navController) },
        bottomBar = { AppBottomNavigation(navController = navController) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp), // Add padding around the column
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "Your Calorie Intake Over Dates",
                    color = Color.Black,
                    fontSize = 20.sp, // Set title font size
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp) // Spacing below the title
                )

                // Box or Card for the chart
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp) // Adjust height as needed
                        .background(Color(0xFFBBBABA), shape = RoundedCornerShape(8.dp)) // Use the specified color with rounded corners
                        .padding(16.dp) // Padding inside the box
                ) {
                    // Render the Calories Chart
                    CaloriesChart(calorieHistory)
                }

                // Space between chart and description
                Spacer(modifier = Modifier.height(16.dp))

                // Additional descriptive text below the chart
                Text(
                    text = "This chart shows your calorie intake for the month.",
                    color = Color.Gray,
                    fontSize = 14.sp, // Set description font size
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}


