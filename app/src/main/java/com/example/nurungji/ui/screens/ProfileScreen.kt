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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.FirebaseFirestore

// 아까 만든 파일들을 가져옵니다.
import com.example.nurungji.ui.navigation.Screen
import com.example.nurungji.data.UserProfile
import com.example.nurungji.ui.components.MenuActionCard
import com.example.nurungji.ui.viewmodels.InventoryViewModel
import com.example.nurungji.ui.viewmodels.ShoppingListViewModel

@Composable
fun ProfileScreen(
    onNavigate: (Screen) -> Unit = {},
    onLogOut: () -> Unit,
    inventoryViewModel: InventoryViewModel = viewModel(),
    shoppingListViewModel: ShoppingListViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()
    val inventoryItems by inventoryViewModel.inventoryItems.collectAsState()
    val shoppingItems by shoppingListViewModel.shoppingItems.collectAsState()
    var nickname by remember { mutableStateOf(currentUser?.displayName?.takeIf { it.isNotBlank() } ?: "사용자") }
    var showNicknameDialog by remember { mutableStateOf(false) }
    var nicknameInput by remember { mutableStateOf(nickname) }

    LaunchedEffect(Unit) {
        inventoryViewModel.loadInventory()
        shoppingListViewModel.loadShoppingItems(
            context = context,
            includeAutoRecommendations = false
        )
    }

    DisposableEffect(currentUser?.uid) {
        val uid = currentUser?.uid
        if (uid == null) {
            onDispose { }
        } else {
            val listener = db.collection("user_profiles")
                .document(uid)
                .addSnapshotListener { snapshot, _ ->
                    val savedNickname = snapshot?.getString("nickname")
                    nickname = savedNickname?.takeIf { it.isNotBlank() }
                        ?: currentUser.displayName?.takeIf { it.isNotBlank() }
                        ?: "사용자"
                    if (!showNicknameDialog) {
                        nicknameInput = nickname
                    }
                }

            onDispose {
                listener.remove()
            }
        }
    }

    val nowMillis = System.currentTimeMillis()
    val threeDaysMillis = 3L * 24 * 60 * 60 * 1000
    val expiringFoodCount = inventoryItems.count { item ->
        val expireMillis = item.expireDate?.toDate()?.time ?: return@count false
        expireMillis in nowMillis..(nowMillis + threeDaysMillis)
    }

    val userProfile = UserProfile(
        name = nickname,
        email = currentUser?.email?.takeIf { it.isNotBlank() } ?: "이메일 없음",
        registeredFoodCount = inventoryItems.size,
        shoppingListCount = shoppingItems.size,
        expiringFoodCount = expiringFoodCount
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFBFA)) // 전체 배경색 (연한 회색)
            .verticalScroll(scrollState)
            .padding(bottom = 80.dp)
    ) {
        // 1. 맨 위 초록색 프로필 영역
        ProfileHeaderSection(
            user = userProfile,
            onEditNickname = {
                nicknameInput = nickname
                showNicknameDialog = true
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. 중간 메뉴 버튼들 (components에서 만든 것 재사용)
        MenuActionCard(
            icon = Icons.Outlined.Article,
            title = "내가 쓴 글",
            onClick = { onNavigate(Screen.MyRecipes) }
        )

        MenuActionCard(
            icon = Icons.Outlined.BookmarkBorder,
            title = "저장한 레시피",
            iconBgColor = Color(0xFFFFF3E0),
            iconColor = Color(0xFFFF9800),
            onClick = { onNavigate(Screen.SavedRecipes)}
        )



        Spacer(modifier = Modifier.height(16.dp))

        // 3. 로그아웃 버튼
        LogoutButton(
            onLogOut=onLogOut
        )

        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showNicknameDialog) {
        AlertDialog(
            onDismissRequest = { showNicknameDialog = false },
            title = { Text("닉네임 설정", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = nicknameInput,
                    onValueChange = { nicknameInput = it },
                    label = { Text("닉네임") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val uid = currentUser?.uid
                        val newNickname = nicknameInput.trim()
                        when {
                            uid == null -> {
                                Toast.makeText(context, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                            }

                            newNickname.isBlank() -> {
                                Toast.makeText(context, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show()
                            }

                            else -> {
                            nickname = newNickname
                            showNicknameDialog = false

                            db.collection("user_profiles")
                                .document(uid)
                                .set(
                                    mapOf(
                                        "nickname" to newNickname,
                                        "email" to (currentUser.email ?: ""),
                                        "displayName" to (currentUser.displayName ?: "")
                                    ),
                                    SetOptions.merge()
                                )
                                .addOnSuccessListener {
                                    Toast.makeText(context, "닉네임이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                                    db.collection("recipes")
                                        .whereEqualTo("authorId", uid)
                                        .get()
                                        .addOnSuccessListener { snapshot ->
                                            snapshot.documents.forEach { document ->
                                                document.reference.update("authorNickname", newNickname)
                                            }
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        context,
                                        "닉네임 저장 권한이 없습니다. Firestore Rules에 user_profiles 권한을 추가해주세요.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    }
                ) {
                    Text("저장")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNicknameDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
}


// --- 아래부터는 ProfileScreen을 그리기 위한 부품들입니다 ---

@Composable
private fun ProfileHeaderSection(
    user: UserProfile,
    onEditNickname: () -> Unit
) {
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
                        Column(modifier = Modifier.weight(1f)) {
                            // 전달받은 user 데이터 사용
                            Text(user.name, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text(user.email, color = Color.White, fontSize = 14.sp)
                        }
                        IconButton(onClick = onEditNickname) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "닉네임 수정",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        // 전달받은 user 데이터 사용
                        StatItem("📦", "${user.registeredFoodCount}", "등록한 식품")
                        StatItem("🛒", "${user.shoppingListCount}", "장보기 리스트")
                        StatItem("⏰", "${user.expiringFoodCount}", "곧 만료")
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
private fun LogoutButton(
    onLogOut: () -> Unit
) {
    Card(
        onClick = { onLogOut() },
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
