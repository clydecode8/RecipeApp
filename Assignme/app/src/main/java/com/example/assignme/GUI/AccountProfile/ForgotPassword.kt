package com.example.assignme.GUI.AccountProfile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.assignme.R
import com.example.assignme.ViewModel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException


@Composable
fun ForgotPasswordPage(navController: NavController, userViewModel: UserViewModel = viewModel()){

    var email by remember { mutableStateOf("") }
    var dialogMessage by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var sent by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .statusBarsPadding()
    ) {

        Row(
            modifier = Modifier
                .padding(start = 15.dp, top = 50.dp)
                .fillMaxWidth()
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

            )
        }

        Column(
            modifier = Modifier
                .padding(top = 150.dp, start = 20.dp)
                .fillMaxSize()
                .safeContentPadding()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            // Texts
            Text(
                text = "OTP Verification",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,

            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Don't worry! It occurs. Please enter the email address linked with your account.",
                fontSize = 18.sp,

            )
            Spacer(modifier = Modifier.height(8.dp))

            Spacer(modifier = Modifier.height(20.dp)) // Adds space between texts and text fields

            // Text Fields and Forgot Password text
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(end = 15.dp, top = 45.dp)
            ) {
                TextField(
                    value = email,
                    onValueChange = { newText -> email = newText },
                    placeholder = { Text(text = "Enter your email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp) // Adds space between text fields
                )

                Button(
                    onClick = {
                        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            dialogMessage = "Invalid email format."
                            showDialog = true

                        } else {
                            sendPasswordResetEmail(
                                email,
                                onSuccess = {
                                    dialogMessage = "A password reset email has been sent to $email if it exists."
                                    showDialog = true
                                    sent = true
                                },
                                onFailure = { error ->
                                    dialogMessage = error
                                    showDialog = true
                                }
                            )
                        }

                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE23E3E)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Verify")
                }
            }
        }
    }
    Column(
        modifier = Modifier
            .padding(bottom = 15.dp)
            .fillMaxSize()
            .safeContentPadding()
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        if(sent){


            Row() {
                // Texts
                Text(
                    text = "Didn't Received Code? ",
                    fontSize = 15.sp,
                )
                Text(
                    text = "Resend",
                    fontSize = 15.sp,
                    modifier = Modifier.clickable {
                        // Navigate to Register screen
                        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            dialogMessage = "Invalid email format."
                            showDialog = true
                        } else {
                            sendPasswordResetEmail(
                                email,
                                onSuccess = {
                                    dialogMessage = "A password reset email has been sent to $email if it exists."
                                    showDialog = true
                                },
                                onFailure = { error ->
                                    dialogMessage = error
                                    showDialog = true
                                }
                            )
                        }
                    }
                )
            }
        }


    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Notification") },
            text = { Text(dialogMessage) },
            confirmButton = {
                Button(
                    onClick = { showDialog = false }
                ) {
                    Text("OK")
                }
            }
        )
    }
}


fun sendPasswordResetEmail(
    email: String,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val auth = FirebaseAuth.getInstance()

    auth.sendPasswordResetEmail(email)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Email sent successfully
                onSuccess()
            } else {
                // Error occurred
                val errorMessage = when (task.exception) {
                    is FirebaseAuthInvalidUserException -> "The email address is not registered."
                    is FirebaseAuthInvalidCredentialsException -> "Invalid email format."
                    else -> "Failed to send reset email. Please try again."
                }
                onFailure(errorMessage)
            }
        }
}

@Preview(showBackground = true)
@Composable
fun PreviewForgotPassword() {
    val navController = rememberNavController() // Mock NavController for preview
    ForgotPasswordPage(navController)
}