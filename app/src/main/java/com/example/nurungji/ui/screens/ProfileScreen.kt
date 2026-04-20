package com.example.nurungji.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat

// 아까 만든 파일들을 가져옵니다.
import com.example.nurungji.ui.navigation.Screen
import com.example.nurungji.data.UserProfile
import com.example.nurungji.ui.components.MenuActionCard

@Composable
fun ProfileScreen(
    // 당장 화면에 보일 가짜 데이터를 기본으로 넣어둡니다.
    userProfile: UserProfile = UserProfile(
        name = "김민수",
        email = "minsu@example.com",
        registeredFoodCount = 24,
        savedMoney = 45000,
        preventedWasteKg = 3.2
    ),
    onNavigate: (Screen) -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFBFA)) // 전체 배경색 (연한 회색)
            .verticalScroll(scrollState)
            .padding(bottom = 80.dp)
    ) {
        // 1. 맨 위 초록색 프로필 영역
        ProfileHeaderSection(userProfile)

        Spacer(modifier = Modifier.height(16.dp))

        // 2. 중간 메뉴 버튼들 (components에서 만든 것 재사용)
        MenuActionCard(
            icon = Icons.Outlined.Article,
            title = "내가 쓴 글",
            onClick = { /* 나중에 추가 */ }
        )

        MenuActionCard(
            icon = Icons.Outlined.BookmarkBorder,
            title = "저장한 레시피",
            iconBgColor = Color(0xFFFFF3E0),
            iconColor = Color(0xFFFF9800),
            onClick = { /* 나중에 추가 */ }
        )

        MenuActionCard(
            icon = Icons.Default.NotificationsNone,
            title = "알림 설정",
            onClick = { /* 나중에 추가 */ }
        )

        MenuActionCard(
            icon = Icons.Outlined.Settings,
            title = "설정",
            iconBgColor = Color(0xFFECEFF1),
            iconColor = Color(0xFF607D8B),
            onClick = { /* 나중에 추가 */ }
        )

        MenuActionCard(
            icon = Icons.Outlined.HelpOutline,
            title = "도움말",
            iconBgColor = Color(0xFFECEFF1),
            iconColor = Color(0xFF607D8B),
            onClick = { /* 나중에 추가 */ }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. 에코 챔피언 영역
        EcoChampionSection()

        Spacer(modifier = Modifier.height(24.dp))

        // 4. 로그아웃 버튼
        LogoutButton()

        Spacer(modifier = Modifier.height(32.dp))
    }
}


// --- 아래부터는 ProfileScreen을 그리기 위한 부품들입니다 ---

@Composable
private fun ProfileHeaderSection(user: UserProfile) {
    // 돈 단위를 ₩45,000 처럼 콤마 찍어주는 기능
    val moneyFormat = DecimalFormat("#,###")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF56A781), // 메인 초록색
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column {
            Text("프로필", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(bottom = 24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(60.dp).background(Color(0xFF90CFA8), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF4A346C), modifier = Modifier.size(36.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            // 전달받은 user 데이터 사용
                            Text(user.name, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text(user.email, color = Color.White, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        // 전달받은 user 데이터 사용
                        StatItem("📦", "${user.registeredFoodCount}", "등록한 식품")
                        StatItem("💰", "₩${moneyFormat.format(user.savedMoney)}", "절약한 비용")
                        StatItem("♻️", "${user.preventedWasteKg}kg", "방지한 낭비")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(icon: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, fontSize = 24.sp, modifier = Modifier.padding(bottom = 4.dp))
        Text(text = value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(text = label, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
    }
}

@Composable
private fun EcoChampionSection() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F3ED)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🏆", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("이달의 에코 챔피언", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text("음식물 낭비를 줄여주셔서 감사합니다!", fontSize = 13.sp, color = Color.DarkGray)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                BadgeItem("🌱", "초보 절약가")
                BadgeItem("⭐", "레시피 마스터")
                BadgeItem("🌟", "에코 워리어")
            }
        }
    }
}

@Composable
private fun RowScope.BadgeItem(icon: String, title: String) {
    Card(
        modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, fontSize = 11.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun LogoutButton() {
    Card(
        onClick = { /* 나중에 로그아웃 기능 넣는 곳 */ },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "로그아웃", tint = Color(0xFFE53935))
            Spacer(modifier = Modifier.width(8.dp))
            Text("로그아웃", color = Color(0xFFE53935), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}