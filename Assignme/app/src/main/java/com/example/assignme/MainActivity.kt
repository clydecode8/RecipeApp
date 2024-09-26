package com.example.assignme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.assignme.ViewModel.ThemeInterface
import com.example.assignme.ViewModel.ThemePreference
import com.example.assignme.ViewModel.ThemeViewModel
import com.example.assignme.ViewModel.UserViewModel
import com.example.assignme.ui.theme.AssignmeTheme
import com.google.firebase.FirebaseApp


class MainActivity : ComponentActivity() {

    private lateinit var themeViewModel: ThemeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ThemePreference and ThemeViewModel
        val themePreference = ThemePreference(this) // Pass context to ThemePreference
        themeViewModel = ThemeViewModel(themePreference) // Pass the ThemePreference to ThemeViewModel

        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()

        setContent {
            // Track the current theme state
            val currentTheme by remember { mutableStateOf(themeViewModel.isDarkTheme.value) }

            // This should listen for changes in the themeViewModel
            // Update the theme dynamically if the theme changes
            themeViewModel.isDarkTheme.value.let {
                AssignmeTheme(darkTheme = it) {
                    // Create a navigation
                    val navController = rememberNavController()
                    val userViewModel: UserViewModel = viewModel() // Use Hilt or manually provide the ViewModel
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        NavigationGraph(navController, userViewModel, themeViewModel = themeViewModel)
                    }
                }
            }
        }
    }
}

