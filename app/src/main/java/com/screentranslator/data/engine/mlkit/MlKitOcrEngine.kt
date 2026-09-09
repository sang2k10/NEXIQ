package com.screentranslator.data.engine.mlkit

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.screentranslator.core.util.ImageOcrEnhancer
import com.screentranslator.domain.engine.OcrEngine
import com.screentranslator.domain.engine.TextParagraphClusterer
import com.screentranslator.domain.model.BoundingBox
import com.screentranslator.domain.model.Language
import com.screentranslator.domain.model.OcrBlock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MlKitOcrEngine : OcrEngine {

    override val id: String = "mlkit"
    override val displayName: String = "Google ML Kit (Multi-Script On-Device)"

    // Script-specific recognizers
    private val latinRecognizer: TextRecognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    private val japaneseRecognizer: TextRecognizer by lazy {
        TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
    }

    private val chineseRecognizer: TextRecognizer by lazy {
        TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
    }

    private val koreanRecognizer: TextRecognizer by lazy {
        TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
    }

    private val devanagariRecognizer: TextRecognizer by lazy {
        TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())
    }

    override suspend fun recognize(
        bitmap: Bitmap,
        preferredLanguage: Language?
    ): Result<List<OcrBlock>> = withContext(Dispatchers.Default) {
        try {
            if (bitmap.isRecycled) {
                return@withContext Result.failure(IllegalStateException("Bitmap is already recycled"))
            }

            // 1. Auto-upscale small regions/crops so character glyphs exceed ML Kit's 24x24px threshold
            val (scaledBitmap, scaleFactor) = ImageOcrEnhancer.upscaleIfNeeded(bitmap, minDimension = 400)
            val isScaled = scaledBitmap != bitmap

            try {
                // Pass 1: Primary recognition pass
                var blocks = executeRecognitionPass(scaledBitmap, preferredLanguage, scaleFactor)

                // Pass 2: If primary pass detected 0 blocks, run contrast-enhanced pass (rescues video subtitles)
                if (blocks.isEmpty()) {
                    val contrastBitmap = ImageOcrEnhancer.enhanceContrast(scaledBitmap)
                    if (contrastBitmap != null) {
                        try {
                            blocks = executeRecognitionPass(contrastBitmap, preferredLanguage, scaleFactor)
                        } finally {
                            if (!contrastBitmap.isRecycled) {
                                contrastBitmap.recycle()
                            }
                        }
                    }
                }

                Result.success(blocks)
            } finally {
                if (isScaled && !scaledBitmap.isRecycled) {
                    scaledBitmap.recycle()
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun executeRecognitionPass(
        targetBitmap: Bitmap,
        preferredLanguage: Language?,
        scaleFactor: Float
    ): List<OcrBlock> = coroutineScope {
        val inputImage = InputImage.fromBitmap(targetBitmap, 0)

        if (preferredLanguage == null || preferredLanguage.isAutoDetect) {
            // Concurrently run Chinese and Latin recognizers.
            // ML Kit Chinese recognizer accurately detects both Hanzi and Latin/digits.
            val chnDeferred = async {
                try {
                    val text = processImage(chineseRecognizer, inputImage)
                    convertToDomainBlocks(text, "zh", scaleFactor)
                } catch (_: Exception) {
                    emptyList()
                }
            }
            val latinDeferred = async {
                try {
                    val text = processImage(latinRecognizer, inputImage)
                    convertToDomainBlocks(text, "en", scaleFactor)
                } catch (_: Exception) {
                    emptyList()
                }
            }

            val chnBlocks = chnDeferred.await()
            val latinBlocks = latinDeferred.await()

            // Count authentic CJK ideographs
            val cjkCharCount = chnBlocks.sumOf { block: OcrBlock ->
                block.text.count { c: Char ->
                    c in '\u4E00'..'\u9FFF' || c in '\u3040'..'\u30FF' || c in '\uAC00'..'\uD7AF'
                }
            }

            if (cjkCharCount > 0) {
                return@coroutineScope chnBlocks
            }

            // If no CJK found, prefer the recognizer that detected more substantial content
            val bestBlocks = if (chnBlocks.size > latinBlocks.size && chnBlocks.any { it.text.length > 1 }) {
                chnBlocks
            } else {
                latinBlocks
            }

            if (bestBlocks.isNotEmpty()) {
                return@coroutineScope bestBlocks
            }

            // Fallback: Check Japanese specific kana if both returned empty
            val jpnText = try { processImage(japaneseRecognizer, inputImage) } catch (_: Exception) { null }
            if (jpnText != null) {
                val jpnBlocks = convertToDomainBlocks(jpnText, "ja", scaleFactor)
                if (jpnBlocks.isNotEmpty()) {
                    return@coroutineScope jpnBlocks
                }
            }

            emptyList()
        } else {
            // Explicit language requested
            val recognizer = selectRecognizer(preferredLanguage)
            val visionText = processImage(recognizer, inputImage)
            convertToDomainBlocks(visionText, preferredLanguage.code, scaleFactor)
        }
    }

    override suspend fun isLanguageSupported(language: Language): Boolean {
        return when (language.code.lowercase()) {
            "auto", "en", "vi", "fr", "de", "es", "it", "pt", "ru",
            "ja", "zh", "ko", "hi", "mr" -> true
            else -> false
        }
    }

    private fun selectRecognizer(preferredLanguage: Language?): TextRecognizer {
        if (preferredLanguage == null || preferredLanguage.isAutoDetect) {
            return latinRecognizer
        }
        return when (preferredLanguage.code.lowercase()) {
            "ja" -> japaneseRecognizer
            "zh", "zh-cn", "zh-tw" -> chineseRecognizer
            "ko" -> koreanRecognizer
            "hi", "mr" -> devanagariRecognizer
            else -> latinRecognizer
        }
    }

    private suspend fun processImage(
        recognizer: TextRecognizer,
        inputImage: InputImage
    ): Text = suspendCancellableCoroutine { continuation ->
        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                if (continuation.isActive) {
                    continuation.resume(visionText)
                }
            }
            .addOnFailureListener { exception ->
                if (continuation.isActive) {
                    continuation.resumeWithException(exception)
                }
            }
    }

    /**
     * Converts ML Kit vision text to domain OCR blocks.
     * Extracts text at Line granularity so multi-line text (subtitles, titles, buttons)
     * is accurately segmented with tight bounding boxes instead of being merged into giant slabs.
     */
    private fun convertToDomainBlocks(
        visionText: Text,
        detectedLangCode: String?,
        scaleFactor: Float = 1.0f
    ): List<OcrBlock> {
        val domainBlocks = mutableListOf<OcrBlock>()

        for (block in visionText.textBlocks) {
            if (block.lines.isNotEmpty()) {
                val lineBlocks = mutableListOf<OcrBlock>()
                for (line in block.lines) {
                    val rect = line.boundingBox ?: continue
                    val text = line.text.trim()
                    if (text.isEmpty()) continue

                    val boundingBox = if (scaleFactor != 1.0f && scaleFactor > 0f) {
                        BoundingBox(
                            left = rect.left / scaleFactor,
                            top = rect.top / scaleFactor,
                            right = rect.right / scaleFactor,
                            bottom = rect.bottom / scaleFactor
                        )
                    } else {
                        BoundingBox.fromRect(rect)
                    }

                    if (boundingBox.width <= 0f || boundingBox.height <= 0f) continue

                    val langCode = detectedLangCode
                        ?: line.recognizedLanguage
                        ?: detectScriptLanguage(text)

                    lineBlocks.add(
                        OcrBlock(
                            text = text,
                            boundingBox = boundingBox,
                            confidence = 1.0f,
                            detectedLanguageCode = langCode
                        )
                    )
                }

                // Intelligently cluster lines into coherent paragraphs or isolated UI blocks
                val clustered = TextParagraphClusterer.clusterLines(lineBlocks)
                domainBlocks.addAll(clustered)
            } else {
                val rect = block.boundingBox ?: continue
                val text = block.text.trim()
                if (text.isEmpty()) continue

                val boundingBox = if (scaleFactor != 1.0f && scaleFactor > 0f) {
                    BoundingBox(
                        left = rect.left / scaleFactor,
                        top = rect.top / scaleFactor,
                        right = rect.right / scaleFactor,
                        bottom = rect.bottom / scaleFactor
                    )
                } else {
                    BoundingBox.fromRect(rect)
                }

                if (boundingBox.width <= 0f || boundingBox.height <= 0f) continue

                val langCode = detectedLangCode
                    ?: block.recognizedLanguage
                    ?: detectScriptLanguage(text)

                domainBlocks.add(
                    OcrBlock(
                        text = text,
                        boundingBox = boundingBox,
                        confidence = 1.0f,
                        detectedLanguageCode = langCode
                    )
                )
            }
        }

        return domainBlocks
    }

    /**
     * Fast regex heuristic to determine script when ML Kit language detection is unavailable.
     */
    private fun detectScriptLanguage(text: String): String {
        return when {
            // Japanese: Hiragana, Katakana, Kanji
            text.any { it in '\u3040'..'\u309F' || it in '\u30A0'..'\u30FF' } -> "ja"
            // Korean: Hangul Syllables
            text.any { it in '\uAC00'..'\uD7AF' || it in '\u1100'..'\u11FF' } -> "ko"
            // Chinese: CJK Unified Ideographs
            text.any { it in '\u4E00'..'\u9FFF' } -> "zh"
            // Cyrillic: Russian, Ukrainian, etc.
            text.any { it in '\u0400'..'\u04FF' } -> "ru"
            else -> "en"
        }
    }
}

