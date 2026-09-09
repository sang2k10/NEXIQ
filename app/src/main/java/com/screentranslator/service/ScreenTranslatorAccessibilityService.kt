package com.screentranslator.service

import android.accessibilityservice.AccessibilityService
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.hardware.HardwareBuffer
import android.os.Build
import android.view.Display
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Android Accessibility Service for instant, one-time-granted clean screenshot capture.
 *
 * Benefits over MediaProjection:
 * - Granted ONCE by the user in Android Settings > Accessibility.
 * - NEVER prompts for screen recording or casting permission again.
 * - Takes a static hardware screenshot rather than maintaining an active screen recording session.
 * - Zero activity launch, zero focus loss, zero touch disruption (keeps Douyin videos paused and photos locked).
 */
class ScreenTranslatorAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No-op: We only use this service for on-demand clean static screenshots
    }

    override fun onInterrupt() {}

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        if (instance == this) {
            instance = null
        }
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
        }
    }

    companion object {
        var instance: ScreenTranslatorAccessibilityService? = null
            private set

        fun isRunning(): Boolean = instance != null

        suspend fun captureScreenshot(): Result<Bitmap> {
            val service = instance
                ?: return Result.failure(IllegalStateException("Accessibility Service is not active"))

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                return Result.failure(UnsupportedOperationException("Accessibility screenshots require Android 11+"))
            }

            val deferred = CompletableDeferred<Result<Bitmap>>()

            service.takeScreenshot(
                Display.DEFAULT_DISPLAY,
                service.mainExecutor,
                object : TakeScreenshotCallback {
                    override fun onSuccess(screenshotResult: ScreenshotResult) {
                        var hardwareBuffer: HardwareBuffer? = null
                        var hwBitmap: Bitmap? = null
                        try {
                            hardwareBuffer = screenshotResult.hardwareBuffer
                            val colorSpace = screenshotResult.colorSpace
                            hwBitmap = Bitmap.wrapHardwareBuffer(hardwareBuffer, colorSpace)

                            if (hwBitmap != null) {
                                // MUST copy to software bitmap WHILE hardwareBuffer is still open and valid!
                                val softwareBitmap = try {
                                    hwBitmap.copy(Bitmap.Config.ARGB_8888, true)
                                } catch (_: Throwable) {
                                    null
                                } ?: run {
                                    // Safe software Canvas fallback
                                    val sw = Bitmap.createBitmap(hwBitmap.width, hwBitmap.height, Bitmap.Config.ARGB_8888)
                                    val canvas = Canvas(sw)
                                    val paint = Paint(Paint.FILTER_BITMAP_FLAG)
                                    canvas.drawBitmap(hwBitmap, 0f, 0f, paint)
                                    sw
                                }

                                if (softwareBitmap != null) {
                                    deferred.complete(Result.success(softwareBitmap))
                                } else {
                                    deferred.complete(Result.failure(IllegalStateException("Failed to convert hardware screenshot to software bitmap")))
                                }
                            } else {
                                deferred.complete(Result.failure(IllegalStateException("Failed to wrap hardware buffer into Bitmap")))
                            }
                        } catch (e: Throwable) {
                            deferred.complete(Result.failure(e))
                        } finally {
                            try {
                                hwBitmap?.recycle()
                            } catch (_: Throwable) {}
                            try {
                                hardwareBuffer?.close()
                            } catch (_: Throwable) {}
                        }
                    }

                    override fun onFailure(errorCode: Int) {
                        deferred.complete(Result.failure(IllegalStateException("Accessibility screenshot failed (code: $errorCode)")))
                    }
                }
            )

            val result = withTimeoutOrNull(2500L) {
                deferred.await()
            }

            return result ?: Result.failure(IllegalStateException("Accessibility screenshot timed out"))
        }
    }
}
