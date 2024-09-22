package com.example.assignme.GUI.Recipe

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.assignme.AndroidBar.AppBottomNavigation
import com.example.assignme.AndroidBar.AppTopBar
import com.example.assignme.DataClass.Ingredient
import com.example.assignme.DataClass.Recipes
import com.example.assignme.GUI.Recipe.ui.theme.Orange
import com.example.assignme.R
import com.example.assignme.ViewModel.RecipeViewModel
import okio.blackholeSink
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import com.example.assignme.DataClass.CalorieNinjasResponse
import com.example.assignme.DataClass.RetrofitInstance
import android.util.Log
import com.example.assignme.DataClass.fetchCaloriesForIngredient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRecipe(
    navController: NavController,
    viewModel: RecipeViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    MaterialTheme(colors = darkColors()) {


        val context = LocalContext.current
        var hasCameraPermission by remember { mutableStateOf(false) }
        // State variables for the recipe fields
        var recipeTitle by remember { mutableStateOf("Bento lunch box ideas for work!") }
        var serves by remember { mutableStateOf(1) }
        var cookTime by remember { mutableStateOf(45) }
        var ingredients by remember { mutableStateOf(listOf(Ingredient("", "", null))) }
        var description by remember { mutableStateOf("A healthy lunch recipe.") }
        var instructions by remember { mutableStateOf(listOf("")) }  // Step-by-step instructions
        var selectedCategory by remember { mutableStateOf("Lunch") } // Default category
        var imageUri by remember { mutableStateOf<Uri?>(null) }
        var showDialog by remember { mutableStateOf(false) }
        val scrollState = rememberScrollState()

        val categories = listOf("Breakfast", "Lunch", "Dinner", "Snack") // Category options

        val imagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
            onResult = { uri -> imageUri = uri }
        )

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            hasCameraPermission = isGranted
        }

        val cameraLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicturePreview(),
            onResult = { bitmap ->
                if (bitmap != null) {
                    val savedUri = saveImageToInternalStorage(context, bitmap)
                    imageUri = savedUri
                }
            }
        )

        LaunchedEffect(Unit) {
            hasCameraPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        }

        // Apply MaterialTheme to ensure dark/light theme works
        Scaffold(
            topBar = { AppTopBar(title = "Create Recipe", navController = navController) },
            bottomBar = { AppBottomNavigation(navController = navController) },

            ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(scrollState)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Image Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = "Recipe Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            "Upload or Take Photo",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.onSurface // Use theme color
                        )
                    }
                    IconButton(
                        onClick = { showDialog = true },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Image",
                            tint = Orange
                        )
                    }
                }

                if (showDialog) {
                    showImageSourceDialog(
                        showDialog = showDialog,
                        onDismiss = { showDialog = false },
                        onPickFromGallery = { imagePickerLauncher.launch("image/*") },
                        onTakePhoto = {
                            if (hasCameraPermission) {
                                cameraLauncher.launch(null)
                            } else {
                                // Request camera permission if not granted
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    )
                }

                // Recipe Title and Description
                OutlinedTextField(
                    value = recipeTitle,
                    onValueChange = { recipeTitle = it },
                    label = { Text("Recipe Title", color = Color.White) }, // White label text
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFFE23E3E), // Orange color when focused
                        textColor = Color.White, // Text color white
                        cursorColor = Color(0xFFE23E3E), // Orange cursor
                        focusedLabelColor = Color.White, // White label when focused
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f), // Slightly transparent white when unfocused
                        placeholderColor = Color.White.copy(alpha = 0.7f) // Placeholder text color
                    )
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description", color = Color.White) }, // White label text
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFFE23E3E), // Orange color when focused
                        textColor = Color.White, // Text color white
                        cursorColor = Color(0xFFE23E3E), // Orange cursor
                        focusedLabelColor = Color.White, // White label when focused
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f), // Slightly transparent white when unfocused
                        placeholderColor = Color.White.copy(alpha = 0.7f) // Placeholder text color
                    )
                )


                // Category Selector
                Text(
                    "Category",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colors.onSurface
                )
                CategorySelector(
                    selectedCategory = selectedCategory,
                    categories = categories,
                    onCategorySelected = { selectedCategory = it }
                )

                // Serves and Cook time section
                ServesItem(serves = serves, onServeChange = { serves = it })
                CookTimeItem(cookTime = cookTime, onCookTimeChange = { cookTime = it })

                // Ingredients Section
                Text(
                    "Ingredients",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colors.onSurface
                )
                ingredients.forEachIndexed { index, ingredient ->
                    val caloriesPerUnit =
                        100.0 // Assuming you fetched this from CaloriesNinja API for 100g
                    val amountInGrams = ingredient.amount.toDoubleOrNull() ?: 0.0
                    val calculatedCalories =
                        (caloriesPerUnit / 100) * amountInGrams // Recalculate based on entered amount

                    IngredientItem(
                        name = ingredient.name,
                        quantity = ingredient.amount,
                        caloriesPerUnit = caloriesPerUnit,
                        onNameChange = { newName ->
                            ingredients = ingredients.toMutableList().also {
                                it[index] = it[index].copy(name = newName)
                            }
                        },
                        onQuantityChange = { newQuantity ->
                            ingredients = ingredients.toMutableList().also {
                                it[index] = it[index].copy(amount = newQuantity)
                            }
                        },
                        calories = calculatedCalories, // Pass recalculated calories
                        onRemove = {
                            ingredients = ingredients.toMutableList().also {
                                it.removeAt(index)
                            }
                        }
                    )
                }
                AddNewIngredientButton {
                    ingredients = ingredients + Ingredient("", "", 0.0)
                }

                // Instructions Section
                Text(
                    "Instructions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colors.onSurface
                )
                instructions.forEachIndexed { index, instruction ->
                    InstructionItem(
                        instruction = instruction,
                        onInstructionChange = { newInstruction ->
                            instructions = instructions.toMutableList().also {
                                it[index] = newInstruction
                            }
                        },
                        onRemove = {
                            instructions = instructions.toMutableList().also {
                                it.removeAt(index)
                            }
                        }
                    )
                }

                AddNewInstructionButton {
                    instructions = instructions + ""
                }

                // Save Button
                Button(
                    onClick = {
                        val newRecipe = Recipes(
                            id = UUID.randomUUID().toString(),
                            title = recipeTitle,
                            description = description,
                            ingredients = ingredients,
                            cookTime = "$cookTime min",
                            servings = serves,
                            imageUrl = imageUri?.toString() ?: "",
                            category = selectedCategory,
                            instructions = instructions,
                            authorId = "currentUserId" // Replace with actual author ID
                        )

                        viewModel.addRecipe(newRecipe, onSuccess = {
                            navController.popBackStack() // Navigate back on success
                        }, onFailure = {
                            // Handle failure
                        })
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Orange)
                ) {
                    Text("Save my recipes", color = Color.White)
                }

                Spacer(modifier = Modifier.height(25.dp))
            }
        }
    }
}


