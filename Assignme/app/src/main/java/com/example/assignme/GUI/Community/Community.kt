package com.example.assignme.GUI.Community

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.assignme.AndroidBar.AppBottomNavigation
import com.example.assignme.AndroidBar.AppTopBar
import com.example.assignme.DataClass.WindowInfo
import com.example.assignme.DataClass.rememberWidowInfo
import com.example.assignme.R
import com.example.assignme.ViewModel.Comment
import com.example.assignme.ViewModel.ThemeViewModel
import com.example.assignme.ViewModel.UserProfile
import com.example.assignme.ViewModel.UserViewModel


@Composable
fun SocialAppUI(navController: NavController, userViewModel: UserViewModel, themeViewModel: ThemeViewModel) {
    val windowInfo = rememberWidowInfo()
    val userProfile by userViewModel.userProfile.observeAsState(UserProfile())
    val userName = userProfile.name ?: "User"
    var selectedTab by remember { mutableStateOf(0) }

    // 使用 MaterialTheme 来设置整体主题
    MaterialTheme(
        colors = if (themeViewModel.isDarkTheme.value) {
            darkColors()
        } else {
            lightColors()
        }
    ) {
        Scaffold(
            topBar = { AppTopBar(title = "Welcome $userName,", navController = navController) },
            bottomBar = { AppBottomNavigation(navController) },
            // 在这里为 Scaffold 的内容添加背景颜色
//            modifier = Modifier.background(MaterialTheme.colors.onSurface)
        ) { innerPadding ->
            when (windowInfo.screenWidthInfo) {
                is WindowInfo.WindowType.Compact -> CompactLayout(
                    innerPadding = innerPadding,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    userViewModel = userViewModel,
                    themeViewModel = themeViewModel
                )
                is WindowInfo.WindowType.Medium -> MediumLayout(
                    innerPadding = innerPadding,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    userViewModel = userViewModel,
                    themeViewModel = themeViewModel
                )
                is WindowInfo.WindowType.Expanded -> ExpandedLayout(
                    innerPadding = innerPadding,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    userViewModel = userViewModel,
                    themeViewModel = themeViewModel
                )
            }
        }
    }
}

@Composable
fun TabRowContent(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    themeViewModel: ThemeViewModel
) {
//    val backgroundColor = if (themeViewModel.isDarkTheme.value) {
//        MaterialTheme.colors.surface // 深色主题使用默认的surface颜色
//    } else {
//        Color(0xFFE77575)
//    }
//
//    val contentColor = if (themeViewModel.isDarkTheme.value) {
//        MaterialTheme.colors.onSurface // 深色主题使用默认的onSurface颜色
//    } else {
//        Color.White
//    }

    TabRow(
        selectedTabIndex = selectedTab,
//        backgroundColor = backgroundColor,
//        contentColor = contentColor,
        modifier = Modifier.height(50.dp)
    ) {
        Tab(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            text = { Text("Community") }
        )
        Tab(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            text = { Text("My Posts") }
        )
    }
}

@Composable
fun CompactLayout(
    innerPadding: PaddingValues,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    userViewModel: UserViewModel,
    themeViewModel: ThemeViewModel
) {
    Column(modifier = Modifier.padding(innerPadding)
//        .background(MaterialTheme.colors.background)
    ) {
        TabRowContent(selectedTab, onTabSelected,themeViewModel)
        PostComposer(userViewModel)
        if (selectedTab == 0) {
            PostList(userViewModel)
        } else {
            MyPostList(userViewModel)
        }
    }
}

@Composable
fun MediumLayout(
    innerPadding: PaddingValues,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    userViewModel: UserViewModel,
    themeViewModel: ThemeViewModel
) {
    Column(modifier = Modifier.padding(innerPadding)) {
        TabRowContent(selectedTab, onTabSelected, themeViewModel)
        Row {
            Column(modifier = Modifier.weight(1f)) {
                PostComposer(userViewModel)
                if (selectedTab == 0) {
                    PostList(userViewModel)
                } else {
                    MyPostList(userViewModel)
                }
            }
            Column(modifier = Modifier.width(200.dp)) {
                // Additional content or controls can be added here
            }
        }
    }
}

