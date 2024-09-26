package com.example.assignme.GUI.AccountProfile

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.darkColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.lightColors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.assignme.R
import com.example.assignme.DataClass.Recipe
import com.example.assignme.ViewModel.MockUserViewModel
import com.example.assignme.ViewModel.UserProfileProvider
import com.example.assignme.ViewModel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeApproveScreen(navController: NavController, userViewModel: UserProfileProvider) {
    var searchQuery by remember { mutableStateOf("") }
    val recipes = remember { getSampleRecipes() }
    val filteredRecipes = recipes.filter {
        it.title.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = { /* Handle back navigation */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            RecipeList(recipes = filteredRecipes)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Search recipes") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        shape = RoundedCornerShape(50),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedBorderColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun RecipeList(recipes: List<Recipe>) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(recipes.chunked(2)) { rowRecipes ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                rowRecipes.forEach { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        modifier = Modifier.weight(1f)
                    )
                }
                // If odd number of recipes, add an empty space
                if (rowRecipes.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun RecipeCard(recipe: Recipe, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box {
            Image(
                painter = painterResource(id = recipe.imageRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = "${recipe.calories} kCal | ${recipe.duration} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

fun getSampleRecipes(): List<Recipe> {
    return listOf(
        Recipe("Luxembourg Stew","1000", "40", R.drawable.background, 2),
        Recipe("Steak","2500", "180", R.drawable.background, 2),
        Recipe("Green Burger","900", "30", R.drawable.background, 2),
        Recipe("Biscuits","999", "10", R.drawable.background, 2),
        Recipe("Biscuits","999", "10", R.drawable.background, 2)
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewApprove() {

    RecipeApproveScreen(
        navController = rememberNavController(),
        userViewModel = MockUserViewModel()
    )
}