package com.example.assignme.DataClass

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.util.Log
import com.example.assignme.R
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class GoogleAuthUiClient(
    private val context: Context,
    private val oneTapClient: SignInClient
) {
    private val auth = Firebase.auth

    suspend fun signIn(): IntentSender? {
        val result = try {
            oneTapClient.beginSignIn(
                buildSignInRequest()
            ).await()
        } catch(e: Exception) {
            e.printStackTrace()
            if(e is CancellationException) throw e
            null
        }
        return result?.pendingIntent?.intentSender
    }

    suspend fun registerWithIntent(intent: Intent): SignInResult {
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = credential.googleIdToken
        val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
        return try {
            val authResult = auth.signInWithCredential(googleCredentials).await()
            val user = authResult.user
            val isNewUser = authResult.additionalUserInfo?.isNewUser == true

            // Log user data if available
            if (isNewUser) {
                Log.d("SignInWithIntent", "New user data: ${user?.run { "ID: $uid, Username: $displayName, Profile URL: ${photoUrl?.toString()}" }}")
            } else {
                Log.d("SignInWithIntent", "Existing user data: Data not included")
            }

            SignInResult(
                data = if (isNewUser) {
                    user?.run {
                        UserData(
                            userId = uid,
                            username = displayName,
                            profilePictureUrl = photoUrl?.toString()
                        )
                    }
                } else {
                    // For existing users, set data to null
                    null
                },
                errorMessage = null,
                isNewUser = isNewUser
            )
        } catch(e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            SignInResult(
                data = null,
                errorMessage = e.message
            )
        }
    }

    suspend fun signInWithIntent(intent: Intent): SignInResult {
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = credential.googleIdToken
        val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
        return try {
            val authResult = auth.signInWithCredential(googleCredentials).await()
            val user = authResult.user
            val isNewUser = authResult.additionalUserInfo?.isNewUser == true

            // Logging user data based on whether the user is new or existing
            if (isNewUser) {
                Log.d("SignInWithIntent", "New user detected. User data will not be included.")
            } else {
                Log.d("SignInWithIntent", "Existing user data: ID: ${user?.uid}, Username: ${user?.displayName}, Profile URL: ${user?.photoUrl}")
            }

            SignInResult(
                data = if (!isNewUser) { // Only set data for existing users
                    user?.let {
                        UserData(
                            userId = it.uid,
                            username = it.displayName ?: "Unknown",
                            profilePictureUrl = it.photoUrl?.toString()
                        )
                    }
                } else {
                    // For new users, data is set to null
                    null
                },
                errorMessage = null,
                isNewUser = isNewUser
            )
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            SignInResult(
                data = null,
                errorMessage = e.message
            )
        }
    }



    suspend fun signOut() {
        try {
            oneTapClient.signOut().await()
            auth.signOut()
        } catch(e: Exception) {
            e.printStackTrace()
            if(e is CancellationException) throw e
        }
    }

    fun getSignedInUser(): UserData? = auth.currentUser?.run {
        UserData(
            userId = uid,
            username = displayName,
            profilePictureUrl = photoUrl?.toString()
        )
    }

    private fun buildSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.Builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.default_web_client_id))
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()
    }
}

