package com.example.assignme.DataClass

data class Ingredient(
    val name: String = "",       // 设置默认值
    val amount: String = "",   // 设置默认值
    val calories: Double? = 0.0        // 设置默认值
)