package com.example.nurungji.models // 폴더 경로에 맞춰 수정하세요

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Recipe(
    @DocumentId val id: String = "",           // 문서 ID
    val title: String = "",        // 제목
    val content: String = "",      // 내용
    val authorId: String? = null,  // 작성자 UID
    val createdAt: Timestamp? = null, // 작성 시간
    val recommendUids: List<String> = emptyList(), // 추천한 사람들
    val scrapUids: List<String> = emptyList() // 스크랩
)