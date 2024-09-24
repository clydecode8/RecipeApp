package com.example.assignme.GUI.DailyTracker

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import android.widget.Toast
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import com.example.assignme.ViewModel.UserViewModel

@Composable
fun SetUpInfo(
    navController: NavController,
    userViewModel: UserViewModel
) {
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) } // Track loading state

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    val context = LocalContext.current
    val userId by userViewModel.userId.observeAsState() // Observing the user ID from the UserViewModel

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            Text(
                text = "Set up your information",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            InputField(
                label = "Weight (KG):",
                value = weight,
                onValueChange = { weight = it },
                keyboardType = KeyboardType.Number
            )

            InputField(
                label = "Height (CM):",
                value = height,
                onValueChange = { height = it },
                keyboardType = KeyboardType.Number
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Your current body image:",
                fontWeight = FontWeight.SemiBold
            )

            Button(
                onClick = { imagePicker.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373))
            ) {
                Text("Upload Image")
            }

            Spacer(modifier = Modifier.height(16.dp))

            imageUri?.let {
                val painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(context)
                        .data(it)
                        .build()
                )
                Image(
                    painter = painter,
                    contentDescription = "Uploaded Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f)) // Pushes the continue button to the bottom

            Button(
                onClick = {
                    if (!isLoading) {
                        isLoading = true // Set loading state
                        // Ensure userId is not null
                        if (weight.isNotBlank() && height.isNotBlank() && userId != null) {
                            val weightValue = weight.toFloatOrNull()
                            val heightValue = height.toFloatOrNull()

                            // Validate weight and height values
                            if (weightValue != null && heightValue != null && weightValue > 0 && heightValue > 0) {
                                userViewModel.saveUserData(
                                    weight = weightValue.toString(),
                                    height = heightValue.toString(),
                                    imageUri = imageUri,
                                    userId = userId!!,
                                    onComplete = { success -> // Use the onComplete callback
                                        isLoading = false // Reset loading state
                                        if (success) {
                                            Toast.makeText(context, "Data saved successfully!", Toast.LENGTH_SHORT).show()
                                            navController.navigate("tracker_page") // Navigate to TrackerPage
                                        } else {
                                            Toast.makeText(context, "Failed to save data!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            } else {
                                Toast.makeText(context, "Please enter valid weight and height!", Toast.LENGTH_SHORT).show()
                                isLoading = false // Reset loading state if validation fails
                            }
                        } else {
                            Toast.makeText(context, "Please fill all fields!", Toast.LENGTH_SHORT).show()
                            isLoading = false // Reset loading state if fields are blank
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White) // Show loading indicator
                } else {
                    Text("Continue Journey")
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputField(label: String, value: String, onValueChange: (String) -> Unit, keyboardType: KeyboardType) {
    Column {
        Text(text = label)
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("Enter $label") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = keyboardType
            ),
            visualTransformation = VisualTransformation.None
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewInformationSetupScreen() {
    SetUpInfo(navController = NavController(LocalContext.current), userViewModel = UserViewModel())
}
