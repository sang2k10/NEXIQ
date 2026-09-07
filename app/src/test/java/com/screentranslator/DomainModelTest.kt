package com.screentranslator

import com.screentranslator.domain.model.BoundingBox
import com.screentranslator.domain.model.Language
import org.junit.Assert.*
import org.junit.Test

class DomainModelTest {

    @Test
    fun boundingBox_dimensions_calculatedCorrectly() {
        val box = BoundingBox(left = 10f, top = 20f, right = 110f, bottom = 80f)
        assertEquals(100f, box.width, 0.001f)
        assertEquals(60f, box.height, 0.001f)
        assertEquals(60f, box.centerX, 0.001f)
        assertEquals(50f, box.centerY, 0.001f)
    }

    @Test
    fun boundingBox_scaling_scalesCoordinatesCorrectly() {
        val box = BoundingBox(left = 10f, top = 20f, right = 50f, bottom = 40f)
        val scaled = box.scaled(2.0f, 3.0f)
        assertEquals(20f, scaled.left, 0.001f)
        assertEquals(60f, scaled.top, 0.001f)
        assertEquals(100f, scaled.right, 0.001f)
        assertEquals(120f, scaled.bottom, 0.001f)
    }

    @Test
    fun boundingBox_contains_detectsPointInsideAndOutside() {
        val box = BoundingBox(left = 0f, top = 0f, right = 100f, bottom = 100f)
        assertTrue(box.contains(50f, 50f))
        assertTrue(box.contains(0f, 0f))
        assertTrue(box.contains(100f, 100f))
        assertFalse(box.contains(101f, 50f))
        assertFalse(box.contains(-1f, 50f))
    }

    @Test
    fun language_lookup_findsPredefinedLanguages() {
        val vietnamese = Language.fromCode("vi")
        assertEquals(Language.VIETNAMESE, vietnamese)
        assertEquals("Vietnamese", vietnamese.displayName)

        val japanese = Language.fromCode("ja")
        assertEquals(Language.JAPANESE, japanese)
        assertEquals("Japanese", japanese.displayName)

        val auto = Language.fromCode("auto")
        assertEquals(Language.AUTO, auto)
        assertTrue(auto.isAutoDetect)
    }
}
