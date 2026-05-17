package com.example.nurungji.ui.viewmodels

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.google.firebase.Timestamp
import java.util.Calendar


class ShoppingListViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _shoppingItems = MutableStateFlow<List<ShoppingItem>>(emptyList())
    val shoppingItems: StateFlow<List<ShoppingItem>> = _shoppingItems.asStateFlow()

    fun loadShoppingItems(context: Context) {
        val uid = auth.currentUser?.uid ?: return

        addAutoRecommendedItems(context)

        db.collection("shopping_list")
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val items = snapshot.documents.map { doc ->
                        ShoppingItem(
                            id = doc.id,
                            userId = doc.getString("userId") ?: "",
                            name = doc.getString("name") ?: "",
                            checked = doc.getBoolean("checked") ?: false,
                            source = doc.getString("source") ?: "manual"
                        )
                    }

                    _shoppingItems.value = items
                }
            }
    }

    fun addItem(context: Context, itemName: String) {
        val uid = auth.currentUser?.uid ?: return

        if (itemName.isBlank()) {
            Toast.makeText(context, "재료명을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val data = hashMapOf(
            "userId" to uid,
            "name" to itemName.trim(),
            "checked" to false,
            "source" to "manual"
        )

        db.collection("shopping_list")
            .add(data)
            .addOnSuccessListener {
                Toast.makeText(context, "재료가 추가되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    context,
                    "추가 실패: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    fun toggleChecked(itemId: String, currentChecked: Boolean) {
        db.collection("shopping_list")
            .document(itemId)
            .update("checked", !currentChecked)
    }

    fun deleteItem(id: String) {
        db.collection("shopping_list")
            .document(id)
            .delete()
    }

    fun addAutoRecommendedItems(context: Context) {
        val uid = auth.currentUser?.uid ?: return

        db.collection("purchase_history")
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { snapshot ->

                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, -30)
                val thirtyDaysAgo = Timestamp(calendar.time)

                val purchaseNames = snapshot.documents.mapNotNull { doc ->
                    val purchaseDate = doc.getTimestamp("purchaseDate")
                    val itemName = doc.getString("itemName")?.trim()

                    if (
                        purchaseDate != null &&
                        purchaseDate.seconds >= thirtyDaysAgo.seconds &&
                        !itemName.isNullOrBlank()
                    ) {
                        itemName
                    } else {
                        null
                    }
                }

                val frequentItems = purchaseNames
                    .groupingBy { it }
                    .eachCount()
                    .filter { it.value >= 3 }
                    .keys

                val currentItemNames = _shoppingItems.value
                    .map { it.name.trim() }
                    .toSet()

                val itemsToAdd = frequentItems.filter { it !in currentItemNames }

                itemsToAdd.forEach { itemName ->
                    val data = hashMapOf(
                        "userId" to uid,
                        "name" to itemName,
                        "checked" to false,
                        "source" to "auto"
                    )

                    db.collection("shopping_list")
                        .add(data)
                        .addOnSuccessListener {
                            Toast.makeText(
                                context,
                                "$itemName 자동 추천 추가됨",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                context,
                                "자동 추천 저장 실패: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    context,
                    "구매기록 불러오기 실패: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}
