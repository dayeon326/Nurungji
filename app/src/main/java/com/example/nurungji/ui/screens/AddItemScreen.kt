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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ReceiptLong
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nurungji.ui.InventoryViewModel
import com.example.nurungji.ui.navigation.Screen
import com.example.nurungji.ui.theme.PrimaryGreenDark
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
    onNavigate: (Screen) -> Unit,
    viewModel: InventoryViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var expirationDateText by remember { mutableStateOf("") }

    var categoryExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val categories = listOf("채소", "육류", "유제품", "과일", "음료", "냉동식품", "기타")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            PrimaryGreenDark
                        )
                    )
                )
                .padding(top = 40.dp, bottom = 20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                IconButton(onClick = { onNavigate(Screen.Home) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = Color.White
                    )
                }

                Text(
                    text = "식품 추가",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickButton(
                    text = "사진 촬영",
                    icon = Icons.Default.CameraAlt,
                    modifier = Modifier.weight(1f)
                )

                QuickButton(
                    text = "영수증 스캔",
                    icon = Icons.Default.ReceiptLong,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    errorMessage = null
                },
                label = { Text("식품명") },
                placeholder = { Text("예: 토마토") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                isError = errorMessage != null && name.isBlank()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("카테고리") },
                    placeholder = { Text("선택하세요") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { categoryExpanded = true },
                    shape = RoundedCornerShape(16.dp),
                    isError = errorMessage != null && category.isBlank()
                )

                DropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    categories.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                category = item
                                categoryExpanded = false
                                errorMessage = null
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = quantity,
                onValueChange = {
                    quantity = it
                    errorMessage = null
                },
                label = { Text("수량") },
                placeholder = { Text("예: 3") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                isError = errorMessage != null && (
                        quantity.isBlank() ||
                                quantity.toLongOrNull() == null ||
                                (quantity.toLongOrNull() ?: 0L) <= 0L
                        )
            )

            Spacer(modifier = Modifier.height(12.dp))

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
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    isError = errorMessage != null && expirationDateText.isBlank()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val quantityLong = quantity.toLongOrNull()

                    errorMessage = when {
                        name.isBlank() -> "식품명을 입력해주세요."
                        category.isBlank() -> "카테고리를 선택해주세요."
                        quantity.isBlank() -> "수량을 입력해주세요."
                        quantityLong == null -> "수량은 숫자로 입력해주세요."
                        quantityLong <= 0L -> "수량은 1 이상이어야 합니다."
                        expirationDateText.isBlank() -> "유통기한을 선택해주세요."
                        else -> null
                    }

                    if (errorMessage == null) {
                        val expireTimestamp = parseDateToTimestamp(expirationDateText)

                        viewModel.addInventory(
                            itemName = name.trim(),
                            category = category,
                            quantity = quantityLong!!,
                            expireDate = expireTimestamp
                        )

                        name = ""
                        category = ""
                        quantity = ""
                        expirationDateText = ""
                        errorMessage = null

                        onNavigate(Screen.Inventory)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("식품 추가하기")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8F5E9)
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "📝 유통기한 알림",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "유통기한이 가까워지면 자동으로 알림을 보내드려요.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }

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
                            errorMessage = null
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false }
                ) {
                    Text("취소")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun QuickButton(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = if (text == "영수증 스캔") Color(0xFFD4A574) else MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text)
        }
    }
}

fun parseDateToTimestamp(dateString: String): Timestamp? {
    return try {
        if (dateString.isBlank()) return null

        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("Asia/Seoul")
        val date = format.parse(dateString)

        if (date != null) Timestamp(date) else null
    } catch (e: Exception) {
        null
    }
}

fun formatMillisToDate(millis: Long): String {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return format.format(Date(millis))
}