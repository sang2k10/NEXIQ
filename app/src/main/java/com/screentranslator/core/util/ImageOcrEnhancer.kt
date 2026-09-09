package com.screentranslator.core.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint

/**
 * Image enhancement utilities designed to maximize text recognition rates
 * on mobile screens, social media video frames (Douyin/TikTok), and cropped sub-regions.
 */
object ImageOcrEnhancer {

    /**
     * Creates a contrast-boosted ARGB_8888 bitmap to improve OCR recognition on low-contrast
     * screens, subtle video subtitles, and noisy video backgrounds.
     */
    fun enhanceContrast(source: Bitmap): Bitmap? {
        if (source.isRecycled) return null
        return try {
            val width = source.width
            val height = source.height
            val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

            // Boost contrast by 1.35x and adjust brightness to separate character strokes
            val contrast = 1.35f
            val brightness = -15f
            val cm = ColorMatrix(
                floatArrayOf(
                    contrast, 0f, 0f, 0f, brightness,
                    0f, contrast, 0f, 0f, brightness,
                    0f, 0f, contrast, 0f, brightness,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            paint.colorFilter = ColorMatrixColorFilter(cm)
            canvas.drawBitmap(source, 0f, 0f, paint)
            output
        } catch (_: Throwable) {
            null
        }
    }

    /**
     * Scales up small bitmaps (e.g. crop regions) so character glyphs meet or exceed
     * ML Kit's minimum recognition size (24x24px for CJK, 16x16px for Latin).
     */
    fun upscaleIfNeeded(source: Bitmap, minDimension: Int = 400): Pair<Bitmap, Float> {
        if (source.isRecycled) return Pair(source, 1.0f)
        val minSide = minOf(source.width, source.height)
        if (minSide >= minDimension) {
            return Pair(source, 1.0f)
        }
        return try {
            val scale = (minDimension.toFloat() / minSide.toFloat()).coerceIn(1.5f, 2.5f)
            val targetW = (source.width * scale).toInt()
            val targetH = (source.height * scale).toInt()
            val scaled = Bitmap.createScaledBitmap(source, targetW, targetH, true)
            Pair(scaled, scale)
        } catch (_: Throwable) {
            Pair(source, 1.0f)
        }
    }
}
