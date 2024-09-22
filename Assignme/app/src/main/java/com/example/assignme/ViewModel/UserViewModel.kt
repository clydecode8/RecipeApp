package com.example.assignme.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


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
        db.collection("posts").document(postId)
            .update("likes", if (liked) FieldValue.increment(1) else FieldValue.increment(-1))
            .addOnSuccessListener {
                fetchPosts() // 刷新帖子以获取最新点赞数量
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
            "timestamp" to comment.timestamp
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


    fun addPost(content: String, imagePath: String?) {
        val db = FirebaseFirestore.getInstance()
        val post = Post(userId = _userId.value ?: "", content = content, imagePath = imagePath)

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

    fun fetchUsers() {
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

    fun fetchPosts() {
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
                    commentsLiveData.value = emptyList() // Handle failure by returning an empty list
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val comments = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Comment::class.java)
                    }
                    commentsLiveData.value = comments
                } else {
                    Log.d("UserViewModel", "No comments found")
                    commentsLiveData.value = emptyList() // Return empty list if no comments are found
                }
            }

        return commentsLiveData
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
    val comments: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

data class Comment(
    val userName: String = "",
    val content: String = "",
    val timestamp: Long = 0L
)




