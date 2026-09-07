package com.screentranslator.presentation.overlay.render

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.screentranslator.domain.model.TranslatedBlock

object LensTextRenderer {

    private val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }

    /**
     * Renders a translated text block directly over its original image coordinates on the Canvas.
     */
    fun renderBlock(
        canvas: Canvas,
        block: TranslatedBlock,
        baseOffsetX: Float,
        baseOffsetY: Float
    ) {
        val box = block.boundingBox
        if (box.width <= 0 || box.height <= 0) return

        // Compute local drawing coordinates relative to canvas
        val left = baseOffsetX + box.left
        val top = baseOffsetY + box.top
        val right = baseOffsetX + box.right
        val bottom = baseOffsetY + box.bottom

        val targetWidth = (right - left).coerceAtLeast(10f)
        val targetHeight = (bottom - top).coerceAtLeast(10f)

        // 1. Draw Background Mask Patch
        maskPaint.color = block.backgroundColor ?: 0xEE1E293B.toInt()
        val patchRect = RectF(
            left - 2f,
            top - 2f,
            right + 2f,
            bottom + 2f
        )
        val cornerRadius = 6f
        canvas.drawRoundRect(patchRect, cornerRadius, cornerRadius, maskPaint)

        // 2. Measure & Layout Translated Text
        val text = block.translatedText
        if (text.isBlank()) return

        textPaint.color = block.textColor

        // Dynamically compute optimal font size
        var fontSize = (targetHeight * 0.75f).coerceIn(11f, 72f)
        textPaint.textSize = fontSize

        var staticLayout = createStaticLayout(text, textPaint, targetWidth.toInt())

        // Shrink font size if text exceeds bounding box height
        var attempts = 0
        while (staticLayout.height > targetHeight && fontSize > 10f && attempts < 8) {
            fontSize *= 0.85f
            textPaint.textSize = fontSize
            staticLayout = createStaticLayout(text, textPaint, targetWidth.toInt())
            attempts++
        }

        // 3. Center and Draw Text inside Bounding Box
        val textVerticalOffset = top + ((targetHeight - staticLayout.height) / 2f).coerceAtLeast(0f)

        canvas.save()
        canvas.translate(left, textVerticalOffset)
        staticLayout.draw(canvas)
        canvas.restore()
    }

    private fun createStaticLayout(
        text: CharSequence,
        paint: TextPaint,
        width: Int
    ): StaticLayout {
        val safeWidth = width.coerceAtLeast(10)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(text, 0, text.length, paint, safeWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setIncludePad(false)
                .setMaxLines(4)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(
                text,
                paint,
                safeWidth,
                Layout.Alignment.ALIGN_CENTER,
                1.0f,
                0.0f,
                false
            )
        }
    }
}
