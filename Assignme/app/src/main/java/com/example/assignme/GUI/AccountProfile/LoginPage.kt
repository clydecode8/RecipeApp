package com.example.assignme.GUI.AccountProfile

import android.app.Activity.RESULT_OK
import android.util.Log
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.assignme.DataClass.GoogleAuthUiClient
import com.example.assignme.R
import com.example.assignme.DataClass.SignInResult
import com.example.assignme.DataClass.UserData
import com.example.assignme.ViewModel.MockUserViewModel
import com.example.assignme.ViewModel.UserProfileProvider
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import kotlin.math.sign


@Composable
fun LoginPage(navController: NavController, userViewModel: UserProfileProvider) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val auth = FirebaseAuth.getInstance()
    var showErrorDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }
    var signInResult by remember { mutableStateOf<SignInResult?>(null) }

    // Initialize GoogleAuthUiClient
    val context = LocalContext.current
    val googleAuthUiClient = remember {
        GoogleAuthUiClient(
            context = context,
            oneTapClient = Identity.getSignInClient(context)
        )
    }

    //Google sign in
    fun handleSignInResult(signInResult: SignInResult, navController: NavController, userViewModel: UserProfileProvider) {
        Log.d("HandleSignInResult", "Handling sign-in result. Is new user: ${signInResult.isNewUser}")

        when {
            signInResult.errorMessage != null -> {
                Log.e("HandleSignInResult", "Error during sign-in: ${signInResult.errorMessage}")
                dialogMessage = signInResult.errorMessage
                showErrorDialog = true
            }
            signInResult.data != null -> {
                if (signInResult.isNewUser) {
                    Log.d("HandleSignInResult", "New user detected. Redirecting to registration.")
                    dialogMessage = "You need to register an account first. Redirecting to the registration page."
                    showErrorDialog = true
                    // Ensure this route is correct
                    navController.navigate("registrationScreen")
                } else {
                    Log.d("HandleSignInResult", "Existing user logged in successfully. Navigating to home.")
                    dialogMessage = "Login successful. Welcome back!"
                    showSuccessDialog = true

                    signInResult.data?.userId?.let {
                        userViewModel.setUserId(it)
                        Log.d("LoginPage", "Setting user ID: ${userViewModel.userId.value}")
                        navController.navigate("profile_page") {
                            popUpTo("main_page") {
                                inclusive = true
                            } // Clear the back stack, removing main_page

                        }
                        // Ensure this route is correct
                    }
                }
            }
            else -> {
                Log.e("HandleSignInResult", "Unknown issue occurred. No data and no error message.")
                dialogMessage = "An unknown issue occurred. Please try again."
                showErrorDialog = true
            }
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { result ->
            if (result.resultCode == RESULT_OK) {
                coroutineScope.launch {
                    val signInResult = googleAuthUiClient.signInWithIntent(
                        intent = result.data ?: return@launch
                    )
                    handleSignInResult(signInResult, navController, userViewModel)
                }
            }
        }
    )

    fun signInGoogle() {
        coroutineScope.launch {
            val signInIntentSender = googleAuthUiClient.signIn()
            signInIntentSender?.let {
                launcher.launch(IntentSenderRequest.Builder(it).build())
            }
        }
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
                    }
            )
        }

        Column(
            modifier = Modifier
                .padding(start = 20.dp, top = 100.dp, end = 20.dp)
                .fillMaxHeight()
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            // Texts
            Text(
                text = "Welcome back! Glad to see you, again!",
                fontSize = 30.sp,
                lineHeight = 36.sp, // Adjust lineHeight to increase spacing between lines
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 15.dp),
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(20.dp)) // Space between texts and text fields

            // Text Fields
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(end = 15.dp, top = 20.dp) // Reduced top padding
            ) {
                TextField(
                    value = email,
                    onValueChange = { newText -> email = newText },
                    placeholder = { Text(text = "Enter your email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp)
                )

                TextField(
                    value = password,
                    onValueChange = { newText -> password = newText },
                    placeholder = { Text(text = "Enter your password") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp)
                )

                // Forgot Password text
                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = "Forgot Password?",
                        color = Color.Black,
                        modifier = Modifier.clickable {
                            // Navigate to ForgotPassword screen
                            navController.navigate("forgot_password_page")
                        }
                    )
                }

                Spacer(modifier = Modifier.height((screenHeight * 0.03f).dp)) // Pushes the button to the bottom

                // Login Button
                Button(
                    onClick = {
                        // Call login function
                        login(auth, email, password, navController,
                            onError = {
                            // On error, show error dialog
                            errorMessage = it
                            showErrorDialog = true
                        },
                            onSuccess = { result ->
                            signInResult = result
                            // Safely access signInResult
                            signInResult?.data?.userId?.let { userId ->
                                println("Sign-in successful: $userId")
                            }// On success, show success dialog
                            showSuccessDialog = true
                        })
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE23E3E)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Login")
                }

                Spacer(modifier = Modifier.height(16.dp)) // Space below the button

                // Divider section with "or login with"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HorizontalDivider(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        thickness = 1.dp,
                        color = Color.Gray
                    )
                    Text(text = "or login with", color = Color.Black)
                    HorizontalDivider(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        thickness = 1.dp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(16.dp)) // Space below the divider

                // Google Icon Button
                Button(
                    onClick = { signInGoogle() },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8ECF4)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.google), // Replace with your drawable resource
                        contentDescription = "Google Login",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Unspecified
                    )
                }

                Spacer(modifier = Modifier.height(24.dp)) // Space below the Google button

                // Registration Text
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),

                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Don't have an account? ",
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                    Text(
                        text = "Register now",
                        color = Color.Blue,
                        fontSize = 12.sp,
                        modifier = Modifier.clickable {
                            // Navigate to Register screen
                            navController.navigate("register_page")
                        }
                    )
                }
            }
        }
    }

    // Success AlertDialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Success") },
            text = { Text("You have successfully logged in.") },
            confirmButton = {
                Button(onClick = {
                        // Debugging: Ensure signInResult is not null
                        signInResult?.let { result ->
                            println("Dialog confirm button clicked. signInResult: $signInResult")
                            handleSignInResult2(result, navController, userViewModel)
                            showSuccessDialog = false
                        } ?: run {
                            println("Error: signInResult is null")
                        }
                    }) {
                    Text("OK")
                }
            }
        )
    }

    // Error AlertDialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error") },
            text = { Text(errorMessage ?: "Unknown error") },
            confirmButton = {
                Button(onClick = { showErrorDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

//Firebase
fun handleSignInResult2(result: SignInResult, navController: NavController, userViewModel: UserProfileProvider) {
    // Assuming you have the userId from the result
    result.data?.userId?.let {
        userViewModel.setUserId(it)
        Log.d("LoginPage", "Setting user ID: ${userViewModel.userId.value}")

        if(userViewModel.userId.value == "JOVQ9eF5fcQ5BkXgcQBa0SBF8Ct1"){
            navController.navigate("admin_page") {
                popUpTo("main_page") { inclusive = true } // Clear the back stack, removing main_page
            }

        }else{
            navController.navigate("profile_page") {
                popUpTo("main_page") { inclusive = true } // Clear the back stack, removing main_page
            }

        }


    }

    // Navigate to the next screen

}


// Firebase login function
fun login(
    auth: FirebaseAuth,
    email: String,
    password: String,
    navController: NavController,
    onError: (String) -> Unit,
    onSuccess: (SignInResult) -> Unit
) {
    if (email.isNotEmpty() && password.isNotEmpty()) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            onError("Email is badly formatted")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Login successful
                    val user = task.result?.user
                    val isNewUser = task.result?.additionalUserInfo?.isNewUser ?: false
                    val signInResult = mapAuthResultToSignInResult(user, isNewUser)
                    onSuccess(signInResult)
                } else {
                    // Login failed, show error
                    val error = task.exception?.localizedMessage ?: "Login failed"
                    onError(error)
                }
            }
    } else {
        onError("Please enter email and password")
    }
}


// Function to convert Firebase AuthResult to your SignInResult
fun mapAuthResultToSignInResult(user: FirebaseUser?, isNewUser: Boolean): SignInResult {
    return SignInResult(
        data = user?.let {
            UserData(
                userId = it.uid,
                username = it.displayName,
                profilePictureUrl = it.photoUrl?.toString()
            )
        },
        errorMessage = null,
        isNewUser = isNewUser
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewLogin() {

    LoginPage(
        navController = rememberNavController(),
        userViewModel = MockUserViewModel()
    )
}