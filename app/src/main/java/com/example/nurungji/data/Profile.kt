package com.example.nurungji.data

// 프로필 화면에 들어갈 데이터 모음
data class UserProfile(
    val name: String,               // 이름 (예: 김민수)
    val email: String,              // 이메일
    val registeredFoodCount: Int,   // 등록한 식품 수
    val shoppingListCount: Int,     // 장보기 리스트 수
    val expiringFoodCount: Int      // 곧 만료되는 식품 수
)
