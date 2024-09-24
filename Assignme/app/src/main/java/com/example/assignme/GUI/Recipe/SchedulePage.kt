package com.example.assignme.GUI.Recipe

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.assignme.ViewModel.RecipeViewModel
import com.example.assignme.ViewModel.UserViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*
import androidx.compose.material3.TopAppBar
import com.example.assignme.AndroidBar.AppBottomNavigation
import com.example.assignme.GUI.Recipe.ui.theme.Orange
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.format.DateTimeFormatter
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SchedulePage(navController: NavController, viewModel: RecipeViewModel, userModel: UserViewModel,  onBackClick: () -> Unit) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recipe Schedule") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = { AppBottomNavigation(navController = navController) }

    ) { padding -> // padding 参数提供给 Scaffold 内容
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding) // 将 padding 应用到 Column 上
        ) {
            CalendarSection(selectedDate = selectedDate, onDateSelected = { newDate ->
                selectedDate = newDate // 当用户点击日历时，更新 selectedDate
            })
            MealTypesSection(
                selectedDate = selectedDate,
                userModel = userModel,
                navController = navController
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarSection(selectedDate: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    var currentMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        // Month and Year selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                currentMonth = currentMonth.minusMonths(1) // 上一月
            }) {
                Text("<", style = MaterialTheme.typography.headlineSmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = currentMonth.year.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            IconButton(onClick = {
                currentMonth = currentMonth.plusMonths(1) // 下一月
            }) {
                Text(">", style = MaterialTheme.typography.headlineSmall)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Days of week headers
            items(7) { index ->
                val dayName = LocalDate.now().with(java.time.DayOfWeek.of((index + 1) % 7 + 1))
                    .dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                Text(
                    text = dayName,
                    modifier = Modifier.padding(4.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value % 7
            val daysInMonth = currentMonth.lengthOfMonth()

            // Empty spaces for days before the 1st of the month
            items(firstDayOfWeek) {
                Box(modifier = Modifier.size(40.dp))
            }

            // Days of the month
            items(daysInMonth) { day ->
                val date = currentMonth.atDay(day + 1)
                val isSelected = date == selectedDate

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = if (isSelected) Orange else Color.Transparent,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable {
                            onDateSelected(date) // 设置选中的日期
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (day + 1).toString(),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MealTypesSection(selectedDate: LocalDate, userModel: UserViewModel, navController: NavController) {
    val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snacks")

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(mealTypes.size) { index ->
            MealTypeItem(
                mealType = mealTypes[index],
                selectedDate = selectedDate,
                userModel = userModel,
                navController = navController
            )
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MealTypeItem(mealType: String, selectedDate: LocalDate, userModel: UserViewModel, navController: NavController) {
    var isExpanded by remember { mutableStateOf(false) }
    var mealsForDate by remember { mutableStateOf<List<String>>(emptyList()) }
    val db = FirebaseFirestore.getInstance()
    val dateFormatter = DateTimeFormatter.ofPattern("d/M/yyyy")
    val coroutineScope = rememberCoroutineScope() // 创建一个 CoroutineScope

    // 每当 selectedDate 改变时都重新加载餐食安排
    LaunchedEffect(selectedDate) {
        if (isExpanded) {
            // 查询 Firestore 获取该日期和餐食类型的安排
            val formattedDate = selectedDate.format(dateFormatter)
            val userId = userModel.userId.value.toString() // 假设 userModel 包含 userId

            mealsForDate = fetchMealsForDate(db, userId, formattedDate, mealType)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = mealType, style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = {
                    isExpanded = !isExpanded
                    // 当点击展开时使用 CoroutineScope 启动协程来加载数据
                    if (isExpanded) {
                        coroutineScope.launch {
                            val formattedDate = selectedDate.format(dateFormatter)
                            val userId = userModel.userId.value.toString()
                            mealsForDate = fetchMealsForDate(db, userId, formattedDate, mealType)
                        }
                    }
                }) {
                    Icon(imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, // 切换 + 和 - 图标
                        contentDescription = if (isExpanded) "Collapse" else "Expand")

                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                if (mealsForDate.isEmpty()) {
                    Text("No meals planned")
                } else {
                    mealsForDate.forEach { recipeId ->
                        RecipeCard(recipeId = recipeId, navController = navController)
                    }
                }
            }
        }
    }
}


// Firestore 查询函数，返回指定日期和餐食类型的安排
suspend fun fetchMealsForDate(
    db: FirebaseFirestore,
    userId: String,
    date: String,
    mealType: String
): List<String> {
    return try {
        val snapshot = db.collection("schedule")
            .whereEqualTo("userId", userId)
            .whereEqualTo("date", date)
            .whereEqualTo("mealType", mealType)
            .get()
            .await() // 等待查询完成

        // 提取 recipeId（或其他你想显示的内容）作为结果
        snapshot.documents.map { document ->
            val recipeId = document.getString("recipeId")
            recipeId ?: "Unknown Recipe"
        }
    } catch (e: Exception) {
        emptyList() // 如果查询失败，返回空列表
    }
}

@Composable
fun RecipeCard(recipeId: String, navController: NavController) {
    var recipeName by remember { mutableStateOf("") }
    val db = FirebaseFirestore.getInstance()

    // Fetch recipe name based on the recipeId from Firestore
    LaunchedEffect(recipeId) {
        // 假设在 Firestore 中 recipeId 对应一个 'recipes' 集合中的文档
        val recipeDocument = db.collection("recipes").document(recipeId).get().await()
        recipeName = recipeDocument.getString("title") ?: "Unknown Recipe"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                // Navigate to RecipeScreen with the recipeId
                navController.navigate("recipe_detail_page/$recipeId")
            },
        shape = RoundedCornerShape(8.dp),
        // 将 Card 背景颜色设置为橙色
        colors = CardDefaults.cardColors(
            containerColor = Orange // 橙色
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            // 显示 recipe 名字
            Text(
                text = recipeName,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White // 白色字
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Click to view recipe",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White // 白色字
            )
        }
    }
}