package com.example.assignme.GUI.AccountProfile

import android.app.Activity.RESULT_OK
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.assignme.DataClass.GoogleAuthUiClient
import com.example.assignme.DataClass.SignInResult
import com.example.assignme.R
import com.example.assignme.ViewModel.MockUserViewModel
import com.example.assignme.ViewModel.UserProfileProvider
import com.example.assignme.ViewModel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.launch

@Composable
fun AddAdmin(navController: NavController, userViewModel: UserProfileProvider){


    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmpassword by remember { mutableStateOf("") }
    var showErrorDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }


    // Initialize GoogleAuthUiClient
    val context = LocalContext.current
    val googleAuthUiClient = remember {
        GoogleAuthUiClient(
            context = context,
            oneTapClient = Identity.getSignInClient(context)
        )
    }

    fun handleSignInResult(signInResult: SignInResult, navController: NavController) {
        when {
            signInResult.errorMessage != null -> {
                // Show error dialog with the error message
                dialogMessage = signInResult.errorMessage
                showErrorDialog = true
            }
            signInResult.data != null -> {
                // Check if the user is new or existing
                if (signInResult.isNewUser) {
                    // Handle new user
                    dialogMessage = "Account created successfully."
                    showSuccessDialog = true
                } else {
                    // Handle existing user
                    dialogMessage = "Unknown Error."
                    showErrorDialog = true
                }
            }
            else -> {
                // If data is null and there is no error message, consider it as an unknown issue
                dialogMessage = "Account already exists."
                showErrorDialog = true
            }
        }
    }

    fun handleRegistration() {
        submitAdmin(
            name = username,
            email = email,
            pswd = password,
            pswd2 = confirmpassword,
            onValidationError = {
                dialogMessage = it
                showErrorDialog = true
            },
            onSuccess = {
                dialogMessage = "User registered successfully."
                println("Success callback triggered.")
                showSuccessDialog = true

            },
            onFailure = {
                dialogMessage = it
                showErrorDialog = true
            }
        )
    }





    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .safeContentPadding()
            .statusBarsPadding()
    ) {

        // Define variables to hold screen width and height
        val screenHeight = constraints.maxHeight
        val screenWidth = constraints.maxWidth

        // Back arrow Row at the top
        Row(
            modifier = Modifier
                .padding(start = 15.dp, top = 50.dp)
                .fillMaxWidth()
                .safeContentPadding()
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                painter = painterResource(id = R.drawable.back_arrow),
                contentDescription = "Back",
                modifier = Modifier
                    .size(30.dp)
                    .clickable {
                        // Navigate back
                        navController.navigateUp()
                    },
                tint = Color.Black
            )
        }

        // Column containing text fields and other components
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = (screenHeight * 0.03f).dp)
                .safeContentPadding()
                .statusBarsPadding(),
            verticalArrangement = Arrangement.Center
        ) {
            // Heading Text
            Text(
                text = "Input details.",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Text Fields
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .safeContentPadding()
                    .statusBarsPadding()
                    .padding(end = 15.dp, top = 20.dp)
            ) {
                TextField(
                    value = username,
                    onValueChange = { newText -> username = newText },
                    placeholder = { Text(text = "Username") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp)
                )

                TextField(
                    value = email,
                    onValueChange = { newText -> email = newText },
                    placeholder = { Text(text = "Email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp)
                )

                TextField(
                    value = password,
                    onValueChange = { newText -> password = newText },
                    placeholder = { Text(text = "Password") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp)
                )

                TextField(
                    value = confirmpassword,
                    onValueChange = { newText -> confirmpassword = newText },
                    placeholder = { Text(text = "Confirm Password") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp)
                )
            }

            // Button
            Button(
                onClick = {
                    handleRegistration()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE23E3E)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Register")
            }

            //Dialog logic
            if (showErrorDialog) {
                ErrorDialogAdmin(
                    errorMessage = dialogMessage,
                    onDismiss = { showErrorDialog = false }
                )
            }

            if (showSuccessDialog) {
                SuccessDialogAdmin(
                    message = dialogMessage,
                    onDismiss = {
                        showSuccessDialog = false
                        navController.navigate("login_page") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Divider section with "or register with"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .safeContentPadding()
                    .statusBarsPadding()
            ) {
                HorizontalDivider(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    thickness = 1.dp,
                    color = Color.Gray
                )
                Text(text = "You are held responsible for creating an account.", color = Color.Black, fontSize = 2.em)
                HorizontalDivider(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    thickness = 1.dp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }


    }

}





fun submitAdmin(
    name: String,
    email: String,
    pswd: String,
    pswd2: String,
    phoneNumber: String? = null,
    profilePictureUrl: String? = null,
    gender: String? = null,
    country: String? = null,
    onValidationError: (String) -> Unit,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    println("Starting registration process...")

    // Check if passwords match
    if (pswd != pswd2) {
        onValidationError("Passwords do not match")
        return
    }

    // Initialize Firebase Auth
    val auth = FirebaseAuth.getInstance()
    println("Firebase Auth instance obtained.")

    // Create a new user
    auth.createUserWithEmailAndPassword(email, pswd)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                println("User registration successful.")
                val userId = task.result?.user?.uid ?: run {
                    onFailure("Failed to get user ID")
                    return@addOnCompleteListener
                }
                Log.d("SubmitRegistration", "User ID: $userId")

                // Add user data to Firestore
                val db = FirebaseFirestore.getInstance()
                println("Firestore instance obtained.")

                // Create a map for user data including profilePictureUrl even if it's null
                val user = hashMapOf(
                    "name" to name,
                    "email" to email,
                    "userId" to userId,
                    "phoneNumber" to phoneNumber,
                    "profilePictureUrl" to profilePictureUrl, // This will be null if not provided
                    "gender" to gender,
                    "country" to country
                )

                db.collection("admin").document(userId)
                    .set(user)
                    .addOnSuccessListener {
                        println("User data added to Firestore.")

                        // Add profile picture URL to the 'profile_picture' collection
                        val profilePicture = hashMapOf(
                            "profilePictureUrl" to profilePictureUrl, // You can add more fields if needed
                            "timestamp" to FieldValue.serverTimestamp() // Optional: Adds a timestamp
                        )

                        db.collection("admin").document(userId)
                            .collection("profile_picture")
                            .add(profilePicture)
                            .addOnSuccessListener {
                                println("Profile picture added to Firestore.")
                                onSuccess() // Notify success
                            }
                            .addOnFailureListener { e ->
                                println("Error adding profile picture: ${e.message}")
                                onFailure("Error adding profile picture: ${e.message}")
                            }
                    }
                    .addOnFailureListener { e ->
                        println("Error adding user to Firestore: ${e.message}")
                        onFailure("Error adding user: ${e.message}")
                    }
            } else {
                println("Registration failed: ${task.exception?.message}")
                onFailure("Registration failed: ${task.exception?.message}")
            }
        }
}



@Composable
fun ErrorDialogAdmin(
    errorMessage: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Error") },
        text = { Text(errorMessage) },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@Composable
fun SuccessDialogAdmin(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Success") },
        text = { Text(message) },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewAddAdmin() {

    AddAdmin(
        navController = rememberNavController(),
        userViewModel = MockUserViewModel()
    )
}