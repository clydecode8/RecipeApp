package com.example.assignme.DataClass

import androidx.lifecycle.ViewModel

class AuthViewModel : ViewModel() {

//    var showDialog by mutableStateOf(false)
//    var dialogMessage by mutableStateOf("")
//
//    private val context = LocalContext.current
//    private val googleAuthUiClient = Identity.getSignInClient(context)
//
//    fun signInGoogle(coroutineScope: CoroutineScope, launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>) {
//        coroutineScope.launch {
//            val signInIntentSender = signIn()
//            if (signInIntentSender != null) {
//                launcher.launch(IntentSenderRequest.Builder(signInIntentSender).build())
//            } else {
//                dialogMessage = "No Google account registered."
//                showDialog = true
//            }
//        }
//    }
//
//    private suspend fun signIn(): IntentSender? {
//        val result = try {
//            googleAuthUiClient.beginSignIn(buildSignInRequest()).await()
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//        return result?.pendingIntent?.intentSender
//    }
//
//    private fun buildSignInRequest(): BeginSignInRequest {
//        return BeginSignInRequest.Builder()
//            .setGoogleIdTokenRequestOptions(
//                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
//                    .setSupported(true)
//                    .setFilterByAuthorizedAccounts(false)
//                    .setServerClientId(context.getString(R.string.default_web_client_id))
//                    .build()
//            )
//            .setAutoSelectEnabled(true)
//            .build()
//    }
//
//    @Composable
//    fun ShowAlertDialog() {
//        if (showDialog) {
//            AlertDialog(
//                onDismissRequest = { showDialog = false },
//                title = { Text("Error") },
//                text = { Text(dialogMessage) },
//                confirmButton = {
//                    Button(onClick = { showDialog = false }) {
//                        Text("OK")
//                    }
//                }
//            )
//        }
//    }
}

