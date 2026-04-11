package com.example.nurungji.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nurungji.data.InventoryItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class InventoryViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _inventoryItems = MutableStateFlow<List<InventoryItem>>(emptyList())
    val inventoryItems: StateFlow<List<InventoryItem>> = _inventoryItems

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun loadInventory() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid

                if (uid == null) {
                    _errorMessage.value = "로그인한 사용자 정보가 없습니다."
                    _inventoryItems.value = emptyList()
                    return@launch
                }

                val snapshot = db.collection("inventory")
                    .whereEqualTo("userId", uid)
                    .get()
                    .await()

                val items = snapshot.documents.mapNotNull { document ->
                    document.toObject(InventoryItem::class.java)
                }

                _inventoryItems.value = items
                _errorMessage.value = null

            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun addInventory(itemName: String, category: String, quantity: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid

                if (uid == null) {
                    _errorMessage.value = "로그인한 사용자 정보가 없습니다."
                    return@launch
                }

                val item = InventoryItem(
                    itemName = itemName,
                    category = category,
                    quantity = quantity,
                    userId = uid
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
}