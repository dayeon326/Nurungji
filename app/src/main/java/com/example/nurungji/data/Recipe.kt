package com.example.nurungji.data

data class Recipe(
    val name: String,
    val time: String,
    val availableIngredients: List<String>,
    val missingIngredients: List<String>,
    val imageRes: Int
)