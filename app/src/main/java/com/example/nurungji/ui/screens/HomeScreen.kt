package com.example.nurungji.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nurungji.data.InventoryItem
import com.example.nurungji.ui.viewmodels.InventoryViewModel
import com.example.nurungji.ui.components.ExpiringItemCard
import com.example.nurungji.ui.components.QuickActionCard
import com.example.nurungji.ui.components.SummaryCard
import com.example.nurungji.ui.navigation.Screen
import com.example.nurungji.ui.theme.PrimaryGreenDark
import com.example.nurungji.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    onNavigate: (Screen) -> Unit,
    viewModel: InventoryViewModel = viewModel()
) {
    val inventoryItems by viewModel.inventoryItems.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadInventory()
    }

    val expiringItems = inventoryItems.filter { item ->
        val date = item.expireDate?.toDate() ?: return@filter false
        val diff = date.time - System.currentTimeMillis()
        val days = diff / (1000L * 60L * 60L * 24L)
        days in 0..14
    }.sortedBy { item ->
        item.expireDate?.toDate()?.time ?: Long.MAX_VALUE
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
    ) {
        HeaderSection()

        SummarySection(
            totalCount = inventoryItems.size,
            weeklyConsumed = 0
        )

        ExpiringSection(
            expiringItems = expiringItems,
            onNavigate = onNavigate
        )

        QuickActionSection(onNavigate)

        ShoppingSection(
            totalCount = inventoryItems.size,
            expiringCount = expiringItems.size,
            onNavigate = onNavigate
        )

        RecipeSection(onNavigate)

        TipSection()
    }
}

@Composable
fun HeaderSection() {
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
                        MaterialTheme.colorScheme.primary,
                        PrimaryGreenDark
                    )
                )
            )
            .padding(top = 40.dp, bottom = 28.dp, start = 24.dp, end = 24.dp)
    ) {
        Column {
            Text(
                text = "나의 냉장고",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )
            Text(
                text = "신선함을 지키는 스마트한 방법",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
fun SummarySection(
    totalCount: Int,
    weeklyConsumed: Int
) {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        SummaryCard(
            title = "총 식품 개수",
            value = totalCount.toString(),
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(12.dp))

        SummaryCard(
            title = "이번 주 소비",
            value = weeklyConsumed.toString(),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ExpiringSection(
    expiringItems: List<InventoryItem>,
    onNavigate: (Screen) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "곧 만료되는 식품",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "전체보기",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    onNavigate(Screen.Inventory)
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (expiringItems.isEmpty()) {
            Text(
                text = "14일 이내 만료 식품이 없습니다 👍",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(expiringItems.take(10)) { item ->
                    ExpiringItemCard(item = item)
                }
            }
        }
    }
}

@Composable
fun QuickActionSection(onNavigate: (Screen) -> Unit) {

    Column(
        modifier = Modifier.padding(16.dp)
    ) {

        // 제목
        Text(
            text = "빠른 작업",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 카드 영역
        Row(
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
}

@Composable
fun ShoppingSection(
    totalCount: Int,
    expiringCount: Int,
    onNavigate: (Screen) -> Unit
) {
    val subtitle = when {
        expiringCount > 0 -> "곧 만료 식품 ${expiringCount}개 확인해보세요"
        totalCount == 0 -> "아직 등록된 식품이 없어요"
        else -> "현재 등록된 식품 ${totalCount}개"
    }

    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .clickable { onNavigate(Screen.Shopping) },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFE5B4)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "장보기 리스트",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "장보기 이동"
                )
            }
        }
    }
}

@Composable
fun RecipeSection(onNavigate: (Screen) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "인기 레시피",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigate(Screen.Recipes) },
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "남은 채소로 만드는 볶음밥",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "요리왕김씨 · ❤️ 234",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigate(Screen.Recipes) },
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "1인 가구 냉장고 정리법",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "정리왕 · ❤️ 298",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun TipSection() {
    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8F5E9)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📝 오늘의 팁",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "토마토는 실온 보관이 더 좋아요!",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}