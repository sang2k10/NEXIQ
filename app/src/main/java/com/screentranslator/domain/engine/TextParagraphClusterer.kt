package com.screentranslator.domain.engine

import com.screentranslator.domain.model.BoundingBox
import com.screentranslator.domain.model.OcrBlock
import java.util.UUID

/**
 * Intelligent paragraph and reading block clustering algorithm.
 *
 * Groups discrete OCR lines into cohesive paragraph blocks for natural sentence-level
 * translation, while keeping isolated UI elements (buttons, menu items, headers) distinct.
 */
object TextParagraphClusterer {

    // Regex matching terminal sentence punctuation across Latin, CJK, Arabic, and Indic scripts
    private val TERMINAL_PUNCTUATION_REGEX = Regex(
        """[.!?…]+["'”’\)\]]?$|[。！？…]+[」』”’\)\]]?$|[؟.]+["'”’\)\]]?$|[।?!]+["'”’\)\]]?$"""
    )

    // Regex matching end-of-line hyphenation (e.g. "extraor-")
    private val HYPHENATED_WORD_REGEX = Regex("""([a-zA-ZÀ-ÿ0-9])-+$""")

    /**
     * Clusters a list of OCR lines (already ordered in reading sequence) into paragraph blocks.
     */
    fun clusterLines(lines: List<OcrBlock>): List<OcrBlock> {
        val validLines = lines.filter { it.text.isNotBlank() && it.boundingBox.width > 0f && it.boundingBox.height > 0f }
        if (validLines.size <= 1) return validLines

        val clusteredBlocks = mutableListOf<OcrBlock>()
        var currentCluster = mutableListOf<OcrBlock>()

        for (line in validLines) {
            if (currentCluster.isEmpty()) {
                currentCluster.add(line)
            } else {
                val previousLine = currentCluster.last()
                if (shouldMergeIntoParagraph(previousLine, line)) {
                    currentCluster.add(line)
                } else {
                    clusteredBlocks.add(mergeClusterToBlock(currentCluster))
                    currentCluster = mutableListOf(line)
                }
            }
        }

        if (currentCluster.isNotEmpty()) {
            clusteredBlocks.add(mergeClusterToBlock(currentCluster))
        }

        return clusteredBlocks
    }

    /**
     * Determines whether [curr] line forms a continuous paragraph with [prev] line.
     */
    fun shouldMergeIntoParagraph(prev: OcrBlock, curr: OcrBlock): Boolean {
        val prevBox = prev.boundingBox
        val currBox = curr.boundingBox

        val prevH = prevBox.height
        val currH = currBox.height
        val avgH = (prevH + currH) / 2f
        if (avgH <= 0f) return false

        // 1. Line Height / Font Size Consistency
        val minH = minOf(prevH, currH)
        val maxH = maxOf(prevH, currH)
        val heightRatio = minH / maxH
        if (heightRatio < 0.55f) {
            // Distinct font styles (e.g. Header vs Body text)
            return false
        }

        // 2. Vertical Proximity & Ordering
        val verticalGap = currBox.top - prevBox.bottom

        // Excessive negative gap implies severe vertical overlap or inverted order (e.g. columns)
        if (verticalGap < -0.35f * avgH) {
            return false
        }

        // 3. Horizontal Alignment & Overlap
        val overlapLeft = maxOf(prevBox.left, currBox.left)
        val overlapRight = minOf(prevBox.right, currBox.right)
        val overlapWidth = (overlapRight - overlapLeft).coerceAtLeast(0f)
        val minWidth = minOf(prevBox.width, currBox.width)
        if (minWidth <= 0f) return false

        val overlapRatio = overlapWidth / minWidth
        if (overlapRatio < 0.25f) {
            // Disjoint columns or unrelated horizontal elements
            return false
        }

        // 4. Sentence Flow & Paragraph Break Analysis
        val prevEndsWithTerminal = endsWithTerminalPunctuation(prev.text)

        val maxAllowedGap = if (!prevEndsWithTerminal) {
            // Unfinished sentence: strong continuation affinity
            1.55f * avgH
        } else {
            // Sentence ended: within same paragraph only if standard line leading applies
            1.05f * avgH
        }

        return verticalGap <= maxAllowedGap
    }

