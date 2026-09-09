package com.screentranslator.presentation.overlay.matrix

import com.screentranslator.domain.model.CropRect
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * 2D point representation for coordinate transformations.
 */
data class Point2D(val x: Float, val y: Float)

/**
 * High-precision affine coordinate transformer between display canvas coordinates
 * and original screen bitmap coordinates.
 *
 * Accounts for 2D viewport panning (offsetX, offsetY), pinch zoom (scale),
 * viewport center pivot, and rotation.
 */
object CoordinateTransformer {

    /**
     * Maps a point from original image coordinates to display canvas coordinates.
     */
    fun mapImagePointToCanvas(
        imageX: Float,
        imageY: Float,
        canvasWidth: Float,
        canvasHeight: Float,
        imageWidth: Float,
        imageHeight: Float,
        scale: Float,
        offsetX: Float,
        offsetY: Float,
        rotationDegrees: Float
    ): Point2D {
        val cx = canvasWidth / 2f
        val cy = canvasHeight / 2f

        val x1 = imageX - (imageWidth / 2f)
        val y1 = imageY - (imageHeight / 2f)

        val x2 = x1 * scale
        val y2 = y1 * scale

        val rad = (rotationDegrees * PI / 180.0).toFloat()
        val cosT = cos(rad)
        val sinT = sin(rad)

        val x3 = x2 * cosT - y2 * sinT
        val y3 = x2 * sinT + y2 * cosT

        val canvasX = x3 + cx + offsetX
        val canvasY = y3 + cy + offsetY

        return Point2D(canvasX, canvasY)
    }

    /**
     * Maps a point from display canvas coordinates back to original image coordinates.
     */
    fun mapCanvasPointToImage(
        canvasX: Float,
        canvasY: Float,
        canvasWidth: Float,
        canvasHeight: Float,
        imageWidth: Float,
        imageHeight: Float,
        scale: Float,
        offsetX: Float,
        offsetY: Float,
        rotationDegrees: Float
    ): Point2D {
        val safeScale = if (scale == 0f) 1.0f else scale
        val cx = canvasWidth / 2f
        val cy = canvasHeight / 2f

        val x3 = canvasX - cx - offsetX
        val y3 = canvasY - cy - offsetY

        val rad = (rotationDegrees * PI / 180.0).toFloat()
        val cosT = cos(rad)
        val sinT = sin(rad)

        val x2 = x3 * cosT + y3 * sinT
        val y2 = -x3 * sinT + y3 * cosT

        val x1 = x2 / safeScale
        val y1 = y2 / safeScale

        val imageX = x1 + (imageWidth / 2f)
        val imageY = y1 + (imageHeight / 2f)

        return Point2D(imageX, imageY)
    }

    /**
     * Maps an axis-aligned bounding box from canvas space to image space.
     * Computes the bounding box enclosing all 4 transformed corners and clamps
     * to bitmap bounds [0, imageWidth] x [0, imageHeight].
     */
    fun mapCanvasRectToImageCropRect(
        canvasLeft: Float,
        canvasTop: Float,
        canvasRight: Float,
        canvasBottom: Float,
        canvasWidth: Float,
        canvasHeight: Float,
        imageWidth: Float,
        imageHeight: Float,
        scale: Float,
        offsetX: Float,
        offsetY: Float,
        rotationDegrees: Float
    ): CropRect {
        if (imageWidth <= 0f || imageHeight <= 0f || canvasWidth <= 0f || canvasHeight <= 0f) {
            return CropRect(0f, 0f, max(1f, imageWidth), max(1f, imageHeight))
        }

        val p1 = mapCanvasPointToImage(canvasLeft, canvasTop, canvasWidth, canvasHeight, imageWidth, imageHeight, scale, offsetX, offsetY, rotationDegrees)
        val p2 = mapCanvasPointToImage(canvasRight, canvasTop, canvasWidth, canvasHeight, imageWidth, imageHeight, scale, offsetX, offsetY, rotationDegrees)
        val p3 = mapCanvasPointToImage(canvasRight, canvasBottom, canvasWidth, canvasHeight, imageWidth, imageHeight, scale, offsetX, offsetY, rotationDegrees)
        val p4 = mapCanvasPointToImage(canvasLeft, canvasBottom, canvasWidth, canvasHeight, imageWidth, imageHeight, scale, offsetX, offsetY, rotationDegrees)

        val minX = min(min(p1.x, p2.x), min(p3.x, p4.x)).coerceIn(0f, imageWidth)
        val minY = min(min(p1.y, p2.y), min(p3.y, p4.y)).coerceIn(0f, imageHeight)
        val maxX = max(max(p1.x, p2.x), max(p3.x, p4.x)).coerceIn(0f, imageWidth)
        val maxY = max(max(p1.y, p2.y), max(p3.y, p4.y)).coerceIn(0f, imageHeight)

        val safeRight = if (maxX <= minX) (minX + 16f).coerceAtMost(imageWidth) else maxX
        val safeBottom = if (maxY <= minY) (minY + 16f).coerceAtMost(imageHeight) else maxY

        return CropRect(
            left = minX,
            top = minY,
            right = safeRight,
            bottom = safeBottom
        )
    }

    /**
     * Maps normalized crop coordinates (0f..1f of canvas) into pixel bounds of the original image
     * taking the GestureTransformState into account.
     */
    fun mapNormalizedCropToImage(
        normCrop: CropRect,
        canvasWidth: Float,
        canvasHeight: Float,
        imageWidth: Float,
        imageHeight: Float,
        transformState: GestureTransformState
    ): CropRect {
        val canvasLeft = normCrop.left * canvasWidth
        val canvasTop = normCrop.top * canvasHeight
        val canvasRight = normCrop.right * canvasWidth
        val canvasBottom = normCrop.bottom * canvasHeight

        return mapCanvasRectToImageCropRect(
            canvasLeft = canvasLeft,
            canvasTop = canvasTop,
            canvasRight = canvasRight,
            canvasBottom = canvasBottom,
            canvasWidth = canvasWidth,
            canvasHeight = canvasHeight,
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            scale = transformState.scale,
            offsetX = transformState.offsetX,
            offsetY = transformState.offsetY,
            rotationDegrees = transformState.rotationDegrees
        )
    }
}
