package com.screentranslator.domain.engine

import com.screentranslator.domain.model.BoundingBox
import com.screentranslator.domain.model.OcrBlock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TextParagraphClustererTest {

    @Test
    fun `empty list returns empty list`() {
        val result = TextParagraphClusterer.clusterLines(emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `single line returns unchanged`() {
        val single = OcrBlock(
            text = "Welcome to the App",
            boundingBox = BoundingBox(50f, 100f, 250f, 130f)
        )
        val result = TextParagraphClusterer.clusterLines(listOf(single))
        assertEquals(1, result.size)
        assertEquals("Welcome to the App", result[0].text)
        assertEquals(single.boundingBox, result[0].boundingBox)
    }

    @Test
    fun `multiline English paragraph merges into single block with spaces`() {
        val line1 = OcrBlock(
            text = "Machine learning models are trained on large",
            boundingBox = BoundingBox(50f, 100f, 450f, 124f) // height 24, gap to next = 8
        )
        val line2 = OcrBlock(
            text = "datasets to recognize patterns and make accurate",
            boundingBox = BoundingBox(50f, 132f, 460f, 156f) // height 24, gap to next = 8
        )
        val line3 = OcrBlock(
            text = "predictions on unseen real-world data.",
            boundingBox = BoundingBox(50f, 164f, 340f, 188f) // height 24
        )

        val result = TextParagraphClusterer.clusterLines(listOf(line1, line2, line3))

        assertEquals(1, result.size)
        val merged = result[0]
        assertEquals(
            "Machine learning models are trained on large datasets to recognize patterns and make accurate predictions on unseen real-world data.",
            merged.text
        )
        assertEquals(50f, merged.boundingBox.left, 0.01f)
        assertEquals(100f, merged.boundingBox.top, 0.01f)
        assertEquals(460f, merged.boundingBox.right, 0.01f)
        assertEquals(188f, merged.boundingBox.bottom, 0.01f)
    }

    @Test
    fun `hyphenated line breaks are cleanly unwrapped without spaces`() {
        val line1 = OcrBlock(
            text = "This is a revolutionary trans-",
            boundingBox = BoundingBox(50f, 100f, 400f, 124f)
        )
        val line2 = OcrBlock(
            text = "formation in on-device AI.",
            boundingBox = BoundingBox(50f, 132f, 350f, 156f)
        )

        val result = TextParagraphClusterer.clusterLines(listOf(line1, line2))

        assertEquals(1, result.size)
        assertEquals("This is a revolutionary transformation in on-device AI.", result[0].text)
    }

    @Test
    fun `continuous CJK text is joined without spaces`() {
        val line1 = OcrBlock(
            text = "人工知能の急速な発展により",
            boundingBox = BoundingBox(40f, 100f, 420f, 128f),
            detectedLanguageCode = "ja"
        )
        val line2 = OcrBlock(
            text = "多くの日常業務が自動化されています。",
            boundingBox = BoundingBox(40f, 136f, 430f, 164f),
            detectedLanguageCode = "ja"
        )

        val result = TextParagraphClusterer.clusterLines(listOf(line1, line2))

        assertEquals(1, result.size)
        assertEquals("人工知能の急速な発展により多くの日常業務が自動化されています。", result[0].text)
    }

    @Test
    fun `isolated buttons separated by large gap are not merged`() {
        val button1 = OcrBlock(
            text = "Confirm",
            boundingBox = BoundingBox(60f, 100f, 180f, 124f) // height 24
        )
        val button2 = OcrBlock(
            text = "Cancel",
            boundingBox = BoundingBox(60f, 180f, 180f, 204f) // gap = 56 (2.3x height)
        )

        val result = TextParagraphClusterer.clusterLines(listOf(button1, button2))

        assertEquals(2, result.size)
        assertEquals("Confirm", result[0].text)
        assertEquals("Cancel", result[1].text)
    }

    @Test
    fun `header and body with mismatched font size are not merged`() {
        val header = OcrBlock(
            text = "CHAPTER ONE",
            boundingBox = BoundingBox(50f, 100f, 300f, 160f) // height 60
        )
        val bodyLine = OcrBlock(
            text = "The morning sun was shining bright.",
            boundingBox = BoundingBox(50f, 172f, 380f, 192f) // height 20 (heightRatio = 20/60 = 0.33)
        )

        val result = TextParagraphClusterer.clusterLines(listOf(header, bodyLine))

        assertEquals(2, result.size)
        assertEquals("CHAPTER ONE", result[0].text)
        assertEquals("The morning sun was shining bright.", result[1].text)
    }

    @Test
    fun `two separate paragraphs with blank line are kept separate`() {
        val p1Line1 = OcrBlock(
            text = "The quick brown fox jumps over the lazy dog.",
            boundingBox = BoundingBox(50f, 100f, 450f, 120f) // height 20
        )
        // Gap of 35px after sentence-ending period (1.75x height)
        val p2Line1 = OcrBlock(
            text = "A second paragraph begins right after the space.",
            boundingBox = BoundingBox(50f, 155f, 460f, 175f) // height 20
        )

        val result = TextParagraphClusterer.clusterLines(listOf(p1Line1, p2Line1))

        assertEquals(2, result.size)
        assertEquals("The quick brown fox jumps over the lazy dog.", result[0].text)
        assertEquals("A second paragraph begins right after the space.", result[1].text)
    }

    @Test
    fun `disjoint horizontal columns are not merged`() {
        val leftColumn = OcrBlock(
            text = "Left column content",
            boundingBox = BoundingBox(20f, 100f, 180f, 120f)
        )
        val rightColumn = OcrBlock(
            text = "Right column content",
            boundingBox = BoundingBox(260f, 125f, 420f, 145f)
        )

        val result = TextParagraphClusterer.clusterLines(listOf(leftColumn, rightColumn))

        assertEquals(2, result.size)
        assertEquals("Left column content", result[0].text)
        assertEquals("Right column content", result[1].text)
    }
}
