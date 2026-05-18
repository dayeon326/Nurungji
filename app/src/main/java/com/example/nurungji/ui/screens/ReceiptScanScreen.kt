package com.example.nurungji.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nurungji.ui.navigation.Screen
import com.example.nurungji.ui.viewmodels.InventoryViewModel
import com.example.nurungji.ui.viewmodels.ReceiptInventoryItem
import com.example.nurungji.utils.classifyFoodCategory
import com.example.nurungji.utils.estimateExpirationDateText
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions

data class ReceiptEditableItem(
    val itemName: String = "",
    val category: String = "기타",
    val quantity: String = "1",
    val expirationDateText: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScanScreen(
    onBack: () -> Unit,
    onNavigate: (Screen) -> Unit,
    viewModel: InventoryViewModel = viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isExtracting by remember { mutableStateOf(false) }
    val editableItems = remember { mutableStateListOf<ReceiptEditableItem>() }

    val recognizer = remember {
        TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
    }

    val categories = listOf("육류", "유제품", "채소", "과일", "음료", "냉동식품", "기타")

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
        imageBitmap = if (uri != null) loadBitmapFromUri(context, uri) else null
        editableItems.clear()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("영수증 스캔", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로 가기"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap!!.asImageBitmap(),
                    contentDescription = "영수증 이미지",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .background(Color.LightGray)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val uri = imageUri ?: return@Button
                        isExtracting = true
                        editableItems.clear()

                        val image = InputImage.fromFilePath(context, uri)
                        recognizer.process(image)
                            .addOnSuccessListener { visionText ->
                                val extracted = extractReceiptItems(visionText.text)
                                editableItems.clear()
                                editableItems.addAll(
                                    extracted.map {
                                        val category = classifyFoodCategory(it)
                                        ReceiptEditableItem(
                                            itemName = it,
                                            category = category,
                                            quantity = "1",
                                            expirationDateText = estimateExpirationDateText(it, category)
                                        )
                                    }
                                )
                                isExtracting = false
                            }
                            .addOnFailureListener {
                                editableItems.clear()
                                isExtracting = false
                            }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = imageUri != null && !isExtracting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF579D74)
                    )
                ) {
                    Text(
                        text = if (isExtracting) "추출 중..." else "사진에서 글자 추출하기",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "선택한 영수증 이미지가 없습니다.",
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (editableItems.isNotEmpty()) {
                Text(
                    text = "추출한 품목 정보",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                editableItems.forEachIndexed { index, item ->
                    ReceiptEditableItemCard(
                        index = index,
                        item = item,
                        categories = categories,
                        onItemChange = { updated ->
                            editableItems[index] = updated
                        },
                        onDelete = {
                            editableItems.removeAt(index)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                Button(
                    onClick = {
                        val converted = editableItems.mapNotNull { item ->
                            val name = item.itemName.trim()
                            val quantityLong = item.quantity.toLongOrNull() ?: 1L

                            if (name.isBlank()) {
                                null
                            } else {
                                ReceiptInventoryItem(
                                    itemName = name,
                                    category = item.category.ifBlank { "기타" },
                                    quantity = if (quantityLong <= 0L) 1L else quantityLong,
                                    expireDate = parseDateToTimestamp(item.expirationDateText)
                                )
                            }
                        }

                        if (converted.isNotEmpty()) {
                            viewModel.addInventoryItemsFromReceipt(converted)
                            onNavigate(Screen.Inventory)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D32)
                    )
                ) {
                    Text(
                        text = "냉장고에 넣기",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E88E5)
                )
            ) {
                Text(
                    text = if (imageBitmap == null) "갤러리에서 영수증 선택" else "다른 영수증 선택",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReceiptEditableItemCard(
    index: Int,
    item: ReceiptEditableItem,
    categories: List<String>,
    onItemChange: (ReceiptEditableItem) -> Unit,
    onDelete: () -> Unit
) {
    var categoryExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "품목 ${index + 1}",
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = item.itemName,
                onValueChange = { newName ->
                    val suggestedCategory = classifyFoodCategory(newName)
                    onItemChange(
                        item.copy(
                            itemName = newName,
                            category = suggestedCategory,
                            expirationDateText = if (newName.isBlank()) {
                                ""
                            } else {
                                estimateExpirationDateText(newName, suggestedCategory)
                            }
                        )
                    )
                },
                label = { Text("식품명") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "삭제"
                        )
                    }
                }
            )

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = item.category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("카테고리") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Transparent)
                ) {
                    Button(
                        onClick = { categoryExpanded = true },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF74C69D)
                        )
                    ) {
                        Text("선택")
                    }

                    DropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    onItemChange(
                                        item.copy(
                                            category = category,
                                            expirationDateText = if (item.itemName.isBlank()) {
                                                item.expirationDateText
                                            } else {
                                                estimateExpirationDateText(item.itemName, category)
                                            }
                                        )
                                    )
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = item.quantity,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() } || newValue.isBlank()) {
                        onItemChange(item.copy(quantity = newValue))
                    }
                },
                label = { Text("수량") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB7E4C7))
            ) {
                Text(
                    text = if (item.expirationDateText.isBlank()) {
                        "유통기한 선택"
                    } else {
                        "유통기한: ${item.expirationDateText}"
                    },
                    color = Color.Black
                )
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
                                    onItemChange(
                                        item.copy(
                                            expirationDateText = formatMillisToDate(millis)
                                        )
                                    )
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
    }
}

