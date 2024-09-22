package com.example.assignme.GUI.Recipe

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.assignme.DataClass.Ingredient
import com.example.assignme.DataClass.Recipes
import com.example.assignme.ViewModel.RecipeViewModel
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeUploadPage(navController: NavController, viewModel: RecipeViewModel = viewModel()) {
    val firestore = FirebaseFirestore.getInstance()
    // 定义状态以保存用户输入的数据
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var cookTime by remember { mutableStateOf("") }
    var servings by remember { mutableStateOf(0) }
    var totalCalories by remember { mutableStateOf(0) }
    var imageUrl by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    // 定义食材列表 (例如这里可以手动定义一个简单的列表)
    val ingredients = listOf(
        Ingredient(name = "Chicken Breast", amount = "200g", calories = 300.0),
        Ingredient(name = "Lettuce", amount = "100g", calories = 15.0)
    )

    val instructions = listOf(
        "Step 1: ...",
        "Step 2: ..."
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 表单输入框
        TextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Recipe Title") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = cookTime,
            onValueChange = { cookTime = it },
            label = { Text("Cook Time") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = imageUrl,
            onValueChange = { imageUrl = it },
            label = { Text("Image URL") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Category") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 上传按钮
        Button(
            onClick = {
                val recipe = Recipes(
                    id = firestore.collection("recipes").document().id,  // 自动生成ID
                    title = title,
                    description = description,
                    ingredients = ingredients,
                    cookTime = cookTime,
                    servings = servings,
                    totalCalories = totalCalories,
                    authorId = "user123", // 假设当前用户的 ID
                    imageUrl = imageUrl,
                    category = category,
                    instructions = instructions
                )

                viewModel.addRecipe(
                    recipe = recipe,
                    onSuccess = { /* 处理成功逻辑 */ },
                    onFailure = { e -> /* 处理失败逻辑，显示错误信息 */ }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Upload Recipe")
        }
    }
}