@Composable
fun CategorySelector(
    selectedCategory: String,
    categories: List<String>,
    onCategorySelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // Ensure that the DropdownMenu width matches the width of the OutlinedButton
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = selectedCategory,
                modifier = Modifier.weight(1f),
                color = Color.White
                 // Set the text color to orange
            )
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint=Orange)
        }

        // DropdownMenu with the same width as the OutlinedButton
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()// Ensure the dropdown width matches the button width
        ) {
            categories.forEach { category ->
                DropdownMenuItem(onClick = {
                    onCategorySelected(category)
                    expanded = false
                }) {
                    Text(text = category)
                }
            }
        }
    }
}



// Instruction Item Composable (For each step)
@Composable
fun InstructionItem(
    instruction: String,
    onInstructionChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = instruction,
            onValueChange = onInstructionChange,
            modifier = Modifier.weight(1f),
            label = { Text("Instruction") },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(0xFFE23E3E), // Set the focused border color to orange
                unfocusedBorderColor = Color.Gray, // Unfocused border color
                focusedLabelColor = Color(0xFFE23E3E), // Set the label color to orange when focused
                cursorColor = Color(0xFFE23E3E) // Set the cursor color to orange
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = onRemove) {
            Icon(
                painter = painterResource(id = R.drawable.remove),
                contentDescription = "Remove instruction",
                tint = Orange,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}



// Add New Instruction Button
@Composable
fun AddNewInstructionButton(onAdd: () -> Unit) {
    TextButton(onClick = onAdd, modifier = Modifier.fillMaxWidth()) {
        Icon(
            painter = painterResource(id = R.drawable.addbtn),
            contentDescription = "Add new instruction",
            tint = Orange,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Add new Instruction", color = Orange)
    }
}

// Save bitmap image to internal storage and return the URI
fun saveImageToInternalStorage(context: Context, bitmap: Bitmap): Uri {
    val filename = "recipe_${UUID.randomUUID()}.jpg"
    val file = File(context.filesDir, filename)
    val fos = FileOutputStream(file)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
    fos.flush()
    fos.close()
    return Uri.fromFile(file)
}

@Composable
fun ServesItem(serves: Int, onServeChange: (Int) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(10.dp),
        backgroundColor = Color(0xFF424242) // Light gray background
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), // Padding inside the card
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.serve), // Serves icon
                    contentDescription = "Serves",
                    tint = Color(0xFFE23E3E), // Your red tint color
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Serves", fontWeight = FontWeight.Medium, color = Color.White)
            }

            // Decrease Button, Serve Count, and Increase Button
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    if (serves > 1) onServeChange(serves - 1) // Prevent going below 1
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_left),
                        contentDescription = "Decrease serves",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = String.format("%02d", serves), // Show number in two digits (e.g., 01)
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(onClick = {
                    onServeChange(serves + 1) // Increase serves
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_right),
                        contentDescription = "Increase serves",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CookTimeItem(cookTime: Int, onCookTimeChange: (Int) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(10.dp),
        backgroundColor = Color(0xFF424242) // Light gray background
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), // Padding inside the card
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.clock), // Clock icon for cook time
                    contentDescription = "Cook time",
                    tint = Color(0xFFE23E3E), // Your red tint color
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cook time", fontWeight = FontWeight.Medium, color = Color.White)
            }

            // Decrease Button, Cook Time, and Increase Button
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    if (cookTime > 1) onCookTimeChange(cookTime - 5) // Prevent going below 1 minute
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_left),
                        contentDescription = "Decrease cook time",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = "$cookTime min", // Show cook time in minutes
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(onClick = {
                    onCookTimeChange(cookTime + 5) // Increase cook time
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_right),
                        contentDescription = "Increase cook time",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}



