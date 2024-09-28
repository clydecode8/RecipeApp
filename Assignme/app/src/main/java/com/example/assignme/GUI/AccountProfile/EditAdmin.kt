package com.example.assignme.GUI.AccountProfile

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextButton
import androidx.compose.material.darkColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.lightColors
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.example.assignme.AndroidBar.AppBottomNavigation
import com.example.assignme.AndroidBar.AppTopBar
import com.example.assignme.ViewModel.MockUserViewModel
import com.example.assignme.R
import com.example.assignme.ViewModel.AdminProfile
import com.example.assignme.ViewModel.MockThemeViewModel
import com.example.assignme.ViewModel.ThemeViewModel
import com.example.assignme.ViewModel.UserProfile
import com.example.assignme.ViewModel.UserProfileProvider
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAdminProfileScreen(
    navController: NavController,
    userViewModel: UserProfileProvider,
    themeViewModel: ThemeViewModel
) {

    // Lists of options
    val countries = listOf("Singapore", "Malaysia")
    val genders = listOf("Male", "Female")

    // Observe user profile data
    val userProfile by userViewModel.adminProfile.observeAsState(AdminProfile())
    val userId = userViewModel.userId.value.toString()
    // Local states for the form fields, initialized with userProfile data
    var name by remember { mutableStateOf(userProfile.name ?: "") }
    var email by remember { mutableStateOf(userProfile.email ?: "") }
    var phoneNumber by remember { mutableStateOf(userProfile.phoneNumber ?: "") }
    var country by remember { mutableStateOf(userProfile.country ?: "") }
    var gender by remember { mutableStateOf(userProfile.gender ?: "") }
    var profilePictureUri by remember { mutableStateOf<Uri?>(null) }


    var selectedCountry by remember { mutableStateOf(AdminCountry.DEFAULT) }
    println(selectedCountry)
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var showErrorDialog2 by remember { mutableStateOf(false) }
    var passwordVerificationFailed by remember { mutableStateOf(false) }
    var test = true
    var cleanedPhoneNumber = ""
    var invalidPassword by remember { mutableStateOf(false) }
    // Fetch user profile data if it's not already loaded
    LaunchedEffect(userViewModel.userId.value) {
        userViewModel.userId.value?.let {
            userViewModel.fetchAdminProfile(it)
        }
    }

    // Image picker launcher
    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { selectedImageUri ->
            profilePictureUri = selectedImageUri
        }
    }

    // Function to determine selected country based on phone number
    fun updateSelectedCountryAdmin(phoneNumber: String) {
        if(test == true){
            println(phoneNumber)
            if (phoneNumber.isNotEmpty()) {
                selectedCountry = when {
                    phoneNumber.startsWith("+60") -> AdminCountry.MALAYSIA
                    phoneNumber.startsWith("+65") -> AdminCountry.SINGAPORE
                    else -> selectedCountry // Keep the current selected country
                }
                // Remove prefixes and validate phone number based on country
                cleanedPhoneNumber = when (selectedCountry) {
                    AdminCountry.MALAYSIA -> phoneNumber.removePrefix("+60").trim()
                    AdminCountry.SINGAPORE -> phoneNumber.removePrefix("+65").trim()
                    else -> phoneNumber
                }

            }
            test = false
        }

    }



    // Update local states when userProfile changes
    LaunchedEffect(userProfile) {
        name = userProfile.name ?: ""
        email = userProfile.email ?: ""
        phoneNumber = userProfile.phoneNumber ?: ""
        updateSelectedCountryAdmin(phoneNumber)
        phoneNumber = cleanedPhoneNumber
        country = userProfile.country ?: ""
        gender = userProfile.gender ?: ""
        profilePictureUri = userProfile.profilePictureUrl?.let { Uri.parse(it) }
    }

    if (showPasswordDialog) {
        PasswordInputDialog(
            onDismiss = { showPasswordDialog = false },
            onConfirm = { enteredPassword ->
                // Perform re-authentication
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    val credential = EmailAuthProvider.getCredential(user.email!!, enteredPassword)

                    user.reauthenticate(credential).addOnCompleteListener { reAuthTask ->
                        if (reAuthTask.isSuccessful) {
                            // Password is correct, now call saveUserProfile
                            if (profilePictureUri != null) {
                                uploadProfilePictureToFirebase2(userId, profilePictureUri!!) { imageUrl ->
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
                                    gender = gender,
                                ) { success ->
                                    if (success) {
                                        showSuccessDialog = true
                                    } else {
                                        showErrorDialog = true
                                    }
                                }
                            }
                        } else {
                            // Handle incorrect password
                            Log.w("Firebase", "Re-authentication failed: ${reAuthTask.exception?.message}")
                            passwordVerificationFailed = true // You can show an error message here
                            showErrorDialog2 = true
                        }
                    }
                }
            },
            onCancel = {
                // Handle any cancellation logic if needed
                invalidPassword = false // Reset invalid password state
            },
            invalidPassword = invalidPassword, // Pass the current invalidPassword state
            onInvalidPasswordChanged = { isInvalid ->
                invalidPassword = isInvalid // Callback to update invalidPassword state
            }
        )
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
                AdminProfileTextField2(
                    label = "Name",
                    value = name,
                    onValueChange = { name = it }
                )
            }

            item {
                AdminProfileTextField2(
                    label = "Email",
                    value = email,
                    onValueChange = { email = it }
                )
            }

            item {
                // Phone Number Input Field
                AdminProfileTextFieldPhone(
                    label = "Phone number",
                    value = phoneNumber,
                    keyboardType = KeyboardType.Number,
                    onValueChange = { newPhoneNumber ->
                        phoneNumber = newPhoneNumber
                        updateSelectedCountryAdmin(newPhoneNumber) // Call the function here
                    },
                    leadingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {

                            // Make the leading flag clickable to toggle countries
                            Image(
                                painter = painterResource(id = when (selectedCountry) {
                                    AdminCountry.MALAYSIA -> R.drawable.flag
                                    AdminCountry.SINGAPORE -> R.drawable.flag2
                                    AdminCountry.DEFAULT -> R.drawable.flag
                                }),
                                contentDescription = "${selectedCountry.name} Flag",
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(start = 4.dp)
                                    .clickable {
                                        // Toggle between Malaysia and Singapore
                                        selectedCountry = if (selectedCountry == AdminCountry.MALAYSIA) {
                                            AdminCountry.SINGAPORE
                                        } else {
                                            AdminCountry.MALAYSIA
                                        }
                                        // Reset phone number on country change
                                        phoneNumber = ""
                                    }
                            )

                            // Display unmodifiable prefix for the country code
                            Text(
                                text = when (selectedCountry) {
                                    AdminCountry.MALAYSIA -> "+60 "
                                    AdminCountry.SINGAPORE -> "+65 "
                                    AdminCountry.DEFAULT -> ""
                                },

                                modifier = Modifier.padding(start = 8.dp) // Add some space after the prefix
                            )

                        }
                    }
                )
            }

            item {
                ProfileDropdown2(
                    label = "Country",
                    value = country,
                    onValueChange = { country = it },
                    options = countries
                )

            }

            item {
                ProfileDropdown2(
                    label = "Gender",
                    value = gender,
                    onValueChange = { gender = it },
                    options = genders
                )
            }

            item {
                Button(
                    onClick = {
                        showPasswordDialog = true // Show the dialog when the button is clicked
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE23E3E))
                ) {
                    Text("Update")
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        // Show dialogs if needed
        if (showSuccessDialog) {
            SuccessDialogAdmin2(navController, onDismiss = { showSuccessDialog = false })
        }

        if (showErrorDialog) {
            ErrorDialogAdmin2(onDismiss = { showErrorDialog = false })
        }

        if(showErrorDialog2){

            ErrorDialogAdmin3(onDismiss = { showErrorDialog2 = false })
        }
    }
}

