package com.example.nurungji.ui.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.nurungji.models.Recipe
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.io.ByteArrayOutputStream

class RecipeViewModel : ViewModel() {
    var selectedRecipeId by mutableStateOf<String?>(null)
    var recipeLoadError by mutableStateOf<String?>(null)

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    private var recipeListener: ListenerRegistration? = null

    // 화면에 보여줄 진짜 리스트
    var recipes = mutableStateListOf<Recipe>()

    init {
        fetchRecipes()
    }

    // 데이터 가져오는 요리사!
    private fun fetchRecipes() {
        recipeListener?.remove()
        recipeListener = db.collection("recipes")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    recipeLoadError = e.message
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { document ->
                        document.toObject(Recipe::class.java)?.copy(
                            id = document.id
                        )
                    }.sortedByDescending {
                        it.createdAt?.seconds ?: 0L
                    }
                    recipes.clear()
                    recipes.addAll(list)
                    recipeLoadError = null
                }
            }
    }

    // 데이터 쏘는 요리사
    fun addRecipe(
        context: Context,
        title: String,
        content: String,
        cookingTime: String,
        ingredients: List<String>,
        hashtags: List<String>,
        imageUri: Uri?,
        onSuccess: () -> Unit
    ) {
        val currentUserUid = auth.currentUser?.uid
        val fallbackNickname = auth.currentUser?.displayName?.takeIf { it.isNotBlank() } ?: "익명"

        val saveRecipe: (String, String) -> Unit = { imageUrl, authorNickname ->
            val newRecipe = hashMapOf(
            "title" to title,
            "name" to title, // RecipeCard에서 사용
            "content" to content,
            "time" to "${cookingTime}분",
            "ingredients" to ingredients,
            "hashtags" to hashtags,
            "authorId" to currentUserUid,
            "authorNickname" to authorNickname,
            "imageUrl" to imageUrl,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
            "recommendUids" to emptyList<String>(),
            "scrapUids" to emptyList<String>()
            )

            db.collection("recipes")
                .add(newRecipe)
                .addOnSuccessListener {
                    Toast.makeText(context, "레시피 등록 완료!", Toast.LENGTH_SHORT).show()
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "등록 실패: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        loadCurrentNickname(fallbackNickname) { authorNickname ->
            prepareRecipeImage(
                context = context,
                imageUri = imageUri,
                onSuccess = { imageUrl -> saveRecipe(imageUrl, authorNickname) },
                onFailure = { e ->
                    Toast.makeText(context, "사진 처리 실패: ${e.message}", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    private fun loadCurrentNickname(
        fallbackNickname: String,
        onLoaded: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onLoaded(fallbackNickname)
            return
        }

        db.collection("user_profiles")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                val nickname = document.getString("nickname")
                    ?.takeIf { it.isNotBlank() }
                    ?: fallbackNickname
                onLoaded(nickname)
            }
            .addOnFailureListener {
                onLoaded(fallbackNickname)
            }
    }

    // 추천 누르기 / 취소하기 기능
    fun toggleRecommend(context: Context, recipeId: String, currentRecommendUids: List<String>) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(context, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }
        val recipeRef = db.collection("recipes").document(recipeId) // 업데이트할 문서 찾기

        val isCancel = currentRecommendUids.contains(uid)
        val updateTask = if (isCancel) {
            // 이미 내 UID가 명단에 있으면 -> 추천 취소 (명단에서 빼기)
            recipeRef.update("recommendUids", FieldValue.arrayRemove(uid))
        } else {
            // 명단에 없으면 -> 추천하기 (명단에 넣기)
            recipeRef.update("recommendUids", FieldValue.arrayUnion(uid))
        }

        updateTask
            .addOnSuccessListener {
                Toast.makeText(
                    context,
                    if (isCancel) "추천을 취소했습니다." else "추천했습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    context,
                    "추천 실패: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    fun toggleScrap(context: Context, recipeId: String, currentScrapUids: List<String>) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(context, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }
        val recipeRef = db.collection("recipes").document(recipeId)

        val isCancel = currentScrapUids.contains(uid)
        val updateTask = if (isCancel) {
            // 이미 스크랩 명단에 있으면 -> 스크랩 취소 (명단에서 빼기)
            recipeRef.update("scrapUids", FieldValue.arrayRemove(uid))
        } else {
            // 명단에 없으면 -> 스크랩하기 (명단에 넣기)
            recipeRef.update("scrapUids", FieldValue.arrayUnion(uid))
        }

        updateTask
            .addOnSuccessListener {
                Toast.makeText(
                    context,
                    if (isCancel) "스크랩을 취소했습니다." else "스크랩했습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    context,
                    "스크랩 실패: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
    fun updateRecipe(
        context: Context,
        recipeId: String,
        title: String,
        content: String,
        cookingTime: String,
        ingredients: List<String>,
        hashtags: List<String>,
        imageUri: Uri?,
        currentImageUrl: String,
        onSuccess: () -> Unit
    ) {
        val updateRecipe: (String) -> Unit = { imageUrl ->
            db.collection("recipes").document(recipeId)
                .update(
                    mapOf(
                        "title" to title,
                        "name" to title,
                        "content" to content,
                        "time" to "${cookingTime}분",
                        "ingredients" to ingredients,
                        "hashtags" to hashtags,
                        "imageUrl" to imageUrl,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .addOnSuccessListener {
                    Toast.makeText(context, "수정 완료", Toast.LENGTH_SHORT).show()
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "수정 실패: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        if (imageUri == null) {
            updateRecipe(currentImageUrl)
        } else {
            prepareRecipeImage(
                context = context,
                imageUri = imageUri,
                onSuccess = updateRecipe,
                onFailure = { e ->
                    Toast.makeText(context, "사진 처리 실패: ${e.message}", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    private fun prepareRecipeImage(
        context: Context,
        imageUri: Uri?,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (imageUri == null) {
            onSuccess("")
            return
        }

        Thread {
            try {
                val bytes = context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
                    ?: throw IllegalArgumentException("사진 파일을 읽을 수 없습니다.")
                val originalBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    ?: throw IllegalArgumentException("사진 형식을 읽을 수 없습니다.")

                val resizedBitmap = resizeBitmap(originalBitmap, maxSize = 700)
                val output = ByteArrayOutputStream()
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, output)
                val encodedImage = Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
                val imageData = "data:image/jpeg;base64,$encodedImage"

                Handler(Looper.getMainLooper()).post {
                    onSuccess(imageData)
                }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    onFailure(e)
                }
            }
        }.start()
    }

    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxSize && height <= maxSize) return bitmap

        val ratio = minOf(
            maxSize.toFloat() / width.toFloat(),
            maxSize.toFloat() / height.toFloat()
        )
        val targetWidth = (width * ratio).toInt().coerceAtLeast(1)
        val targetHeight = (height * ratio).toInt().coerceAtLeast(1)

        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }

    fun deleteRecipe(context: Context, recipeId: String, onSuccess: () -> Unit) {
        db.collection("recipes").document(recipeId)
            .delete() // 파이어베이스 문서 삭제
            .addOnSuccessListener {
                Toast.makeText(context, "삭제되었습니다.", Toast.LENGTH_SHORT).show()
                onSuccess()
            }
    }

    override fun onCleared() {
        recipeListener?.remove()
        super.onCleared()
    }
}
