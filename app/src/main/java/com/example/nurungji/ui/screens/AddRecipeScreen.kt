package com.example.nurungji.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
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
    onBack: () -> Unit, // 뒤로가기 (등록 완료 후 돌아갈 때 씀)
    recipeViewModel: RecipeViewModel = viewModel()
) {
    // 사용자가 입력할 제목과 내용을 담을 빈 상자
    var title by remember { mutableStateOf("") }
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. 제목 입력 칸
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("레시피 제목") },
                placeholder = { Text("예: 냉장고 털이 볶음밥") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 2. 내용 입력 칸
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("요리 방법") },
                placeholder = { Text("재료와 만드는 법을 자유롭게 적어주세요!") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp) // 내용 입력 길이
            )

            Spacer(modifier = Modifier.weight(1f)) // 버튼을 맨 아래로 밀어줌

            // 3. 등록하기 버튼
            Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        recipeViewModel.addRecipe(context, title, content) {
                            onBack()
                        }
                    } else {
                        Toast.makeText(context, "제목과 내용을 모두 적어주세요!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF579D74))
            ) {
                Text("레시피 등록하기", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}