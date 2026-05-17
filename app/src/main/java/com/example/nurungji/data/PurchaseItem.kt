package com.example.nurungji.data

import com.google.firebase.Timestamp

data class PurchaseItem(
    val id: String = "",
    val userId: String = "",
    val itemName: String = "",
    val category: String = "",
    val quantity: Long = 0,
    val purchaseDate: Timestamp? = null
)