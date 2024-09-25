package com.example.assignme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.assignme.ViewModel.UserViewModel
import com.example.assignme.ui.theme.AssignmeTheme
import com.google.firebase.FirebaseApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        setContent {
            AssignmeTheme {

                //Create a navigation
                val navController = rememberNavController()
                val userViewModel: UserViewModel = viewModel() // Use Hilt or manually provide the ViewModel

                Scaffold(modifier = Modifier.fillMaxSize()) {
                    innerPadding -> NavigationGraph(navController, userViewModel)
                }
            }
        }
    }
}


