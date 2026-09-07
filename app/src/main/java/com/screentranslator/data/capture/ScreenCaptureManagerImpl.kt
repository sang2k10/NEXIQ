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
import android.os.Looper
import android.util.DisplayMetrics
import android.view.WindowManager
import com.screentranslator.domain.capture.ScreenCaptureManager
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
    private val mainHandler = Handler(Looper.getMainLooper())

    override suspend fun captureScreen(resultCode: Int, resultData: Intent): Result<Bitmap> =
        withContext(Dispatchers.Default) {
            var mediaProjection: MediaProjection? = null
            var virtualDisplay: VirtualDisplay? = null
            var imageReader: ImageReader? = null

            try {
                // 1. Calculate accurate screen metrics
                val (width, height, densityDpi) = getScreenDimensions()

                // 2. Initialize ImageReader for frame capture
                imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)

                // 3. Acquire MediaProjection
                mediaProjection = projectionManager.getMediaProjection(resultCode, resultData)
                    ?: return@withContext Result.failure(IllegalStateException("Failed to obtain MediaProjection instance"))

                // Mandatory callback for Android 14+ (API 34) compliance
                mediaProjection.registerCallback(object : MediaProjection.Callback() {
                    override fun onStop() {
                        super.onStop()
                    }
                }, mainHandler)

                val deferredBitmap = CompletableDeferred<Bitmap>()

                imageReader.setOnImageAvailableListener({ reader ->
                    if (deferredBitmap.isCompleted) return@setOnImageAvailableListener

                    val image = try {
                        reader.acquireLatestImage()
                    } catch (e: Exception) {
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
                        }
                    }
                }, mainHandler)

                // 4. Create VirtualDisplay mirroring the screen into ImageReader
                virtualDisplay = mediaProjection.createVirtualDisplay(
                    VIRTUAL_DISPLAY_NAME,
                    width,
                    height,
                    densityDpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    imageReader.surface,
                    null,
                    mainHandler
                )

                // 5. Wait for frame with timeout (1500ms max)
                val bitmap = withTimeoutOrNull(1500L) {
                    deferredBitmap.await()
                }

                if (bitmap != null) {
                    Result.success(bitmap)
                } else {
                    Result.failure(IllegalStateException("Screen capture timed out waiting for display frame"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            } finally {
                // Tear down virtual display and media projection immediately to release hardware resources
                virtualDisplay?.release()
                mediaProjection?.stop()
                imageReader?.close()
            }
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

        // If row padding was present, crop to exact display dimensions
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
