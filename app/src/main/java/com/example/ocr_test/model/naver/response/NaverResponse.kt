package com.example.ocr_test.model.naver.response

data class NaverResponse(
    val images: List<Image>,
    val requestId: String,
    val timestamp: Long,
    val version: String
)