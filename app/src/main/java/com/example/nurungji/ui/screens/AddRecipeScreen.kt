package com.example.nurungji.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nurungji.ui.components.RecipeImage
import com.example.nurungji.ui.viewmodels.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeScreen(
    onBack: () -> Unit,
    recipeViewModel: RecipeViewModel = viewModel()
) {
    var title by remember { mutableStateOf("") }
    var cookingTime by remember { mutableStateOf("") }
    var ingredients by remember { mutableStateOf("") }
    var hashtags by remember { mutableStateOf("") }
    var content by remember { mutableStateOf(TextFieldValue("")) }
    var imageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val inlineImageUris = remember { mutableStateListOf<android.net.Uri>() }

    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri = uri
    }
    val inlineImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val imageIndex = inlineImageUris.size
            inlineImageUris.add(uri)
            content = content.insertRecipeImageMarker(imageIndex)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("나만의 레시피 등록", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("레시피 제목") },
                placeholder = { Text("예: 냉장고 털이 볶음밥") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = cookingTime,
                onValueChange = { cookingTime = it },
                label = { Text("조리 시간 (분)") },
                placeholder = { Text("예: 15") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = ingredients,
                onValueChange = { ingredients = it },
                label = { Text("재료") },
                placeholder = { Text("예: 계란, 양파, 밥") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = hashtags,
                onValueChange = { hashtags = it },
                label = { Text("해시태그") },
                placeholder = { Text("예: 간단요리, 자취요리") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("요리 방법") },
                placeholder = { Text("재료와 만드는 법을 자유롭게 적어주세요. 중간 사진은 버튼으로 삽입할 수 있어요.") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )

            OutlinedButton(
                onClick = { inlineImagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("요리 방법 중간에 사진 삽입")
            }

            if (inlineImageUris.isNotEmpty()) {
                Text(
                    text = "본문 사진 ${inlineImageUris.size}장 삽입됨",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }

            RecipeImagePicker(
                imageModel = imageUri,
                buttonText = "대표 사진 선택",
                onPickImage = { imagePickerLauncher.launch("image/*") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (title.isNotBlank() && content.text.isNotBlank() && cookingTime.isNotBlank()) {
                        recipeViewModel.addRecipe(
                            context = context,
                            title = title,
                            content = content.text,
                            cookingTime = cookingTime,
                            ingredients = ingredients.split(",")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() },
                            hashtags = hashtags
                                .split(",")
                                .map { it.trim().removePrefix("#") }
                                .filter { it.isNotEmpty() },
                            imageUri = imageUri,
                            inlineImageUris = inlineImageUris.toList()
                        ) {
                            onBack()
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "제목, 조리 시간, 요리 방법을 모두 입력해주세요!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF579D74)
                )
            ) {
                Text(
                    "레시피 등록하기",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

fun TextFieldValue.insertRecipeImageMarker(imageIndex: Int): TextFieldValue {
    val marker = "\n[[image:$imageIndex]]\n"
    val start = selection.start.coerceIn(0, text.length)
    val end = selection.end.coerceIn(0, text.length)
    val newText = text.replaceRange(start.coerceAtMost(end), start.coerceAtLeast(end), marker)
    val cursor = start.coerceAtMost(end) + marker.length
    return copy(
        text = newText,
        selection = androidx.compose.ui.text.TextRange(cursor)
    )
}

@Composable
fun RecipeImagePicker(
    imageModel: Any?,
    buttonText: String,
    onPickImage: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF1F4F2)),
            contentAlignment = Alignment.Center
        ) {
            if (imageModel != null && imageModel.toString().isNotBlank()) {
                RecipeImage(
                    imageSource = imageModel,
                    fallbackImageRes = android.R.drawable.ic_menu_gallery,
                    contentDescription = "레시피 사진",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text("선택된 사진 없음", color = Color.Gray)
            }
        }

        OutlinedButton(
            onClick = onPickImage,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(buttonText)
        }
    }
}
