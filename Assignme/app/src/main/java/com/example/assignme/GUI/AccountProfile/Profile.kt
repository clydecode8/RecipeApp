package com.example.assignme.GUI.AccountProfile

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.example.assignme.AndroidBar.AppBottomNavigation
import com.example.assignme.R
import com.example.assignme.DataClass.Recipe
import com.example.assignme.DataClass.Recipes
import com.example.assignme.GUI.Recipe.ui.theme.Orange
import com.example.assignme.ViewModel.MockThemeViewModel
import com.example.assignme.ViewModel.UserProfile
import com.example.assignme.ViewModel.UserProfileProvider
import com.example.assignme.ViewModel.MockUserViewModel
import com.example.assignme.ViewModel.RecipeViewModel
import com.example.assignme.ViewModel.ThemeViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun ProfilePage(
    navController: NavController,
    userViewModel: UserProfileProvider,
    themeViewModel: ThemeViewModel,
    recipeViewModel: RecipeViewModel
) {
    val currentTheme by rememberUpdatedState(themeViewModel.isDarkTheme.value)
    var showDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") } // State for search query

    val savedRecipes by recipeViewModel.savedRecipes.collectAsState()
    val userId by userViewModel.userId.observeAsState()

    // Fetch the saved recipes when the screen is displayed
    LaunchedEffect(Unit) {
        userId?.let { recipeViewModel.fetchSavedRecipes(it) } // Ensure userId is not null
    }
    println(savedRecipes)
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = {
                    Text(
                        text = "My Profile",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )
                },
                actions = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.moreoptions2),
                            contentDescription = "More actions",
                            modifier = Modifier.size(25.dp)
                        )
                    }
                    // Theme selection dialog
                    ThemeSelectionDialog(
                        isVisible = showDialog,
                        onDismiss = { showDialog = false },
                        navController = navController,
                        onThemeSelected = { selectedTheme ->
                            themeViewModel.isDarkTheme.value = (selectedTheme == "Dark")
                            themeViewModel.toggleTheme()
                            showDialog = false
                        }
                    )
                },
                modifier = Modifier.padding(top = 4.dp)
            )
        },
        bottomBar = { AppBottomNavigation(navController) }
    ) { paddingValues ->

        // Wrap everything in a LazyColumn for vertical scrolling
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Profile Header
                ProfileHeader(navController, userViewModel)
            }

            // Add a button with max width above the TabRow
            item {
                OutlinedButton(
                    onClick = {
                        // Navigate to my_recipe_page with the necessary ViewModel
                        navController.navigate("my_recipe_page")
                    },
                    modifier = Modifier
                        .fillMaxWidth() // Set the button to have maximum width
                        .padding(vertical = 8.dp) // Optional: Add vertical padding for spacing
                ) {
                    Text("My Recipes", color=Orange)
                }
            }


            item {
                // TabRow for saved/created recipes
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Saved Recipes", color=Orange) }
                    )
                }
            }

            item {
                // Search Bar
                SearchBar(searchQuery, onQueryChange = { newQuery -> searchQuery = newQuery })
            }

            // Filter recipes based on search query
            val filteredRecipes = savedRecipes.filter { recipe ->
                recipe.recipe.title.contains(searchQuery, ignoreCase = true)
            }

            // Display the filtered recipes in a grid
            items(filteredRecipes) { savedUserRecipe ->
                RecipeCard(
                    recipe = savedUserRecipe.recipe, // Access the recipe property
                    modifier = Modifier.padding(8.dp),
                    onClick = {
                        println("Recipe ${savedUserRecipe.recipe.id}")
                        navController.navigate("recipe_detail_page2/${savedUserRecipe.recipe.id}")
                        println("Navigated to recipe detail page with recipeId: ${savedUserRecipe.recipe.id}")
                    }
                )
            }
        }
    }

}







@Composable
fun RecipeCard(
    recipe: Recipes,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .size(200.dp, 250.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {


            Image(
                painter = if (recipe.imageUrl.isNotEmpty()) {
                    rememberAsyncImagePainter(recipe.imageUrl) // Load the image from the URL
                } else {
                    painterResource(id = R.drawable.profile) // Use a placeholder image
                },
                contentDescription = "Recipe Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = recipe.title,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "${recipe.totalCalories} kCal")
                    Text(text = recipe.cookTime)
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(navController: NavController, userViewModel: UserProfileProvider) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .safeContentPadding()
            .statusBarsPadding() // Ensure the Row takes full width
    ) {
        // Observe user ID from the view model
        val userId by userViewModel.userId.observeAsState()

        // Observe user profile data
        val userProfile by userViewModel.userProfile.observeAsState(UserProfile())
        Log.d("ProfileSection", "Current User ID: $userId")
        Log.d("ProfileSection", "User Profile: Name: ${userProfile.name}, Profile Picture URL: ${userProfile.profilePictureUrl}")

        // Retrieve and use user data
        userId?.let {
            // Fetch profile data based on userId if not already fetched
            if (userProfile.name == null) {
                userViewModel.fetchUserProfile(it)
            }

            // Column for profile picture and name
            Column(
                modifier = Modifier
                    .weight(1f) // Allow Column to take up remaining space
                    .padding(end = 16.dp) // Add space between Column and button
            ) {
                // Display profile picture if available
                if (userProfile.profilePictureUrl != null) {
                    Image(
                        painter = rememberImagePainter(userProfile.profilePictureUrl),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Display default image if profilePictureUrl is null
                    Image(
                        painter = painterResource(id = R.drawable.google),
                        contentDescription = "Default Profile Picture",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                // Name
                Text(
                    text = "${userProfile.name}",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(top = 8.dp) // Add space between picture and name
                )
            }

            // Edit Profile Button
            OutlinedButton(
                onClick = { navController.navigate("edit_profile") },
                modifier = Modifier.padding(bottom = 5.dp)
            ) {
                Text(
                    "Edit profile",
                    color = Orange // Set your desired color here
                )
            }
        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = { newQuery -> onQueryChange(newQuery) },
        placeholder = { Text("Search recipes") },
        modifier = Modifier.fillMaxWidth()
    )
}


@Composable
fun ThemeSelectionDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    navController: NavController,
    onThemeSelected: (String) -> Unit
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = "Select Theme") },
            text = {
                Column {
                    TextButton(onClick = { onThemeSelected("Dark") }) {
                        Text(text = "Light Theme")
                    }
                    TextButton(onClick = { onThemeSelected("Light") }) {
                        Text(text = "Dark Theme")
                    }
                    TextButton(onClick = { FirebaseAuth.getInstance().signOut()
                        navController.navigate("main_page")}){
                        Text(text = "Sign Out")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}

fun signOut(){


}
// Add this closing brace to complete the function

//@Preview(showBackground = true)
//@Composable
//fun PreviewProfile() {
//
////    ProfilePage(
////        navController = rememberNavController(),
////        userViewModel = MockUserViewModel(),
////        themeViewModel = MockThemeViewModel(),
////    )
//}


