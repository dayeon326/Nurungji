package com.example.nurungji.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nurungji.data.InventoryItem
import com.example.nurungji.ui.viewmodels.InventoryViewModel
import com.example.nurungji.ui.navigation.Screen
import com.example.nurungji.ui.theme.PrimaryGreenDark
import com.example.nurungji.ui.theme.TextSecondary
import com.example.nurungji.utils.inventoryCategoriesWithAll
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import kotlinx.coroutines.launch
import com.google.firebase.Timestamp
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.clip


@Composable
fun InventoryScreen(
    onNavigate: (Screen) -> Unit,
    viewModel: InventoryViewModel = viewModel()
) {
    val inventoryItems by viewModel.inventoryItems.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var selectedCategory by remember { mutableStateOf("전체") }
    var searchQuery by remember { mutableStateOf("") }
    var editingItem by remember { mutableStateOf<InventoryItem?>(null) }

    val categories = inventoryCategoriesWithAll

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadInventory()
    }

    val filteredItems = inventoryItems.filter { item ->
        val matchesCategory = selectedCategory == "전체" || item.category == selectedCategory
        val matchesSearch = item.itemName.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Scaffold(
        snackbarHost = {
            Box(modifier = Modifier.padding(bottom = 80.dp)) {
                SnackbarHost(hostState = snackbarHostState)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            InventoryHeader(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                onAddClick = { onNavigate(Screen.AddItem) }
            )

            CategorySection(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )

            errorMessage?.let {
                Text(
                    text = "오류: $it",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (filteredItems.isEmpty()) {
                EmptyInventoryState(
                    hasSearchQuery = searchQuery.isNotBlank(),
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredItems, key = { it.documentId }) { item ->
                        InventoryGridCard(
                            item = item,
                            onEdit = {
                                editingItem = item
                            },
                            onDelete = {
                                val deletedItem = item

                                viewModel.deleteInventory(deletedItem.documentId)

                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "${deletedItem.itemName} 삭제됨",
                                        actionLabel = "되돌리기",
                                        duration = SnackbarDuration.Long
                                    )

                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.addInventory(
                                            itemName = deletedItem.itemName,
                                            category = deletedItem.category,
                                            quantity = deletedItem.quantity,
                                            expireDate = deletedItem.expireDate
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }

            editingItem?.let { item ->
                EditInventoryDialog(
                    item = item,
                    categories = inventoryCategoriesWithAll.filter { it != "전체" },
                    onDismiss = { editingItem = null },
                    onSave = { itemName, category, quantity, expireDate ->
                        viewModel.updateInventory(
                            documentId = item.documentId,
                            itemName = itemName,
                            category = category,
                            quantity = quantity,
                            expireDate = expireDate
                        )
                        editingItem = null
                    }
                )
            }
        }
    }
}

@Composable
fun InventoryHeader(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onAddClick: () -> Unit
) {
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
            .padding(start = 24.dp, end = 24.dp, top = 40.dp, bottom = 24.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "식품 재고",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )

                IconButton(
                    onClick = onAddClick,
                    modifier = Modifier
                        .background(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "식품 추가",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { onSearchChange(it) },
                placeholder = { Text("식품 검색...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color(0xFF74C69D)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    cursorColor = PrimaryGreenDark
                )
            )
        }
    }
}

@Composable
fun CategorySection(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-8).dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val isSelected = selectedCategory == category

            AssistChip(
                onClick = { onCategorySelected(category) },
                label = { Text(category) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (isSelected) {
                        PrimaryGreenDark
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    labelColor = if (isSelected) {
                        Color.White
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            )
        }
    }
}

@Composable
fun EmptyInventoryState(
    hasSearchQuery: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = Color(0xFFF0F7F4),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "빈 상태",
                    tint = Color(0xFF74C69D),
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (hasSearchQuery) "검색 결과가 없습니다" else "등록된 식품이 없습니다",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun InventoryGridCard(
    item: InventoryItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val expireDate = item.expireDate?.toDate()
    val daysLeft = calculateDaysLeft(expireDate)
    val badgeLabel = dDayText(daysLeft)
    val badgeColor = dDayBackgroundColor(daysLeft)
    val badgeTextColor = dDayTextColor(daysLeft)

    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(56.dp)
                        .background(
                            color = Color(0xFFF0F7F4),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getCategoryEmoji(item.category),
                        style = MaterialTheme.typography.headlineLarge
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 12.dp, y = (-10).dp)
                ) {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "재고 메뉴"
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("수정") },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("삭제") },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null)
                            },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = item.itemName,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${item.quantity}개",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.category,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )

                Text(
                    text = badgeLabel,
                    modifier = Modifier
                        .background(
                            color = badgeColor,
                            shape = RoundedCornerShape(50.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    color = badgeTextColor,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "유통기한: ${formatExpireDate(expireDate)}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditInventoryDialog(
    item: InventoryItem,
    categories: List<String>,
    onDismiss: () -> Unit,
    onSave: (String, String, Long, Timestamp?) -> Unit
) {
    var itemName by remember(item.documentId) { mutableStateOf(item.itemName) }
    var category by remember(item.documentId) { mutableStateOf(item.category.ifBlank { "기타" }) }
    var quantity by remember(item.documentId) { mutableStateOf(item.quantity.toString()) }
    var expirationDateText by remember(item.documentId) {
        mutableStateOf(formatExpireDate(item.expireDate?.toDate()).takeIf { it != "없음" } ?: "")
    }
    var categoryExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("재고 수정") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = itemName,
                    onValueChange = {
                        itemName = it
                        errorMessage = null
                    },
                    label = { Text("식품명") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { categoryExpanded = true }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("카테고리") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    DropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { categoryOption ->
                            DropdownMenuItem(
                                text = { Text(categoryOption) },
                                onClick = {
                                    category = categoryOption
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() } || newValue.isBlank()) {
                            quantity = newValue
                            errorMessage = null
                        }
                    },
                    label = { Text("수량") },
                    placeholder = { Text("예: 1") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                ) {
                    OutlinedTextField(
                        value = expirationDateText,
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("유통기한") },
                        placeholder = { Text("날짜 선택") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val quantityLong = quantity.ifBlank { "1" }.toLongOrNull()
                    errorMessage = when {
                        itemName.isBlank() -> "식품명을 입력해주세요."
                        category.isBlank() -> "카테고리를 선택해주세요."
                        quantityLong == null -> "수량은 숫자로 입력해주세요."
                        quantityLong <= 0L -> "수량은 1 이상이어야 합니다."
                        else -> null
                    }

                    if (errorMessage == null) {
                        onSave(
                            itemName.trim(),
                            category,
                            quantityLong!!,
                            parseDateToTimestamp(expirationDateText)
                        )
                    }
                }
            ) {
                Text("저장")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            expirationDateText = formatMillisToDate(millis)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("취소")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

fun formatExpireDate(date: Date?): String {
    if (date == null) return "없음"
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return format.format(date)
}

fun getCategoryEmoji(category: String): String {
    return when (category) {
        "채소" -> "🥦"
        "육류" -> "🥩"
        "해산물" -> "🐟"
        "유제품" -> "🧀"
        "과일" -> "🍇"
        "음료" -> "🧃"
        "냉동식품" -> "❄️"
        "밀키트" -> "🍲"
        "간편식" -> "🍱"
        "가공식품" -> "🥫"
        "곡류" -> "🌾"
        "조미료" -> "🧂"
        else -> "📦"
    }
}

fun calculateDaysLeft(date: Date?): Int? {
    if (date == null) return null

    val todayCal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val expireCal = Calendar.getInstance().apply {
        time = date
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val diff = expireCal.timeInMillis - todayCal.timeInMillis
    return TimeUnit.MILLISECONDS.toDays(diff).toInt()
}

fun dDayText(daysLeft: Int?): String {
    return when {
        daysLeft == null -> "날짜 없음"
        daysLeft < 0 -> "만료됨"
        daysLeft == 0 -> "D-Day"
        else -> "D-$daysLeft"
    }
}

fun dDayBackgroundColor(daysLeft: Int?): Color {
    return when {
        daysLeft == null -> Color(0xFFE0E0E0)
        daysLeft <= 3 -> Color(0xFFFF6B6B)
        daysLeft <= 7 -> Color(0xFFFFB84D)
        else -> Color(0xFFD8F3DC)
    }
}

fun dDayTextColor(daysLeft: Int?): Color {
    return when {
        daysLeft == null -> Color.Gray
        daysLeft <= 7 -> Color.White
        else -> Color(0xFF2D6A4F)
    }
}
