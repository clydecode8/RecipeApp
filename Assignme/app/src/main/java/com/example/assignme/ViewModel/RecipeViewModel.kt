package com.example.assignme.ViewModel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import com.example.assignme.DataClass.Recipes
import com.example.assignme.DataClass.Ingredient
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class RecipeViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    // StateFlow to observe data changes in Composable
    private val _recipes = MutableStateFlow<List<Recipes>>(emptyList())
    val recipes: StateFlow<List<Recipes>> = _recipes

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories

    private val _filteredRecipes = MutableStateFlow<List<Recipes>>(emptyList())
    val filteredRecipes: StateFlow<List<Recipes>> = _filteredRecipes


    // Fetch recipes from Firestore
    fun fetchRecipes() {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("recipes").get().await()
                val recipesList = snapshot.toObjects(Recipes::class.java)
                _recipes.value = recipesList
                _filteredRecipes.value = recipesList
                println("Fetched Recipes: $recipesList")
                // 添加日志来检查数据是否被正确获取
                recipesList.forEach { recipe ->
                    println("Fetched Recipe: ${recipe.title}, ${recipe.cookTime}")
                }
            } catch (e: Exception) {
                // 处理错误
                println("Error fetching recipes: $e")
            }
        }
    }


    // Fetch categories from Firestore
    fun fetchCategories() {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("recipes").get().await()
                val categoriesSet = mutableSetOf<String>()
                snapshot.documents.forEach { document ->
                    val category = document.getString("category")
                    category?.let { categoriesSet.add(it) }
                }
                _categories.value = categoriesSet.toList() // 将集合转换为列表
            } catch (e: Exception) {
                Log.e("RecipeViewModel", "Error fetching categories: ${e.message}")
            }
        }
    }

    // Search recipes by title
    fun searchRecipes(query: String) {
        viewModelScope.launch {
            val trimmedQuery = query.trim()  // 去掉查询中的空格
            val filteredList = if (trimmedQuery.isEmpty()) {
                _recipes.value // 如果搜索框为空，显示所有食谱
            } else {
                _recipes.value.filter { recipe ->
                    recipe.title.trim().contains(trimmedQuery, ignoreCase = true) // 根据修剪后的标题进行过滤
                }
            }
            _filteredRecipes.value = filteredList // 更新过滤后的列表
            println("Search query: $trimmedQuery")
            println("Filtered recipes: ${filteredList.map { it.title }}")
        }
    }


    // Save a new recipe to Firestore
    fun addRecipe(recipe: Recipes, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                // Firestore `collection("recipes")` 用于存储食谱
                firestore.collection("recipes")
                    .document(recipe.id) // 使用 `recipe.id` 作为文档 ID
                    .set(recipe) // 上传 recipe 数据
                    .await() // 等待上传完成
                onSuccess() // 调用成功回调
            } catch (e: Exception) {
                onFailure(e) // 调用失败回调
            }
        }
    }

    fun getRecipeById(recipeId: String): Recipes? {
        return _recipes.value.find { it.id == recipeId }
    }

    fun uploadImageToFirebase(imageUri: Uri, onSuccess: (String) -> Unit) {
        val storageReference = FirebaseStorage.getInstance().reference
        val imageRef = storageReference.child("recipe_images/${UUID.randomUUID()}.jpg")

        imageRef.putFile(imageUri).addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                onSuccess(uri.toString())  // Return the download URL
            }
        }.addOnFailureListener {
            // Handle any errors
        }
    }

    fun generateRecipeId(): String {
        return UUID.randomUUID().toString()
    }

}
