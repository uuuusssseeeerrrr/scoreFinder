package com.score.ocr

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class OcrResult(
    val text: String,
    val confidence: Float,
    @Transient val bbox: List<List<Double>> = emptyList()
)
