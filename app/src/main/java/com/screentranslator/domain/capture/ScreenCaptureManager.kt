package com.screentranslator.domain.capture

import android.content.Intent
import android.graphics.Bitmap

/**
 * Interface for capturing the current device screen into an in-memory Bitmap.
 */
interface ScreenCaptureManager {
    /**
     * Captures the screen using the provided MediaProjection user consent token.
     * Tears down virtual display resources immediately after frame extraction.
     */
    suspend fun captureScreen(resultCode: Int, resultData: Intent): Result<Bitmap>
}
