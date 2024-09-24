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

    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> get() = _posts

    private val _users = MutableLiveData<Map<String, UserProfile>>()
    val users: LiveData<Map<String, UserProfile>> get() = _users

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

    fun toggleLike(postId: String, liked: Boolean) {
        val db = FirebaseFirestore.getInstance()
        val currentUserId = _userId.value ?: return

        // 获取当前帖子的点赞信息
        db.collection("posts").document(postId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val post = document.toObject(Post::class.java)
                    post?.let {
                        val isAlreadyLiked = post.likedUsers.contains(currentUserId)

                        if (liked && !isAlreadyLiked) {
                            // 如果用户还没有点赞，则进行点赞
                            db.collection("posts").document(postId)
                                .update(
                                    "likes",
                                    FieldValue.increment(1),
                                    "likedUsers",
                                    FieldValue.arrayUnion(currentUserId) // 添加用户到 likedUsers 列表
                                )
                        } else if (!liked && isAlreadyLiked) {
                            // 如果用户已经点赞，则取消点赞
                            db.collection("posts").document(postId)
                                .update(
                                    "likes",
                                    FieldValue.increment(-1),
                                    "likedUsers",
                                    FieldValue.arrayRemove(currentUserId) // 从 likedUsers 列表中移除用户
                                )
                        } else {
                            // Else case for completeness (no action needed)
                            Log.d("UserViewModel", "No change in like status")
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.w("UserViewModel", "Error updating likes", e)
            }
    }


    fun addComment(postId: String, comment: Comment) {
        val db = FirebaseFirestore.getInstance()
        val commentData = hashMapOf(
            "userName" to comment.userName,
            "content" to comment.content,
            "timestamp" to comment.timestamp,
            "userProfileImage" to comment.userProfileImage // 添加头像 URL
        )

        db.collection("posts").document(postId).collection("comments")
            .add(commentData)
            .addOnSuccessListener {
                Log.d("UserViewModel", "Comment added successfully")
                updateCommentCount(postId) // 更新评论计数
            }
            .addOnFailureListener { e ->
                Log.w("UserViewModel", "Error adding comment", e)
            }
    }


    private fun updateCommentCount(postId: String) {
        val db = FirebaseFirestore.getInstance()

        // 获取当前评论数量
        db.collection("posts").document(postId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val currentComments = document.getLong("comments")?.toInt() ?: 0
                    val newCommentsCount = currentComments + 1

                    // 更新评论数量
                    db.collection("posts").document(postId)
                        .update("comments", newCommentsCount)
                        .addOnSuccessListener {
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


    fun addPost(content: String, imageUri: Uri?) {
        if (imageUri != null) {
            val storageRef =
                FirebaseStorage.getInstance().reference.child("images/${UUID.randomUUID()}")
            storageRef.putFile(imageUri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        val post = Post(
                            userId = _userId.value ?: "",
                            content = content,
                            imagePath = downloadUrl.toString()
                        )
                        savePostToFirestore(post)
                    }
                        .addOnFailureListener { e ->
                            Log.w("UserViewModel", "Error uploading image", e)
                        }
                }
                .addOnFailureListener { e ->
                    Log.w("UserViewModel", "Error uploading image", e)
                }
        } else {
            val post = Post(userId = _userId.value ?: "", content = content, imagePath = null)
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

    private fun fetchPosts() {
        val currentUserId = _userId.value
        if (currentUserId.isNullOrEmpty()) {
            Log.w("UserViewModel", "User ID is null or empty, skipping post fetch.")
            return
        }

        val db = FirebaseFirestore.getInstance()
        Log.d("UserViewModel", "Fetching posts for user ID: $currentUserId")
        db.collection("posts")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { documents ->
                val postList = documents.mapNotNull { document ->
                    val post = document.toObject(Post::class.java)
                    post.copy(id = document.id) // 设置 post 的 id
                }
                Log.d("UserViewModel", "Fetched posts: $postList")
                _posts.value = postList
                fetchUsers() // 获取用户信息
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
    val country: String? = null
)

data class Post(
    val id: String = "",
    val userId: String = "",
    val content: String = "",
    val imagePath: String? = null,
    val likes: Int = 0,
    val likedUsers: List<String> = emptyList(), // 存储点赞用户的 ID
    val comments: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
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


