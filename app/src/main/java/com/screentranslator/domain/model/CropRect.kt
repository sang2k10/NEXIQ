package com.screentranslator.domain.model

/**
 * Normalized or pixel-based crop selection rectangle.
 */
data class CropRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val width: Float get() = (right - left).coerceAtLeast(0f)
    val height: Float get() = (bottom - top).coerceAtLeast(0f)

    fun toBoundingBox(): BoundingBox = BoundingBox(left, top, right, bottom)
}
