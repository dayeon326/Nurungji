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
fun EditRecipeScreen(
    onBack: () -> Unit,
    recipeViewModel: RecipeViewModel = viewModel()
) {
    // 지금 보고 있는 레시피 정보 가져오기
    val recipe = recipeViewModel.recipes.find { it.id == recipeViewModel.selectedRecipeId }
    val context = LocalContext.current

    if (recipe == null) {
        onBack()
        return
    }

    var title by remember { mutableStateOf(recipe.title) }
    var content by remember { mutableStateOf(recipe.content) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("레시피 수정", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("레시피 제목") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("요리 방법") },
                modifier = Modifier.fillMaxWidth().height(250.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        // 새로 적은 내용을 뷰모델로 보내서 업데이트
                        recipeViewModel.updateRecipe(context, recipe.id, title, content) {
                            onBack() // 수정 완료 후 이전 화면으로 돌아가기
                        }
                    } else {
                        Toast.makeText(context, "제목과 내용을 적어주세요.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF579D74))
            ) {
                Text("수정 완료", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}