enum class AdminCountry {
    MALAYSIA, SINGAPORE, DEFAULT
}

@Composable
fun AdminProfileTextFieldPhone(
    label: String,
    value: String?,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType? = null,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value ?: "", // If value is null, display an empty string
        onValueChange = { onValueChange(it) },
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = keyboardType ?: KeyboardType.Text // Default to Text if null
        ),
        label = { Text(label) },
        leadingIcon = leadingIcon,
        placeholder = { Text("Please input") }, // Placeholder text if value is null
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun AdminProfileTextField2(
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
fun AdminProfileDropdown2(
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

//fun uploadProfilePictureToFirebase2(
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
//        // Log the current profile picture URL for debugging
//        Log.d("Firebase", "Current profile picture URL: $currentProfilePictureUrl")
//
//        // Delete the old profile picture if it exists
//        if (!currentProfilePictureUrl.isNullOrEmpty()) {
//            try {
//                val oldPictureRef = FirebaseStorage.getInstance().getReferenceFromUrl(currentProfilePictureUrl)
//                oldPictureRef.delete().addOnSuccessListener {
//                    Log.d("Firebase", "Old profile picture deleted successfully.")
//                }.addOnFailureListener { exception ->
//                    Log.e("Firebase", "Failed to delete old profile picture: ${exception.message}")
//                }
//            } catch (e: IllegalArgumentException) {
//                Log.e("Firebase", "Invalid URL for old profile picture: $currentProfilePictureUrl")
//            }
//        } else {
//            Log.d("Firebase", "No old profile picture to delete.")
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

fun uploadAdminProfilePictureToFirebase2(
    userId: String,
    uri: Uri,
    onComplete: (String?) -> Unit
) {
    // Firestore reference
    val db = FirebaseFirestore.getInstance()
    val userDocRef = db.collection("admin").document(userId)

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
        uploadNewProfilePicture(userId, uri, userDocRef, onComplete)

    }.addOnFailureListener { exception ->
        Log.e("Firestore", "Failed to retrieve current profile picture: ${exception.message}")
        onComplete(null)
    }
}

private fun uploadNewProfilePicture(
    userId: String,
    uri: Uri,
    userDocRef: DocumentReference,
    onComplete: (String?) -> Unit
) {
    val storageRef = FirebaseStorage.getInstance().reference
    val profilePicturesRef = storageRef.child("admin/$userId/profile_picture/${System.currentTimeMillis()}.jpg")

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
    var newNumb = ""
    // Extract authentication method from user providers
    val authProvider = user?.providerData?.getOrNull(1)?.providerId ?: "unknown"

    // Remove prefixes and validate phone number based on country
    val cleanedPhoneNumber = when (country) {
        "Malaysia" -> phoneNumber.removePrefix("+60 ").trim()
        "Singapore" -> phoneNumber.removePrefix("+65 ").trim()
        else -> phoneNumber
    }
    println(country)
    println(cleanedPhoneNumber)

    // Validation for phone number based on country
    val isPhoneNumberValid = when (country) {
        "Malaysia" -> cleanedPhoneNumber.length in 9..10 // Must be 10 or 11 digits
        "Singapore" -> cleanedPhoneNumber.length == 8 // Must be exactly 8 digits
        else -> false
    }

    val prefix = when (country) {
        "Malaysia" -> "+60"
        "Singapore" -> "+65"
        else -> null
    }

    if (prefix != null) {
        newNumb = "$prefix$cleanedPhoneNumber" // Concatenate prefix and cleaned number
    }

    // Validation for email format
    val isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

    // If validations fail, call onSuccess with false
    if (!isPhoneNumberValid || !isEmailValid) {
        Log.w("Validation", "Invalid phone number or email format.")
        onSuccess(false)
        return
    }

    user!!.updateEmail(email)
        .addOnCompleteListener { updateTask ->
            if (updateTask.isSuccessful) {
                Log.d("Firebase", "Email updated successfully.")

                // Send verification email
                user.sendEmailVerification()
                    .addOnCompleteListener { sendVerificationTask ->
                        if (sendVerificationTask.isSuccessful) {
                            Log.d("Firebase", "Verification email sent.")
                            // Optionally, inform the user to check their email for verification
                        } else {
                            Log.w("Firebase", "Error sending verification email", sendVerificationTask.exception)
                        }
                    }

            } else {
                Log.w("Firebase", "Error updating email", updateTask.exception)
            }
        }
    // Create a user profile map
    val userProfile = hashMapOf(
        "name" to name,
        "email" to email,
        "phoneNumber" to newNumb,
        "country" to country,
        "gender" to gender,
        "profilePictureUrl" to (profilePictureUri ?: null), // Set to null if profilePictureUri is null
        "authmethod" to authProvider,
        "type" to "user"
    )

    // Fetch existing profile to retain the PFP if not updated
    db.collection("admin").document(userId)
        .get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                val existingProfile = document.data ?: emptyMap<String, Any>()
                val existingProfilePictureUri = existingProfile["profilePictureUrl"] as? String

                // Create a user profile map
                val userProfile = hashMapOf(
                    "name" to name,
                    "email" to email,
                    "phoneNumber" to phoneNumber,
                    "country" to country,
                    "gender" to gender,
                    "profilePictureUrl" to (profilePictureUri
                        ?: existingProfilePictureUri), // Retain existing if null
                    "authmethod" to authProvider,
                    "type" to "user"
                )

                // Save to Firestore
                db.collection("admin").document(userId)
                    .set(userProfile)
                    .addOnSuccessListener {
                        Log.d("Firebase", "Profile successfully updated!")
                        onSuccess(true)
                    }
                    .addOnFailureListener { e ->
                        Log.w("Firebase", "Error updating profile", e)
                        onSuccess(false)
                    }
            } else {
                Log.w("Firebase", "User profile does not exist.")
                onSuccess(false)
            }
        }
        .addOnFailureListener { e ->
            Log.w("Firebase", "Error fetching existing profile", e)
            onSuccess(false)
        }
}


