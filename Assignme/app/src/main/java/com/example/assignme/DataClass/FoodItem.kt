package com.example.assignme.DataClass

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import retrofit2.Callback
import retrofit2.Response

// API response model
data class FoodItem(
    val name: String,
    val calories: Double,
    val serving_size_g: Double,
    val protein_g: Double,
    val fat_total_g: Double,
    val carbohydrates_total_g: Double
)

// API service interface
interface CalorieNinjasApi {
    @Headers("X-Api-Key: 9Swi22FOhHhLZMEljMqTaA==FEPdS4BVlLsozkY3")
    @GET("v1/nutrition")
    fun getCalories(@Query("query") query: String): Call<CalorieNinjasResponse>
}

// Response model for API
data class CalorieNinjasResponse(
    val items: List<FoodItem>
)

// Retrofit setup
val retrofit = Retrofit.Builder()
    .baseUrl("https://api.calorieninjas.com/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val calorieNinjasApi = retrofit.create(CalorieNinjasApi::class.java)

// Function to fetch calories
fun fetchCaloriesForIngredient(ingredient: String, onCaloriesFetched: (Double) -> Unit) {
    val call = calorieNinjasApi.getCalories(ingredient)
    call.enqueue(object : Callback<CalorieNinjasResponse> {
        override fun onResponse(call: Call<CalorieNinjasResponse>, response: Response<CalorieNinjasResponse>) {
            val foodItems = response.body()?.items ?: emptyList()
            val calories = foodItems.firstOrNull()?.calories ?: 0.0
            onCaloriesFetched(calories)
        }

        override fun onFailure(call: Call<CalorieNinjasResponse>, t: Throwable) {
            onCaloriesFetched(0.0)  // Set calories to 0 on failure
        }
    })
}
