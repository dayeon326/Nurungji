package com.example.nurungji.utils

import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

suspend fun classifyFoodCategoriesWithApi(
    functions: FirebaseFunctions,
    itemNames: List<String>
): Map<String, String> {
    val names = itemNames
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()

    if (names.isEmpty()) return emptyMap()

    val data = hashMapOf("names" to names)
    val result = functions
        .getHttpsCallable("classifyFoodItems")
        .call(data)
        .await()

    val map = result.data as? Map<*, *> ?: return emptyMap()
    val rawItems = map["items"] as? List<*> ?: return emptyMap()

    return rawItems.mapNotNull { raw ->
        val item = raw as? Map<*, *> ?: return@mapNotNull null
        val name = item["itemName"] as? String ?: return@mapNotNull null
        val category = item["category"] as? String ?: return@mapNotNull null
        if (category in inventoryCategories) name to category else null
    }.toMap()
}
