package com.example.assignme.GUI.AccountProfile

import android.app.DatePickerDialog
import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenuItem
import com.example.assignme.GUI.Recipe.ui.theme.Orange
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.assignme.DataClass.Recipes
import com.example.assignme.ViewModel.RecipeViewModel
import com.example.assignme.ViewModel.UserViewModel
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.fragment.app.FragmentActivity
import com.example.assignme.GUI.Recipe.BottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedRecipeScreen(recipe: Recipes, userModel:UserViewModel, viewModel: RecipeViewModel = viewModel(),  onBackClick: () -> Unit) {

    //mh
    val userId by userModel.userId.observeAsState()
    var isSaved by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = recipe.id) {
        // Call the suspend function from your ViewModel
        isSaved = viewModel.isRecipeSaved(userId.toString(), recipe.id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recipe.title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },

                //mh
                actions = {
                    IconButton(onClick = {
                        println(isSaved)
                        if (isSaved) {
                            // Handle unsaving the recipe
                            viewModel.removeRecipeFromSavedRecipes(
                                recipe.id,
                                userId.toString(),
                                onSuccess = {
                                    isSaved = false // Update state
                                    println("Recipe removed from saved recipes.")
                                },
                                onFailure = { exception ->
                                    println("Failed to remove recipe: ${exception.message}")
                                }
                            )
                        } else {
                            // Handle saving the recipe
                            viewModel.saveRecipeToSavedRecipes(
                                recipe = recipe,
                                userId = userId.toString(), // Pass the userId value here
                                onSuccess = {
                                    isSaved = true // Update state
                                    println("Recipe saved successfully.")
                                },
                                onFailure = { exception ->
                                    println("Failed to save recipe: ${exception.message}")
                                }
                            )
                        }
                    }) {
                        // Change the icon based on the saved state
                        if (isSaved) {
                            Icon(Icons.Default.Favorite, contentDescription = "Saved") // Filled icon
                        } else {
                            Icon(Icons.Default.FavoriteBorder, contentDescription = "Save Recipe") // Outlined icon
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomBar(recipe.id,
                userModel.userId.value.toString(),
                onAddToSchedule = { /* Handle Add to Schedule */ },
                onShare = { /* Handle Share */ })

        },
        contentWindowInsets = WindowInsets.navigationBars

    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            item {
                Image(
                    painter = rememberAsyncImagePainter(recipe.imageUrl),
                    contentDescription = recipe.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InfoCard(title = "${recipe.totalCalories}kCal", subtitle = "Calories")
                    InfoCard(title = recipe.cookTime, subtitle = "Total Time")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Ingredients", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("${recipe.ingredients.size} items", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

                Spacer(modifier = Modifier.height(8.dp))
            }

            items(recipe.ingredients) { ingredient ->
                Text("• ${ingredient.amount} ${ingredient.name}", modifier = Modifier.padding(vertical = 4.dp))
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))

                Text("Instructions", fontSize = 20.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(8.dp))
            }

            items(recipe.instructions.withIndex().toList()) { (index, instruction) ->
                Text(
                    text = "${index + 1}. $instruction",
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun InfoCard(title: String, subtitle: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}