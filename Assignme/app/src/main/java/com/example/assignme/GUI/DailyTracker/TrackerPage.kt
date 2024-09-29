package com.example.assignme.GUI.DailyTracker

import android.content.ContentValues.TAG
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color // Add this line
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.assignme.AndroidBar.AppBottomNavigation
import com.example.assignme.AndroidBar.AppTopBar
import com.example.assignme.ViewModel.TrackerViewModel
import com.example.assignme.ViewModel.UserViewModel
import java.time.LocalDate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.example.assignme.DataClass.TrackerRecord
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import java.time.YearMonth

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.core.chart.column.ColumnChart
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.text.VerticalPosition
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.formatter.DecimalFormatValueFormatter


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TrackerPage(
    navController: NavController,
    userViewModel: UserViewModel,
    trackerViewModel: TrackerViewModel
) {
    val userId by userViewModel.userId.observeAsState()
    val currentWaterIntake by trackerViewModel.currentWaterIntake.observeAsState(0)
    val currentDate = LocalDate.now()

    LaunchedEffect(userId) {
        userId?.let { id ->
            userViewModel.fetchTrackerData(id) { trackerData ->
                if (trackerData == null || trackerData.weight.isBlank() || trackerData.height.isBlank() || trackerData.bodyImageUri == null) {
                    navController.navigate("setup_info_page")
                }
            }
        }
    }
    LaunchedEffect(currentDate) {
        trackerViewModel.fetchWaterIntakeForDate(currentDate) // Fetch water intake for today
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Weight Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(
//                        containerColor = Color(0xFFBBBABA),  // Light blue background
//                        contentColor = Color.Black           // Content (text, icons) color
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Title Row
                        Text(
                            text = "Weight",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = MaterialTheme.typography.headlineSmall.fontSize // Keep the same size
                            )
                        )
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

                            // Right Column for Weight Chart (Clickable)
                            Box(
                                modifier = Modifier
                                    .padding(start = 16.dp)
                                    .size(height = 100.dp, width = 200.dp)
                                    .clickable { navController.navigate("lineChart_page") } // Navigate to line chart
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
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number // Set keyboard type to number
                                ),
                                visualTransformation = VisualTransformation.None // Optionally, disable any transformation
                            )
                            Button(
                                onClick = {
                                    if (weight.isNotEmpty()) {
                                        trackerViewModel.updateWeight(currentDate, weight.toFloat())
                                        weightUpdateMessage = "Successfully updated weight to $weight kg."
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE23E3E)) // Change color as needed
                            ) {
                                Text("Update")
                            }
                        }
                    }
                }
            }

            // Water Intake Section
            item {
                WaterIntakeSection(
                    currentWaterIntake = currentWaterIntake,
                    onAddWaterClick = {
                        trackerViewModel.addWaterIntake(currentDate)
                        waterIntakeMessage = "Water intake updated."
                    }
                )
            }

            // Calories Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(
//                        containerColor = Color(0xFFBBBABA),  // Light blue background
//                        contentColor = Color.Black           // Content (text, icons) color
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Title Row
                        Text(
                            text = "Calories",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = MaterialTheme.typography.headlineSmall.fontSize // Keep the same size
                            )
                        )

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

                            // Right Column for Calories Chart (Clickable)
                            Box(
                                modifier = Modifier
                                    .padding(start = 16.dp)
                                    .size(height = 100.dp, width = 200.dp)
                                    .clickable { navController.navigate("barChart_page") } // Navigate to bar chart
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
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number // Set keyboard type to number
                                ),
                                visualTransformation = VisualTransformation.None // Optionally, disable any transformation
                            )
                            Button(
                                onClick = {
                                    if (calories > 0) {
                                        trackerViewModel.addCalories(currentDate, calories.toFloat())
                                        calorieAddMessage = "Successfully added $calories kcal."
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE23E3E)) // Change color as needed
                            ) {
                                Text("Add")
                            }
                        }
                    }
                }
            }

            // Daily Analysis and Body Comparison Buttons Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween, // Space buttons to opposite ends
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            navController.navigate("transformation_page")
                            Log.d("TrackerPage", "Body Comparison button clicked")
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE23E3E)) // Change color as needed
                    ) {
                        Text("Transformations")
                    }

                    Spacer(modifier = Modifier.width(16.dp)) // Optional spacing between buttons

                    // Daily Analysis Button
                    Button(
                        onClick = {
                            navController.navigate("daily_analysis")
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE23E3E)) // Use the colors parameter
                    ) {
                        Text("Daily Analysis")
                    }
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
        modifier = Modifier
            .fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
