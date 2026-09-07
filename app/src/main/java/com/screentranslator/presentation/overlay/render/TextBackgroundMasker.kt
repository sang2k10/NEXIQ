package com.screentranslator.presentation.overlay.render

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.ColorUtils
import com.screentranslator.domain.model.BoundingBox

data class ColorAnalysis(
    val backgroundColor: Int,
    val textColor: Int
)

object TextBackgroundMasker {

    /**
     * Extracts dominant background color by sampling pixels along the perimeter of the bounding box.
     * Selects an optimal contrasting text color.
     */
    fun analyzeColors(bitmap: Bitmap, box: BoundingBox): ColorAnalysis {
        if (bitmap.isRecycled) {
            return ColorAnalysis(
                backgroundColor = 0xEE1E293B.toInt(),
                textColor = 0xFFFFFFFF.toInt()
            )
        }

        val bWidth = bitmap.width
        val bHeight = bitmap.height

        val left = box.left.toInt().coerceIn(0, bWidth - 1)
        val top = box.top.toInt().coerceIn(0, bHeight - 1)
        val right = box.right.toInt().coerceIn(left, bWidth - 1)
        val bottom = box.bottom.toInt().coerceIn(top, bHeight - 1)

        val width = right - left
        val height = bottom - top

        if (width <= 2 || height <= 2) {
            return ColorAnalysis(
                backgroundColor = 0xEE1E293B.toInt(),
                textColor = 0xFFFFFFFF.toInt()
            )
        }

        var sumR = 0L
        var sumG = 0L
        var sumB = 0L
        var count = 0

        // Sample top and bottom edge pixels
        for (x in left..right step (width / 6).coerceAtLeast(1)) {
            val pixelTop = bitmap.getPixel(x, top)
            sumR += (pixelTop ushr 16) and 0xFF
            sumG += (pixelTop ushr 8) and 0xFF
            sumB += pixelTop and 0xFF
            count++

            val pixelBottom = bitmap.getPixel(x, bottom)
            sumR += (pixelBottom ushr 16) and 0xFF
            sumG += (pixelBottom ushr 8) and 0xFF
            sumB += pixelBottom and 0xFF
            count++
        }

        // Sample left and right edge pixels
        for (y in top..bottom step (height / 6).coerceAtLeast(1)) {
            val pixelLeft = bitmap.getPixel(left, y)
            sumR += (pixelLeft ushr 16) and 0xFF
            sumG += (pixelLeft ushr 8) and 0xFF
            sumB += pixelLeft and 0xFF
            count++

            val pixelRight = bitmap.getPixel(right, y)
            sumR += (pixelRight ushr 16) and 0xFF
            sumG += (pixelRight ushr 8) and 0xFF
            sumB += pixelRight and 0xFF
            count++
        }

        val avgR = (sumR / count.coerceAtLeast(1)).toInt().coerceIn(0, 255)
        val avgG = (sumG / count.coerceAtLeast(1)).toInt().coerceIn(0, 255)
        val avgB = (sumB / count.coerceAtLeast(1)).toInt().coerceIn(0, 255)

        // Make background mask slightly opaque (alpha = 245 / 0xF5) to cover underlying text solidly
        val finalBg = (0xF5 shl 24) or (avgR shl 16) or (avgG shl 8) or avgB

        // Compute relative luminance (WCAG formula) to select dark or light text for best contrast
        val luminance = (0.299 * avgR + 0.587 * avgG + 0.114 * avgB) / 255.0
        val finalTextColor = if (luminance > 0.5) {
            0xFF0F172A.toInt() // Dark text
        } else {
            0xFFF8FAFC.toInt() // Light text
        }

        return ColorAnalysis(
            backgroundColor = finalBg,
            textColor = finalTextColor
        )
    }
}
