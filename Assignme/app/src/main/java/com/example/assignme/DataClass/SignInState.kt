package com.example.assignme.DataClass

data class SignInState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null
)