package com.example.assignme.GUI.Recipe

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.assignme.AndroidBar.AppBottomNavigation
import com.example.assignme.AndroidBar.AppTopBar
import com.example.assignme.DataClass.WindowInfo
import com.example.assignme.ViewModel.RecipeViewModel
import com.example.assignme.ViewModel.UserViewModel
import kotlinx.coroutines.FlowPreview

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun MyRecipe(
    navController: NavController,
    viewModel: RecipeViewModel = viewModel(),
    userModel: UserViewModel,
    windowInfo: WindowInfo
) {
    val filteredRecipes by viewModel.filteredRecipes.collectAsState()
    val userId by userModel.userId.observeAsState()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        userId?.let { id ->
            viewModel.loadUserRecipes(id) // Load only recipes with authorId == userId
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = "My recipes", navController = navController) },
        bottomBar = { AppBottomNavigation(navController = navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {

            // 搜索栏
            SearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = {
                    searchQuery = it
                    viewModel.searchUserRecipes(it, userId ?: "")  // 每次输入时立即触发搜索
                },
                onSearch = { viewModel.searchRecipes(searchQuery) }
            )

            // 根据屏幕大小设置网格列数
            val columns = when (windowInfo.screenWidthInfo) {
                WindowInfo.WindowType.Compact -> 2  // 小屏幕显示2列
                WindowInfo.WindowType.Medium -> 3  // 中等屏幕显示3列
                WindowInfo.WindowType.Expanded -> 4  // 大屏幕显示4列
            }

            if (filteredRecipes.isNotEmpty()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns), // 动态设置列数
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredRecipes) { recipe ->
                        RecipeCard(
                            recipe = recipe,
                            onClick = {
                                navController.navigate("recipe_detail_page/${recipe.id}")
                            }
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No recipes found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
