package com.example.assignme.GUI.Recipe

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.assignme.AndroidBar.AppBottomNavigation
import com.example.assignme.AndroidBar.AppTopBar
import com.example.assignme.DataClass.WindowInfo
import com.example.assignme.DataClass.rememberWidowInfo
import com.example.assignme.ViewModel.RecipeViewModel
import kotlinx.coroutines.FlowPreview

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun SearchResultsPage(navController: NavController, viewModel: RecipeViewModel = viewModel()) {
    val filteredRecipes by viewModel.filteredRecipes.collectAsState()
    val allRecipes by viewModel.recipes.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        // 检查所有食谱是否加载成功
        println("All Recipes in SearchResultsPage: ${allRecipes.map { it.title }}")
    }

    // 获取窗口信息
    val windowInfo = rememberWidowInfo()

    Scaffold(
        topBar = { AppTopBar(title = "Recipes", navController = navController) },
        bottomBar = { AppBottomNavigation(navController = navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {

            // 与主页一致的搜索栏
            SearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = {
                    searchQuery = it
                    viewModel.searchRecipes(it) // 每次输入时立即触发搜索
                },
                onSearch = { viewModel.searchRecipes(searchQuery) }
            )

            // 根据屏幕信息调整列数
            val columnCount = when {
                windowInfo.screenWidthInfo == WindowInfo.WindowType.Expanded ||
                        (windowInfo.screenWidthInfo == WindowInfo.WindowType.Medium && isLandscape()) -> 3
                else -> 2
            }

            if (filteredRecipes.isNotEmpty()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columnCount),
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

// 辅助函数：检查当前屏幕方向
@Composable
fun isLandscape(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}