private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    } catch (_: Exception) {
        null
    }
}

private fun extractReceiptItems(text: String): List<String> {
    val excludeKeywords = listOf(
        "합계", "총액", "금액", "할인", "카드", "신용카드", "부가세", "공급가액",
        "결제", "승인", "매출전표", "고객", "영수증", "거스름", "잔액", "수량", "단가",
        "과세", "면세", "봉사료", "판매총액", "받을금액", "결제수단", "할인내역",
        "사업자", "대표", "주소", "전화", "TEL", "Tel", "tel", "마트", "매장",
        "점포", "포인트", "적립", "쿠폰", "교환", "환불", "문의", "계산원",
        "일시", "거래", "바코드", "품목", "상품명", "단위", "공급", "합산"
    )

    val lines = text.lines()
        .map { it.trim() }
        .filter { it.isNotBlank() }

    val items = mutableListOf<String>()

    for (index in lines.indices) {
        val line = lines[index]
        val nextLine = lines.getOrNull(index + 1).orEmpty()

        if (excludeKeywords.any { keyword -> line.contains(keyword) }) continue
        if (line.matches(Regex("^[0-9,.:\\- /()]+$"))) continue
        if (line.contains(Regex("\\d{2,4}[./-]\\d{1,2}[./-]\\d{1,2}"))) continue
        if (line.contains(Regex("\\d{2,3}-\\d{3,4}-\\d{4}"))) continue

        val hasPriceInLine = line.contains(Regex("\\d{3,}(,\\d{3})*"))
        val nextLineLooksLikePrice = nextLine.matches(Regex("^[0-9, ]{3,}원?$"))

        var candidate = line
            .replace(Regex("\\([^)]*\\)"), " ")
            .replace(Regex("^\\d+\\s*"), " ")
            .replace(Regex("\\s+[0-9,]{3,}\\s*원?$"), " ")
            .replace(Regex("[0-9,]+\\s*원"), " ")
            .replace(Regex("\\d+[.]\\d+"), " ")
            .replace(Regex("\\d+[개입봉캔병장팩kgKgGmlML]+"), " ")
            .replace(Regex("[*#●■□▶▷ㆍ·]"), " ")
            .replace(":", " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        candidate = candidate
            .replace(Regex("^[가-힣A-Za-z]?\\s"), "")
            .trim()

        val hasName = candidate.contains(Regex("[가-힣A-Za-z]"))
        val knownFoodCategory = classifyFoodCategory(candidate)
        val likelyProductLine = hasPriceInLine || nextLineLooksLikePrice || knownFoodCategory != "기타"

        if (
            likelyProductLine &&
            hasName &&
            candidate.length in 2..24 &&
            excludeKeywords.none { candidate.contains(it) } &&
            !candidate.matches(Regex("^[ㄱ-ㅎㅏ-ㅣ]+$")) &&
            !candidate.matches(Regex("^[A-Za-z]{1,3}$"))
        ) {
            items.add(candidate)
        }
    }

    return items.distinct().take(15)
}