//            containerColor = Color(0xFFBBBABA),  // Light blue background
//            contentColor = Color.Black           // Content (text, icons) color
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title Row
            Text(
                text = "Water Intake",
                fontWeight = FontWeight.Bold, // Make the title bold
                fontSize = MaterialTheme.typography.titleLarge.fontSize // Keep the same font size
            )
            Text("${currentWaterIntake * 100}ml water (${currentWaterIntake} Glass)")
        }

        Button(
                onClick = onAddWaterClick, // Use the normal function here
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE23E3E)) // Set the button color
            ) {
                Text("Add water")
            }
        }
    }

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeightChart(weightHistory: List<TrackerRecord>, lineColor: Color = Color.Blue) {
    // Get the current YearMonth and number of days in the current month
    val currentMonth = YearMonth.now()
    val daysInMonth = currentMonth.lengthOfMonth()

    // Create a map for easy lookup of weight by day
    val weightByDay = weightHistory.associate { it.date.dayOfMonth to it.weight }

    // Fill the missing days with default values (e.g., 0f if no data for that day)
    val filledWeightData = (1..daysInMonth).map { day ->
        day.toFloat() to (weightByDay[day] ?: 0f) // x-axis is day, y-axis is weight
    }

    // Log the filled weight data for debugging
    Log.d("WeightChart", "Entries: ${filledWeightData.size}, Data: $filledWeightData")

    // Create the entry model with the filled data
    val chartEntryModel = entryModelOf(*filledWeightData.toTypedArray())

    // Define the line specification with the given line color
    val lineSpec = LineChart.LineSpec(
        lineColor = lineColor.toArgb() // Convert Color to Int if needed
    )

    // Render the line chart with the start and bottom axes
    Chart(
        chart = lineChart(
            lines = listOf(lineSpec) // Pass the line specification
        ),
        model = chartEntryModel,
        // Inside your WeightChart function definition
        startAxis = startAxis(
            title = "Weight (kg)",
            valueFormatter = { value, _ -> value.toInt().toString() } // Ensure integer formatting
        ),
        bottomAxis = bottomAxis(
            title = "Day of Month", // X-axis: Day of month
            valueFormatter = { value, _ -> value.toInt().toString() } // Format X-axis values
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

    // Define the line components for the columns
    val lineComponent = lineComponent(
        color = Color.Blue // Customize your color here
    )
    // Create the ColumnChart with smaller bar widths
    val columnChart = ColumnChart(
        columns = listOf(lineComponent),
        spacingDp = 1f, // Reduce distance between neighboring column collections
        innerSpacingDp = 2f, // Reduce distance between grouped columns
        mergeMode = ColumnChart.MergeMode.Grouped
    )
    // Render the chart with customized axes
    Chart(
        chart = columnChart,
        model = chartEntryModel,
        startAxis = startAxis(
            title = "Calories", // Title for the Y-axis
            valueFormatter = { value, _ -> value.toInt().toString() } // Format for Y-axis labels
        ),
        bottomAxis = bottomAxis(
            title = "Day of Month", // Title for the X-axis
            valueFormatter = { value, _ -> value.toInt().toString() } // Format for X-axis labels
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    )
}