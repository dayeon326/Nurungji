package com.example.nurungji.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nurungji.ui.viewmodels.RecipeViewModel
import com.example.nurungji.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRecipesScreen(
    onBack: () -> Unit, // 뒤로가기 버튼 누르면 실행될 동작
    onNavigate: (Screen) -> Unit, // 화면 이동
    recipeViewModel: RecipeViewModel = viewModel()
) {
    val currentUserId = recipeViewModel.auth.currentUser?.uid
    val myRecipes = recipeViewModel.recipes.filter { it.authorId == currentUserId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("내가 쓴 레시피", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로 가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF9FBF9)
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {

            // 내가 쓴 글이 하나도 없을 때 보여줄 화면
            if (myRecipes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("아직 작성한 레시피가 없습니다.", color = Color.Gray)
                }
            } else {
                // 내가 쓴 글이 있을 때
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(myRecipes) { recipe ->
                        // 기본 카드
                        Card(
                            onClick = {
                                recipeViewModel.selectedRecipeId = recipe.id // 1. 어떤 글 눌렀는지 뷰모델에 저장
                                onNavigate(Screen.RecipeDetail)              // 2. 상세 화면으로 이동!
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = recipe.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = recipe.content, color = Color.DarkGray, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "❤️ 추천 ${recipe.recommendUids.size}", color = Color(0xFF579D74), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}