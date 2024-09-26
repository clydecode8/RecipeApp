package com.example.assignme

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.assignme.DataClass.Recipes
import com.example.assignme.GUI.AccountProfile.AddAdmin
import com.example.assignme.GUI.AccountProfile.AdminDashboard
import com.example.assignme.GUI.AccountProfile.AppFirstPage
import com.example.assignme.GUI.AccountProfile.EditProfileScreen
import com.example.assignme.GUI.AccountProfile.ForgotPasswordPage
import com.example.assignme.GUI.AccountProfile.LoginPage
import com.example.assignme.GUI.AccountProfile.ProfilePage
import com.example.assignme.GUI.AccountProfile.RecipeApproveScreen
import com.example.assignme.GUI.AccountProfile.RegisterPage
import com.example.assignme.GUI.AccountProfile.SocialFeedScreen
import com.example.assignme.GUI.Community.SocialAppUI
import com.example.assignme.GUI.DailyTracker.SetUpInfo
import com.example.assignme.GUI.DailyTracker.TrackerPage
import com.example.assignme.GUI.FirstPage
import com.example.assignme.GUI.Recipe.CreateRecipe
import com.example.assignme.GUI.Recipe.MyRecipe
import com.example.assignme.GUI.Recipe.RecipeMainPage
import com.example.assignme.GUI.Recipe.RecipeScreen
import com.example.assignme.GUI.Recipe.RecipeUploadPage
import com.example.assignme.GUI.Recipe.SchedulePage
import com.example.assignme.GUI.Recipe.SearchResultsPage
import com.example.assignme.ViewModel.RecipeViewModel
import com.example.assignme.ViewModel.ThemeViewModel
import com.example.assignme.ViewModel.UserViewModel


@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun NavigationGraph(navController: NavHostController = rememberNavController(), userViewModel: UserViewModel, themeViewModel: ThemeViewModel){

    NavHost(
        navController = navController,
        startDestination = "main_page",
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

            ProfilePage(navController, userViewModel, themeViewModel)
        }

        composable("edit_profile") {

            EditProfileScreen(navController, userViewModel, themeViewModel)
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
        composable("chat") {
            SocialAppUI(navController, userViewModel)
        }

        composable("recipe_main_page") { backStackEntry ->
            val parentEntry = remember(backStackEntry) { navController.getBackStackEntry("recipe_main_page") }
            val viewModel: RecipeViewModel = viewModel(parentEntry)
            RecipeMainPage(navController = navController, viewModel = viewModel, userViewModel,)
        }

        composable("recipe_upload_page") { backStackEntry ->
            val parentEntry = remember(backStackEntry) { navController.getBackStackEntry("recipe_main_page") }
            val viewModel: RecipeViewModel = viewModel(parentEntry)
            RecipeUploadPage(navController = navController, viewModel = viewModel)
        }

        composable("search_results") { backStackEntry ->
            val parentEntry = remember(backStackEntry) { navController.getBackStackEntry("recipe_main_page") }
            val viewModel: RecipeViewModel = viewModel(parentEntry)
            SearchResultsPage(navController = navController, viewModel = viewModel)
        }

        composable("recipe_detail_page/{recipeId}") { backStackEntry ->
            val parentEntry = remember(backStackEntry) { navController.getBackStackEntry("recipe_main_page") }
            val recipeId = backStackEntry.arguments?.getString("recipeId")
            val viewModel: RecipeViewModel = viewModel(parentEntry)

            // Log the recipeId passed from the previous page
            println("Navigated to recipe detail page with recipeId: $recipeId")

            // Fetch the recipe by its ID
            val recipe = viewModel.getRecipeById(recipeId ?: "")

            // Log whether the recipe was found
            if (recipe != null) {
                println("Recipe found: ${recipe.title}")
                RecipeScreen(recipe = recipe, userViewModel, viewModel = viewModel, onBackClick = { navController.popBackStack() })
            } else {
                println("Recipe not found for id: $recipeId")
                Text("Recipe not found", modifier = Modifier.padding(16.dp))
            }
        }

        composable("create_recipe") {
            val viewModel: RecipeViewModel = viewModel() // Get a ViewModel scoped to CreateRecipe
            CreateRecipe(
                navController = navController,
                viewModel = viewModel,
                userViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("my_recipe_page") { backStackEntry ->
            val parentEntry = remember(backStackEntry) { navController.getBackStackEntry("recipe_main_page") }
            val viewModel: RecipeViewModel = viewModel(parentEntry)
            MyRecipe(navController = navController, viewModel = viewModel, userViewModel,)
        }

        composable("schedule_page"){backStackEntry ->
            val parentEntry = remember(backStackEntry) { navController.getBackStackEntry("recipe_main_page") }
            val viewModel: RecipeViewModel = viewModel(parentEntry)
            SchedulePage(navController = navController, viewModel = viewModel, userModel = userViewModel) {

            }
        }

        composable("setup_info_page") {
            SetUpInfo(navController, userViewModel)
        }
        composable("tracker_page") {
            TrackerPage(navController, userViewModel)
        }
    }

}

