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
import com.screentranslator.domain.engine.OcrEngine
import com.screentranslator.domain.model.BoundingBox
import com.screentranslator.domain.model.Language
import com.screentranslator.domain.model.OcrBlock
import kotlinx.coroutines.Dispatchers
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

            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val recognizer = selectRecognizer(preferredLanguage)

            val visionText = processImage(recognizer, inputImage)
            val blocks = convertToDomainBlocks(visionText, preferredLanguage?.code)

            // If auto-detect was requested and latin recognizer found very little text,
            // try Japanese as a fallback for East Asian scripts
            if (blocks.isEmpty() && (preferredLanguage == null || preferredLanguage.isAutoDetect)) {
                val jpnText = processImage(japaneseRecognizer, inputImage)
                val jpnBlocks = convertToDomainBlocks(jpnText, "ja")
                if (jpnBlocks.isNotEmpty()) {
                    return@withContext Result.success(jpnBlocks)
                }

                val chnText = processImage(chineseRecognizer, inputImage)
                val chnBlocks = convertToDomainBlocks(chnText, "zh")
                if (chnBlocks.isNotEmpty()) {
                    return@withContext Result.success(chnBlocks)
                }
            }

            Result.success(blocks)
        } catch (e: Exception) {
            Result.failure(e)
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

    private fun convertToDomainBlocks(
        visionText: Text,
        detectedLangCode: String?
    ): List<OcrBlock> {
        val domainBlocks = mutableListOf<OcrBlock>()

        for (block in visionText.textBlocks) {
            val rect = block.boundingBox ?: continue
            val boundingBox = BoundingBox.fromRect(rect)

            // Reject zero-size blocks
            if (boundingBox.width <= 0f || boundingBox.height <= 0f) continue

            // Determine script language code if not passed
            val langCode = detectedLangCode
                ?: block.recognizedLanguage
                ?: detectScriptLanguage(block.text)

            domainBlocks.add(
                OcrBlock(
                    text = block.text.trim(),
                    boundingBox = boundingBox,
                    confidence = 1.0f,
                    detectedLanguageCode = langCode
                )
            )
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
