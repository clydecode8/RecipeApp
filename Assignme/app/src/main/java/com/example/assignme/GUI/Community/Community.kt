package com.example.assignme.GUI.Community

import android.net.Uri
import android.widget.FrameLayout
import android.widget.ListPopupWindow.MATCH_PARENT
import android.widget.Toast
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
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.assignme.AndroidBar.AppBottomNavigation
import com.example.assignme.AndroidBar.AppTopBar
import com.example.assignme.DataClass.WindowInfo
import com.example.assignme.DataClass.rememberWidowInfo
import com.example.assignme.R
import com.example.assignme.ViewModel.Comment
import com.example.assignme.ViewModel.UserProfile
import com.example.assignme.ViewModel.UserViewModel
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.exoplayer2.MediaItem
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialAppUI(navController: NavController, userViewModel: UserViewModel) {
    val windowInfo = rememberWidowInfo()
    val userProfile by userViewModel.userProfile.observeAsState(UserProfile())
    val userName = userProfile.name ?: "User"
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf(TextFieldValue()) }
    var isSearching by remember { mutableStateOf(false) }


    Scaffold(
        topBar = {
            if (isSearching) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = {
                        searchQuery = it
                        if (it.text.isEmpty()) {
                            // Reset search results when the query is cleared
                            userViewModel.clearSearchResults()
                        }
                    },
                    onSearch = {
                        userViewModel.searchUsers(it.text)
                        isSearching = false
                    },
                    onClose = {
                        isSearching = false
                        searchQuery = TextFieldValue() // Clear the search query
                        userViewModel.clearSearchResults() // Reset to all posts
                    }
                )
            } else {
                TopAppBar(
                    title = { Text("Welcome $userName") },
                    actions = {
                        IconButton(onClick = { isSearching = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                )
            }
        },
            bottomBar = { AppBottomNavigation(navController) },
        ) { innerPadding ->
            when (windowInfo.screenWidthInfo) {
                is WindowInfo.WindowType.Compact -> CompactLayout(
                    innerPadding = innerPadding,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    userViewModel = userViewModel,
                )
                is WindowInfo.WindowType.Medium -> MediumLayout(
                    innerPadding = innerPadding,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    userViewModel = userViewModel,
                )
                is WindowInfo.WindowType.Expanded -> ExpandedLayout(
                    innerPadding = innerPadding,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    userViewModel = userViewModel,
                )
            }
        }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: TextFieldValue,
    onQueryChange: (TextFieldValue) -> Unit,
    onSearch: (TextFieldValue) -> Unit,
    onClose: () -> Unit
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search users...") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        leadingIcon = {
            IconButton(onClick = { onSearch(query) }) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
        },
        trailingIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }
    )
}