@Composable
fun ExpandedLayout(
    innerPadding: PaddingValues,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    userViewModel: UserViewModel,
    themeViewModel: ThemeViewModel
) {
    Column(modifier = Modifier.padding(innerPadding)) {
        TabRowContent(selectedTab, onTabSelected,themeViewModel)
        Row {
            Column(modifier = Modifier.weight(1f)) {
                PostComposer(userViewModel)
                PostList(userViewModel)
            }
            Column(modifier = Modifier.weight(1f)) {
                MyPostList(userViewModel)
            }
        }
    }
}


@Composable
fun PostComposer(userViewModel: UserViewModel) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var postText by remember { mutableStateOf("") }
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
            Image(
                painter = rememberImagePainter(profilePictureUrl ?: R.drawable.profile),
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
                    modifier = Modifier.size(30.dp)
                )
            }
        }

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

        Button(
            onClick = {
                selectedImageUri?.let { uri ->
                    userViewModel.addPost(postText, uri) // 上传图片
                    postText = ""  // 清空文本框
                    selectedImageUri = null  // 清空选择的图片
                } ?: run {
                    userViewModel.addPost(postText, null) // 没有选择图片
                    postText = ""  // 清空文本框
                }
            },
            enabled = postText.isNotEmpty(),
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 8.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFFE23E3E),  // 设置背景颜色为红色
                contentColor = Color.White    // 设置文本颜色为白色
            )
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
    imagePath: String? = null,
    likedUsers: List<String>,
    timestamp: Long,
    isMyPost: Boolean
) {
    val currentUserId = userViewModel.userId.value ?: ""
    var liked by remember { mutableStateOf(likedUsers.contains(currentUserId)) }
    var currentLikes by remember { mutableStateOf(likes) }
    var showCommentsDialog by remember { mutableStateOf(false) }
    var currentComments by remember { mutableStateOf(comments) }
    var showImageDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var editedContent by remember { mutableStateOf(content) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Row for Author and Post options (edit, delete, report)
            Row {
                Image(
                    painter = rememberImagePainter(authorImageUrl),
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, androidx.compose.material3.MaterialTheme.colorScheme.primary, CircleShape)
                )
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(
                        text = authorName,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = userViewModel.formatTimestamp(timestamp),
                        style = MaterialTheme.typography.body2,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Box {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(
                            painter = painterResource(id = R.drawable.dot),
                            contentDescription = "More Options",
                            modifier = Modifier.size(25.dp)
                        )
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        if (isMyPost) {
                            DropdownMenuItem(onClick = {
                                showMenu = false
                                showEditDialog = true
                            }) {
                                Text("Edit")
                            }
                            DropdownMenuItem(onClick = {
                                showMenu = false
                                userViewModel.deletePost(postId)
                            }) {
                                Text("Delete")
                            }
                        } else {
                            DropdownMenuItem(onClick = {
                                showMenu = false
                                showReportDialog = true
                            }) {
                                Text("Report")
                            }
                        }
                    }
                }
            }

            // Post content
            Text(text = content, modifier = Modifier.padding(vertical = 8.dp))

            // Post Image (if available)
            imagePath?.let { imageUrl ->
                Image(
                    painter = rememberImagePainter(data = imageUrl),
                    contentDescription = "Post image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clickable { showImageDialog = true },
                    contentScale = ContentScale.Crop
                )
            }

            // Like and comment buttons
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
                        painter = painterResource(id = if (liked) R.drawable.like else R.drawable.like),
                        contentDescription = "Like",
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    "$currentLikes likes",
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .align(Alignment.CenterVertically)
                )

                Spacer(modifier = Modifier.weight(1f))

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
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }

    // Image Dialog
    if (showImageDialog && imagePath != null) {
        ImageDialog(
            imageUrl = imagePath,
            onDismiss = { showImageDialog = false }
        )
    }

    // Comments Dialog
    if (showCommentsDialog) {
        CommentsDialog(
            onDismiss = { showCommentsDialog = false },
            postId = postId,
            userViewModel = userViewModel,
            currentUserName = userViewModel.userProfile.value?.name ?: "Anonymous",
            onCommentAdded = {
                currentComments++
            }
        )
    }

    // Edit Post Dialog
    if (showEditDialog) {
        EditPostDialog(
            currentContent = editedContent,
            onDismiss = {
                showEditDialog = false
                editedContent = content
            },
            onConfirm = { newContent ->
                editedContent = newContent
                userViewModel.updatePost(postId, newContent)
                showEditDialog = false
            }
        )
    }

    // Report Post Dialog
    if (showReportDialog) {
        ReportDialog(
            onDismiss = { showReportDialog = false },
            onReport = { reason, reportedBy ->
                userViewModel.reportPost(postId, reason, reportedBy) // 传递举报者 ID
                showReportDialog = false
            },
            reportedBy = currentUserId // 传递当前用户 ID
        )
    }
}


