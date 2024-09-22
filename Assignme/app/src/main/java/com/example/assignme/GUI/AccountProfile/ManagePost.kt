package com.example.assignme.GUI.AccountProfile

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.assignme.AndroidBar.AppTopBar
import com.example.assignme.R
import com.example.assignme.ViewModel.MockUserViewModel
import com.example.assignme.ViewModel.UserProfileProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialFeedScreen(navController: NavController, userViewModel: UserProfileProvider) {
    Scaffold(
        topBar = { AppTopBar(title = "Back", navController = navController, modifier = Modifier) },

        ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            item { ReportsButton() }
            item { TrendingSection() }
            items(getSamplePosts()) { post ->
                PostItem(post)
            }
        }
    }
}

@Composable
fun ReportsButton() {
    Button(
        onClick = {},
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
fun PostItem(post: Post) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(1.dp, Color.LightGray, MaterialTheme.shapes.medium)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = post.authorImageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(MaterialTheme.shapes.small),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(post.authorName, fontWeight = FontWeight.Bold)
                    Text(post.timestamp, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = {}) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(post.content)
            if (post.imageRes != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    painter = painterResource(id = post.imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.LightGray)
            ) {
                LikeButton(post.likes)
                CommentButton(post.comments)
            }
        }
    }
}

@Composable
fun LikeButton(likes: Int) {
    Row(
        modifier = Modifier

            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.like),
            contentDescription = "Likes",
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(likes.toString())
    }
}

@Composable
fun CommentButton(comments: Int) {
    Row(
        modifier = Modifier

            .padding(8.dp)
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = RectangleShape
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_comment),
            contentDescription = "Comments",
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(comments.toString())
    }
}

data class Post(
    val authorName: String,
    val authorImageRes: Int,
    val timestamp: String,
    val content: String,
    val imageRes: Int? = null,
    val likes: Int,
    val comments: Int
)

fun getSamplePosts(): List<Post> {
    return listOf(
        Post(
            authorName = "Elizabeth Jie",
            authorImageRes = R.drawable.background,
            timestamp = "16 Feb at 19:56",
            content = "Hello guys! Let me know what food to post in the comments below!",
            likes = 9,
            comments = 5
        ),
        Post(
            authorName = "Clyde",
            authorImageRes = R.drawable.back_arrow,
            timestamp = "16 Feb at 19:56",
            content = "Currently working from home.",
            imageRes = R.drawable.background,
            likes = 2,
            comments = 3
        )
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewManagePost() {

    SocialFeedScreen(
        navController = rememberNavController(),
        userViewModel = MockUserViewModel()
    )
}