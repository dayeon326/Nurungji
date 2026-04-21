package com.example.nurungji.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nurungji.R
import com.example.nurungji.ui.navigation.Screen
import com.example.nurungji.ui.theme.*

data class CommunityRecipe(
    val title: String,
    val description: String,
    val author: String,
    val time: String,
    val hashtags: List<String>,
    val availableIngredients: List<String>,
    val neededIngredients: List<String>,
    val likes: Int,
    val imageRes: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(onNavigate: (Screen) -> Unit) {
    // 1. searchText 변수 선언 추가
    var selectedTab by remember { mutableStateOf("인기") }
    var searchText by remember { mutableStateOf("") }

    val recipes = listOf(
        CommunityRecipe(
            title = "남은 채소로 만드는 간단 볶음밥",
            description = "냉장고에 남은 채소들을 활용한 볶음밥 레시피입니다. 간단하면서도 맛있어요!",
            author = "요리왕김씨",
            time = "15분",
            hashtags = listOf("#볶음밥", "#채소", "#간단요리"),
            availableIngredients = listOf("밥", "양파", "당근"),
            neededIngredients = listOf("계란", "간장"),
            likes = 234,
            imageRes = R.drawable.ic_launcher_foreground
        ),
        CommunityRecipe(
            title = "닭가슴살 스테이크",
            description = "고단백 저칼로리 닭가슴살 스테이크 레시피",
            author = "헬스왕",
            time = "20분",
            hashtags = listOf("#닭가슴살", "#다이어트", "#단백질"),
            availableIngredients = listOf("닭가슴살", "마늘"),
            neededIngredients = listOf("로즈마리", "올리브오일"),
            likes = 180,
            imageRes = R.drawable.ic_launcher_foreground
        )
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* 글쓰기 화면 이동 로직 */ },
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
        ) { // 중괄호 시작
            // --- 상단 헤더 영역 ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF579D74), Color(0xFF4A8B63))))
                    .padding(top = 40.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)
            ) {
                Column {
                    Text(
                        text = "레시피",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        placeholder = { Text("레시피를 검색해보세요", fontSize = 14.sp, color = Color.LightGray) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                        shape = RoundedCornerShape(16.dp),
                        // 2. 에러 방지를 위한 최신 colors 설정 방식
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                            .padding(4.dp)
                    ) {
                        TabButton("인기", selectedTab == "인기", Modifier.weight(1f)) { selectedTab = "인기" }
                        TabButton("최신", selectedTab == "최신", Modifier.weight(1f)) { selectedTab = "최신" }
                    }
                }
            }

            // --- 리스트 영역 ---
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(recipes) { recipe ->
                    CommunityRecipeCard(recipe)
                }
            }
        } // 3. Column 닫는 중괄호 추가
    }
}

// TabButton 및 CommunityRecipeCard 컴포넌트 코드는 동일하게 유지...
@Composable
fun TabButton(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Color.White else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color(0xFF333333) else Color.White,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 15.sp
        )
    }
}

@Composable
fun CommunityRecipeCard(recipe: CommunityRecipe) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFE8F5E9)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = recipe.imageRes),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = recipe.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF222222))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = recipe.description, fontSize = 13.sp, color = Color.Gray, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "${recipe.author} • ${recipe.time}", fontSize = 12.sp, color = Color(0xFF888888))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                recipe.hashtags.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFF2F6F3), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(text = tag, fontSize = 12.sp, color = Color(0xFF579D74))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAF9), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    if (recipe.availableIngredients.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF579D74), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "보유 재료", fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            recipe.availableIngredients.forEach { ingredient ->
                                Box(
                                    modifier = Modifier.background(Color(0xFFE8F5E9), CircleShape).padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(text = ingredient, fontSize = 12.sp, color = Color(0xFF2E7D32))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (recipe.neededIngredients.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Outlined.ShoppingCart, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "필요한 재료", fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            recipe.neededIngredients.forEach { ingredient ->
                                Box(
                                    modifier = Modifier.background(Color.White, CircleShape).padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(text = ingredient, fontSize = 12.sp, color = Color(0xFF444444))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp) // Divider -> HorizontalDivider (M3 권장)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.FavoriteBorder, contentDescription = "좋아요", tint = Color.Gray, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "${recipe.likes}", color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.weight(1f))
                Icon(imageVector = Icons.Outlined.BookmarkBorder, contentDescription = "스크랩", tint = Color.Gray, modifier = Modifier.size(24.dp))
            }
        }
    }
}