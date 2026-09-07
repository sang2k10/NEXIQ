package com.screentranslator.domain.model

import java.util.UUID

/**
 * Represents a discrete recognized text block from the OCR engine.
 */
data class OcrBlock(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val boundingBox: BoundingBox,
    val confidence: Float = 1.0f,
    val orientationDegrees: Float = 0f,
    val detectedLanguageCode: String? = null
)
