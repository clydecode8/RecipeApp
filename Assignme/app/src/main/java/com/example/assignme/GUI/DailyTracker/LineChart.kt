package com.example.assignme.GUI.DailyTracker

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
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
fun LineChart(
    navController: NavController,
    userViewModel: UserViewModel,
    trackerViewModel: TrackerViewModel
) {
    // Fetch weight history here or pass it from the previous screen if needed
    val weightHistory by trackerViewModel.weightHistory.observeAsState(emptyList())

    Scaffold(
        topBar = { AppTopBar(title = "Weight Chart", navController = navController) },
        bottomBar = { AppBottomNavigation(navController = navController) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background) // Set background color
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp), // Add padding around the column
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "Your Weight Over Dates",
                    color = Color.Black,
                    fontSize = 20.sp, // Set title font size
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp) // Spacing below the title
                )

                // Full line chart display
                WeightChart(weightHistory, lineColor = Color.Blue)

                // Space between chart and description
                Spacer(modifier = Modifier.height(16.dp))

                // Additional descriptive text below the chart
                Text(
                    text = "This chart shows your weight trends for the month.",
                    color = Color.Gray,
                    fontSize = 14.sp, // Set description font size
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