@Composable
fun TabRowContent(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,

) {
    TabRow(
        selectedTabIndex = selectedTab,
//        backgroundColor = backgroundColor,
//        contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
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
) {
    Column(modifier = Modifier.padding(innerPadding)
//        .background(MaterialTheme.colors.background)
    ) {
        TabRowContent(selectedTab, onTabSelected)
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
) {
    Column(modifier = Modifier.padding(innerPadding)) {
        TabRowContent(selectedTab, onTabSelected)
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
) {
    Column(modifier = Modifier.padding(innerPadding)) {
        TabRowContent(selectedTab, onTabSelected)
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
    var selectedMediaUri by remember { mutableStateOf<Uri?>(null) }
    var postText by remember { mutableStateOf("") }
    var mediaType by remember { mutableStateOf<String?>(null) }
    var showMessage by remember { mutableStateOf(false) } // 用于显示消息
    val userProfile by userViewModel.userProfile.observeAsState(UserProfile())
    val profilePictureUrl = userProfile.profilePictureUrl


    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            selectedMediaUri = uri
            mediaType = "image"
        }
    )

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            selectedMediaUri = uri
            mediaType = "video"
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

            IconButton(onClick = { videoPickerLauncher.launch("video/*") }) {
                Icon(
                    painter = painterResource(id = R.drawable.addvideo),
                    contentDescription = "Add video",
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        if (selectedMediaUri != null) {
            when (mediaType) {
                "image" -> Image(
                    painter = rememberImagePainter(selectedMediaUri),
                    contentDescription = "Selected image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(top = 8.dp)
                        .background(Color.Gray),
                    contentScale = ContentScale.Crop
                )
                "video" -> VideoPlayer(
                    videoUri = selectedMediaUri!!,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(top = 8.dp)
                )
            }
        }

        Button(
            onClick = {
                // 上传帖子
                selectedMediaUri?.let { uri ->
                    userViewModel.addPost(postText, uri, mediaType ?: "")
                    postText = ""
                    selectedMediaUri = null
                    mediaType = null
                    showMessage = true // 上传成功，显示消息
                } ?: run {
                    userViewModel.addPost(postText, null, "")
                    postText = ""
                    showMessage = true // 上传成功，显示消息
                }
            },
            enabled = postText.isNotEmpty(),
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 8.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFFE23E3E),
                disabledBackgroundColor = Color(0xFFEA5959),
                contentColor = Color.White,
                disabledContentColor = Color.White.copy(alpha = 0.7f)
            )
        ) {
            Text("Post")
        }
    }
    val context = LocalContext.current
    // 显示成功消息的 Toast
    if (showMessage) {
        // 这个LaunchedEffect会在showMessage为true时启动
        LaunchedEffect(showMessage) {
            // 使用 LocalContext.current 来显示 Toast
            Toast.makeText(context, "Post successfully!", Toast.LENGTH_SHORT).show()

            // 等待一段时间后重置消息状态
            kotlinx.coroutines.delay(2000) // 2秒后隐藏消息
            showMessage = false // 重置状态
        }
    }
}


@Composable
fun VideoPlayer(videoUri: Uri, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val exoPlayer = remember {
        SimpleExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(videoUri)  // Create a MediaItem from the Uri
            setMediaItem(mediaItem)  // Set the media item
            prepare()  // Prepare the player
        }
    }

    DisposableEffect(
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                }
            },
            modifier = modifier
        )
    ) {
        onDispose {
            exoPlayer.release()  // Release the player on dispose
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
    videoPath: String? = null,
    mediaType: String? = null,
    likedUsers: List<String>,
    timestamp: Long,
    isMyPost: Boolean
) {
    val context = LocalContext.current
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
    var showFullscreenVideo by remember { mutableStateOf(false) }
    var showReportSuccessMessage by remember { mutableStateOf(false) }
    var showEditSuccessMessage by remember { mutableStateOf(false) }
    var showDeleteSuccessMessage by remember { mutableStateOf(false) }
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
                                showDeleteSuccessMessage = true
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
            // Toast 消息显示
            if (showDeleteSuccessMessage) {
                LaunchedEffect(showDeleteSuccessMessage) {
                    Toast.makeText(context, "Delete successfully!", Toast.LENGTH_SHORT).show()
                    kotlinx.coroutines.delay(2000) // 等待2秒
                    showDeleteSuccessMessage = false // 重置状态
                }
            }
            // Post content
            Text(text = content, modifier = Modifier.padding(vertical = 8.dp))

            // Post Media (Image or Video)
            when (mediaType) {
                "image" -> imagePath?.let { imageUrl ->
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
                "video" -> videoPath?.let { videoUrl ->
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                        VideoPlayer(
                            videoUri = Uri.parse(videoUrl),
                            modifier = Modifier.fillMaxSize()
                        )
                        IconButton(
                            onClick = { showFullscreenVideo = true },
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.fullscreen), // Replace with your drawable name
                                contentDescription = "Fullscreen",
                                modifier = Modifier.size(24.dp)
                                    .graphicsLayer(alpha = 0.5f), // Adjust size as needed
                                tint = Color.White
                            )
                        }
                    }
                }
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
                        painter = painterResource(id = R.drawable.like), // 使用同一个图标
                        contentDescription = "Like",
                        tint = if (liked) Color.Blue else Color.Gray, // 根据 liked 状态设置颜色
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

    // 全屏视频对话框
    if (showFullscreenVideo && videoPath != null) {
        FullscreenVideoDialog(
            videoUri = Uri.parse(videoPath),
            onDismiss = { showFullscreenVideo = false }
        )
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
                showEditSuccessMessage = true
            }
        )
    }

    // Toast 消息显示
    if (showEditSuccessMessage) {
        LaunchedEffect(showEditSuccessMessage) {
            Toast.makeText(context, "Edit successfully!", Toast.LENGTH_SHORT).show()
            kotlinx.coroutines.delay(2000) // 等待2秒
            showEditSuccessMessage = false // 重置状态
        }
    }

    if (showReportDialog) {
        ReportDialog(
            onDismiss = { showReportDialog = false },
            onReport = { reason, reportedBy ->
                userViewModel.reportPost(postId, reason, reportedBy) // 传递举报者 ID
                showReportDialog = false
                showReportSuccessMessage = true // 显示成功消息
            },
            reportedBy = currentUserId // 传递当前用户 ID
        )
    }


    // Toast 消息显示
    if (showReportSuccessMessage) {
        LaunchedEffect(showReportSuccessMessage) {
            Toast.makeText(context, "Report successfully!", Toast.LENGTH_SHORT).show()
            kotlinx.coroutines.delay(2000) // 等待2秒
            showReportSuccessMessage = false // 重置状态
        }
    }
}