@Composable
fun ReportDialog(
    onDismiss: () -> Unit,
    onReport: (String, String) -> Unit, // 接收举报原因和举报者 ID
    reportedBy: String // 新增参数：举报者 ID
) {
    var reportReason by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Report Post", style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = reportReason,
                    onValueChange = { reportReason = it },
                    label = { Text("Reason for reporting") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onReport(reportReason, reportedBy) // 传递举报原因和举报者 ID
                        },
                        enabled = reportReason.isNotBlank()
                    ) {
                        Text("Submit Report")
                    }
                }
            }
        }
    }
}

@Composable
fun ImageDialog(imageUrl: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        val imagePainter = rememberImagePainter(data = imageUrl)

        Box(
            modifier = Modifier
                .wrapContentSize() // 根据内容大小调整
                .padding(16.dp)
        ) {
            // 图片
            Image(
                painter = imagePainter,
                contentDescription = "Enlarged post image",
                modifier = Modifier
                    .fillMaxWidth() // 使图片宽度填满对话框
                    .wrapContentHeight() // 根据图片高度调整
                    .scale(1.25f), // 略微放大图片
                contentScale = ContentScale.Fit // 确保图片适应对话框
            )

            // 关闭按钮
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd) // 放置在右上角
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.LightGray // 设置图标颜色为白色
                )
            }
        }
    }
}

@Composable
fun EditPostDialog(currentContent: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var newContent by remember { mutableStateOf(currentContent) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Edit Post", style = MaterialTheme.typography.h6)
                TextField(
                    value = newContent,
                    onValueChange = { newContent = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Update your post...") }
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    TextButton(onClick = {
                        onConfirm(newContent)
                    }) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}


@Composable
fun CommentsDialog(
    onDismiss: () -> Unit,
    postId: String,
    userViewModel: UserViewModel,
    currentUserName: String,
    onCommentAdded: () -> Unit
) {
    var commentText by remember { mutableStateOf("") }

    val comments by userViewModel.getCommentsForPost(postId).observeAsState(emptyList())

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(1.0f)
                .fillMaxHeight(0.8f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 标题栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Comments", style = MaterialTheme.typography.h6)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // 评论列表
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(comments) { comment ->
                        val profileImageUrl =
                            comment.userProfileImage.ifEmpty { R.drawable.profile.toString() }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = rememberImagePainter(profileImageUrl),
                                contentDescription = "Profile picture",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                            )

                            Text(
                                text = "${comment.userName}: ${comment.content} at ${
                                    userViewModel.formatTimestamp(
                                        comment.timestamp
                                    )
                                }",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        Divider()
                    }
                }

                // 输入框和发送按钮
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    TextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        label = { Text("Write a comment") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val newComment = Comment(
                                userName = currentUserName,
                                content = commentText,
                                timestamp = System.currentTimeMillis(),
                                userProfileImage = userViewModel.userProfile.value?.profilePictureUrl
                                    ?: ""
                            )
                            userViewModel.addComment(postId, newComment)
                            onCommentAdded()
                            commentText = ""
                        }
                    ) {
                        Text("Send")
                    }
                }
            }
        }
    }
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
            items(posts.reversed(),key = { it.id }) { post ->
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
                    userViewModel = userViewModel,
                    likedUsers = post.likedUsers,
                    timestamp = post.timestamp, // 确保传递时间戳
                    isMyPost = false // 这是公共帖子
                )
            }
        }
    }
}

@Composable
fun MyPostList(userViewModel: UserViewModel) {
    val posts by userViewModel.posts.observeAsState(emptyList())
    val users by userViewModel.users.observeAsState(emptyMap())

    // 过滤出用户的帖子
    val myPosts = posts.filter { it.userId == userViewModel.userId.value }

    LazyColumn {
        if (myPosts.isEmpty()) {
            item {
                Text("No posts available", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.body1)
            }
        } else {
            items(myPosts.reversed(), key = { it.id }) { post ->
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
                    userViewModel = userViewModel,
                    likedUsers = post.likedUsers,
                    timestamp = post.timestamp, // 确保传递时间戳
                    isMyPost = true // 这是用户的帖子
                )
            }
        }
    }
}



