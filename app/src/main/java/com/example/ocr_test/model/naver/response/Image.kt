package com.example.ocr_test.model.naver.response

data class Image(
    val convertedImageInfo: ConvertedImageInfo,
    val fields: List<Field>,
    val inferResult: String,
    val message: String,
    val name: String,
    val uid: String,
    val validationResult: ValidationResult
)