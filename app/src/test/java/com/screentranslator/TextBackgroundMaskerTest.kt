package com.screentranslator

import android.graphics.Bitmap
import android.graphics.Color
import com.screentranslator.domain.model.BoundingBox
import com.screentranslator.presentation.overlay.render.TextBackgroundMasker
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Test

class TextBackgroundMaskerTest {

    @Test
    fun analyzeColors_onDarkBackground_selectsWhiteText() {
        val bitmap = mockk<Bitmap>()
        every { bitmap.isRecycled } returns false
        every { bitmap.width } returns 200
        every { bitmap.height } returns 200
        // Return dark pixels (e.g. RGB 20, 20, 20) -> 0xFF141414
        every { bitmap.getPixel(any(), any()) } returns 0xFF141414.toInt()

        val box = BoundingBox(10f, 10f, 100f, 50f)
        val analysis = TextBackgroundMasker.analyzeColors(bitmap, box)

        // Text color should be light / white for dark background
        assertEquals(0xFFF8FAFC.toInt(), analysis.textColor)
    }

    @Test
    fun analyzeColors_onLightBackground_selectsDarkText() {
        val bitmap = mockk<Bitmap>()
        every { bitmap.isRecycled } returns false
        every { bitmap.width } returns 200
        every { bitmap.height } returns 200
        // Return white pixels (e.g. RGB 240, 240, 240) -> 0xFFF0F0F0
        every { bitmap.getPixel(any(), any()) } returns 0xFFF0F0F0.toInt()

        val box = BoundingBox(10f, 10f, 100f, 50f)
        val analysis = TextBackgroundMasker.analyzeColors(bitmap, box)

        // Text color should be dark for light background
        assertEquals(0xFF0F172A.toInt(), analysis.textColor)
    }

    @Test
    fun analyzeColors_whenBitmapIsRecycled_returnsSafeDefaults() {
        val bitmap = mockk<Bitmap>()
        every { bitmap.isRecycled } returns true

        val box = BoundingBox(10f, 10f, 100f, 50f)
        val analysis = TextBackgroundMasker.analyzeColors(bitmap, box)

        assertNotNull(analysis)
        assertEquals(0xFFFFFFFF.toInt(), analysis.textColor)
    }
}
