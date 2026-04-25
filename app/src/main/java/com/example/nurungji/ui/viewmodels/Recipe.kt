package com.example.nurungji.models

data class Recipe(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val authorId: String = "",

    val time: String = "",
    val ingredients: List<String> = emptyList(),
    val hashtags: List<String> = emptyList(),

    val imageRes: Int = android.R.drawable.ic_menu_gallery,

    val recommendUids: List<String> = emptyList(),
    val scrapUids: List<String> = emptyList()
)