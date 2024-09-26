package com.example.assignme.GUI.AccountProfile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.TextButton
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.example.assignme.AndroidBar.AppBottomNavigation
import com.example.assignme.R
import com.example.assignme.DataClass.Recipe
import com.example.assignme.ViewModel.MockThemeViewModel
import com.example.assignme.ViewModel.UserProfile
import com.example.assignme.ViewModel.UserProfileProvider
import com.example.assignme.ViewModel.MockUserViewModel
import com.example.assignme.ViewModel.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePage(navController: NavController,
                userViewModel: UserProfileProvider,
                themeViewModel: ThemeViewModel) {

    val currentTheme by rememberUpdatedState(themeViewModel.isDarkTheme.value)
    var showDialog by remember { mutableStateOf(false) }
    // Sample data
    val recipes = listOf(
        Recipe(
            title = "How to make Italian Spaghetti at home",
            calories = "1000 kcal",
            duration = "40 min",
            imageRes = R.drawable.back_arrow,
            action = 1
        ),
        Recipe(
            title = "Simple chicken meal prep dishes",
            calories = "500 kcal",
            duration = "40 min",
            imageRes = R.drawable.background,
            action = 0
        ),
        Recipe(
            title = "Simple chicken meal prep dishes",
            calories = "500 kcal",
            duration = "40 min",
            imageRes = R.drawable.background,
            action = 1
        )
        // Add more recipes here
    )

    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") } // State for search query

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
                    IconButton(onClick = { showDialog = true })
                    {
                        Icon(
                            painter = painterResource(id = R.drawable.moreoptions2), // Replace with your icon resource
                            contentDescription = "More actions",
                            modifier = Modifier.size(25.dp)

                        )
                    }
                    // Display the theme selection dialog
                    ThemeSelectionDialog(
                        isVisible = showDialog,
                        onDismiss = { showDialog = false },
                        onThemeSelected = { selectedTheme ->
                            themeViewModel.isDarkTheme.value = (selectedTheme == "Dark")
                            themeViewModel.toggleTheme() // Update the theme preference
                            showDialog = false // Dismiss the dialog after selection
                        }
                    )
                },
                modifier = Modifier.padding(top = 4.dp)
            )
        },
        bottomBar = { AppBottomNavigation(navController) }
    ) { paddingValues ->
        // Wrap the Column with a Box and set the background color to white
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                item {
                    ProfileHeader(navController, userViewModel)

                    TabRow(
                        selectedTabIndex = selectedTab,

                    ) {

                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Saved Recipes") }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Created Recipes") }

                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    SearchBar(searchQuery, onQueryChange = { newQuery -> searchQuery = newQuery })
                    Spacer(modifier = Modifier.height(16.dp))
                }

                val filteredRecipes = recipes.filter { recipe ->
                    // Filter by selected tab (action) and search query
                    recipe.action == selectedTab && recipe.title.contains(searchQuery, ignoreCase = true)
                }

                items(filteredRecipes) { recipe ->
                    RecipeCard(
                        title = recipe.title,
                        calories = recipe.calories,
                        duration = recipe.duration,
                        imageRes = recipe.imageRes
                    )
                    Spacer(modifier = Modifier.height(16.dp)) // Add space between cards
                }
            }
        }

    }
}

@Composable
fun ThemeSelectionDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
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
                    text = "${userProfile.name}", //change name
                    fontSize = 18.sp,
                    modifier = Modifier.padding(top = 8.dp) // Add space between picture and name
                )
            }

            // Edit Profile Button
            OutlinedButton(
                onClick = { navController.navigate("edit_profile") },
                modifier = Modifier.padding(bottom = 5.dp)
            ) {
                Text("Edit profile")

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
fun RecipeCard(title: String, calories: String, duration: String, imageRes: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(4.dp)  // Fixed elevation
    ) {
        Box {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(title, fontWeight = FontWeight.Bold)
                Text("$calories | $duration")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfile() {

    ProfilePage(
        navController = rememberNavController(),
        userViewModel = MockUserViewModel(),
        themeViewModel = MockThemeViewModel()
    )
}


