package com.example.nurungji.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nurungji.data.InventoryItem
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
                val snapshot = db.collection("inventory")
                    .get()
                    .await()

                val items = snapshot.documents.mapNotNull { document ->
                    document.toObject(InventoryItem::class.java)
                }

                _inventoryItems.value = items
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }
}