package com.screentranslator.data.capture

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.DisplayMetrics
import android.view.WindowManager
import com.screentranslator.domain.capture.ScreenCaptureManager
import com.screentranslator.service.ScreenTranslatorAccessibilityService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.nio.ByteBuffer

class ScreenCaptureManagerImpl(
    private val context: Context
) : ScreenCaptureManager {

    private val projectionManager =
        context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    private val windowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    // Dedicated background handler thread so screen capture never touches the Main UI thread
    private var workerThread: HandlerThread? = null
    private var workerHandler: Handler? = null

    private var activeMediaProjection: MediaProjection? = null
    private var activeVirtualDisplay: VirtualDisplay? = null
    private var activeImageReader: ImageReader? = null
    private var currentWidth: Int = 0
    private var currentHeight: Int = 0

    private fun getWorkerHandler(): Handler {
        val existingHandler = workerHandler
        if (existingHandler != null && workerThread?.isAlive == true) {
            return existingHandler
        }
        val thread = HandlerThread("ScreenCaptureWorkerThread").apply { start() }
        workerThread = thread
        val handler = Handler(thread.looper)
        workerHandler = handler
        return handler
    }

    override fun hasActiveProjection(): Boolean {
        // If Accessibility Service is running, we always have instant zero-prompt static screenshot ready!
        return ScreenTranslatorAccessibilityService.isRunning()
    }

    override suspend fun captureScreen(resultCode: Int, resultData: Intent): Result<Bitmap> =
        withContext(Dispatchers.Default) {
            // Priority 1: If Accessibility Service is active, use instant hardware screenshot!
            if (ScreenTranslatorAccessibilityService.isRunning()) {
                val accResult = ScreenTranslatorAccessibilityService.captureScreenshot()
                if (accResult.isSuccess) {
                    return@withContext accResult
                }
            }

            try {
                // Stop any previous stale projection
                stopProjection()

                val (width, height, densityDpi) = getScreenDimensions()
                currentWidth = width
                currentHeight = height

                val handler = getWorkerHandler()
                val imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 1)
                activeImageReader = imageReader

                val mediaProjection = projectionManager.getMediaProjection(resultCode, resultData)
                    ?: return@withContext Result.failure(IllegalStateException("Failed to obtain MediaProjection instance"))
                activeMediaProjection = mediaProjection

                mediaProjection.registerCallback(object : MediaProjection.Callback() {
                    override fun onStop() {
                        super.onStop()
                        stopProjection()
                    }
                }, handler)

                val deferredBitmap = CompletableDeferred<Bitmap>()

                imageReader.setOnImageAvailableListener({ reader ->
                    val image = try {
                        reader.acquireLatestImage()
                    } catch (_: Exception) {
                        null
                    }

                    if (image != null) {
                        try {
                            val bitmap = processImageToBitmap(image, width, height)
                            if (bitmap != null && !deferredBitmap.isCompleted) {
                                deferredBitmap.complete(bitmap)
                            }
                        } catch (e: Exception) {
                            if (!deferredBitmap.isCompleted) {
                                deferredBitmap.completeExceptionally(e)
                            }
                        } finally {
                            image.close()
                            // Immediately remove listener so we don't continuously process frames
                            reader.setOnImageAvailableListener(null, null)
                        }
                    }
                }, handler)

                val virtualDisplay = mediaProjection.createVirtualDisplay(
                    VIRTUAL_DISPLAY_NAME,
                    width,
                    height,
                    densityDpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    imageReader.surface,
                    null,
                    handler
                )
                activeVirtualDisplay = virtualDisplay

                val bitmap = withTimeoutOrNull(1500L) {
                    deferredBitmap.await()
                }

                // ALWAYS stop projection immediately after single frame capture!
                // This ensures the system screen-recording indicator at the top of the screen disappears immediately.
                stopProjection()

                if (bitmap != null) {
                    Result.success(bitmap)
                } else {
                    Result.failure(IllegalStateException("Screen capture timed out waiting for display frame"))
                }
            } catch (e: Exception) {
                stopProjection()
                Result.failure(e)
            }
        }

    override suspend fun captureFromActiveProjection(): Result<Bitmap> =
        withContext(Dispatchers.Default) {
            // If Accessibility Service is running, capture directly without touching MediaProjection
            if (ScreenTranslatorAccessibilityService.isRunning()) {
                val accResult = ScreenTranslatorAccessibilityService.captureScreenshot()
                if (accResult.isSuccess) {
                    return@withContext accResult
                }
            }

            Result.failure(IllegalStateException("No active screenshot session available"))
        }

    override fun stopProjection() {
        try {
            activeVirtualDisplay?.release()
        } catch (_: Exception) {}
        activeVirtualDisplay = null

        try {
            activeMediaProjection?.stop()
        } catch (_: Exception) {}
        activeMediaProjection = null

        try {
            activeImageReader?.close()
        } catch (_: Exception) {}
        activeImageReader = null

        try {
            workerThread?.quitSafely()
        } catch (_: Exception) {}
        workerThread = null
        workerHandler = null
    }

    private fun getScreenDimensions(): Triple<Int, Int, Int> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = windowManager.currentWindowMetrics
            val bounds = metrics.bounds
            val densityDpi = context.resources.configuration.densityDpi
            Triple(bounds.width(), bounds.height(), densityDpi)
        } else {
            val displayMetrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getRealMetrics(displayMetrics)
            Triple(displayMetrics.widthPixels, displayMetrics.heightPixels, displayMetrics.densityDpi)
        }
    }

    private fun processImageToBitmap(image: Image, width: Int, height: Int): Bitmap? {
        val planes = image.planes
        if (planes.isEmpty()) return null

        val buffer: ByteBuffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * width

        val bitmapWidth = width + rowPadding / pixelStride
        val rawBitmap = Bitmap.createBitmap(bitmapWidth, height, Bitmap.Config.ARGB_8888)
        rawBitmap.copyPixelsFromBuffer(buffer)

        return if (rowPadding == 0) {
            rawBitmap
        } else {
            val cleanBitmap = Bitmap.createBitmap(rawBitmap, 0, 0, width, height)
            if (cleanBitmap != rawBitmap) {
                rawBitmap.recycle()
            }
            cleanBitmap
        }
    }

    companion object {
        private const val VIRTUAL_DISPLAY_NAME = "ScreenTranslatorVirtualDisplay"
    }
}
