package com.example.assignme.GUI.AccountProfile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.assignme.R
import com.example.assignme.ViewModel.UserProfileProvider
import com.example.assignme.ViewModel.MockUserViewModel


@Composable
fun AppFirstPage(navController: NavController, userViewModel: UserProfileProvider) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .statusBarsPadding()
            .background(Color.Black)
    ) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = "Burger layers",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .safeContentPadding(), // Set a background color to contrast the white icon
            horizontalAlignment = Alignment.CenterHorizontally, // Center horizontally
            verticalArrangement = Arrangement.Top // Arrange content at the top
        ){

            //Premium recipe
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(30.dp)

            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_main_star),
                    contentDescription = "Star Icon",
                    modifier = Modifier.size(12.dp),
                    tint = Color.White
                )

                Spacer(modifier = Modifier.width(8.dp)) // Add space between the icon and text
                Text(
                    text = "Premium Recipes",
                    style = MaterialTheme.typography.labelMedium, // Customize the text style
                    color = Color.White
                )
            }

        }

        Column(

            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding()
                .statusBarsPadding(), // Set a background color to contrast the white icon
            horizontalAlignment = Alignment.CenterHorizontally, // Center horizontally
            verticalArrangement = Arrangement.Center // Arrange content at the top
        ){

            Text(
                text = "Let's",
                fontSize = 56.sp,
                color = Color.White,
            )

            Text(
                text = "Cooking",
                fontSize = 56.sp,
                color = Color.White,
            )

            Text(
                text = "Find best recipes for cooking",
                fontSize = 20.sp,
                modifier = Modifier.padding(top = 30.dp),
                color = Color.White,
            )


        }

        Box(modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 150.dp),
            contentAlignment = Alignment.BottomCenter){

            //Next
            Button(
//                onClick = { navController.navigate("recipe_main_page") },
                onClick = { navController.navigate("login_page") },

                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .size(width = 206.dp, height = 54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE23E3E)), // Set button color
                shape = RoundedCornerShape(10.dp) // Set the button shape to rectangular

            ) {

                Spacer(modifier = Modifier.width(8.dp)) // Space between icon and text
                Text(text = "Start Cooking")
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainPage() {

    AppFirstPage(
        navController = rememberNavController(),
        userViewModel = MockUserViewModel()
    )
}