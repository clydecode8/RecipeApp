package com.example.assignme

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.assignme.GUI.AccountProfile.AddAdmin
import com.example.assignme.GUI.AccountProfile.AdminDashboard
import com.example.assignme.GUI.AccountProfile.AppFirstPage
import com.example.assignme.GUI.AccountProfile.EditProfileScreen
import com.example.assignme.GUI.FirstPage
import com.example.assignme.GUI.AccountProfile.ForgotPasswordPage
import com.example.assignme.GUI.AccountProfile.LoginPage
import com.example.assignme.GUI.AccountProfile.ProfilePage
import com.example.assignme.GUI.AccountProfile.RecipeApproveScreen
import com.example.assignme.GUI.AccountProfile.RegisterPage
import com.example.assignme.GUI.AccountProfile.SocialFeedScreen
import com.example.assignme.ViewModel.UserViewModel


@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun NavigationGraph(navController: NavHostController = rememberNavController(), userViewModel: UserViewModel){

    NavHost(
        navController = navController,
        startDestination = "main_page"
    ){

        composable("main_page"){

            AppFirstPage(navController, userViewModel)
        }

        composable("first_page"){

            FirstPage(navController, userViewModel)
        }

        composable("login_page"){

            LoginPage(navController, userViewModel)
        }

        composable("register_page"){

            RegisterPage(navController, userViewModel)
        }

        composable("forgot_password_page"){

            ForgotPasswordPage(navController, userViewModel)
        }

        composable("profile_page"){

            ProfilePage(navController, userViewModel)
        }

        composable("edit_profile") {

            EditProfileScreen(navController, userViewModel)
        }

        composable("admin_page"){

            AdminDashboard(navController, userViewModel)
        }

        composable("approve_recipe"){

            RecipeApproveScreen(navController, userViewModel)
        }

        composable("manage_post"){

            SocialFeedScreen(navController, userViewModel)
        }

        composable("add_admin"){

            AddAdmin(navController, userViewModel)
        }


    }

}

