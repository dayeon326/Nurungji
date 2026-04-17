package com.example.nurungji.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.nurungji.ui.navigation.Screen
import com.example.nurungji.ui.components.ExpiringItemCard
import com.example.nurungji.ui.components.QuickActionCard
import com.example.nurungji.ui.components.SummaryCard

@Composable
fun HomeScreen(
    onNavigate: (Screen) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
    ) {
        HeaderSection()
        SummarySection()
        ExpiringSection(onNavigate)
        QuickActionSection(onNavigate)
        ShoppingSection(onNavigate)
        CommunitySection(onNavigate)
        TipSection()
    }
}

@Composable
fun HeaderSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column {
            Text(
                "나의 냉장고",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )
            Text(
                "신선함을 지키는 스마트한 방법",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
fun SummarySection() {
    Row(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            title = "총 식품 개수",
            value = "24",
            modifier = Modifier.weight(1f)
        )

        SummaryCard(
            title = "이번 주 소비",
            value = "8",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ExpiringSection(onNavigate: (Screen) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("곧 만료되는 식품")
            Text(
                "전체보기",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    onNavigate(Screen.Inventory)
                }
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(listOf("🍅", "🥛", "🥬")) { item ->
                ExpiringItemCard(item)
            }
        }
    }
}

@Composable
fun QuickActionSection(onNavigate: (Screen) -> Unit) {
    Row(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionCard(
            title = "식품 추가",
            onClick = { onNavigate(Screen.AddItem) },
            modifier = Modifier.weight(1f)
        )
        QuickActionCard(
            title = "영수증 스캔",
            onClick = { onNavigate(Screen.AddItem) },
            modifier = Modifier.weight(1f)
        )
        QuickActionCard(
            title = "레시피",
            onClick = { onNavigate(Screen.Recipes) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ShoppingSection(onNavigate: (Screen) -> Unit) {
    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .clickable { onNavigate(Screen.Shopping) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("장보기 리스트")
            Text("필요한 재료 5개 남음")
        }
    }
}

@Composable
fun CommunitySection(onNavigate: (Screen) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("인기 레시피")
        Text("남은 채소로 만드는 볶음밥")
        Text("1인 가구 냉장고 정리법")
    }
}

@Composable
fun TipSection() {
    Card(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            "토마토는 실온 보관이 더 좋아요!",
            modifier = Modifier.padding(16.dp)
        )
    }
}