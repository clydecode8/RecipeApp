package com.example.assignme.GUI.AccountProfile

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.example.assignme.AndroidBar.AppBottomNavigation
import com.example.assignme.AndroidBar.AppTopBar
import com.example.assignme.ViewModel.MockUserViewModel
import com.example.assignme.R
import com.example.assignme.ViewModel.UserProfile
import com.example.assignme.ViewModel.UserProfileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    userViewModel: UserProfileProvider
) {
    // Lists of options
    val countries = listOf("USA", "Canada", "Mexico", "UK", "Germany")
    val genders = listOf("Male", "Female", "Non-binary", "Other")

    // Observe user profile data
    val userProfile by userViewModel.userProfile.observeAsState(UserProfile())

    // Local states for the form fields, initialized with userProfile data
    var name by remember { mutableStateOf(userProfile.name ?: "") }
    var email by remember { mutableStateOf(userProfile.email ?: "") }
    var phoneNumber by remember { mutableStateOf(userProfile.phoneNumber ?: "") }
    var country by remember { mutableStateOf(userProfile.country ?: "") }
    var gender by remember { mutableStateOf(userProfile.gender ?: "") }
    var profilePictureUri by remember { mutableStateOf<Uri?>(null) }

    // State to manage the success and error dialogs
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }

    // Fetch user profile data if it's not already loaded
    LaunchedEffect(userViewModel.userId.value) {
        userViewModel.userId.value?.let {
            userViewModel.fetchUserProfile(it)
        }
    }

    // Image picker launcher
    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { selectedImageUri ->
            profilePictureUri = selectedImageUri
        }
    }

    // Update local states when userProfile changes
    LaunchedEffect(userProfile) {
        name = userProfile.name ?: ""
        email = userProfile.email ?: ""
        phoneNumber = userProfile.phoneNumber ?: ""
        country = userProfile.country ?: ""
        gender = userProfile.gender ?: ""
        profilePictureUri = userProfile.profilePictureUrl?.let { Uri.parse(it) }
    }

    Scaffold(
        topBar = { AppTopBar(title = "Edit Profile", navController = navController, modifier = Modifier) },
        bottomBar = { AppBottomNavigation(navController) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                // Row for profile picture and name input
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Profile picture with ability to change
                    Image(
                        painter = rememberImagePainter(profilePictureUri ?: R.drawable.profile),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .clickable {
                                imagePickerLauncher.launch("image/*")
                            },
                        contentScale = ContentScale.Crop
                    )
                }
            }

            item {
                ProfileTextField(
                    label = "Name",
                    value = name,
                    onValueChange = { name = it }
                )
            }

            item {
                ProfileTextField(
                    label = "Email",
                    value = email,
                    onValueChange = { email = it }
                )
            }

            item {
                ProfileTextField(
                    label = "Phone number",
                    value = phoneNumber,
                    keyboardType = KeyboardType.Number,
                    onValueChange = { phoneNumber = it },
                    leadingIcon = {
                        Image(
                            painter = painterResource(id = R.drawable.flag),
                            contentDescription = "US Flag",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                )
            }

            item {
                ProfileDropdown(
                    label = "Country",
                    value = country,
                    onValueChange = { country = it },
                    options = countries
                )
                ProfileDropdown(
                    label = "Gender",
                    value = gender,
                    onValueChange = { gender = it },
                    options = genders
                )
            }

            item {
                Button(
                    onClick = {
                        val userId = userViewModel.userId.value ?: return@Button

                        // Save user profile and optionally update profile picture
                        if (profilePictureUri != null) {
                            uploadProfilePictureToFirebase(userId, profilePictureUri!!) { imageUrl ->
                                saveUserProfile(
                                    userId = userId,
                                    name = name,
                                    email = email,
                                    phoneNumber = phoneNumber,
                                    country = country,
                                    gender = gender,
                                    profilePictureUri = imageUrl
                                ) { success ->
                                    if (success) {
                                        showSuccessDialog = true
                                    } else {
                                        showErrorDialog = true
                                    }
                                }
                            }
                        } else {
                            saveUserProfile(
                                userId = userId,
                                name = name,
                                email = email,
                                phoneNumber = phoneNumber,
                                country = country,
                                gender = gender
                            ) { success ->
                                if (success) {
                                    showSuccessDialog = true
                                } else {
                                    showErrorDialog = true
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("SUBMIT", color = Color.White)
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        // Show dialogs if needed
        if (showSuccessDialog) {
            SuccessDialog(navController, onDismiss = { showSuccessDialog = false })
        }

        if (showErrorDialog) {
            ErrorDialog(onDismiss = { showErrorDialog = false })
        }
    }
}



@Composable
fun ProfileTextField(
    label: String,
    value: String?,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType? = null,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value ?: "", // If value is null, display an empty string
        onValueChange = {
            onValueChange(it)
        },
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = (keyboardType ?: KeyboardType.Text) // Default to Text if null
        ),
        label = { Text(label) },
        leadingIcon = leadingIcon,
        placeholder = { Text("Please input") }, // Placeholder text if value is null
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDropdown(
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { /* Do nothing here */ },
            label = { Text(label) },
            readOnly = true,
            trailingIcon = {
                Icon(Icons.Filled.ArrowDropDown, contentDescription = "Dropdown")
            },
            modifier = modifier
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }

        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },

                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }

    }
}

fun uploadProfilePictureToFirebase(
    userId: String,
    uri: Uri,
    onComplete: (String?) -> Unit
) {
    // Firestore reference
    val db = FirebaseFirestore.getInstance()
    val userDocRef = db.collection("users").document(userId)

    // First, retrieve the current profile picture URL
    userDocRef.get().addOnSuccessListener { document ->
        val currentProfilePictureUrl = document.getString("profilePictureUrl")

        // Log the current profile picture URL for debugging
        Log.d("Firebase", "Current profile picture URL: $currentProfilePictureUrl")

        // Delete the old profile picture if it exists
        if (!currentProfilePictureUrl.isNullOrEmpty()) {
            try {
                val oldPictureRef = FirebaseStorage.getInstance().getReferenceFromUrl(currentProfilePictureUrl)
                oldPictureRef.delete().addOnSuccessListener {
                    Log.d("Firebase", "Old profile picture deleted successfully.")
                }.addOnFailureListener { exception ->
                    Log.e("Firebase", "Failed to delete old profile picture: ${exception.message}")
                }
            } catch (e: IllegalArgumentException) {
                Log.e("Firebase", "Invalid URL for old profile picture: $currentProfilePictureUrl")
            }
        } else {
            Log.d("Firebase", "No old profile picture to delete.")
        }

        // Proceed with uploading the new profile picture
        val storageRef = FirebaseStorage.getInstance().reference
        val profilePicturesRef = storageRef.child("users/$userId/profile_picture/${System.currentTimeMillis()}.jpg")

        profilePicturesRef.putFile(uri)
            .addOnSuccessListener {
                // Retrieve the new download URL
                profilePicturesRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    val newProfilePictureUrl = downloadUrl.toString()

                    // Update Firestore with the new profile picture URL
                    val profilePicData: Map<String, Any> = mapOf("profilePictureUrl" to newProfilePictureUrl)

                    userDocRef.update(profilePicData)
                        .addOnSuccessListener {
                            Log.d("Firestore", "New profile picture URL updated successfully.")
                            onComplete(newProfilePictureUrl)
                        }
                        .addOnFailureListener { exception ->
                            Log.e("Firestore", "Failed to update profile picture URL in Firestore: ${exception.message}")
                            onComplete(null)
                        }

                }.addOnFailureListener { exception ->
                    Log.e("Firebase", "Failed to get new profile picture URL: ${exception.message}")
                    onComplete(null)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Failed to upload new profile picture: ${exception.message}")
                onComplete(null)
            }

    }.addOnFailureListener { exception ->
        Log.e("Firestore", "Failed to retrieve current profile picture: ${exception.message}")
        onComplete(null)
    }
}


//fun uploadProfilePictureToFirebase(
//    userId: String,
//    uri: Uri,
//    onComplete: (String?) -> Unit
//) {
//    // Firestore reference
//    val db = FirebaseFirestore.getInstance()
//    val userDocRef = db.collection("users").document(userId)
//
//    // First, retrieve the current profile picture URL
//    userDocRef.get().addOnSuccessListener { document ->
//        val currentProfilePictureUrl = document.getString("profilePictureUrl")
//
//        // Delete the old profile picture if it exists
//        currentProfilePictureUrl?.let { oldUrl ->
//            val oldPictureRef = FirebaseStorage.getInstance().getReferenceFromUrl(oldUrl)
//            oldPictureRef.delete().addOnSuccessListener {
//                Log.d("Firebase", "Old profile picture deleted successfully.")
//            }.addOnFailureListener { exception ->
//                Log.e("Firebase", "Failed to delete old profile picture: ${exception.message}")
//            }
//        }
//
//        // Proceed with uploading the new profile picture
//        val storageRef = FirebaseStorage.getInstance().reference
//        val profilePicturesRef = storageRef.child("users/$userId/profile_picture/${System.currentTimeMillis()}.jpg")
//
//        profilePicturesRef.putFile(uri)
//            .addOnSuccessListener {
//                // Retrieve the new download URL
//                profilePicturesRef.downloadUrl.addOnSuccessListener { downloadUrl ->
//                    val newProfilePictureUrl = downloadUrl.toString()
//
//                    // Update Firestore with the new profile picture URL
//                    // Update Firestore with the new profile picture URL
//                    val profilePicData: Map<String, Any> = mapOf("profilePictureUrl" to newProfilePictureUrl)
//
//                    userDocRef.update(profilePicData)
//                        .addOnSuccessListener {
//                            Log.d("Firestore", "New profile picture URL updated successfully.")
//                            onComplete(newProfilePictureUrl)
//                        }
//                        .addOnFailureListener { exception ->
//                            Log.e("Firestore", "Failed to update profile picture URL in Firestore: ${exception.message}")
//                            onComplete(null)
//                        }
//
//                }.addOnFailureListener { exception ->
//                    Log.e("Firebase", "Failed to get new profile picture URL: ${exception.message}")
//                    onComplete(null)
//                }
//            }
//            .addOnFailureListener { exception ->
//                Log.e("Firebase", "Failed to upload new profile picture: ${exception.message}")
//                onComplete(null)
//            }
//
//    }.addOnFailureListener { exception ->
//        Log.e("Firestore", "Failed to retrieve current profile picture: ${exception.message}")
//        onComplete(null)
//    }
//}

private fun saveUserProfile(
    userId: String,
    name: String,
    email: String,
    phoneNumber: String,
    country: String,
    gender: String,
    profilePictureUri: String? = null,
    onSuccess: (Boolean) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    // Extract authentication method from user providers
    val authProvider = user?.providerData?.getOrNull(1)?.providerId ?: "unknown"

    // Create a user profile map
    val userProfile = hashMapOf(
        "name" to name,
        "email" to email,
        "phoneNumber" to phoneNumber,
        "country" to country,
        "gender" to gender,
        "profilePictureUrl" to (profilePictureUri ?: null), // Set to null if profilePictureUri is null
        "authmethod" to authProvider  // Automatically set the auth method
    )

    // Save to Firestore
    db.collection("users").document(userId)
        .set(userProfile)
        .addOnSuccessListener {
            Log.d("Firebase", "Profile successfully updated!")
            onSuccess(true)
        }
        .addOnFailureListener { e ->
            Log.w("Firebase", "Error updating profile", e)
            onSuccess(false)
        }
}


@Composable
fun SuccessDialog(navController: NavController ,onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            Button(
                onClick = {
                    // Perform navigation
                    navController.navigate("profile_page")
                    // Dismiss the dialog after navigation
                    onDismiss()
                }
            ) {
                Text("OK")
            }
        },
        title = { Text(text = "Profile Updated") },
        text = { Text("Your profile has been successfully updated.") }
    )
}

@Composable
fun ErrorDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            Button(
                onClick = { onDismiss() }
            ) {
                Text("OK")
            }
        },
        title = { Text(text = "Update Failed") },
        text = { Text("There was an error updating your profile. Please try again.") }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewEditProfileScreen() {

    EditProfileScreen(
        navController = rememberNavController(),
        userViewModel = MockUserViewModel()
    )
}

