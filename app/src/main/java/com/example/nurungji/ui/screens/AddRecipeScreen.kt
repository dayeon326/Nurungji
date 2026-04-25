package com.example.nurungji.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nurungji.ui.viewmodels.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeScreen(
    onBack: () -> Unit,
    recipeViewModel: RecipeViewModel = viewModel()
) {
    var title by remember { mutableStateOf("") }
    var cookingTime by remember { mutableStateOf("") }
    var ingredients by remember { mutableStateOf("") }
    var hashtags by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("나만의 레시피 등록", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("레시피 제목") },
                placeholder = { Text("예: 냉장고 털이 볶음밥") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = cookingTime,
                onValueChange = { cookingTime = it },
                label = { Text("조리 시간 (분)") },
                placeholder = { Text("예: 15") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = ingredients,
                onValueChange = { ingredients = it },
                label = { Text("재료") },
                placeholder = { Text("예: 계란, 양파, 밥") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = hashtags,
                onValueChange = { hashtags = it },
                label = { Text("해시태그") },
                placeholder = { Text("예: 간단요리, 자취요리") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("요리 방법") },
                placeholder = { Text("재료와 만드는 법을 자유롭게 적어주세요!") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank() && cookingTime.isNotBlank()) {
                        recipeViewModel.addRecipe(
                            context = context,
                            title = title,
                            content = content,
                            cookingTime = cookingTime,
                            ingredients = ingredients.split(",")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() },
                            hashtags = hashtags
                                .split(",")
                                .map { it.trim().removePrefix("#") }
                                .filter { it.isNotEmpty() }
                        ) {
                            onBack()
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "제목, 조리 시간, 요리 방법을 모두 입력해주세요!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF579D74)
                )
            ) {
                Text(
                    "레시피 등록하기",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}