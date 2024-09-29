package com.example.assignme.GUI.AccountProfile

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.darkColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.lightColors
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.assignme.AndroidBar.AppBottomNavigation
import com.example.assignme.AndroidBar.AppTopBar
import com.example.assignme.DataClass.GoogleAuthUiClient
import com.example.assignme.DataClass.SignInResult
import com.example.assignme.R
import com.example.assignme.ViewModel.MockUserViewModel
import com.example.assignme.ViewModel.UserProfileProvider
import com.example.assignme.ViewModel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RegisterPage(navController: NavController, userViewModel: UserProfileProvider) {

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmpassword by remember { mutableStateOf("") }
    var showErrorDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }
    var verificationId: String? by remember { mutableStateOf(null) }
    var phoneNumber by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }

    val pagerState = rememberPagerState { 1 }

    var usernameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

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
                    dialogMessage = "Account created successfully. Please sign in."
                    showSuccessDialog = true
                } else {
                    // Handle existing user
                    dialogMessage = "Unknown Error."
                    showErrorDialog = true
                }
            }
            else -> {
                // If data is null and there is no error message, consider it as an unknown issue
                dialogMessage = "Account already exists. Please sign in."
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
                    val signInResult = googleAuthUiClient.registerWithIntent(
                        intent = result.data ?: return@launch
                    )
                    handleSignInResult(signInResult, navController)
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

    fun handleRegistration() {
        // Validate inputs
        val usernameError = validateUsername(username)
        val emailError = validateEmail(email)
        val passwordError = validatePassword(password)
        val confirmPasswordError = validateConfirmPassword(password, confirmpassword)

        // Check for any errors
        if (usernameError != null || emailError != null || passwordError != null || confirmPasswordError != null) {

            return
        }

        // Proceed with registration if there are no errors
        submitRegistration(
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


    fun handlePhoneRegistration() {
        submitRegistration(
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

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Registration Page",
                navController = navController,
                modifier = Modifier
            )
        },
        bottomBar = { /* Add a bottom bar if needed */ }
    ) { innerPadding ->

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding()
        ) {

            // Define variables to hold screen width and height
            val screenHeight = constraints.maxHeight

            // Use LazyColumn for scrollable content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.Start // Ensure correct alignment is used
            ) {
                item {
                    // Heading Text
                    Text(
                        text = "Hello! Register to get started.",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 40.sp // Set the line height here
                    )
                }

                item {
                    // Tabs for Registration Forms
                    TabRow(
                        selectedTabIndex = pagerState.currentPage,
                    ) {
                        Tab(
                            selected = pagerState.currentPage == 0,
                            onClick = { coroutineScope.launch { pagerState.scrollToPage(0) } },
                            text = { Text("Email") }
                        )

                    }
                }

                item {
                    // Pager for Registration Forms
                    HorizontalPager(
                        state = pagerState,
                        pageSize = PageSize.Fill,
                        modifier = Modifier
                            .fillMaxWidth() // Ensure the pager fills the width

                    ) { page ->
                        when (page) {
                            0 -> EmailRegistration(
                                username = username,
                                email = email,
                                password = password,
                                confirmpassword = confirmpassword,
                                onUsernameChange = { username = it },
                                onEmailChange = { email = it },
                                onPasswordChange = { password = it },
                                onConfirmpasswordChange = { confirmpassword = it },
                                onRegister = {
                                    // Validate fields and set error messages
                                    usernameError = validateUsername(username)
                                    emailError = validateEmail(email)
                                    passwordError = validatePassword(password)
                                    confirmPasswordError = validateConfirmPassword(password, confirmpassword)

                                    // Check if there are any errors
                                    if (usernameError == null && emailError == null && passwordError == null && confirmPasswordError == null) {
                                        // No errors, proceed to registration
                                        handleRegistration()
                                    }
                                    // If there are errors, they will already be set and displayed in the UI
                                },
                                usernameError = usernameError,
                                emailError = emailError,
                                passwordError = passwordError,
                                confirmPasswordError = confirmPasswordError,
                                setUsernameError = { usernameError = it },
                                setEmailError = { emailError = it },
                                setPasswordError = { passwordError = it },
                                setConfirmPasswordError = { confirmPasswordError = it }
                            )

                        }
                    }
                }

                item {
                    // Navigation dots
                    PagerIndicator(pagerState = pagerState)
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                            .align(Alignment.BottomCenter) // Keep the button fixed at the bottom
                    ) {
//                        Button(
//
//                            onClick = {
//                                // Check the current page and call the respective registration function
//                                if (pagerState.currentPage == 0) {
//                                    handleRegistration() // Email registration
//                                } else if (pagerState.currentPage == 1) {
//                                    handlePhoneRegistration() // Phone registration
//                                }
//                            },
//                            modifier = Modifier
//                                .height(54.dp)
//                                .fillMaxWidth()
//                                .padding(horizontal = 16.dp),
//                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE23E3E)),
//                            shape = RoundedCornerShape(10.dp)
//                        ) {
//                            Text(text = "Register", fontWeight = FontWeight.Bold)
//                        }
                    }
                }

                item {
                    // Dialog logic
                    if (showErrorDialog) {
                        ErrorDialog(
                            errorMessage = dialogMessage,
                            onDismiss = { showErrorDialog = false }
                        )
                    }

                    if (showSuccessDialog) {
                        SuccessDialog(
                            message = dialogMessage,
                            onDismiss = {
                                showSuccessDialog = false
                                navController.navigate("login_page")
                            }
                        )
                    }
                }

                item {
                    // Divider section with "or register with"
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        HorizontalDivider(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            thickness = 1.dp
                        )
                        Text(text = "Or Register with")
                        HorizontalDivider(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            thickness = 1.dp,
                        )
                    }
                }

                item {
                    // Leave space at the bottom for the fixed button

                }

                item {
                    // Google Icon Button centered at the bottom
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = { signInGoogle() },
                            modifier = Modifier
                                .height(54.dp)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8ECF4)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.google),
                                contentDescription = "Google Login",
                                modifier = Modifier.size(24.dp),
                                tint = Color.Unspecified
                            )

                        }

                    }
                }
            }
        }
    }

}

