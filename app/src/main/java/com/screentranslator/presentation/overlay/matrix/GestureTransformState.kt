package com.screentranslator.presentation.overlay.matrix

import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue

/**
 * Manages 2D transformation state (pan, zoom, rotation) and matrix coordinate mapping
 * between original image pixels and display canvas pixels.
 */
class GestureTransformState(
    initialScale: Float = 0.90f,
    private val minScale: Float = 0.40f,
    private val maxScale: Float = 6.00f
) {
    var scale by mutableFloatStateOf(initialScale)
        private set

    var offsetX by mutableFloatStateOf(0f)
        private set

    var offsetY by mutableFloatStateOf(0f)
        private set

    var rotationDegrees by mutableFloatStateOf(0f)
        private set

    private val matrix = Matrix()
    private val inverseMatrix = Matrix()

    fun onTransform(panChangeX: Float, panChangeY: Float, zoomChange: Float, rotationChange: Float) {
        scale = (scale * zoomChange).coerceIn(minScale, maxScale)
        rotationDegrees = (rotationDegrees + rotationChange) % 360f
        offsetX += panChangeX
        offsetY += panChangeY
        updateMatrices()
    }

    fun reset(newScale: Float = 0.90f) {
        scale = newScale
        offsetX = 0f
        offsetY = 0f
        rotationDegrees = 0f
        updateMatrices()
    }

    private fun updateMatrices() {
        matrix.reset()
        matrix.postTranslate(offsetX, offsetY)
        matrix.postRotate(rotationDegrees)
        matrix.postScale(scale, scale)
        matrix.invert(inverseMatrix)
    }

    /**
     * Maps a bounding box in image coordinates to screen canvas coordinates.
     */
    fun mapImageRectToCanvas(imageRect: RectF, canvasCenterX: Float, canvasCenterY: Float): RectF {
        val dest = RectF()
        val localMatrix = Matrix()

        // Center origin for rotation and scaling
        localMatrix.postTranslate(-imageRect.width() / 2f, -imageRect.height() / 2f)
        localMatrix.postRotate(rotationDegrees)
        localMatrix.postScale(scale, scale)
        localMatrix.postTranslate(canvasCenterX + offsetX, canvasCenterY + offsetY)

        localMatrix.mapRect(dest, imageRect)
        return dest
    }

    /**
     * Maps screen canvas coordinates back to original image pixel coordinates.
     */
    fun mapCanvasPointToImage(canvasX: Float, canvasY: Float, imageWidth: Float, imageHeight: Float, canvasWidth: Float, canvasHeight: Float): PointF {
        val pts = floatArrayOf(canvasX, canvasY)
        val localMatrix = Matrix()

        val cx = canvasWidth / 2f
        val cy = canvasHeight / 2f

        localMatrix.postTranslate(-cx - offsetX, -cy - offsetY)
        localMatrix.postRotate(-rotationDegrees)
        localMatrix.postScale(1f / scale, 1f / scale)
        localMatrix.postTranslate(imageWidth / 2f, imageHeight / 2f)

        localMatrix.mapPoints(pts)
        return PointF(pts[0], pts[1])
    }
}
