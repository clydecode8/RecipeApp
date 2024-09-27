package com.example.assignme.GUI.AccountProfile

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.assignme.AndroidBar.AppTopBar
import com.example.assignme.R
import com.example.assignme.ViewModel.Post
import com.example.assignme.ViewModel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialFeedScreen(navController: NavController, userViewModel: UserViewModel) {
    val posts by userViewModel.posts.observeAsState(emptyList())

    Scaffold(
        topBar = { AppTopBar(title = "Back", navController = navController) }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            item { ReportsButton(navController) }
            item { TrendingSection() }
            items(posts) { post ->
                PostItem(post, userViewModel)
            }
        }
    }
}

@Composable
fun ReportsButton(navController: NavController) {
    Button(
        onClick = { navController.navigate("manageReportPost") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Reports")
        Spacer(Modifier.weight(1f))
        Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.rotate(180f))
    }
}

@Composable
fun TrendingSection() {
    Text(
        "Trending now 🔥",
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
fun PostItem(
    post: Post,
    userViewModel: UserViewModel
) {
    val users by userViewModel.users.observeAsState(emptyMap())
    val author = users[post.userId]
    val authorName = author?.name ?: "User"
    val authorImageUrl = author?.profilePictureUrl ?: R.drawable.profile.toString()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Profile Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = rememberImagePainter(authorImageUrl),
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = authorName,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = userViewModel.formatTimestamp(post.timestamp),
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }

            // Post Content
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )

            // Post Image (if available)
            post.imagePath?.let { imageUrl ->
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    painter = rememberImagePainter(data = imageUrl),
                    contentDescription = "Post image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            // Spacer after content and image
            Spacer(modifier = Modifier.height(8.dp))

            // Like and Comment Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                LikeButton(post.likes) // Display number of likes
                Spacer(modifier = Modifier.width(16.dp))
                CommentButton(post.comments) // Display number of comments
            }
        }
    }
}

@Composable
fun LikeButton(likes: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = R.drawable.like),
            contentDescription = "Likes",
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = "$likes likes",
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
fun CommentButton(comments: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = R.drawable.ic_comment),
            contentDescription = "Comments",
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = "$comments comments",
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}




//@Composable
//fun LikeButton(likes: Int) {
//    Row(
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Icon(
//            painter = painterResource(id = R.drawable.like),
//            contentDescription = "Likes",
//            modifier = Modifier.size(24.dp)
//        )
//        Text(
//            "$likes likes",
//            modifier = Modifier
//                .padding(start = 4.dp)
//                .align(Alignment.CenterVertically)
//        )
//    }
//}
//
//@Composable
//fun CommentButton(comments: Int) {
//    Row(
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Icon(
//            painter = painterResource(id = R.drawable.ic_comment),
//            contentDescription = "Comments",
//            modifier = Modifier.size(24.dp)
//        )
//        Text(
//            "$comments comments",
//            modifier = Modifier
//                .padding(start = 4.dp)
//                .align(Alignment.CenterVertically)
//        )
//    }
//}

//data class Post(
//    val authorName: String,
//    val authorImageRes: Int,
//    val timestamp: String,
//    val content: String,
//    val imageRes: Int? = null,
//    val likes: Int,
//    val comments: Int
//)
//
//fun getSamplePosts(): List<Post> {
//    return listOf(
//        Post(
//            authorName = "Elizabeth Jie",
//            authorImageRes = R.drawable.background,
//            timestamp = "16 Feb at 19:56",
//            content = "Hello guys! Let me know what food to post in the comments below!",
//            likes = 9,
//            comments = 5
//        ),
//        Post(
//            authorName = "Clyde",
//            authorImageRes = R.drawable.back_arrow,
//            timestamp = "16 Feb at 19:56",
//            content = "Currently working from home.",
//            imageRes = R.drawable.background,
//            likes = 2,
//            comments = 3
//        )
//    )
//}

//@Preview(showBackground = true)
//@Composable
//fun PreviewManagePost() {
//
//    SocialFeedScreen(
//        navController = rememberNavController(),
//        userViewModel = MockUserViewModel()
//    )
//}