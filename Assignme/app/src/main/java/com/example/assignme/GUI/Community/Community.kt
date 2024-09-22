package com.example.assignme.GUI.Community

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.assignme.AndroidBar.AppBottomNavigation
import com.example.assignme.R
import com.example.assignme.ViewModel.Comment
import com.example.assignme.ViewModel.UserProfile
import com.example.assignme.ViewModel.UserViewModel


@Composable
fun SocialAppUI(navController: NavController, userViewModel: UserViewModel) {
    val userProfile by userViewModel.userProfile.observeAsState(UserProfile())
    val userName = userProfile.name ?: "User" // 使用用户的名字或默认值

    Scaffold(
        topBar = { TopAppBar(title = { Text("Welcome $userName,") }) },
        bottomBar = { AppBottomNavigation(navController) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            PostComposer(userViewModel)
            PostList(userViewModel) // Display the list of posts
        }
    }
}


@Composable
fun PostComposer(userViewModel: UserViewModel) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var postText by remember { mutableStateOf("") }

    // 从用户视图模型获取用户头像 URL
    val userProfile by userViewModel.userProfile.observeAsState(UserProfile())
    val profilePictureUrl = userProfile.profilePictureUrl

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            selectedImageUri = uri
        }
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 使用用户的头像或默认头像
            Image(
                painter = rememberImagePainter(profilePictureUrl ?: R.drawable.profile), // 替换为默认头像
                contentDescription = "Profile picture",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )

            TextField(
                value = postText,
                onValueChange = { postText = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                placeholder = { Text("What's on your mind?") }
            )

            IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                Icon(
                    painter = painterResource(id = R.drawable.addimage),
                    contentDescription = "Add image",
                    modifier = Modifier.size(30.dp) // Set the size of the icon here
                )
            }
        }

        // Show selected image
        if (selectedImageUri != null) {
            Image(
                painter = rememberImagePainter(selectedImageUri),
                contentDescription = "Selected image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(top = 8.dp)
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )
        }

        // Post button
        Button(
            onClick = {
                userViewModel.addPost(postText, selectedImageUri.toString()) // Convert URI to string
                postText = ""  // Clear text after posting
                selectedImageUri = null  // Clear image after posting
            },
            enabled = postText.isNotEmpty(),
            modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
        ) {
            Text("Post")
        }
    }
}

@Composable
fun PostItem(
    authorName: String,
    authorImageUrl: String,
    content: String,
    likes: Int,
    comments: Int,
    postId: String,
    userViewModel: UserViewModel,
    imagePath: String? = null // 可选参数
) {
    var liked by remember { mutableStateOf(false) }
    var showCommentsDialog by remember { mutableStateOf(false) }
    var currentLikes by remember { mutableStateOf(likes) }
    var currentComments by remember { mutableStateOf(comments) } // 添加当前评论数量

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                Image(
                    painter = rememberImagePainter(authorImageUrl),
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                )
                Text(
                    text = authorName,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Text(text = content, modifier = Modifier.padding(vertical = 8.dp))

            imagePath?.let {
                Image(
                    painter = rememberImagePainter(data = it),
                    contentDescription = "Post image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = {
                    liked = !liked
                    currentLikes = if (liked) currentLikes + 1 else currentLikes - 1
                    userViewModel.toggleLike(postId, liked)
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.like),
                        contentDescription = "Like",
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    "$currentLikes likes",
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .align(Alignment.CenterVertically) // 让文本垂直居中
                )

                Spacer(modifier = Modifier.weight(1f)) // 占据剩余空间

                IconButton(onClick = { showCommentsDialog = true }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_comment),
                        contentDescription = "Comment",
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    "$currentComments comments",
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .align(Alignment.CenterVertically) // 让文本垂直居中
                )
            }


        }
    }

    if (showCommentsDialog) {
        CommentsDialog(
            onDismiss = { showCommentsDialog = false },
            postId = postId,
            userViewModel = userViewModel,
            currentUserName = userViewModel.userProfile.value?.name ?: "Anonymous", // 获取当前用户名
            onCommentAdded = {
                currentComments++ // 每次添加评论时更新评论数量
            }
        )
    }
}



@Composable
fun CommentsDialog(
    onDismiss: () -> Unit,
    postId: String,
    userViewModel: UserViewModel,
    currentUserName: String, // Add current userName
    onCommentAdded: () -> Unit // Callback to trigger UI refresh after comment is added
) {
    var commentText by remember { mutableStateOf("") }

    // Observe comments LiveData from ViewModel
    val comments by userViewModel.getCommentsForPost(postId).observeAsState(emptyList())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Comments") },
        text = {
            Column {
                // Display each comment
                comments.forEach { comment ->
                    Text("${comment.userName}: ${comment.content} at ${userViewModel.formatTimestamp(comment.timestamp)}")
                }
                TextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    label = { Text("Write a comment") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newComment = Comment(
                        userName = currentUserName,
                        content = commentText,
                        timestamp = System.currentTimeMillis()
                    )
                    userViewModel.addComment(postId, newComment) // Add comment via ViewModel
                    onCommentAdded() // Trigger callback to refresh UI or state
                    commentText = "" // Clear text field
                }
            ) {
                Text("Send")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}



@Composable
fun PostList(userViewModel: UserViewModel) {
    val posts by userViewModel.posts.observeAsState(emptyList())
    val users by userViewModel.users.observeAsState(emptyMap())

    LazyColumn {
        if (posts.isEmpty()) {
            item {
                Text("No posts available", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.body1)
            }
        } else {
            items(posts) { post ->
                val author = users[post.userId]
                val authorName = author?.name ?: "User"
                val authorImageUrl = author?.profilePictureUrl ?: R.drawable.profile.toString()

                PostItem(
                    authorName = authorName,
                    authorImageUrl = authorImageUrl,
                    content = post.content,
                    imagePath = post.imagePath,
                    likes = post.likes,
                    comments = post.comments,
                    postId = post.id,
                    userViewModel = userViewModel
                )
            }
        }
    }
}





