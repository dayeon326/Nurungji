package com.example.nurungji.utils

import android.content.Context
import com.example.nurungji.data.InventoryItem
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Locale

suspend fun generateFoodStorageTipWithApi(
    context: Context,
    functions: FirebaseFunctions,
    inventoryItems: List<InventoryItem>
): String {
    val cacheKey = buildFoodStorageTipCacheKey(inventoryItems)
    val prefs = context.getSharedPreferences("food_storage_tip_cache", Context.MODE_PRIVATE)
    val cachedKey = prefs.getString("cache_key", "")
    val cachedTip = prefs.getString("tip", "").orEmpty()

    if (cachedKey == cacheKey && cachedTip.isNotBlank()) {
        return cachedTip
    }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val items = inventoryItems
        .take(20)
        .map {
            mapOf(
                "itemName" to it.itemName,
                "category" to it.category,
                "expireDate" to (it.expireDate?.toDate()?.let(dateFormat::format) ?: "")
            )
        }

    val result = functions
        .getHttpsCallable("generateInventoryTip")
        .call(hashMapOf("items" to items))
        .await()

    val map = result.data as? Map<*, *> ?: return ""
    val tip = (map["tip"] as? String).orEmpty().trim()

    if (tip.isNotBlank()) {
        prefs.edit()
            .putString("cache_key", cacheKey)
            .putString("tip", tip)
            .apply()
    }

    return tip
}

private fun buildFoodStorageTipCacheKey(inventoryItems: List<InventoryItem>): String {
    val today = LocalDate.now().toString()
    val inventoryKey = inventoryItems
        .sortedWith(compareBy<InventoryItem> { it.documentId }.thenBy { it.itemName })
        .joinToString("|") {
            listOf(
                it.documentId,
                it.itemName,
                it.category,
                it.expireDate?.seconds?.toString().orEmpty()
            ).joinToString(":")
        }

    return "$today::$inventoryKey"
}
