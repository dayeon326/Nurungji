package com.example.nurungji.data

import com.google.firebase.Timestamp

data class InventoryItem(
    val userId: String = "",
    val itemName: String = "",
    val category: String = "",
    val quantity: Long = 0,
    val expireDate: Timestamp? = null
)