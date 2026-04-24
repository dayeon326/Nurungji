package com.example.nurungji.ui.viewmodels

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.nurungji.models.Recipe
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class RecipeViewModel : ViewModel() {
    var selectedRecipeId by androidx.compose.runtime.mutableStateOf<String?>(null)

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    // 화면에 보여줄 진짜 리스트
    var recipes = mutableStateListOf<Recipe>()

    init {
        fetchRecipes()
    }

    // 데이터 가져오는 요리사!
    private fun fetchRecipes() {
        db.collection("recipes")
            .orderBy("createdAt", Query.Direction.DESCENDING) // 최신순 정렬
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener

                if (snapshot != null) {
                    val list = snapshot.toObjects(Recipe::class.java)
                    recipes.clear()
                    recipes.addAll(list)
                }
            }
    }

    // 데이터 쏘는 요리사
    fun addRecipe(context: Context, title: String, content: String, onSuccess: () -> Unit) {
        val currentUserUid = auth.currentUser?.uid

        val newRecipe = hashMapOf(
            "title" to title,
            "content" to content,
            "authorId" to currentUserUid,
            "createdAt" to FieldValue.serverTimestamp(),
            "recommendUids" to emptyList<String>()
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

    // 추천 누르기 / 취소하기 기능
    fun toggleRecommend(recipeId: String, currentRecommendUids: List<String>) {
        val uid = auth.currentUser?.uid ?: return // 로그인 안 했으면 무시
        val recipeRef = db.collection("recipes").document(recipeId) // 업데이트할 문서 찾기

        if (currentRecommendUids.contains(uid)) {
            // 이미 내 UID가 명단에 있으면 -> 추천 취소 (명단에서 빼기)
            recipeRef.update("recommendUids", FieldValue.arrayRemove(uid))
        } else {
            // 명단에 없으면 -> 추천하기 (명단에 넣기)
            recipeRef.update("recommendUids", FieldValue.arrayUnion(uid))
        }
    }

    fun toggleScrap(recipeId: String, currentScrapUids: List<String>) {
        val uid = auth.currentUser?.uid ?: return
        val recipeRef = db.collection("recipes").document(recipeId)

        if (currentScrapUids.contains(uid)) {
            // 이미 스크랩 명단에 있으면 -> 스크랩 취소 (명단에서 빼기)
            recipeRef.update("scrapUids", FieldValue.arrayRemove(uid))
        } else {
            // 명단에 없으면 -> 스크랩하기 (명단에 넣기)
            recipeRef.update("scrapUids", FieldValue.arrayUnion(uid))
        }
    }
    fun updateRecipe(context: Context, recipeId: String, title: String, content: String, onSuccess: () -> Unit) {
        db.collection("recipes").document(recipeId)
            .update(mapOf("title" to title, "content" to content))
            .addOnSuccessListener {
                Toast.makeText(context, "수정 완료", Toast.LENGTH_SHORT).show()
                onSuccess()
            }
    }
    fun deleteRecipe(context: Context, recipeId: String, onSuccess: () -> Unit) {
        db.collection("recipes").document(recipeId)
            .delete() // 파이어베이스 문서 삭제
            .addOnSuccessListener {
                Toast.makeText(context, "삭제되었습니다.", Toast.LENGTH_SHORT).show()
                onSuccess()
            }
    }
}