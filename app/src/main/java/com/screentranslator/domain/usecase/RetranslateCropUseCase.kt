package com.screentranslator.domain.usecase

import android.graphics.Bitmap
import android.util.Log
import com.screentranslator.domain.engine.OcrEngine
import com.screentranslator.domain.model.BoundingBox
import com.screentranslator.domain.model.CropRect
import com.screentranslator.domain.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Executes a focused crop extraction from the original full-resolution screen bitmap,
 * re-runs OCR on the cropped sub-image, maps bounding boxes back to global screen coordinates,
 * and executes translation on the newly detected blocks.
 */
class RetranslateCropUseCase(
    private val sessionRepository: SessionRepository,
    private val ocrEngine: OcrEngine,
    private val translateBlocksUseCase: TranslateBlocksUseCase
) {
    companion object {
        private const val TAG = "RetranslateCropUseCase"
        private const val MIN_CROP_DIMENSION = 16
    }

    suspend operator fun invoke(cropRect: CropRect): Result<Unit> = withContext(Dispatchers.Default) {
        val currentSession = sessionRepository.activeSession.value
            ?: return@withContext Result.failure(IllegalStateException("No active session present"))

        val originalBitmap = currentSession.originalBitmap
            ?: return@withContext Result.failure(IllegalStateException("No original bitmap present in session"))

        if (originalBitmap.isRecycled) {
            return@withContext Result.failure(IllegalStateException("Original bitmap is already recycled"))
        }

        val bWidth = originalBitmap.width
        val bHeight = originalBitmap.height

        // Clamping pixel coordinates to within original bitmap bounds
        val left = cropRect.left.toInt().coerceIn(0, bWidth - MIN_CROP_DIMENSION)
        val top = cropRect.top.toInt().coerceIn(0, bHeight - MIN_CROP_DIMENSION)
        val right = cropRect.right.toInt().coerceIn(left + MIN_CROP_DIMENSION, bWidth)
        val bottom = cropRect.bottom.toInt().coerceIn(top + MIN_CROP_DIMENSION, bHeight)

        val cropW = right - left
        val cropH = bottom - top

        Log.d(TAG, "Extracting crop sub-bitmap: left=$left, top=$top, w=$cropW, h=$cropH")

        val subBitmap = try {
            Bitmap.createBitmap(originalBitmap, left, top, cropW, cropH)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create crop sub-bitmap", e)
            return@withContext Result.failure(e)
        }

        try {
            sessionRepository.updateSession {
                it.copy(
                    isCropMode = false,
                    isOcrRunning = true,
                    errorMessage = null
                )
            }

            // 1. Run OCR on the high-resolution cropped sub-bitmap
            val ocrResult = ocrEngine.recognize(
                bitmap = subBitmap,
                preferredLanguage = if (currentSession.sourceLanguage.isAutoDetect) null else currentSession.sourceLanguage
            )

            val localBlocks = ocrResult.getOrElse { error ->
                sessionRepository.updateSession {
                    it.copy(isOcrRunning = false, errorMessage = "Crop OCR failed: ${error.localizedMessage}")
                }
                return@withContext Result.failure(error)
            }

            if (localBlocks.isEmpty()) {
                sessionRepository.updateSession {
                    it.copy(
                        isOcrRunning = false,
                        ocrBlocks = emptyList(),
                        translatedBlocks = emptyList(),
                        errorMessage = "No text found in selected crop region"
                    )
                }
                return@withContext Result.success(Unit)
            }

            // 2. Map bounding boxes back to global screen coordinates
            val globalBlocks = localBlocks.map { localBlock ->
                val localBox = localBlock.boundingBox
                val globalBox = BoundingBox(
                    left = localBox.left + left,
                    top = localBox.top + top,
                    right = localBox.right + left,
                    bottom = localBox.bottom + top
                )
                localBlock.copy(boundingBox = globalBox)
            }

            sessionRepository.updateSession {
                it.copy(
                    isOcrRunning = false,
                    ocrBlocks = globalBlocks
                )
            }

            // 3. Batch translate the adjusted blocks
            translateBlocksUseCase()
            Result.success(Unit)
        } finally {
            // Guarantee immediate reclamation of the temporary crop sub-bitmap
            if (!subBitmap.isRecycled && subBitmap != originalBitmap) {
                subBitmap.recycle()
            }
        }
    }
}
