package com.example.nurungji.utils

fun recipePreviewText(content: String): String {
    return content
        .replace(Regex("\\[\\[image:\\d+]]"), "")
        .replace(Regex("\\n{3,}"), "\n\n")
        .trim()
}
