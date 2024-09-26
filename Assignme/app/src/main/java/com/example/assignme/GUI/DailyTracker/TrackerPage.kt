package com.example.assignme.GUI.DailyTracker

import android.annotation.SuppressLint
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
import androidx.compose.ui.Alignment
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
import com.example.assignme.DataClass.TrackerRecord
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerPage(
    navController: NavController,
    userViewModel: UserViewModel,
    trackerViewModel: TrackerViewModel
) {
    val userId by userViewModel.userId.observeAsState()
    val currentWaterIntake by trackerViewModel.currentWaterIntake.observeAsState(0)

    LaunchedEffect(userId) {
        userId?.let { id ->
            userViewModel.fetchTrackerData(id) { trackerData ->
                if (trackerData == null || trackerData.weight.isBlank() || trackerData.height.isBlank() || trackerData.bodyImageUri == null) {
                    navController.navigate("setup_info_page")
                }
            }
        }
    }

    var weight by remember { mutableStateOf("0") }
    var calories by remember { mutableStateOf(0) }

    val weightHistory by trackerViewModel.weightHistory.observeAsState(emptyList())
    val calorieHistory by trackerViewModel.calorieHistory.observeAsState(emptyList())

    var weightUpdateMessage by remember { mutableStateOf("") }
    var calorieAddMessage by remember { mutableStateOf("") }
    var waterIntakeMessage by remember { mutableStateOf("") }

    val context = LocalContext.current

    // Display toast messages for updates
    LaunchedEffect(weightUpdateMessage) {
        if (weightUpdateMessage.isNotEmpty()) {
            Toast.makeText(context, weightUpdateMessage, Toast.LENGTH_SHORT).show()
            weightUpdateMessage = ""
        }
    }

    LaunchedEffect(calorieAddMessage) {
        if (calorieAddMessage.isNotEmpty()) {
            Toast.makeText(context, calorieAddMessage, Toast.LENGTH_SHORT).show()
            calorieAddMessage = ""
        }
    }

    LaunchedEffect(waterIntakeMessage) {
        if (waterIntakeMessage.isNotEmpty()) {
            Toast.makeText(context, waterIntakeMessage, Toast.LENGTH_SHORT).show()
            waterIntakeMessage = ""
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
            // Weight Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Title Row
                    Text(text = "Weight", style = MaterialTheme.typography.headlineSmall)

                    // Row for weight value and chart
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Column for weight value
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(text = "$weight Kg", style = MaterialTheme.typography.headlineMedium)
                        }

                        // Right Column for Weight Chart
                        Box(
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .size(height = 100.dp, width = 150.dp) // Set height and width here
                        ) {
                            WeightChart(weightHistory)
                        }
                    }

                    // Last Row for Text Box and Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextField(
                            value = weight,
                            onValueChange = { weight = it },
                            label = { Text("Today's weight") },
                            modifier = Modifier.weight(1f)
                        )
                        Button(onClick = {
                            if (weight.isNotEmpty()) {
                                val currentDate = LocalDate.now()
                                trackerViewModel.updateWeight(currentDate, weight.toFloat())
                                weightUpdateMessage = "Successfully updated weight to $weight kg."
                            }
                        }) {
                            Text("Update")
                        }
                    }
                }
            }

            // Water Intake Section
            WaterIntakeSection(
                currentWaterIntake = currentWaterIntake,
                onAddWaterClick = {
                    val currentDate = LocalDate.now()
                    trackerViewModel.addWaterIntake(currentDate)
                    waterIntakeMessage = "Water intake updated."
                }
            )

            // Calories Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Title Row
                    Text(text = "Calories", style = MaterialTheme.typography.headlineSmall)

                    // Row for calorie value and chart
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Column for calorie value
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(text = "$calories Kcal", style = MaterialTheme.typography.headlineMedium)
                        }

                        // Right Column for Calories Chart
                        Box(
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .size(height = 100.dp, width = 150.dp) // Set height and width here
                        ) {
                            CaloriesChart(calorieHistory)
                        }
                    }

                    // Last Row for Text Box and Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextField(
                            value = calories.toString(),
                            onValueChange = { calories = it.toIntOrNull() ?: 0 },
                            label = { Text("Today's calories") },
                            modifier = Modifier.weight(1f)
                        )
                        Button(onClick = {
                            if (calories > 0) {
                                val currentDate = LocalDate.now()
                                trackerViewModel.addCalories(currentDate, calories.toFloat())
                                calorieAddMessage = "Successfully added $calories kcal."
                            }
                        }) {
                            Text("Add")
                        }
                    }
                }
            }

            // Daily Analysis Button
            Button(
                onClick = {
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeightChart(weightHistory: List<TrackerRecord>) {
    // Prepare the chart data as pairs of (x: day of month, y: weight)
    val chartData = weightHistory.map { it.date.dayOfMonth.toFloat() to it.weight }

    // Log the chart data
    Log.d("WeightChart", "Entries: ${chartData.size}, Data: $chartData")

    // Create the entry model
    val chartEntryModel = entryModelOf(*chartData.toTypedArray())

    Chart(
        chart = lineChart(),
        model = chartEntryModel,
        startAxis = startAxis(
            title = "Weight (kg)",
            valueFormatter = { value, _ -> value.toString() }
        ),
        bottomAxis = bottomAxis(
            title = "Day of Month",
            valueFormatter = { value, _ -> value.toInt().toString() }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CaloriesChart(calorieHistory: List<TrackerRecord>) {
    // Prepare the chart data as pairs of (x: day of month, y: calories intake)
    val chartData = calorieHistory.map { it.date.dayOfMonth.toFloat() to it.caloriesIntake.toFloat() }

    // Log the chart data
    Log.d("CaloriesChart", "Entries: ${chartData.size}, Data: $chartData")

    // Create the entry model
    val chartEntryModel = entryModelOf(*chartData.toTypedArray())

    Chart(
        chart = columnChart(),
        model = chartEntryModel,
        startAxis = startAxis(
            title = "Calories",
            valueFormatter = { value, _ -> value.toInt().toString() }
        ),
        bottomAxis = bottomAxis(
            title = "Day of Month",
            valueFormatter = { value, _ -> value.toInt().toString() }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    )
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