@SuppressLint("DefaultLocale")
@Composable
fun IngredientItem(
    name: String,
    quantity: String,
    caloriesPerUnit: Double,
    onNameChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    calories: Double,
    onRemove: () -> Unit
) {
    var fetchedCalories by remember { mutableStateOf<Double?>(null) }
    var totalCalories by remember { mutableStateOf(0.0) }

    fun recalculateTotalCalories(quantity: String, caloriesPerUnit: Double?) {
        val amountInGrams = quantity.toDoubleOrNull() ?: 0.0
        totalCalories = if (caloriesPerUnit != null && caloriesPerUnit > 0) {
            (caloriesPerUnit / 100) * amountInGrams // Adjust for entered amount
        } else {
            0.0
        }
    }

    LaunchedEffect(name) {
        if (name.isNotEmpty()) {
            fetchCaloriesForIngredient(name) { calories ->
                fetchedCalories = calories // Calories per standard unit (e.g., 100g)
                recalculateTotalCalories(quantity, calories) // Recalculate calories
            }
        }
    }



    LaunchedEffect(quantity) {
        recalculateTotalCalories(quantity, fetchedCalories ?: 0.0)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ingredient Name Input
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            modifier = Modifier.weight(1f),
            label = { Text("Name", fontSize = 12.sp) },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(0xFFE23E3E),
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = Color(0xFFE23E3E),
                cursorColor = Color(0xFFE23E3E)
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Ingredient Quantity Input
        OutlinedTextField(
            value = quantity,
            onValueChange = { newQuantity ->
                onQuantityChange(newQuantity) // Trigger the state update
                recalculateTotalCalories(newQuantity, fetchedCalories) // Recalculate on quantity change
            },
            modifier = Modifier.weight(1f),
            label = { Text("Amount(g)", fontSize = 12.sp)},
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(0xFFE23E3E),
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = Color(0xFFE23E3E),
                cursorColor = Color(0xFFE23E3E)
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Calories Display (if fetched)
        OutlinedTextField(
            value = String.format("%.1f", totalCalories),  // Display the recalculated calories
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.weight(1f),
            label = { Text("Calories", fontSize = 12.sp) },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(0xFFE23E3E),
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = Color(0xFFE23E3E),
                cursorColor = Color(0xFFE23E3E)
            )
        )

        // Remove Ingredient Button
        IconButton(onClick = onRemove) {
            Icon(
                painter = painterResource(id = R.drawable.remove),
                contentDescription = "Remove ingredient",
                tint = Orange,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}



@Composable
fun AddNewIngredientButton(onAdd: () -> Unit) {
    TextButton(
        onClick = onAdd,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            painter = painterResource(id = R.drawable.addbtn),
            contentDescription = "Add new ingredient",
            tint = Orange,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Add new Ingredient", color = Orange)
    }
}

// Only one definition of showImageSourceDialog
@Composable
fun showImageSourceDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onPickFromGallery: () -> Unit,
    onTakePhoto: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Choose Image Source") },
            text = { Text("Pick an image from gallery or take a new photo.") },
            confirmButton = {
                TextButton(onClick = {
                    onPickFromGallery() // Launch the gallery picker
                    onDismiss()
                }) {
                    Text("Gallery")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onTakePhoto() // Launch the camera picker
                    onDismiss()
                }) {
                    Text("Camera")
                }
            }
        )
    }
}



fun getCaloriesForIngredient(ingredientName: String) {
    val call = RetrofitInstance.api.getCalories(ingredientName)

    call.enqueue(object : Callback<CalorieNinjasResponse> {
        override fun onResponse(call: Call<CalorieNinjasResponse>, response: Response<CalorieNinjasResponse>) {
            if (response.isSuccessful) {
                val foodItems = response.body()?.items ?: emptyList()

                if (foodItems.isNotEmpty()) {
                    val foodItem = foodItems.first()
                    Log.d("Calorie Info", "Food: ${foodItem.name}, Calories: ${foodItem.calories}")
                } else {
                    Log.d("Calorie Info", "No data found for the ingredient")
                }
            }
        }

        override fun onFailure(call: Call<CalorieNinjasResponse>, t: Throwable) {
            Log.e("Calorie Info", "Error fetching data", t)
        }
    })
}



