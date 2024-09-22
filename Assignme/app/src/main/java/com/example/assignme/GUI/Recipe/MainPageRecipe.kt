package com.example.assignme.GUI.Recipe

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.assignme.AndroidBar.AppBottomNavigation
import com.example.assignme.AndroidBar.AppTopBar
import com.example.assignme.DataClass.Recipes
import com.example.assignme.R
import com.example.assignme.ViewModel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeMainPage(navController: NavController, viewModel: RecipeViewModel = viewModel()) {
    val recipes by viewModel.filteredRecipes.collectAsState()
    val categories by viewModel.categories.collectAsState()
    var selectedCategory by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.fetchRecipes()
        viewModel.fetchCategories()
    }

    Scaffold(
        topBar = { AppTopBar(title = "Recipes", navController = navController) },
        bottomBar = { AppBottomNavigation(navController = navController) }
    ) { paddingValues ->
        // Make the entire page scrollable vertically
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Header()
            }

            item {

                // Search bar: Click to navigate to the search page
                SearchBar2(onSearch = { navController.navigate("search_results") })
            }

            item {
                // Horizontal scrolling for recipes
                if (recipes.isNotEmpty()) {
                    TrendingSection(recipes = recipes, navController = navController)
                }else {
                    // If no recipes are found
                    Text(
                        text = "No recipes found",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                if (categories.isNotEmpty()) {
                    PopularCategorySection(
                        categories = categories,
                        selectedCategory = selectedCategory,
                        onCategorySelected = { selectedCategory = it }
                    )
                }
            }

            // Display filtered recipes by category
            if (selectedCategory.isNotEmpty()) {
                val filteredRecipes = recipes.filter { it.category == selectedCategory }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredRecipes) { recipe ->
                            RecipeCard(recipe = recipe, onClick = {
                                navController.navigate("recipe_detail_page/${recipe.id}")
                            })
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}


@Composable
fun Header() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Find best recipes\nfor cooking",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Image(
            painter = painterResource(id = R.drawable.profile),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar2(
    onSearch: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                onSearch()  // 触发搜索跳转
            }
            .height(56.dp) // 搜索栏的高度
            .padding(horizontal = 16.dp), // 内边距
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Icon",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Search recipes", // 搜索栏的占位符文本
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Composable
fun TrendingSection(recipes: List<Recipes>, navController: NavController) {
    val shuffledRecipes = remember { recipes.shuffled() }  // Shuffle recipes for randomness

    Column(
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        // Header row for Trending
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Trending now 🔥",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = {
                navController.navigate("search_results")
            }) {
                Text("See all")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Horizontal scrolling for trending recipes
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Display the shuffled recipes
            items(shuffledRecipes.take(5)) { recipe ->
                // Each recipe card in Trending
                RecipeCard(recipe = recipe, onClick = {
                    navController.navigate("recipe_detail_page/${recipe.id}")
                })
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PopularCategorySection(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    Column {
        Text(
            text = "Popular category",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow {
            items(categories) { category ->
                FilterChip(
                    onClick = { onCategorySelected(category) },
                    label = { Text(category) },
                    selected = category == selectedCategory,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }
    }
}
