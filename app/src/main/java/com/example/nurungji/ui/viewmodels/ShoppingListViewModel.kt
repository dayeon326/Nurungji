package com.example.nurungji.ui.viewmodels

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ShoppingListViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _shoppingItems = MutableStateFlow<List<ShoppingItem>>(emptyList())
    val shoppingItems: StateFlow<List<ShoppingItem>> = _shoppingItems.asStateFlow()

    fun loadShoppingItems() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("shopping_list")
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val items = snapshot.documents.map { doc ->
                        ShoppingItem(
                            id = doc.id,
                            userId = doc.getString("userId") ?: "",
                            name = doc.getString("name") ?: "",
                            checked = doc.getBoolean("checked") ?: false
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
            "checked" to false
        )

        db.collection("shopping_list")
            .add(data)
            .addOnSuccessListener {
                Toast.makeText(context, "재료가 추가되었습니다.", Toast.LENGTH_SHORT).show()
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
}