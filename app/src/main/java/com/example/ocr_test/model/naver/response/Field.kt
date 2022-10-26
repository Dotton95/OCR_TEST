package com.example.ocr_test.model.naver.response

data class Field(
    val boundingPoly: BoundingPoly,
    val inferConfidence: Double,
    val inferText: String,
    val lineBreak: Boolean,
    val type: String,
    val valueType: String
)