@Composable
fun FullscreenVideoDialog(videoUri: Uri, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box(modifier = Modifier.fillMaxSize()) {
            VideoPlayer(videoUri = videoUri, modifier = Modifier.fillMaxSize())
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
            }
        }
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
                Text("Report Post")
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
                        enabled = reportReason.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFFE23E3E), // 按钮背景颜色
                            disabledBackgroundColor = Color(0xFFEA5959), // 按钮禁用时的背景颜色
                            contentColor = Color.White, // 按钮文本颜色
                            disabledContentColor = Color.White.copy(alpha = 0.7f) // 按钮禁用时的文本颜色
                        )
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
                Text("Edit Post")
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
    var showToast by remember { mutableStateOf(false) }
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
                    Text(text = "Comments")
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
//                        Divider()
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
                            showToast = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFFE23E3E),
                            contentColor = Color.White// 设置背景颜色为红色
                        )
                    ) {
                        Text("Send")
                    }
                }
            }
        }
    }
    val context = LocalContext.current
    // Toast 消息显示
    if (showToast) {
        LaunchedEffect(showToast) {
            Toast.makeText(context, "Comment successfully!", Toast.LENGTH_SHORT).show()
            kotlinx.coroutines.delay(2000) // 等待2秒
            showToast = false // 重置状态
        }
    }
}
@Composable
fun PostList(userViewModel: UserViewModel) {
    val posts by userViewModel.posts.observeAsState(emptyList())
    val users by userViewModel.users.observeAsState(emptyMap())
    val searchResults by userViewModel.searchResults.observeAsState(emptyList())

    LazyColumn {
        if (searchResults.isNotEmpty()) {
            items(searchResults, key = { it.id }) { post ->
                val author = users[post.userId]
                val authorName = author?.name ?: "User"
                val authorImageUrl = author?.profilePictureUrl ?: R.drawable.profile.toString()

                PostItem(
                    authorName = authorName,
                    authorImageUrl = authorImageUrl,
                    content = post.content,
                    imagePath = post.imagePath,
                    videoPath = post.videoPath,
                    mediaType = post.mediaType,
                    likes = post.likes,
                    comments = post.comments,
                    postId = post.id,
                    userViewModel = userViewModel,
                    likedUsers = post.likedUsers,
                    timestamp = post.timestamp,
                    isMyPost = false
                )
            }
        } else {
            items(posts.reversed(), key = { it.id }) { post ->
                val author = users[post.userId]
                val authorName = author?.name ?: "User"
                val authorImageUrl = author?.profilePictureUrl ?: R.drawable.profile.toString()

                PostItem(
                    authorName = authorName,
                    authorImageUrl = authorImageUrl,
                    content = post.content,
                    imagePath = post.imagePath,
                    videoPath = post.videoPath, // 新增视频路径参数
                    mediaType = post.mediaType, // 新增媒体类型参数
                    likes = post.likes,
                    comments = post.comments,
                    postId = post.id,
                    userViewModel = userViewModel,
                    likedUsers = post.likedUsers,
                    timestamp = post.timestamp,
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
                Text("No posts available", modifier = Modifier.padding(16.dp))
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
                    videoPath = post.videoPath, // 新增视频路径参数
                    mediaType = post.mediaType, // 新增媒体类型参数
                    likes = post.likes,
                    comments = post.comments,
                    postId = post.id,
                    userViewModel = userViewModel,
                    likedUsers = post.likedUsers,
                    timestamp = post.timestamp,
                    isMyPost = true // 这是用户的帖子
                )
            }
        }
    }
}



