package com.screentranslator.presentation.overlay.render

import android.graphics.Bitmap
import android.graphics.Canvas
import com.screentranslator.domain.model.TranslatedBlock

/**
 * Composites translated text blocks and background masks directly onto
 * a copy of the original screen capture bitmap for saving to gallery.
 */
object TranslatedImageComposer {

    fun compose(
        originalBitmap: Bitmap,
        translatedBlocks: List<TranslatedBlock>
    ): Bitmap {
        val compositeBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(compositeBitmap)

        for (block in translatedBlocks) {
            LensTextRenderer.renderBlock(
                canvas = canvas,
                block = block,
                baseOffsetX = 0f,
                baseOffsetY = 0f
            )
        }

        return compositeBitmap
    }
}
