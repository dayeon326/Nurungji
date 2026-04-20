package com.example.nurungji.ui.screens

import com.example.nurungji.R
import androidx.compose.runtime.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

import com.example.nurungji.ui.navigation.Screen
import com.example.nurungji.data.Recipe
import com.example.nurungji.ui.components.RecipeCard
import com.example.nurungji.ui.theme.*

@Composable
fun RecipeScreen(onNavigate: (Screen) -> Unit) {

    var searchText by remember { mutableStateOf("") }

    val recipes = listOf(
        Recipe(
            name = "토마토 달걀볶음",
            time = "15분",
            availableIngredients = listOf("토마토", "계란", "양파"),
            missingIngredients = listOf("간장"),
            imageRes = R.drawable.ic_launcher_foreground
        ),
        Recipe(
            name = "샐러드",
            time = "10분",
            availableIngredients = listOf("양상추", "토마토"),
            missingIngredients = listOf("올리브 오일", "발사믹"),
            imageRes = R.drawable.ic_launcher_foreground
        )
    )


    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        // 🔹 헤더
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        bottomStart = 32.dp,
                        bottomEnd = 32.dp
                    )
                )
                .background(
                    Brush.verticalGradient(
                        listOf(
                            PrimaryGreen,
                            PrimaryGreenDark
                        )
                    )
                )
                .padding(top = 40.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)
        ) {

            Column {

                Text(
                    text = "레시피 추천",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontSize = 22.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "무엇을 만들고 싶으세요?",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // 🔹 리스트
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(recipes) { recipe ->
                RecipeCard(recipe)
            }
        }
    }
}