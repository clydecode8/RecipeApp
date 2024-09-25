package com.example.assignme.GUI.DailyTracker

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.assignme.AndroidBar.AppBottomNavigation
import com.example.assignme.AndroidBar.AppTopBar
import com.example.assignme.ViewModel.TrackerViewModel
import com.example.assignme.ViewModel.UserViewModel
import java.time.LocalDate
import androidx.compose.ui.platform.LocalContext

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerPage(
    navController: NavController,
    userViewModel: UserViewModel,
    trackerViewModel: TrackerViewModel
) {
    // Observe userId and current water intake as state
    val userId by userViewModel.userId.observeAsState()
    val currentWaterIntake by trackerViewModel.currentWaterIntake.observeAsState(0)

    // Fetch user data based on the userId
    LaunchedEffect(userId) {
        Log.d("TrackerPage", "Fetching tracker data for userId: $userId")
        userId?.let { id ->
            userViewModel.fetchTrackerData(id) { trackerData ->
                if (trackerData == null || trackerData.weight.isBlank() || trackerData.height.isBlank() || trackerData.bodyImageUri == null) {
                    Log.w("TrackerPage", "User data incomplete, navigating to setup_info_page")
                    navController.navigate("setup_info_page")
                } else {
                    Log.d("TrackerPage", "Tracker data fetched successfully: $trackerData")
                }
            }
        }
    }

    // State variables to handle live updates
    var weight by remember { mutableStateOf("0") }
    var calories by remember { mutableStateOf(0) }

    // State variables for success messages
    var weightUpdateMessage by remember { mutableStateOf("") }
    var calorieAddMessage by remember { mutableStateOf("") }
    var waterIntakeMessage by remember { mutableStateOf("") }

    // Get the LocalContext
    val context = LocalContext.current

    // Toast message effects
    LaunchedEffect(weightUpdateMessage) {
        if (weightUpdateMessage.isNotEmpty()) {
            Toast.makeText(context, weightUpdateMessage, Toast.LENGTH_SHORT).show()
            weightUpdateMessage = "" // Reset the message
        }
    }

    LaunchedEffect(calorieAddMessage) {
        if (calorieAddMessage.isNotEmpty()) {
            Toast.makeText(context, calorieAddMessage, Toast.LENGTH_SHORT).show()
            calorieAddMessage = "" // Reset the message
        }
    }

    LaunchedEffect(waterIntakeMessage) {
        if (waterIntakeMessage.isNotEmpty()) {
            Toast.makeText(context, waterIntakeMessage, Toast.LENGTH_SHORT).show()
            waterIntakeMessage = "" // Reset the message
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
            WeightSection(
                weight = weight,
                onWeightChange = { weight = it },
                onUpdateClick = {
                    if (weight.isNotEmpty()) {
                        val currentDate = LocalDate.now()
                        Log.d("TrackerPage", "Updating weight to $weight for date $currentDate")
                        trackerViewModel.updateWeight(currentDate, weight.toFloat())
                        weightUpdateMessage = "Successfully updated weight to $weight kg."
                    } else {
                        Log.w("TrackerPage", "Weight input is empty, cannot update.")
                    }
                }
            )

            WaterIntakeSection(
                currentWaterIntake = currentWaterIntake,
                onAddWaterClick = {
                    val currentDate = LocalDate.now()
                    Log.d("TrackerPage", "Adding water intake for date $currentDate")
                    trackerViewModel.addWaterIntake(currentDate)
                    waterIntakeMessage = "Water intake updated."
                }
            )

            CaloriesSection(
                calories = calories,
                onCaloriesChange = { calories = it.toIntOrNull() ?: 0 },
                onAddClick = {
                    if (calories > 0) {
                        val currentDate = LocalDate.now()
                        Log.d("TrackerPage", "Adding $calories calories for date $currentDate")
                        trackerViewModel.addCalories(currentDate, calories.toFloat())
                        calorieAddMessage = "Successfully added $calories kcal."
                    } else {
                        Log.w("TrackerPage", "Calories input is invalid (<= 0), cannot add.")
                    }
                }
            )

            // Daily Analysis Button
            Button(
                onClick = {
                    Log.d("TrackerPage", "Navigating to Daily Analysis")
                    navController.navigate("daily_analysis")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("Daily Analysis")
            }
        }
    }
}

@Composable
fun WeightSection(
    weight: String,
    onWeightChange: (String) -> Unit,
    onUpdateClick: () -> Unit // Change this to a regular function type
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Weight", fontWeight = MaterialTheme.typography.titleLarge.fontWeight)
            Text(weight, style = MaterialTheme.typography.displayLarge)
            Text("kg", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))

            // Text input for weight
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = onWeightChange,
                    label = { Text("Today's weight") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onUpdateClick, // This will now accept the correct type
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Update")
                }
            }
        }
    }
}

@Composable
fun WaterIntakeSection(
    currentWaterIntake: Int,
    onAddWaterClick: () -> Unit // Keep this as a normal function
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Water Intake", fontWeight = MaterialTheme.typography.titleLarge.fontWeight)
            Text("${currentWaterIntake * 100}ml water (${currentWaterIntake} Glass)")

            Button(
                onClick = onAddWaterClick, // Use the normal function here
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add water")
            }
        }
    }
}

@Composable
fun CaloriesSection(
    calories: Int,
    onCaloriesChange: (String) -> Unit,
    onAddClick: () -> Unit // Keep this as a normal function
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Calories", fontWeight = MaterialTheme.typography.titleLarge.fontWeight)
            Text("$calories", style = MaterialTheme.typography.displaySmall)
            Text("kcal")

            // Text input for calories
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedTextField(
                    value = calories.toString(),
                    onValueChange = onCaloriesChange,
                    label = { Text("Today's calories") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onAddClick, // Use the normal function here
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Add")
                }
            }
        }
    }
}
