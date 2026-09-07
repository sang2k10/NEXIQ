package com.screentranslator.domain.model

import android.graphics.Rect
import android.graphics.RectF

/**
 * Immutable bounding box in pixel coordinates.
 */
data class BoundingBox(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val width: Float get() = (right - left).coerceAtLeast(0f)
    val height: Float get() = (bottom - top).coerceAtLeast(0f)
    val centerX: Float get() = (left + right) / 2f
    val centerY: Float get() = (top + bottom) / 2f

    fun toRect(): Rect = Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
    fun toRectF(): RectF = RectF(left, top, right, bottom)

    fun contains(x: Float, y: Float): Boolean =
        x in left..right && y in top..bottom

    fun scaled(scaleX: Float, scaleY: Float): BoundingBox =
        BoundingBox(left * scaleX, top * scaleY, right * scaleX, bottom * scaleY)

    fun translated(dx: Float, dy: Float): BoundingBox =
        BoundingBox(left + dx, top + dy, right + dx, bottom + dy)

    companion object {
        val ZERO = BoundingBox(0f, 0f, 0f, 0f)

        fun fromRect(rect: Rect): BoundingBox =
            BoundingBox(rect.left.toFloat(), rect.top.toFloat(), rect.right.toFloat(), rect.bottom.toFloat())

        fun fromRectF(rectF: RectF): BoundingBox =
            BoundingBox(rectF.left, rectF.top, rectF.right, rectF.bottom)
    }
}
