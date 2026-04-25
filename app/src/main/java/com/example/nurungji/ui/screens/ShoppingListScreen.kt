package com.example.nurungji.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.example.nurungji.ui.theme.*
import com.example.nurungji.ui.viewmodels.ShoppingItem
import com.example.nurungji.ui.viewmodels.ShoppingListViewModel
import com.example.nurungji.ui.navigation.Screen

@Composable
fun ShoppingListScreen(
    onNavigate: (Screen) -> Unit,
    shoppingListViewModel: ShoppingListViewModel = viewModel()
) {
    val items by shoppingListViewModel.shoppingItems.collectAsState()

    LaunchedEffect(Unit) {
        shoppingListViewModel.loadShoppingItems()
    }

    val remainingItems = items.filter { !it.checked }
    val completedItems = items.filter { it.checked }
    var showAddDialog by remember { mutableStateOf(false) }
    var newItemName by remember { mutableStateOf("") }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundCream)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(PrimaryGreen, PrimaryGreenDark)
                        )
                    )
                    .padding(top = 40.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)
            ) {
                Column {
                    Text(
                        text = "장보기 리스트",
                        color = CardWhite,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CardWhite, RoundedCornerShape(24.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SummaryItem("전체", items.size, "all")
                        SummaryItem("남은 재료", remainingItems.size, "remaining")
                        SummaryItem("구매 완료", completedItems.size, "completed")
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 20.dp,
                    bottom = 100.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (remainingItems.isNotEmpty()) {
                    item {
                        SectionTitle("남은 재료", remainingItems.size, false)
                    }

                    items(remainingItems) { item ->
                        ShoppingItemRow(
                            item = item,
                            onCheck = {
                                shoppingListViewModel.toggleChecked(item.id, item.checked)
                            },
                            onDelete = {
                                shoppingListViewModel.deleteItem(item.id)
                            }
                        )
                    }
                }

                if (completedItems.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        SectionTitle("구매 완료", completedItems.size, true)
                    }

                    items(completedItems) { item ->
                        ShoppingItemRow(
                            item = item,
                            onCheck = {
                                shoppingListViewModel.toggleChecked(item.id, item.checked)
                            },
                            onDelete = {
                                shoppingListViewModel.deleteItem(item.id)
                            }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = PrimaryGreenDark,
            contentColor = CardWhite,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "+",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = {
                    showAddDialog = false
                    newItemName = ""
                },
                title = {
                    Text("재료 추가", fontWeight = FontWeight.Bold)
                },
                text = {
                    OutlinedTextField(
                        value = newItemName,
                        onValueChange = { newItemName = it },
                        placeholder = { Text("예: 계란") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            shoppingListViewModel.addItem(context, newItemName)
                            showAddDialog = false
                            newItemName = ""
                        }
                    ) {
                        Text("추가", color = PrimaryGreenDark)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showAddDialog = false
                            newItemName = ""
                        }
                    ) {
                        Text("취소", color = TextSecondary)
                    }
                }
            )
        }
    }
}

@Composable
private fun SummaryItem(
    title: String,
    count: Int,
    type: String
) {
    val icon = when (type) {
        "remaining" -> Icons.Default.ShoppingCart
        "completed" -> Icons.Default.Check
        else -> Icons.Default.ShoppingCart
    }

    val iconColor = when (type) {
        "remaining" -> AccentYellow
        "completed" -> PrimaryGreenDark
        else -> PrimaryGreenDark
    }

    val bgColor = when (type) {
        "remaining" -> AccentYellow.copy(alpha = 0.35f)
        "completed" -> MutedGreen
        else -> MutedGreen
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(90.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(bgColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(title, fontSize = 13.sp, color = TextSecondary)

        Text(
            text = "${count}개",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

@Composable
private fun SectionTitle(title: String, count: Int, checked: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    if (checked) PrimaryGreenDark else MutedGreen,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = if (checked) CardWhite else PrimaryGreenDark
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "$title ($count)",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

@Composable
private fun ShoppingItemRow(
    item: ShoppingItem,
    onCheck: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.checked,
                onCheckedChange = { onCheck() },
                colors = CheckboxDefaults.colors(
                    checkedColor = PrimaryGreenDark,
                    uncheckedColor = TextSecondary
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = item.name,
                modifier = Modifier.weight(1f),
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (item.checked) TextSecondary else TextPrimary,
                textDecoration = if (item.checked)
                    TextDecoration.LineThrough
                else
                    TextDecoration.None
            )

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "삭제",
                    tint = DestructiveRed.copy(alpha = 0.7f)
                )
            }
        }
    }
}