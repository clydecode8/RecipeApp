package com.example.assignme.GUI.Recipe

import android.app.DatePickerDialog
import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenuItem
import com.example.assignme.GUI.Recipe.ui.theme.Orange
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.assignme.DataClass.Recipes
import com.example.assignme.ViewModel.RecipeViewModel
import com.example.assignme.ViewModel.UserViewModel
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.toArgb
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.fragment.app.FragmentActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(recipe: Recipes, userModel:UserViewModel, viewModel: RecipeViewModel = viewModel(),  onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recipe.title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomBar(recipe.id,
                userModel.userId.value.toString(),
                onAddToSchedule = { /* Handle Add to Schedule */ },
                onShare = { /* Handle Share */ })

        },
        contentWindowInsets = WindowInsets.navigationBars

    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            item {
                Image(
                    painter = rememberAsyncImagePainter(recipe.imageUrl),
                    contentDescription = recipe.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InfoCard(title = "${recipe.totalCalories}kCal", subtitle = "Calories")
                    InfoCard(title = recipe.cookTime, subtitle = "Total Time")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Ingredients", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("${recipe.ingredients.size} items", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

                Spacer(modifier = Modifier.height(8.dp))
            }

            items(recipe.ingredients) { ingredient ->
                Text("• ${ingredient.amount} ${ingredient.name}", modifier = Modifier.padding(vertical = 4.dp))
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))

                Text("Instructions", fontSize = 20.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(8.dp))
            }

            items(recipe.instructions.withIndex().toList()) { (index, instruction) ->
                Text(
                    text = "${index + 1}. $instruction",
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun InfoCard(title: String, subtitle: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}

@Composable
fun AddToScheduleDialog(
    recipeId: String,
    userId: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    // 记住用户选择的日期和餐食类型
    val date = remember { mutableStateOf("") }
    val mealType = remember { mutableStateOf("Breakfast") }
    val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snack")

    // 获取当前日期
    val calendar = Calendar.getInstance()
    val currentYear = calendar.get(Calendar.YEAR)
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

    // 显示日期选择对话框
    val context = LocalContext.current

    // 使用橙色的日期选择器主题
    val datePickerDialog = remember {
        DatePickerDialog(context, { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            date.value = "$dayOfMonth/${month + 1}/$year"
        }, currentYear, currentMonth, currentDay).apply {
            // 修改日历选择器的按钮颜色为橙色
            setOnShowListener {
                this.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(Orange.toArgb())
                this.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(Orange.toArgb())
            }
        }
    }

    // 控制DropdownMenu展开状态的mutableState
    val expanded = remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 0.dp), // 按钮填充整个下半部分
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        if (date.value.isEmpty()) {
                            // 如果用户没有选择日期，则显示提示
                            Toast.makeText(context, "Please select a date", Toast.LENGTH_SHORT).show()
                        } else {
                            // 日期已选择，执行确认操作
                            onConfirm(date.value, mealType.value)
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp), // 按钮占据整个宽度并且高度调整
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Orange, // 按钮背景颜色为橙色
                        contentColor = Color.White // 按钮文本颜色为白色
                    )
                ) {
                    Text("Add to Schedule")
                }
            }
        },

        title = { Text(text = "Add to Schedule") },
        text = {
            Column {
                Text(text = "Select Date")
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { datePickerDialog.show() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Orange, // 按钮背景颜色为橙色
                        contentColor = Color.White // 按钮文本颜色为白色
                    )
                ) {
                    Text(text = if (date.value.isEmpty()) "Pick a Date" else date.value)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Select Meal Type")

                // 添加下拉菜单
                Button(
                    onClick = { expanded.value = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Orange, // 按钮背景颜色为橙色
                        contentColor = Color.White // 按钮文本颜色为白色
                    )
                ) {
                    Text(mealType.value) // 显示当前选择的餐食类型
                }

                DropdownMenu(
                    expanded = expanded.value,
                    onDismissRequest = { expanded.value = false } // 当点击外部时关闭菜单
                ) {
                    mealTypes.forEach { type ->
                        DropdownMenuItem(onClick = {
                            mealType.value = type
                            expanded.value = false // 选择后关闭菜单
                        }) {
                            Text(text = type)
                        }
                    }
                }
            }
        }
    )
}


fun addScheduleToFirestore(
    context: Context,
    userId: String,
    recipeId: String,
    selectedDate: String,
    mealType: String
) {
    val db = FirebaseFirestore.getInstance()
    val scheduleData = hashMapOf(
        "userId" to userId,
        "recipeId" to recipeId,
        "date" to selectedDate,
        "mealType" to mealType
    )

    db.collection("schedule")
        .add(scheduleData)
        .addOnSuccessListener { documentReference ->
            Log.d("Firestore", "Schedule added with ID: ${documentReference.id}")
            Toast.makeText(context, "Add successfully", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error adding schedule", e)
            Toast.makeText(context, "Failed to add", Toast.LENGTH_SHORT).show()
        }
}


@Composable
fun BottomBar(recipeId: String,
              userId: String,
              onAddToSchedule: () -> Unit,
              onShare: () -> Unit) {
    val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val showDialog = remember { mutableStateOf(false) }
    val context = LocalContext.current
    if (showDialog.value) {
        AddToScheduleDialog(
            recipeId = recipeId,
            userId = userId,
            onDismiss = { showDialog.value = false },
            onConfirm = { selectedDate, mealType ->
                addScheduleToFirestore(context, userId, recipeId, selectedDate, mealType)
            }
        )
    }


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = bottomPadding + 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { showDialog.value = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = Orange, // Set the orange color here
                contentColor = Color.White // Set the text color to white
            ),
            modifier = Modifier.weight(1f),

        ) {
            Text(text = "Add to Schedule")
        }

        Spacer(modifier = Modifier.width(16.dp))

        IconButton(onClick = onShare) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share Recipe",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}