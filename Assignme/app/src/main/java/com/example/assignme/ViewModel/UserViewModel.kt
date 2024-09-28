package com.example.assignme.ViewModel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID


class UserViewModel : ViewModel(), UserProfileProvider {
    private val _userId = MutableLiveData<String>()
    override val userId: LiveData<String> get() = _userId

    private val _userProfile = MutableLiveData<UserProfile>()
    override val userProfile: LiveData<UserProfile> get() = _userProfile

    private val _adminProfile = MutableLiveData<AdminProfile>()
    override val adminProfile: LiveData<AdminProfile> get() = _adminProfile

    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> get() = _posts

    private val _users = MutableLiveData<Map<String, UserProfile>>()
    val users: LiveData<Map<String, UserProfile>> get() = _users

    private val _reportedPosts = MutableLiveData<List<Post>>()
    val reportedPosts: LiveData<List<Post>> = _reportedPosts

    override fun setUserId(id: String) {
        _userId.value = id
        if (!id.isNullOrEmpty()) {
            fetchUserProfile(id)
            Log.d("UserViewModel", "User ID is set, calling fetchPosts()")
            fetchPosts()
        } else {
            Log.w("UserViewModel", "User ID is null or empty")
        }
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
                    Log.d("UserViewModel", "No such document for user: $userId")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("UserViewModel", "Get failed with ", exception)
            }
    }

    override fun fetchAdminProfile(userId: String) {
        val db = FirebaseFirestore.getInstance()
        Log.d("UserViewModel", "Fetching admin profile for user ID: $userId")

        db.collection("admin").document(userId)
            .get()
            .addOnSuccessListener { document ->
                Log.d("UserViewModel", "Document retrieved successfully.")

                if (document != null) {
                    Log.d("UserViewModel", "Document exists: ${document.id}")
                    val profile = document.toObject(AdminProfile::class.java)
                    if (profile != null) {
                        _adminProfile.value = profile
                        Log.d("UserViewModel", "Fetched admin profile: $profile")
                    } else {
                        Log.d("UserViewModel", "Profile conversion resulted in null for user: $userId")
                    }
                } else {
                    Log.d("UserViewModel", "No such document for user: $userId")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("UserViewModel", "Get failed with exception: ${exception.message}")
            }
    }



    // Function to update the user profile in the ViewModel (Google)
    fun updateUserProfile(userId: String) {
        val db = FirebaseFirestore.getInstance()

        // Fetch user profile data from Firestore
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    // Map document to UserProfile
                    val userProfile = document.toObject(UserProfile::class.java)

                    // Update the user profile in the ViewModel
                    _userProfile.value = userProfile
                } else {
                    Log.d("UserViewModel", "No such document for user: $userId")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("UserViewModel", "Get failed with ", exception)
            }
    }

    fun toggleLike(postId: String, liked: Boolean) {
        val db = FirebaseFirestore.getInstance()
        val currentUserId = _userId.value ?: return

        // Fetch the post to update likes
        db.collection("posts").document(postId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val post = document.toObject(Post::class.java)
                    post?.let {
                        val isAlreadyLiked = post.likedUsers.contains(currentUserId)

                        if (liked && !isAlreadyLiked) {
                            // User likes the post
                            db.collection("posts").document(postId)
                                .update(
                                    "likes", FieldValue.increment(1),
                                    "likedUsers", FieldValue.arrayUnion(currentUserId)
                                )
                                .addOnSuccessListener {
                                    fetchUpdatedPost(postId)
                                }
                                .addOnFailureListener { e ->
                                    Log.w("UserViewModel", "Error updating like", e)
                                }
                        } else if (!liked && isAlreadyLiked) {
                            // User unlikes the post
                            db.collection("posts").document(postId)
                                .update(
                                    "likes", FieldValue.increment(-1),
                                    "likedUsers", FieldValue.arrayRemove(currentUserId)
                                )
                                .addOnSuccessListener {
                                    fetchUpdatedPost(postId)
                                }
                                .addOnFailureListener { e ->
                                    Log.w("UserViewModel", "Error updating unlike", e)
                                }
                        } else {
                            // Optional else for cases when the state doesn't change
                            Log.d("UserViewModel", "No changes to like state")
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.w("UserViewModel", "Error fetching post", e)
            }
    }


    private fun fetchUpdatedPost(postId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("posts").document(postId)
            .get()
            .addOnSuccessListener { document ->
                val updatedPost = document.toObject(Post::class.java)?.copy(id = document.id)
                if (updatedPost != null) {
                    // Update _posts LiveData with the new post
                    val currentPosts = _posts.value?.toMutableList() ?: mutableListOf()
                    val postIndex = currentPosts.indexOfFirst { it.id == postId }
                    if (postIndex != -1) {
                        currentPosts[postIndex] = updatedPost
                        _posts.value = currentPosts
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.w("UserViewModel", "Error fetching updated post", e)
            }
    }


    fun addComment(postId: String, comment: Comment) {
        val db = FirebaseFirestore.getInstance()
        val commentData = hashMapOf(
            "userName" to comment.userName,
            "content" to comment.content,
            "timestamp" to comment.timestamp,
            "userProfileImage" to comment.userProfileImage
        )

        db.collection("posts").document(postId).collection("comments")
            .add(commentData)
            .addOnSuccessListener {
                Log.d("UserViewModel", "Comment added successfully")
                updateCommentCount(postId)
            }
            .addOnFailureListener { e ->
                Log.w("UserViewModel", "Error adding comment", e)
            }
    }

    private fun updateCommentCount(postId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("posts").document(postId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val currentComments = document.getLong("comments")?.toInt() ?: 0
                    val newCommentsCount = currentComments + 1

                    db.collection("posts").document(postId)
                        .update("comments", newCommentsCount)
                        .addOnSuccessListener {
                            fetchUpdatedPost(postId) // Update local data
                            Log.d("UserViewModel", "Comments count updated successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.w("UserViewModel", "Error updating comments count", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.w("UserViewModel", "Error getting post document", e)
            }
    }


    fun addPost(content: String, mediaUri: Uri?, mediaType: String) {
        if (mediaUri != null) {
            val storageRef = FirebaseStorage.getInstance().reference.child("media/${UUID.randomUUID()}")
            storageRef.putFile(mediaUri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        val post = Post(
                            userId = _userId.value ?: "",
                            content = content,
                            imagePath = if (mediaType == "image") downloadUrl.toString() else null,
                            videoPath = if (mediaType == "video") downloadUrl.toString() else null,
                            mediaType = mediaType
                        )
                        savePostToFirestore(post)
                    }
                        .addOnFailureListener { e ->
                            Log.w("UserViewModel", "Error uploading media", e)
                        }
                }
                .addOnFailureListener { e ->
                    Log.w("UserViewModel", "Error uploading media", e)
                }
        } else {
            val post = Post(userId = _userId.value ?: "", content = content)
            savePostToFirestore(post)
        }
    }

    private fun savePostToFirestore(post: Post) {
        val db = FirebaseFirestore.getInstance()
        db.collection("posts")
            .add(post)
            .addOnSuccessListener {
                Log.d("UserViewModel", "Post added successfully")
                fetchPosts()
            }
            .addOnFailureListener { e ->
                Log.w("UserViewModel", "Error adding post", e)
            }
    }

    private fun fetchUsers() {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                val userMap = documents.associate { it.id to it.toObject(UserProfile::class.java) }
                _users.value = userMap
            }
            .addOnFailureListener { exception ->
                Log.w("UserViewModel", "Error getting users", exception)
            }
    }

    fun reportPost(postId: String, reason: String, reportedBy: String) {
        val db = FirebaseFirestore.getInstance()
        val postRef = db.collection("posts").document(postId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(postRef)
            val currentPost = snapshot.toObject(Post::class.java)
            if (currentPost != null) {
                val updatedPost = currentPost.copy(
                    reportReason = reason,
                    reportedBy = reportedBy // 更新举报者 ID
                )
                transaction.set(postRef, updatedPost)
            }
        }.addOnSuccessListener {
            // 更新本地数据
            val currentPosts = _posts.value?.toMutableList() ?: mutableListOf()
            val postIndex = currentPosts.indexOfFirst { it.id == postId }
            if (postIndex != -1) {
                currentPosts[postIndex] = currentPosts[postIndex].copy(
                    reportReason = reason,
                    reportedBy = reportedBy// 更新本地数据
                )
                _posts.value = currentPosts
                _reportedPosts.value = currentPosts.filter { it.reportReason != null }
            }
        }.addOnFailureListener { e ->
            Log.w("UserViewModel", "Error reporting post", e)
        }
    }

    private fun fetchPosts() {
        val db = FirebaseFirestore.getInstance()
        db.collection("posts")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { documents ->
                val postList = documents.mapNotNull { document ->
                    val post = document.toObject(Post::class.java)
                    post.copy(id = document.id)
                }
                _posts.value = postList
                _reportedPosts.value = postList.filter { it.reportReason != null }
                fetchUsers()
            }
            .addOnFailureListener { exception ->
                Log.w("UserViewModel", "Error getting posts", exception)
            }
    }

    fun formatTimestamp(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }

    fun getCommentsForPost(postId: String): LiveData<List<Comment>> {
        val db = FirebaseFirestore.getInstance()
        val commentsLiveData = MutableLiveData<List<Comment>>()

        db.collection("posts").document(postId).collection("comments")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("UserViewModel", "Listen failed.", e)
                    commentsLiveData.value =
                        emptyList() // Handle failure by returning an empty list
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val comments = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Comment::class.java)
                    }
                    commentsLiveData.value = comments
                } else {
                    Log.d("UserViewModel", "No comments found")
                    commentsLiveData.value =
                        emptyList() // Return empty list if no comments are found
                }
            }

        return commentsLiveData
    }

    //Par of My posts
    // 删除帖子
    fun deletePost(postId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("posts").document(postId)
            .delete()
            .addOnSuccessListener {
                Log.d("UserViewModel", "Post deleted successfully")
                fetchPosts() // 重新获取帖子列表
            }
            .addOnFailureListener { e ->
                Log.w("UserViewModel", "Error deleting post", e)
            }
    }

    fun ignoreReport(postId: String) {
        val db = FirebaseFirestore.getInstance()
        val postRef = db.collection("posts").document(postId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(postRef)
            val currentPost = snapshot.toObject(Post::class.java)
            if (currentPost != null) {
                val updatedPost = currentPost.copy(reportReason = null)
                transaction.set(postRef, updatedPost)
            }
        }.addOnSuccessListener {
            // Update local data
            val currentPosts = _posts.value?.toMutableList() ?: mutableListOf()
            val postIndex = currentPosts.indexOfFirst { it.id == postId }
            if (postIndex != -1) {
                currentPosts[postIndex] = currentPosts[postIndex].copy(reportReason = null)
                _posts.value = currentPosts

                // Update reportedPosts
                _reportedPosts.value = currentPosts.filter { it.reportReason != null }
            }
        }.addOnFailureListener { e ->
            Log.w("UserViewModel", "Error ignoring report", e)
        }
    }
    // 更新帖子的内容
    fun updatePost(postId: String, newContent: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("posts").document(postId)
            .update("content", newContent)
            .addOnSuccessListener {
                Log.d("UserViewModel", "Post updated successfully")
                fetchPosts() // 重新获取帖子列表
            }
            .addOnFailureListener { e ->
                Log.w("UserViewModel", "Error updating post", e)
            }
    }

    private fun saveTrackerDataToFirestore(userId: String, data: Map<String, Any?>) {
        val db = FirebaseFirestore.getInstance()
        // Save tracker data in the "tracker" collection
        db.collection("tracker").document(userId)
            .set(data, SetOptions.merge()) // Use merge to avoid overwriting other fields
            .addOnSuccessListener {
                Log.d("UserViewModel", "Tracker data saved successfully")
            }
            .addOnFailureListener { e ->
                Log.w("UserViewModel", "Error saving tracker data", e)
            }
    }

    // Function to save user data
    fun saveUserData(
        weight: String,
        height: String,
        imageUri: Uri?,
        userId: String,
        onComplete: (Boolean) -> Unit
    ) {
        val userData = mapOf(
            "weight" to weight,
            "height" to height,
            "bodyImageUrl" to imageUri?.toString() // Convert Uri to string
        )

        // Save user data to Firestore under the user's document
        val db = FirebaseFirestore.getInstance()
        db.collection("tracker").document(userId)
            .set(userData, SetOptions.merge()) // Merge with existing data if any
            .addOnSuccessListener {
                Log.d("UserViewModel", "User data saved successfully")
                saveTrackerDataToFirestore(userId, userData) // Save tracker data
                onComplete(true) // Notify success
            }
            .addOnFailureListener { e ->
                Log.w("UserViewModel", "Error saving user data", e)
                onComplete(false) // Notify failure
            }
    }

    fun fetchTrackerData(userId: String, onResult: (TrackerData?) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("tracker").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val weight = document.getString("weight")
                    val height = document.getString("height")
                    val bodyImageUrl = document.getString("bodyImageUrl")

                    // Create TrackerData object
                    val trackerData = TrackerData(
                        weight = weight ?: "",
                        height = height ?: "",
                        bodyImageUri = bodyImageUrl?.let { Uri.parse(it) }
                    )

                    onResult(trackerData) // Return the tracker data
                } else {
                    onResult(null) // No document found
                }
            }
            .addOnFailureListener { e ->
                Log.w("UserViewModel", "Error fetching tracker data", e)
                onResult(null) // Return null on failure
            }
    }

}


data class UserProfile(
    val name: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val profilePictureUrl: String? = null,
    val gender: String? = null,
    val country: String? = null,

)
data class AdminProfile(
    val name: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val profilePictureUrl: String? = null,
    val gender: String? = null,
    val country: String? = null,

)

data class Post(
    val id: String = "",
    val userId: String = "",
    val content: String = "",
    val imagePath: String? = null,
    val videoPath: String? = null,
    val mediaType: String? = null, // "image" or "video"
    val likes: Int = 0,
    val likedUsers: List<String> = emptyList(), // 存储点赞用户的 ID
    val comments: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val reportReason: String? = null,
    val reportedBy: String? = null
)


data class Comment(
    val userName: String = "",
    val content: String = "",
    val timestamp: Long = 0L,
    val userProfileImage: String = ""
)

data class TrackerData(
    val weight: String = 0f.toString(), // Default to 0f for float type
    val height: String = 0f.toString(), // Default to 0f for float type
    val bodyImageUri: Uri? = null,
)


