package com.example.assignme

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable


@Composable
fun NavigationGraph(navController: NavHostController){

    NavHost(

        navController = navController,
        startDestination = "main_page"
    ){

        composable("main_page"){

            MainPage(navController)
        }

        composable("first_page"){

            FirstPage(navController)
        }

        composable("login_page"){

            LoginPage(navController)
        }

        composable("register_page"){

            RegisterPage(navController)
        }

    }

}

