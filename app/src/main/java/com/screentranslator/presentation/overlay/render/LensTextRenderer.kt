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
            left - 3f,
            top - 2f,
            right + 3f,
            bottom + 2f
        )
        val cornerRadius = 6f
        canvas.drawRoundRect(patchRect, cornerRadius, cornerRadius, maskPaint)

        // 2. Measure & Layout Translated Text
        val text = block.translatedText
        if (text.isBlank()) return

        textPaint.color = block.textColor

        // Determine if text is a reading paragraph vs short UI label
        val isReadingBlock = text.length > 35 || targetHeight > 45f
        val alignment = if (isReadingBlock) Layout.Alignment.ALIGN_NORMAL else Layout.Alignment.ALIGN_CENTER

        val padX = if (isReadingBlock) 4f else 2f
        val padY = 2f
        val availableWidth = (targetWidth - padX * 2).coerceAtLeast(10f).toInt()
        val availableHeight = (targetHeight - padY * 2).coerceAtLeast(10f)

        // Dynamically find optimal font size using fast binary search
        val optimalFontSize = findOptimalFontSize(text, availableWidth, availableHeight, isReadingBlock)
        textPaint.textSize = optimalFontSize

        val staticLayout = createStaticLayout(text, textPaint, availableWidth, alignment)

        // 3. Position and Draw Text inside Bounding Box
        val textVerticalOffset = top + padY + ((availableHeight - staticLayout.height) / 2f).coerceAtLeast(0f)
        val textHorizontalOffset = left + padX

        canvas.save()
        canvas.translate(textHorizontalOffset, textVerticalOffset)
        staticLayout.draw(canvas)
        canvas.restore()
    }

    private fun findOptimalFontSize(
        text: CharSequence,
        width: Int,
        height: Float,
        isReadingBlock: Boolean
    ): Float {
        val minSize = 9f
        val maxSize = if (isReadingBlock) {
            (height * 0.7f).coerceIn(12f, 32f)
        } else {
            (height * 0.75f).coerceIn(11f, 64f)
        }

        var low = minSize
        var high = maxSize
        var best = minSize

        // Binary search fits within ~5 iterations
        for (i in 0 until 5) {
            val mid = (low + high) / 2f
            textPaint.textSize = mid
            val layout = createStaticLayout(text, textPaint, width, Layout.Alignment.ALIGN_NORMAL)
            if (layout.height <= height) {
                best = mid
                low = mid + 0.5f
            } else {
                high = mid - 0.5f
            }
        }

        return best
    }

    private fun createStaticLayout(
        text: CharSequence,
        paint: TextPaint,
        width: Int,
        alignment: Layout.Alignment
    ): StaticLayout {
        val safeWidth = width.coerceAtLeast(10)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(text, 0, text.length, paint, safeWidth)
                .setAlignment(alignment)
                .setIncludePad(false)
                .setMaxLines(100)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(
                text,
                paint,
                safeWidth,
                alignment,
                1.0f,
                0.0f,
                false
            )
        }
    }
}