@Composable
fun PasswordInputDialogAdmin(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,// Pass the password entered by the user
    onCancel: () -> Unit, // Add onCancel parameter
    invalidPassword: Boolean, // Add invalidPassword as a parameter
    onInvalidPasswordChanged: (Boolean) -> Unit // Callback to change invalid password state

) {
    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf(false) }


    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Re-authenticate") },
        text = {
            Column {
                Text(text = "Please enter your password to proceed:")
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = false // Clear error when user types
                        onInvalidPasswordChanged(false) // Clear invalid password when user types
                    },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Password
                    ),
                    isError = passwordError || invalidPassword // Indicate error state if blank or invalid
                )
                if (passwordError) {
                    Text(
                        text = "Password cannot be empty.",
                        color = MaterialTheme.colors.error,
                        style = MaterialTheme.typography.body2
                    )
                }
                if (invalidPassword) {
                    Text(
                        text = "Invalid password. Please try again.",
                        color = MaterialTheme.colors.error,
                        style = MaterialTheme.typography.body2
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (password.isBlank()) {
                        passwordError = true // Set error if password is blank
                    } else {
                        onConfirm(password) // Pass the password to the confirm callback
                        // Do not dismiss here; the handling is done in the main logic
                    }
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = {
                onCancel() // Call onCancel when dismissing the dialog
                onDismiss() // Dismiss the dialog
            }) {
                Text("Cancel")
            }
        }
    )

}



@Composable
fun SuccessDialogAdmin2(navController: NavController ,onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            Button(
                onClick = {
                    // Perform navigation
                    navController.navigate("admin_page")
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
fun ErrorDialogAdmin2(onDismiss: () -> Unit) {
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

@Composable
fun ErrorDialogAdmin3(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            Button(
                onClick = { onDismiss() }
            ) {
                Text("OK")
            }
        },
        title = { Text(text = "Invalid Password") },
        text = { Text("Please try again.") }
    )
}

