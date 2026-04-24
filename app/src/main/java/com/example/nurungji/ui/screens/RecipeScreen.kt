package com.example.nurungji.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nurungji.ui.navigation.Screen
import com.example.nurungji.ui.viewmodels.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(
    onNavigate: (Screen) -> Unit,
    recipeViewModel: RecipeViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf("인기") }
    var searchText by remember { mutableStateOf("") }

    // 뷰모델에서 파이어베이스 실시간 리스트 가져오기
    val allRecipes = recipeViewModel.recipes

    // 검색어 필터링 및 탭 정렬 로직
    val displayRecipes = allRecipes.filter {
        it.title.contains(searchText, ignoreCase = true) || it.content.contains(searchText, ignoreCase = true)
    }.let { filteredList ->
        if (selectedTab == "인기") {
            filteredList.sortedByDescending { it.recommendUids.size }
        } else {
            filteredList // 최신순 (뷰모델에서 이미 정렬됨)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigate(Screen.AddRecipe) }, // 글쓰기 화면으로 이동
                containerColor = Color(0xFF579D74),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "글쓰기")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF9FBF9))
        ) {
            // --- 상단 헤더 영역 (검색창, 탭 버튼) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF579D74), Color(0xFF4A8B63))))
                    .padding(top = 40.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)
            ) {
                Column {
                    Text(
                        text = "레시피 커뮤니티",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // 검색창
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        placeholder = { Text("레시피 검색...", fontSize = 14.sp, color = Color.LightGray) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 탭 버튼 영역
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                            .padding(4.dp)
                    ) {
                        Button(
                            onClick = { selectedTab = "인기" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedTab == "인기") Color.White else Color.Transparent,
                                contentColor = if (selectedTab == "인기") Color(0xFF579D74) else Color.White
                            ),
                            elevation = null,
                            shape = RoundedCornerShape(16.dp)
                        ) { Text("인기") }

                        Button(
                            onClick = { selectedTab = "최신" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedTab == "최신") Color.White else Color.Transparent,
                                contentColor = if (selectedTab == "최신") Color(0xFF579D74) else Color.White
                            ),
                            elevation = null,
                            shape = RoundedCornerShape(16.dp)
                        ) { Text("최신") }
                    }
                }
            }

            // --- 레시피 리스트 영역 ---
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(displayRecipes) { recipe ->
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
                            Row {
                                Text(text = "❤️ 추천 ${recipe.recommendUids.size}", color = Color(0xFF579D74), fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = "👤 작성자: ${recipe.authorId?.take(5) ?: "익명"}...", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}