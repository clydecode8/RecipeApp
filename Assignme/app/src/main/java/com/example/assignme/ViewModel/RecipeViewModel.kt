package com.example.assignme.ViewModel

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import com.example.assignme.DataClass.Recipes
import com.example.assignme.DataClass.Ingredient
import com.example.assignme.DataClass.RetrofitInstance
import com.example.assignme.DataClass.SavedUserRecipes
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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

    //mh
    // LiveData to hold the list of saved recipes
    private val _savedRecipes = MutableStateFlow<List<SavedUserRecipes>>(emptyList())
    val savedRecipes: StateFlow<List<SavedUserRecipes>> = _savedRecipes


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

    fun searchUserRecipes(query: String, userId: String) {
        firestore.collection("recipes")
            .whereEqualTo("authorId", userId) // Ensure search is filtered by the userId
            .get()
            .addOnSuccessListener { result ->
                val searchResults = result.map { document ->
                    document.toObject(Recipes::class.java).copy(id = document.id)
                }.filter { recipe ->
                    recipe.title.contains(query, ignoreCase = true)
                }
                _filteredRecipes.value = searchResults // Update the filtered list with search results
            }
            .addOnFailureListener { exception ->
                println("Error searching user recipes: ${exception.message}")
            }
    }

    // Fetch categories from Firestore
    fun fetchCategories() {
        viewModelScope.launch {
            try {
                val authorId = "rxpA9YJO2dVB59oozIeCxzGyobs1"

                val snapshot = firestore.collection("recipes").whereEqualTo("authorId", authorId)  .get().await()
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

    // Now modify getRecipeById to search in saved recipes
    fun getSavedRecipeById(recipeId: String): Recipes? {
        return _savedRecipes.value
            .map { it.recipe } // Extract the Recipes object from SavedUserRecipes
            .find { it.id == recipeId }
    }

    fun getRecipeById(recipeId: String): Recipes? {
        return _recipes.value.find { it.id == recipeId }
    }

    fun loadUserRecipes(userId: String) {
        firestore.collection("recipes")
            .whereEqualTo("authorId", userId) // Filter by authorId
            .get()
            .addOnSuccessListener { result ->
                val userRecipes = result.map { document ->
                    document.toObject(Recipes::class.java).copy(id = document.id)
                }
                _filteredRecipes.value = userRecipes // Update the list of recipes for the user
            }
            .addOnFailureListener { exception ->
                // Handle failure case
                println("Error fetching user recipes: ${exception.message}")
            }
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

    //mh
    // Save a recipe to the savedrecipes collection
    fun saveRecipeToSavedRecipes(
        recipe: Recipes,
        userId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Save the recipe and userId together in the same document
                val savedRecipeData = mapOf(
                    "recipe" to recipe, // Save the entire recipe as a map under the "recipe" field
                    "userId" to userId  // Save the userId under the "userId" field
                )

                firestore.collection("userSavedRecipes")
                    .document(recipe.id) // Use recipe.id as the document ID
                    .set(savedRecipeData) // Upload the combined data
                    .await() // Wait for upload to complete

                println("Saved successfully")
                onSuccess() // Call success callback
            } catch (e: Exception) {
                onFailure(e) // Call failure callback
            }
        }
    }


    fun removeRecipeFromSavedRecipes(
        recipeId: String, // Pass the recipe ID to identify the document to delete
        userId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Delete the recipe document using recipeId and userId
                firestore.collection("userSavedRecipes")
                    .whereEqualTo("userId", userId) // Match the userId to ensure the recipe belongs to the user
                    .whereEqualTo("recipe.id", recipeId) // Match the recipeId to find the specific document
                    .get() // Fetch the documents matching the criteria
                    .await() // Wait for the result
                    .documents
                    .forEach { documentSnapshot ->
                        documentSnapshot.reference.delete().await() // Delete each matching document
                    }

                println("Removed successfully")
                onSuccess() // Call success callback
            } catch (e: Exception) {
                onFailure(e) // Call failure callback
            }
        }
    }


    // Function to fetch saved recipes from Firestore
    fun fetchSavedRecipes(userId: String) {
        viewModelScope.launch {
            try {
                // Get all saved recipes for the user from the userSavedRecipes collection
                val snapshot = firestore.collection("userSavedRecipes")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                // Map the documents to SavedUserRecipes objects
                val savedRecipes = snapshot.documents.mapNotNull { document ->
                    // Get the recipe map from the document
                    val recipeMap = document.get("recipe") as? Map<String, Any> ?: return@mapNotNull null

                    // Convert the recipe map to the Recipes object
                    val recipe = recipeMap.let {
                        Recipes(
                            id = it["id"] as? String ?: "",
                            title = it["title"] as? String ?: "",
                            description = it["description"] as? String ?: "",
                            ingredients = (it["ingredients"] as? List<Map<String, Any>>)?.map { ingredientMap ->
                                Ingredient(
                                    name = ingredientMap["name"] as? String ?: "",
                                    amount = ingredientMap["amount"] as? String ?: "",
                                    calories = (ingredientMap["calories"] as? Number)?.toDouble() ?: 0.0 // Convert to Double
                                )
                            } ?: emptyList(),
                            cookTime = it["cookTime"] as? String ?: "",
                            servings = (it["servings"] as? Long)?.toInt() ?: 0,
                            totalCalories = (it["totalCalories"] as? Long)?.toInt() ?: 0,
                            authorId = it["authorId"] as? String ?: "",
                            imageUrl = it["imageUrl"] as? String ?: "",
                            category = it["category"] as? String ?: "",
                            instructions = it["instructions"] as? List<String> ?: emptyList()
                        )
                    }

                    // Create and return a SavedUserRecipes object
                    SavedUserRecipes(recipe = recipe, userId = userId)
                }

                // Update LiveData with the fetched saved recipes
                _savedRecipes.value = savedRecipes // Assuming _savedRecipes is now of type List<SavedUserRecipes>
                println(_savedRecipes.value)
            } catch (e: Exception) {
                Log.e("RecipeViewModel", "Error fetching saved recipes", e)
            }
        }
    }


    // Function to check if a specific recipe is saved by a user
    suspend fun isRecipeSaved(userId: String, recipeId: String): Boolean {
        return try {
            // Query the userSavedRecipes collection to see if there's a matching document
            val snapshot = firestore.collection("userSavedRecipes")
                .whereEqualTo("userId", userId)
                .whereEqualTo("recipe.id", recipeId) // Assuming recipe is a map with an 'id' field
                .get()
                .await()

            // Return true if at least one document is found
            !snapshot.isEmpty
        } catch (e: Exception) {
            Log.e("RecipeViewModel", "Error checking if recipe is saved", e)
            false // Return false in case of an error
        }
    }




}