    /**
     * Merges a group of lines belonging to the same paragraph into a single composite [OcrBlock].
     */
    fun mergeClusterToBlock(cluster: List<OcrBlock>): OcrBlock {
        if (cluster.size == 1) return cluster.first()

        var minLeft = Float.MAX_VALUE
        var minTop = Float.MAX_VALUE
        var maxRight = Float.MIN_VALUE
        var maxBottom = Float.MIN_VALUE
        var totalConfidence = 0f

        val mergedTextBuilder = StringBuilder()

        for (i in cluster.indices) {
            val block = cluster[i]
            val box = block.boundingBox

            minLeft = minOf(minLeft, box.left)
            minTop = minOf(minTop, box.top)
            maxRight = maxOf(maxRight, box.right)
            maxBottom = maxOf(maxBottom, box.bottom)
            totalConfidence += block.confidence

            if (i == 0) {
                mergedTextBuilder.append(block.text.trim())
            } else {
                val prevText = cluster[i - 1].text.trim()
                val currText = block.text.trim()
                appendJoinedLine(mergedTextBuilder, prevText, currText)
            }
        }

        val firstBlock = cluster.first()
        val compositeBoundingBox = BoundingBox(
            left = minLeft,
            top = minTop,
            right = maxRight,
            bottom = maxBottom
        )

        return OcrBlock(
            id = UUID.randomUUID().toString(),
            text = mergedTextBuilder.toString(),
            boundingBox = compositeBoundingBox,
            confidence = totalConfidence / cluster.size,
            orientationDegrees = firstBlock.orientationDegrees,
            detectedLanguageCode = cluster.firstOrNull { it.detectedLanguageCode != null }?.detectedLanguageCode
        )
    }

    /**
     * Joins [currText] onto [builder] which already contains [prevText], handling
     * hyphenation removal and language-specific word spacing (e.g. no spaces for CJK).
     */
    private fun appendJoinedLine(builder: StringBuilder, prevText: String, currText: String) {
        if (currText.isEmpty()) return

        // 1. Hyphenated line break (e.g. "inter-" + "national" -> "international")
        if (HYPHENATED_WORD_REGEX.containsMatchIn(prevText) && currText.first().isLetter()) {
            // Remove trailing hyphen from builder
            val hyphenIndex = builder.lastIndexOf('-')
            if (hyphenIndex >= 0) {
                builder.deleteCharAt(hyphenIndex)
            }
            builder.append(currText)
            return
        }

        val lastChar = prevText.lastOrNull()
        val firstChar = currText.firstOrNull()

        // 2. CJK text joining (Chinese/Japanese do not use spaces between continuous characters)
        if (lastChar != null && firstChar != null && isCjkNoSpaceBoundary(lastChar, firstChar)) {
            builder.append(currText)
            return
        }

        // 3. Default space-delimited joining (Latin, Vietnamese, Cyrillic, Arabic, etc.)
        if (builder.isNotEmpty() && !builder.endsWith(' ') && !currText.startsWith(' ')) {
            builder.append(' ')
        }
        builder.append(currText)
    }

    /**
     * Checks if the character transition represents a continuous CJK boundary without spaces.
     */
    private fun isCjkNoSpaceBoundary(c1: Char, c2: Char): Boolean {
        val isC1Cjk = isCjkCharacter(c1)
        val isC2Cjk = isCjkCharacter(c2)

        if (isC1Cjk && isC2Cjk) return true
        if (isC1Cjk && isCjkPunctuation(c2)) return true
        if (isCjkPunctuation(c1) && isC2Cjk) return true

        return false
    }

    /**
     * Returns true if the character is in CJK ideograph, Hiragana, Katakana, or Bopomofo ranges.
     */
    fun isCjkCharacter(c: Char): Boolean {
        val code = c.code
        return (code in 0x4E00..0x9FFF) ||   // CJK Unified Ideographs
                (code in 0x3400..0x4DBF) ||   // CJK Unified Ideographs Extension A
                (code in 0x3040..0x309F) ||   // Hiragana
                (code in 0x30A0..0x30FF) ||   // Katakana
                (code in 0x3100..0x312F) ||   // Bopomofo
                (code in 0xF900..0xFAFF)      // CJK Compatibility Ideographs
    }

    /**
     * Checks for common CJK punctuation marks that attach directly without whitespace.
     */
    private fun isCjkPunctuation(c: Char): Boolean {
        val code = c.code
        return (code in 0x3000..0x303F) ||   // CJK Symbols and Punctuation
                (code in 0xFF01..0xFF5E)      // Fullwidth ASCII variants
    }

    /**
     * Returns true if the given text ends with terminal sentence punctuation.
     */
    fun endsWithTerminalPunctuation(text: String): Boolean {
        return TERMINAL_PUNCTUATION_REGEX.containsMatchIn(text.trim())
    }
}
