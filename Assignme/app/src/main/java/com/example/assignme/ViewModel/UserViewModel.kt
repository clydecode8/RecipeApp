package com.example.assignme.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore


class UserViewModel : ViewModel(), UserProfileProvider {
    private val _userId = MutableLiveData<String>()
    override val userId: LiveData<String> get() = _userId

    private val _userProfile = MutableLiveData<UserProfile>()
    override val userProfile: LiveData<UserProfile> get() = _userProfile

    // Set userId and fetch user profile
    override fun setUserId(id: String) {
        _userId.value = id
        fetchUserProfile(id)
    }

    override fun fetchUserProfile(userId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val profile = document.toObject(UserProfile::class.java)
                    _userProfile.value = profile
                } else {
                    Log.d("UserViewModel", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("UserViewModel", "Get failed with ", exception)
            }
    }
}

data class UserProfile(
    val name: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val profilePictureUrl: String? = null,
    val gender: String? = null,
    val country: String? = null
)

