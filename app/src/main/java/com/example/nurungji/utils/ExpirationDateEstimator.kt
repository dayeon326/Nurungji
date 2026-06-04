package com.example.nurungji.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun estimateExpirationDateText(itemName: String, category: String): String {
    val days = estimateExpirationDays(itemName, category)
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, days)
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
}

private fun estimateExpirationDays(itemName: String, category: String): Int {
    val normalized = itemName
        .lowercase()
        .replace(" ", "")
        .replace("-", "")
        .replace("_", "")

    val itemSpecificDays = listOf(
        2 to listOf("딸기", "상추", "깻잎", "샐러드", "숙주", "콩나물", "생선", "회"),
        3 to listOf("두부", "버섯", "시금치", "브로콜리", "닭", "닭고기", "닭가슴살", "돼지고기", "소고기"),
        5 to listOf("우유", "요거트", "요구르트", "요플레", "오이", "호박", "토마토", "포도", "복숭아"),
        7 to listOf("계란", "달걀", "치즈", "양파", "대파", "파프리카", "바나나", "귤", "오렌지"),
        14 to listOf("사과", "배", "감자", "고구마", "당근", "무", "양배추"),
        30 to listOf("버터", "햄", "소시지", "소세지", "베이컨"),
        180 to listOf("냉동")
    )

    itemSpecificDays.forEach { (days, keywords) ->
        if (keywords.any { normalized.contains(it) }) return days
    }

    return when (category) {
        "육류" -> 3
        "해산물" -> 2
        "유제품" -> 7
        "채소" -> 5
        "과일" -> 7
        "음료" -> 30
        "냉동식품" -> 180
        "밀키트" -> 3
        "간편식" -> 5
        "가공식품" -> 14
        "곡류" -> 180
        "조미료" -> 180
        else -> 14
    }
}