fun validateUsername(username: String): String? {
    return when {
        username.isEmpty() -> "Username cannot be empty."
        username.length < 3 -> "Username must be at least 3 characters."
        else -> null
    }
}

fun validateEmail(email: String): String? {
    return when {
        email.isEmpty() -> "Email cannot be empty."
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email format."
        else -> null
    }
}

fun validatePassword(password: String): String? {
    return when {
        password.isEmpty() -> "Password cannot be empty."
        password.length < 6 -> "Password must be at least 6 characters."
        else -> null
    }
}

fun validateConfirmPassword(password: String, confirmPassword: String): String? {
    return when {
        confirmPassword.isEmpty() -> "Confirm password cannot be empty."
        confirmPassword != password -> "Passwords do not match."
        else -> null
    }
}


@Composable
fun EmailRegistration(
    username: String,
    email: String,
    password: String,
    confirmpassword: String,
    onUsernameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmpasswordChange: (String) -> Unit,
    onRegister: () -> Unit,
    usernameError: String? = null,
    emailError: String? = null,
    passwordError: String? = null,
    confirmPasswordError: String? = null,
    setUsernameError: (String?) -> Unit,
    setEmailError: (String?) -> Unit,
    setPasswordError: (String?) -> Unit,
    setConfirmPasswordError: (String?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = { Text("Username") },
            isError = usernameError != null,
            placeholder = { Text("Enter your username") },
            modifier = Modifier.fillMaxWidth()
        )
        if (usernameError != null) {
            Text(
                text = usernameError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            isError = emailError != null,
            placeholder = { Text("Enter your email address") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        if (emailError != null) {
            Text(
                text = emailError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Password") },
            isError = passwordError != null,
            placeholder = { Text("Enter your password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        if (passwordError != null) {
            Text(
                text = passwordError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmpassword,
            onValueChange = onConfirmpasswordChange,
            label = { Text("Confirm Password") },
            isError = confirmPasswordError != null,
            placeholder = { Text("Confirm your password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        if (confirmPasswordError != null) {
            Text(
                text = confirmPasswordError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE23E3E)),
            onClick = {
                // Clear previous errors
                setUsernameError(null)
                setEmailError(null)
                setPasswordError(null)
                setConfirmPasswordError(null)

                // Check if any field is empty
                var isValid = true
                if (username.isEmpty()) {
                    setUsernameError("Username cannot be empty")
                    isValid = false
                }
                if (email.isEmpty()) {
                    setEmailError("Email cannot be empty")
                    isValid = false
                }
                if (password.isEmpty()) {
                    setPasswordError("Password cannot be empty")
                    isValid = false
                }
                if (confirmpassword.isEmpty()) {
                    setConfirmPasswordError("Please confirm your password")
                    isValid = false
                } else if (password != confirmpassword) {
                    setConfirmPasswordError("Passwords do not match")
                    isValid = false
                }

                if (isValid) {
                    onRegister()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }
    }
}



fun submitPhoneRegistration(
    phoneNumber: String,
    context: Context,
    onVerificationIdReceived: (String) -> Unit,
    onValidationError: (String) -> Unit,
    onFailure: (String) -> Unit,
    onSuccess: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val options = PhoneAuthOptions.newBuilder(auth)
        .setPhoneNumber(phoneNumber)
        .setTimeout(60L, TimeUnit.SECONDS)
        .setActivity(context as Activity) // pass activity context
        .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                val verificationId = credential.smsCode
                if (verificationId != null) {
                    onVerificationIdReceived(verificationId)
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                onFailure(e.message ?: "Verification failed")
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                onVerificationIdReceived(verificationId)
                onSuccess()
            }
        })
        .build()

    PhoneAuthProvider.verifyPhoneNumber(options)
}

fun verifyCode(
    verificationId: String,
    code: String,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val credential = PhoneAuthProvider.getCredential(verificationId, code)
    val auth = FirebaseAuth.getInstance()

    auth.signInWithCredential(credential)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onSuccess()
            } else {
                onFailure(task.exception?.message ?: "Verification failed")
            }
        }
}


@Composable
fun PhoneRegistration(
    phoneNumber: String,
    code: String,
    onPhoneNumberChange: (String) -> Unit,
    onOTPChange: (String) -> Unit,
    onRegister: () -> Unit,
    showErrorDialog: (String) -> Unit,
    showSuccessDialog: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = onPhoneNumberChange,
            placeholder = { Text("Phone Number") }
        )
        OutlinedTextField(
            value = code,
            onValueChange = onOTPChange,
            placeholder = { Text("Verification Code") }
        )
        Button(onClick = { onRegister() }) {
            Text(text = "Send/Verify Code")
        }
    }
}



fun submitRegistration(
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
                    "country" to country,
                    "authmethod" to "firebase",
                    "type" to "normal"
                )

                db.collection("users").document(userId)
                    .set(user)
                    .addOnSuccessListener {
                        println("User data added to Firestore.")

                        // Add profile picture URL to the 'profile_picture' collection
                        val profilePicture = hashMapOf(
                            "profilePictureUrl" to profilePictureUrl, // You can add more fields if needed
                            "timestamp" to FieldValue.serverTimestamp() // Optional: Adds a timestamp
                        )

                        db.collection("users").document(userId)
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


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PagerIndicator(pagerState: PagerState) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(3.dp)
    ) {
        repeat(2) { page ->
            Box(
                modifier = Modifier
                    .size(8.dp)

            )
        }
    }
}

@Composable
fun ErrorDialog(
    errorMessage: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Error") },
        text = { Text(errorMessage) },
        confirmButton = {
            Button(colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE23E3E)),
                onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@Composable
fun SuccessDialog(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Success") },
        text = { Text(message) },
        confirmButton = {
            Button(colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE23E3E)),
                onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewRegister() {

    RegisterPage(
        navController = rememberNavController(),
        userViewModel = MockUserViewModel()
    )
}