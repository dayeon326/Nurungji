package com.example.nurungji.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nurungji.ui.viewmodels.InventoryViewModel
import com.example.nurungji.ui.navigation.Screen
import com.example.nurungji.ui.theme.PrimaryGreenDark
import com.example.nurungji.utils.classifyFoodCategoriesWithApi
import com.example.nurungji.utils.classifyFoodCategory
import com.example.nurungji.utils.estimateExpirationDateText
import com.example.nurungji.utils.inventoryCategories
import com.google.firebase.Timestamp
import com.google.firebase.functions.FirebaseFunctions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
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
    var categoryEditedManually by remember { mutableStateOf(false) }

    var categoryExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showPhotoSourceDialog by remember { mutableStateOf(false) }
    var isAnalyzingPhoto by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var detectedItems by remember { mutableStateOf<List<PhotoFoodInfo>>(emptyList()) }
    val inventoryError by viewModel.errorMessage.collectAsState()

    val categories = inventoryCategories

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val functions = remember {
        FirebaseFunctions.getInstance("asia-northeast3")
    }
    val recognizer = remember {
        TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
    }

    LaunchedEffect(inventoryError) {
        val message = inventoryError
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(
                message = "식품 추가 실패: $message",
                duration = SnackbarDuration.Short
            )
        }
    }

    LaunchedEffect(name, categoryEditedManually) {
        val targetName = name.trim()
        if (targetName.isBlank() || categoryEditedManually) return@LaunchedEffect

        delay(700)

        runCatching {
            classifyFoodCategoriesWithApi(functions, listOf(targetName))[targetName]
        }.getOrNull()?.let { apiCategory ->
            if (name.trim() == targetName && !categoryEditedManually) {
                category = apiCategory
                expirationDateText = estimateExpirationDateText(targetName, apiCategory)
            }
        }
    }

    fun applyDetectedFoodInfo(info: PhotoFoodInfo) {
        name = info.itemName
        category = info.category
        quantity = info.quantity
        expirationDateText = info.expirationDateText
        categoryEditedManually = false
        errorMessage = null
    }

    fun applyDetectedFoodInfos(items: List<PhotoFoodInfo>) {
        detectedItems = items
        name = ""
        category = ""
        quantity = ""
        expirationDateText = ""
        categoryEditedManually = false
        errorMessage = null
    }

    fun analyzeFoodImageWithOcr(image: InputImage) {
        isAnalyzingPhoto = true
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val info = extractFoodInfoFromPhotoText(visionText.text)
                if (info == null) {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "식품명을 찾지 못했어요. 직접 입력해주세요.",
                            duration = SnackbarDuration.Short
                        )
                    }
                } else {
                    applyDetectedFoodInfos(listOf(info))
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "사진에서 식품 정보를 채웠어요.",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
                isAnalyzingPhoto = false
            }
            .addOnFailureListener { e ->
                isAnalyzingPhoto = false
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "사진 분석 실패: ${e.message ?: "알 수 없는 오류"}",
                        duration = SnackbarDuration.Short
                    )
                }
            }
    }

    fun analyzeFoodImageWithApi(bitmap: Bitmap, fallbackImage: InputImage) {
        isAnalyzingPhoto = true

        val data = hashMapOf(
            "imageBase64" to bitmap.toCompressedBase64(),
            "mimeType" to "image/jpeg"
        )

        functions
            .getHttpsCallable("analyzeFoodPhoto")
            .call(data)
            .addOnSuccessListener { result ->
                val info = result.data.toPhotoFoodInfoOrNull()
                val items = result.data.toPhotoFoodInfoList()
                if (items.isEmpty() && (info == null || info.itemName.isBlank())) {
                    analyzeFoodImageWithOcr(fallbackImage)
                } else {
                    val normalizedItems = (items.ifEmpty { listOf(info!!) }).map { item ->
                        val finalCategory = item.category.ifBlank {
                            classifyFoodCategory(item.itemName)
                        }
                        item.copy(
                            category = finalCategory,
                            expirationDateText = item.expirationDateText.ifBlank {
                                estimateExpirationDateText(item.itemName, finalCategory)
                            }
                        )
                    }
                    applyDetectedFoodInfos(normalizedItems)
                    isAnalyzingPhoto = false
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "AI가 식품 ${normalizedItems.size}개를 찾았어요.",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }
            .addOnFailureListener {
                analyzeFoodImageWithOcr(fallbackImage)
            }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            analyzeFoodImageWithApi(bitmap, inputImage)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val bitmap = loadBitmapFromUri(context, uri)
            val inputImage = InputImage.fromFilePath(context, uri)
            if (bitmap != null) {
                analyzeFoodImageWithApi(bitmap, inputImage)
            } else {
                analyzeFoodImageWithOcr(inputImage)
            }
        }
    }

    Scaffold(
        snackbarHost = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onNavigate(Screen.Home) },
                        modifier = Modifier
                            .size(60.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(20.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "식품 추가",
                        style = MaterialTheme.typography.headlineLarge,
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickButton(
                        text = if (isAnalyzingPhoto) "분석 중" else "사진 촬영",
                        icon = Icons.Default.CameraAlt,
                        modifier = Modifier.weight(1f),
                        iconBackgroundColor = Color(0xFFD8F3DC),
                        iconTint = PrimaryGreenDark,
                        onClick = {
                            if (!isAnalyzingPhoto) {
                                showPhotoSourceDialog = true
                            }
                        }
                    )

                    QuickButton(
                        text = "영수증 스캔",
                        icon = Icons.Default.ReceiptLong,
                        modifier = Modifier.weight(1f),
                        iconBackgroundColor = Color(0xFFFFE5B4),
                        iconTint = Color(0xFFD4A574),
                        onClick = { onNavigate(Screen.ReceiptScan) } // 영수증 스캔 화면으로 슝!
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (detectedItems.isNotEmpty()) {
                    DetectedPhotoItemsSection(
                        items = detectedItems,
                        categories = categories,
                        onItemsChange = { updatedItems ->
                            detectedItems = updatedItems
                        },
                        onAddAll = {
                            val validItems = detectedItems.mapNotNull { item ->
                                val quantityLong = item.quantity.toLongOrNull() ?: 1L
                                if (item.itemName.isBlank()) {
                                    null
                                } else {
                                    com.example.nurungji.ui.viewmodels.ReceiptInventoryItem(
                                        itemName = item.itemName.trim(),
                                        category = item.category.ifBlank { "기타" },
                                        quantity = if (quantityLong <= 0L) 1L else quantityLong,
                                        expireDate = parseDateToTimestamp(item.expirationDateText)
                                    )
                                }
                            }

                            if (validItems.isNotEmpty()) {
                                viewModel.addInventoryItemsFromReceipt(validItems)
                                detectedItems = emptyList()
                                name = ""
                                category = ""
                                quantity = ""
                                expirationDateText = ""
                                categoryEditedManually = false
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "식품 ${validItems.size}개가 추가되었습니다",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { newName ->
                        val suggestedCategory = classifyFoodCategory(newName)
                        name = newName
                        category = suggestedCategory
                        categoryEditedManually = false
                        expirationDateText = if (newName.isBlank()) {
                            ""
                        } else {
                            estimateExpirationDateText(newName, suggestedCategory)
                        }
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
                        placeholder = { Text("선택하세요") },
                        modifier = Modifier.fillMaxWidth(),
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
                                    categoryEditedManually = true
                                    if (name.isNotBlank()) {
                                        expirationDateText = estimateExpirationDateText(name, item)
                                    }
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
                    placeholder = { Text("예: 1") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    isError = errorMessage != null &&
                            quantity.isNotBlank() &&
                            (quantity.toLongOrNull() == null ||
                                    (quantity.toLongOrNull() ?: 0L) <= 0L)
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
                        val quantityLong = quantity.ifBlank { "1" }.toLongOrNull()

                        errorMessage = when {
                            name.isBlank() -> "식품명을 입력해주세요."
                            category.isBlank() -> "카테고리를 선택해주세요."
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
                            categoryEditedManually = false
                            errorMessage = null

                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "식품이 추가되었습니다",
                                    duration = SnackbarDuration.Short
                                )
                            }
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

        if (showPhotoSourceDialog) {
            AlertDialog(
                onDismissRequest = { showPhotoSourceDialog = false },
                title = { Text("식품 사진 선택") },
                text = { Text("상품명이나 라벨이 잘 보이는 사진을 사용하면 더 정확해요.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showPhotoSourceDialog = false
                            cameraLauncher.launch(null)
                        }
                    ) {
                        Text("카메라 촬영")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showPhotoSourceDialog = false
                            galleryLauncher.launch("image/*")
                        }
                    ) {
                        Text("갤러리 선택")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetectedPhotoItemsSection(
    items: List<PhotoFoodInfo>,
    categories: List<String>,
    onItemsChange: (List<PhotoFoodInfo>) -> Unit,
    onAddAll: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "사진에서 찾은 식품",
            style = MaterialTheme.typography.titleMedium
        )

        items.forEachIndexed { index, item ->
            DetectedPhotoItemCard(
                index = index,
                item = item,
                categories = categories,
                onItemChange = { updated ->
                    onItemsChange(items.toMutableList().also { it[index] = updated })
                },
                onDelete = {
                    onItemsChange(items.toMutableList().also { it.removeAt(index) })
                }
            )
        }

        Button(
            onClick = onAddAll,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("찾은 식품 모두 추가하기")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetectedPhotoItemCard(
    index: Int,
    item: PhotoFoodInfo,
    categories: List<String>,
    onItemChange: (PhotoFoodInfo) -> Unit,
    onDelete: () -> Unit
) {
    var categoryExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "식품 ${index + 1}",
                    style = MaterialTheme.typography.titleSmall
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "삭제")
                }
            }

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
                singleLine = true
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { categoryExpanded = true }
            ) {
                OutlinedTextField(
                    value = item.category,
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

            OutlinedTextField(
                value = item.quantity,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() } || newValue.isBlank()) {
                        onItemChange(item.copy(quantity = newValue))
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
                    value = item.expirationDateText,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text("유통기한") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickButton(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    iconBackgroundColor: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        color = iconBackgroundColor,
                        shape = RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = iconTint,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
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
    } catch (_: Exception) {
        null
    }
}

fun formatMillisToDate(millis: Long): String {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return format.format(Date(millis))
}

private fun loadBitmapFromUri(context: android.content.Context, uri: Uri): Bitmap? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    } catch (_: Exception) {
        null
    }
}

private fun Bitmap.toCompressedBase64(): String {
    val resized = resizeForApi(maxSize = 768)
    val output = ByteArrayOutputStream()
    resized.compress(Bitmap.CompressFormat.JPEG, 75, output)
    return Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
}

private fun Bitmap.resizeForApi(maxSize: Int): Bitmap {
    if (width <= maxSize && height <= maxSize) return this

    val ratio = minOf(
        maxSize.toFloat() / width.toFloat(),
        maxSize.toFloat() / height.toFloat()
    )
    val targetWidth = (width * ratio).toInt().coerceAtLeast(1)
    val targetHeight = (height * ratio).toInt().coerceAtLeast(1)

    return Bitmap.createScaledBitmap(this, targetWidth, targetHeight, true)
}

private data class PhotoFoodInfo(
    val itemName: String,
    val category: String,
    val quantity: String,
    val expirationDateText: String
)

private fun Any?.toPhotoFoodInfoOrNull(): PhotoFoodInfo? {
    val map = this as? Map<*, *> ?: return null
    val itemName = map["itemName"] as? String ?: ""
    val category = map["category"] as? String ?: ""
    val quantity = map["quantity"] as? String ?: ""
    val expirationDateText = map["expirationDateText"] as? String ?: ""

    return PhotoFoodInfo(
        itemName = itemName,
        category = category,
        quantity = quantity,
        expirationDateText = expirationDateText
    )
}

private fun Any?.toPhotoFoodInfoList(): List<PhotoFoodInfo> {
    val map = this as? Map<*, *> ?: return emptyList()
    val rawItems = map["items"] as? List<*> ?: return emptyList()

    return rawItems.mapNotNull { rawItem ->
        rawItem.toPhotoFoodInfoOrNull()
    }.filter { it.itemName.isNotBlank() }
}

private fun extractFoodInfoFromPhotoText(text: String): PhotoFoodInfo? {
    val itemName = extractFoodNameFromPhotoText(text) ?: return null
    val category = classifyFoodCategory(itemName)
    val quantity = extractQuantityFromPhotoText(text)
    val expirationDate = extractFutureDateFromPhotoText(text)
        ?: estimateExpirationDateText(itemName, category)

    return PhotoFoodInfo(
        itemName = itemName,
        category = category,
        quantity = quantity,
        expirationDateText = expirationDate
    )
}

private fun extractFoodNameFromPhotoText(text: String): String? {
    val excludeKeywords = listOf(
        "영양", "정보", "원재료", "함량", "칼로리", "kcal", "보관", "방법", "주의",
        "제조", "판매", "유통", "기한", "소비", "까지", "고객", "센터", "문의",
        "식품", "유형", "품목", "보고", "번호", "바코드", "총내용량", "내용량",
        "나트륨", "탄수화물", "당류", "지방", "단백질", "알레르기", "반품",
        "교환", "주소", "전화", "http", "www"
    )

    val candidates = text.lines()
        .map { line ->
            line.trim()
                .replace(Regex("\\([^)]*\\)"), " ")
                .replace(Regex("[0-9,.:/\\-]+"), " ")
                .replace(Regex("[*#●■□▶▷ㆍ·]"), " ")
                .replace(Regex("\\s+"), " ")
                .trim()
        }
        .filter { it.length in 2..24 }
        .filter { line -> line.contains(Regex("[가-힣A-Za-z]")) }
        .filterNot { line -> excludeKeywords.any { keyword -> line.contains(keyword, ignoreCase = true) } }
        .filterNot { line -> line.matches(Regex("^[A-Za-z]{1,3}$")) }
        .distinct()

    return candidates.firstOrNull { classifyFoodCategory(it) != "기타" }
        ?: candidates.firstOrNull()
}

private fun extractQuantityFromPhotoText(text: String): String {
    val quantityRegexes = listOf(
        Regex("(\\d+)\\s*(개|입|봉|팩|병|캔|장)"),
        Regex("(\\d+)\\s*[xX]\\s*\\d+")
    )

    quantityRegexes.forEach { regex ->
        val quantity = regex.find(text)?.groupValues?.getOrNull(1)?.toLongOrNull()
        if (quantity != null && quantity > 0L) {
            return quantity.toString()
        }
    }

    return "1"
}

private fun extractFutureDateFromPhotoText(text: String): String? {
    val patterns = listOf(
        Regex("(20\\d{2})[.\\-/년 ]\\s*(\\d{1,2})[.\\-/월 ]\\s*(\\d{1,2})"),
        Regex("(\\d{2})[.\\-/]\\s*(\\d{1,2})[.\\-/]\\s*(\\d{1,2})")
    )

    val today = Calendar.getInstance()

    patterns.forEach { regex ->
        regex.findAll(text).forEach { match ->
            val values = match.groupValues
            val rawYear = values.getOrNull(1)?.toIntOrNull() ?: return@forEach
            val year = if (rawYear < 100) 2000 + rawYear else rawYear
            val month = values.getOrNull(2)?.toIntOrNull() ?: return@forEach
            val day = values.getOrNull(3)?.toIntOrNull() ?: return@forEach

            if (month !in 1..12 || day !in 1..31) return@forEach

            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month - 1)
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (!calendar.before(today)) {
                return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            }
        }
    }

    return null
}
