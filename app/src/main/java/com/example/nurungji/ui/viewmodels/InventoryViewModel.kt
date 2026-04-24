package com.example.nurungji.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nurungji.data.InventoryItem
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class InventoryViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _inventoryItems = MutableStateFlow<List<InventoryItem>>(emptyList())
    val inventoryItems: StateFlow<List<InventoryItem>> = _inventoryItems.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadInventory() {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    _errorMessage.value = "로그인이 필요합니다."
                    return@launch
                }

                val snapshot = db.collection("inventory")
                    .whereEqualTo("userId", currentUser.uid)
                    .get()
                    .await()

                val items = snapshot.documents.map { doc ->
                    InventoryItem(
                        documentId = doc.id,
                        userId = doc.getString("userId") ?: "",
                        itemName = doc.getString("itemName") ?: "",
                        category = doc.getString("category") ?: "",
                        quantity = doc.getLong("quantity") ?: 0,
                        expireDate = doc.getTimestamp("expireDate")
                    )
                }

                _inventoryItems.value = items
                _errorMessage.value = null

            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun addInventory(
        itemName: String,
        category: String,
        quantity: Long,
        expireDate: Timestamp?
    ) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    _errorMessage.value = "로그인이 필요합니다."
                    return@launch
                }

                val item = hashMapOf(
                    "userId" to currentUser.uid,
                    "itemName" to itemName,
                    "category" to category,
                    "quantity" to quantity,
                    "expireDate" to expireDate
                )

                db.collection("inventory")
                    .add(item)
                    .await()

                loadInventory()
                _errorMessage.value = null

            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun deleteInventory(documentId: String) {
        viewModelScope.launch {
            try {
                db.collection("inventory")
                    .document(documentId)
                    .delete()
                    .await()

                loadInventory()
                _errorMessage.value = null

            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }
}