package com.example.assignme.ViewModel

// UserProfileProvider.kt
import androidx.lifecycle.LiveData

interface UserProfileProvider {
    val userId: LiveData<String>
    val userProfile: LiveData<UserProfile>
    fun setUserId(id: String)
    abstract fun fetchUserProfile(it: String)
}
