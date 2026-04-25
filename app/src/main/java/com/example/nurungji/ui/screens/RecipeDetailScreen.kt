package com.example.nurungji.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nurungji.ui.navigation.Screen
import com.example.nurungji.ui.viewmodels.RecipeViewModel
import com.example.nurungji.models.Recipe

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    onBack: () -> Unit,
    onNavigate: (Screen) -> Unit,
    recipeViewModel: RecipeViewModel
) {
    val recipe = recipeViewModel.recipes.find { it.id == recipeViewModel.selectedRecipeId }
    val currentUserId = recipeViewModel.auth.currentUser?.uid
    val context = LocalContext.current

    var showDeleteDialog by remember { mutableStateOf(false) }

    if (recipe == null) return

    val isRecommended = recipe.recommendUids.contains(currentUserId)
    val isScrapped = recipe.scrapUids.contains(currentUserId)
    val isMyRecipe = (recipe.authorId == currentUserId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("레시피 상세", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로 가기") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).fillMaxSize().padding(20.dp)
        ) {
            Text(text = recipe.title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "작성자: ${recipe.authorId?.take(5) ?: "익명"}...", color = Color.Gray, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "⏱ 조리 시간: ${recipe.time}",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "재료: ${recipe.ingredients.joinToString(", ")}",
                fontSize = 14.sp
            )

            if (recipe.hashtags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = recipe.hashtags.joinToString(" ") { "#$it" },
                    fontSize = 14.sp,
                    color = Color(0xFF579D74),
                    fontWeight = FontWeight.Medium
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFEEEEEE))
            Text(text = recipe.content, fontSize = 16.sp, lineHeight = 24.sp, modifier = Modifier.weight(1f))

            // 추천 & 스크랩 버튼 영역
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { recipeViewModel.toggleRecommend(recipe.id, recipe.recommendUids) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecommended) Color(0xFFFFEBEE) else Color(0xFFF5F5F5),
                        contentColor = if (isRecommended) Color.Red else Color.Gray
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = if (isRecommended) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("추천 ${recipe.recommendUids.size}", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { recipeViewModel.toggleScrap(recipe.id, recipe.scrapUids) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isScrapped) Color(0xFFFFF3E0) else Color(0xFFF5F5F5),
                        contentColor = if (isScrapped) Color(0xFFFF9800) else Color.Gray
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Star, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isScrapped) "저장됨" else "스크랩", fontWeight = FontWeight.Bold)
                }
            }

            // 내가 쓴 글일 때만 나타나는 버튼
            if (isMyRecipe) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = { onNavigate(Screen.EditRecipe) },
                        modifier = Modifier.padding(end = 8.dp)
                    ) { Text("수정", color = Color.DarkGray) }

                    OutlinedButton(
                        onClick = {
                            showDeleteDialog = true
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                    ) { Text("삭제") }
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false }, // 팝업창 바깥을 터치하면 팝업창 닫기
                title = {
                    Text("레시피 삭제", fontWeight = FontWeight.Bold)
                },
                text = {
                    Text("정말 삭제하시겠습니까?\n삭제된 레시피는 복구할 수 없습니다.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false // 팝업창을 끄고
                            recipeViewModel.deleteRecipe(context, recipe.id) { onBack() } // 파이어베이스에서 삭제
                        }
                    ) {
                        Text("삭제", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteDialog = false } // 단순 취소
                    ) {
                        Text("취소", color = Color.Gray)
                    }
                }
            )
        }
    }
}