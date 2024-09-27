package com.example.assignme.GUI.AccountProfile

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.assignme.AndroidBar.AppTopBar
import com.example.assignme.ViewModel.Post
import com.example.assignme.ViewModel.UserProfile
import com.example.assignme.ViewModel.UserViewModel


@Composable
fun ManageReportPostScreen(navController: NavController, userViewModel: UserViewModel) {
    val reportedPosts by userViewModel.reportedPosts.observeAsState(emptyList())
    val users by userViewModel.users.observeAsState(emptyMap())

    Scaffold(
        topBar = { AppTopBar(title = "Reported Posts", navController = navController) }
    ) { innerPadding ->
        if (reportedPosts.isEmpty()) {
            Text("No reported posts", modifier = Modifier.padding(16.dp))
        } else {
            LazyColumn(modifier = Modifier.padding(innerPadding)) {
                items(reportedPosts) { post ->
                    ReportedPostItem(post, userViewModel, users)
                }
            }
        }
    }
}

@Composable
fun ReportedPostItem(post: Post, userViewModel: UserViewModel, users: Map<String, UserProfile>) {
    val reportedUser = users[post.userId]
    val reportedByUser = users[post.reportedBy ?: ""]

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Reported user info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = rememberImagePainter(reportedUser?.profilePictureUrl),
                    contentDescription = "Reported user profile picture",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                    .border(1.5.dp, androidx.compose.material3.MaterialTheme.colorScheme.primary, CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Reported user: ${reportedUser?.name ?: "Unknown"}",
//                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Post content
            Text(post.content,
//                style = MaterialTheme.typography.bodyLarge
            )

            // Post image
            post.imagePath?.let { imagePath ->
                Image(
                    painter = rememberImagePainter(imagePath),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .padding(vertical = 8.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Report reason and reporter info
            Text(
                "Reported by: ${reportedByUser?.name ?: "Unknown"}",
//                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "Report Reason: ${post.reportReason?.substringAfter(":") ?: "No reason provided"}",
//                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Post stats
            Text("Likes: ${post.likes}",
//                style = MaterialTheme.typography.bodyMedium
            )
            Text("Comments: ${post.comments}",
//                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons
            Row {
                Button(
                    onClick = { userViewModel.deletePost(post.id) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFFE23E3E),
                        contentColor = Color.White// 设置背景颜色为红色
                    )
                ) {
                    Text("Delete Post")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { userViewModel.ignoreReport(post.id) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFFE23E3E),
                        contentColor = Color.White// 设置背景颜色为红色
                    )
                ) {
                    Text("Ignore Report")
                }
            }
        }
    }
}


