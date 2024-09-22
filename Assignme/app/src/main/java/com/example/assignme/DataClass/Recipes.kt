package com.example.assignme.DataClass

data class Recipes(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val ingredients: List<Ingredient> = emptyList(),
    val cookTime: String ="", // e.g. "45 min"
    val servings: Int = 0,
    val totalCalories: Int = 0, // e.g., sum of ingredients' calories
    val authorId: String= "", // User who created the recipe
    val imageUrl: String = "", // URL or image resource for the recipe image
    val category: String = "",// breakfast, lunch, dinner, dessert, etc.
    val instructions: List<String> = emptyList()
)
