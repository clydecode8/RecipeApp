package com.example.assignme.GUI.DailyTracker

import android.os.Build
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
import com.example.assignme.DataClass.TrackerRecord
import com.example.assignme.ViewModel.TrackerViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyAnalysis(
    navController: NavController,
    trackerViewModel: TrackerViewModel
) {
    // Observe all entries from the ViewModel using LiveData
    val allEntries by trackerViewModel.allEntries.observeAsState(emptyList())

    // State variables for search
    var searchDate by remember { mutableStateOf("") }
    var filteredEntries by remember { mutableStateOf(allEntries) }

    // Update filtered entries whenever allEntries changes
    LaunchedEffect(allEntries) {
        filteredEntries = allEntries
    }

    // Update filtered entries when searchDate changes
    LaunchedEffect(searchDate) {
        if (searchDate.isEmpty()) {
            filteredEntries = allEntries
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = "Daily Analysis", navController = navController) },
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
            // Search Bar
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedTextField(
                    value = searchDate,
                    onValueChange = { searchDate = it },
                    label = { Text("Enter date (yyyy-MM-dd)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        // Filter entries based on the input date
                        if (searchDate.isNotEmpty()) {
                            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                            filteredEntries = try {
                                val date = LocalDate.parse(searchDate, dateFormatter)
                                allEntries.filter { it.date == date }
                            } catch (e: DateTimeParseException) {
                                // If the input is invalid, reset to all entries
                                allEntries
                            }
                        } else {
                            // Reset to all entries if search date is empty
                            filteredEntries = allEntries
                        }
                    }
                ) {
                    Text("Search")
                }
            }

            // Header Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Date", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text("Weight (kg)", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text("Water Intake (glasses)", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text("Calories (kcal)", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            }

            // Divider
            Divider()

            // Display filtered recorded data
            if (filteredEntries.isNotEmpty()) {
                filteredEntries.forEach { entry ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(entry.date.toString(), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Text(entry.weight.toString(), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Text(entry.waterIntake.toString(), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Text(entry.caloriesIntake.toString(), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    }
                }
            } else {
                Text("No entries recorded yet.", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

