package com.example.nurungji.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.google.firebase.storage.FirebaseStorage

const val RECIPE_IMAGE_STORAGE_PREFIX = "storage:"
private const val RECIPE_IMAGE_DATA_PREFIX = "data:image"

@Composable
fun RecipeImage(
    imageSource: Any?,
    fallbackImageRes: Int,
    modifier: Modifier = Modifier,
    contentDescription: String? = "레시피 사진"
) {
    when (imageSource) {
        is Uri -> LocalUriImage(
            uri = imageSource,
            fallbackImageRes = fallbackImageRes,
            modifier = modifier,
            contentDescription = contentDescription
        )

        is String -> {
            if (imageSource.startsWith(RECIPE_IMAGE_DATA_PREFIX)) {
                EncodedRecipeImage(
                    imageData = imageSource,
                    fallbackImageRes = fallbackImageRes,
                    modifier = modifier,
                    contentDescription = contentDescription
                )
            } else if (imageSource.startsWith(RECIPE_IMAGE_STORAGE_PREFIX)) {
                StorageRecipeImage(
                    storagePath = imageSource.removePrefix(RECIPE_IMAGE_STORAGE_PREFIX),
                    fallbackImageRes = fallbackImageRes,
                    modifier = modifier,
                    contentDescription = contentDescription
                )
            } else if (imageSource.isBlank()) {
                FallbackRecipeImage(
                    imageRes = fallbackImageRes,
                    modifier = modifier,
                    contentDescription = contentDescription
                )
            } else {
                FallbackRecipeImage(
                    imageRes = fallbackImageRes,
                    modifier = modifier,
                    contentDescription = contentDescription
                )
            }
        }

        else -> FallbackRecipeImage(
            imageRes = fallbackImageRes,
            modifier = modifier,
            contentDescription = contentDescription
        )
    }
}

@Composable
private fun EncodedRecipeImage(
    imageData: String,
    fallbackImageRes: Int,
    modifier: Modifier,
    contentDescription: String?
) {
    val base64 = imageData.substringAfter(",", missingDelimiterValue = "")
    val bitmap = remember(imageData) {
        runCatching {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }.getOrNull()
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        FallbackRecipeImage(
            imageRes = fallbackImageRes,
            modifier = modifier,
            contentDescription = contentDescription
        )
    }
}

@Composable
private fun LocalUriImage(
    uri: Uri,
    fallbackImageRes: Int,
    modifier: Modifier,
    contentDescription: String?
) {
    val context = LocalContext.current
    var imageBytes by remember(uri) { mutableStateOf<ByteArray?>(null) }

    LaunchedEffect(uri) {
        imageBytes = runCatching {
            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        }.getOrNull()
    }

    val bitmap = remember(imageBytes) {
        imageBytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        FallbackRecipeImage(
            imageRes = fallbackImageRes,
            modifier = modifier,
            contentDescription = contentDescription
        )
    }
}

@Composable
private fun StorageRecipeImage(
    storagePath: String,
    fallbackImageRes: Int,
    modifier: Modifier,
    contentDescription: String?
) {
    var imageBytes by remember(storagePath) { mutableStateOf<ByteArray?>(null) }
    var isLoading by remember(storagePath) { mutableStateOf(true) }

    LaunchedEffect(storagePath) {
        isLoading = true
        FirebaseStorage.getInstance("gs://nurungji-33f8c.firebasestorage.app")
            .reference
            .child(storagePath)
            .getBytes(5L * 1024L * 1024L)
            .addOnSuccessListener { bytes ->
                imageBytes = bytes
                isLoading = false
            }
            .addOnFailureListener {
                imageBytes = null
                isLoading = false
            }
    }

    val bitmap = remember(imageBytes) {
        imageBytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
    }

    when {
        bitmap != null -> Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )

        isLoading -> Box(
            modifier = modifier.background(Color(0xFFF1F4F2)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        else -> FallbackRecipeImage(
            imageRes = fallbackImageRes,
            modifier = modifier,
            contentDescription = contentDescription
        )
    }
}

@Composable
private fun FallbackRecipeImage(
    imageRes: Int,
    modifier: Modifier,
    contentDescription: String?
) {
    Image(
        painter = painterResource(id = imageRes),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}
