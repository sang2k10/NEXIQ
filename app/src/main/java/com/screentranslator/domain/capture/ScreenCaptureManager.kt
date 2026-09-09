package com.screentranslator.domain.capture

import android.content.Intent
import android.graphics.Bitmap

/**
 * Interface for capturing the current device screen into an in-memory Bitmap.
 */
interface ScreenCaptureManager {
    /**
     * Checks if a persistent MediaProjection session is active and ready for instant captures.
     */
    fun hasActiveProjection(): Boolean

    /**
     * Captures the screen using the provided MediaProjection user consent token.
     * Keeps the session active for instantaneous subsequent captures.
     */
    suspend fun captureScreen(resultCode: Int, resultData: Intent): Result<Bitmap>

    /**
     * Captures the screen instantaneously from the active MediaProjection session
     * without launching any activity or showing system permission dialogs.
     */
    suspend fun captureFromActiveProjection(): Result<Bitmap>

    /**
     * Tears down the active MediaProjection and VirtualDisplay.
     */
    fun stopProjection()
}
