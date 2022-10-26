package com.example.ocr_test.model.naver.request

data class NaverMulti(
    val images: List<Image>,
    val requestId: String,
    val version: String,
    val timestamp: Int
)