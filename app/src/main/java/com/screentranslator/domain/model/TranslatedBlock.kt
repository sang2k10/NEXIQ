package com.screentranslator.domain.model

import java.util.UUID

/**
 * Represents a translated block mapped to the original image coordinate space.
 */
data class TranslatedBlock(
    val id: String = UUID.randomUUID().toString(),
    val originalText: String,
    val translatedText: String,
    val boundingBox: BoundingBox,
    val sourceLanguage: Language,
    val targetLanguage: Language,
    val orientationDegrees: Float = 0f,
    val backgroundColor: Int? = null,
    val textColor: Int = 0xFFFFFFFF.toInt()
)
