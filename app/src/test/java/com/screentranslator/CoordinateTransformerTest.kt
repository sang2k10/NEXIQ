package com.screentranslator

import com.screentranslator.domain.model.CropRect
import com.screentranslator.presentation.overlay.matrix.CoordinateTransformer
import com.screentranslator.presentation.overlay.matrix.GestureTransformState
import org.junit.Assert.*
import org.junit.Test

class CoordinateTransformerTest {

    @Test
    fun identityTransform_mapsCenterPointAccurately() {
        val canvasW = 1080f
        val canvasH = 2400f
        val imgW = 1080f
        val imgH = 2400f

        val centerCanvas = CoordinateTransformer.mapImagePointToCanvas(
            imageX = 540f,
            imageY = 1200f,
            canvasWidth = canvasW,
            canvasHeight = canvasH,
            imageWidth = imgW,
            imageHeight = imgH,
            scale = 1.0f,
            offsetX = 0f,
            offsetY = 0f,
            rotationDegrees = 0f
        )

        assertEquals(540f, centerCanvas.x, 0.01f)
        assertEquals(1200f, centerCanvas.y, 0.01f)

        val imageBack = CoordinateTransformer.mapCanvasPointToImage(
            canvasX = centerCanvas.x,
            canvasY = centerCanvas.y,
            canvasWidth = canvasW,
            canvasHeight = canvasH,
            imageWidth = imgW,
            imageHeight = imgH,
            scale = 1.0f,
            offsetX = 0f,
            offsetY = 0f,
            rotationDegrees = 0f
        )

        assertEquals(540f, imageBack.x, 0.01f)
        assertEquals(1200f, imageBack.y, 0.01f)
    }

    @Test
    fun forwardAndInverse_invertConsistentlyWithPanZoomRotate() {
        val canvasW = 1080f
        val canvasH = 2400f
        val imgW = 1080f
        val imgH = 2400f
        val scale = 1.75f
        val offsetX = 120f
        val offsetY = -85f
        val rotation = 45f

        val originalX = 350f
        val originalY = 820f

        val canvasPoint = CoordinateTransformer.mapImagePointToCanvas(
            imageX = originalX,
            imageY = originalY,
            canvasWidth = canvasW,
            canvasHeight = canvasH,
            imageWidth = imgW,
            imageHeight = imgH,
            scale = scale,
            offsetX = offsetX,
            offsetY = offsetY,
            rotationDegrees = rotation
        )

        val mappedBack = CoordinateTransformer.mapCanvasPointToImage(
            canvasX = canvasPoint.x,
            canvasY = canvasPoint.y,
            canvasWidth = canvasW,
            canvasHeight = canvasH,
            imageWidth = imgW,
            imageHeight = imgH,
            scale = scale,
            offsetX = offsetX,
            offsetY = offsetY,
            rotationDegrees = rotation
        )

        assertEquals(originalX, mappedBack.x, 0.05f)
        assertEquals(originalY, mappedBack.y, 0.05f)
    }

    @Test
    fun scaledCrop_mapsExactlyToExpectedPixelBounds() {
        val canvasW = 1000f
        val canvasH = 2000f
        val imgW = 1000f
        val imgH = 2000f
        val scale = 0.5f // Scaled down by half centered at (500, 1000)

        // The image on canvas occupies x in [250, 750], y in [500, 1500]
        val cropOnCanvas = CoordinateTransformer.mapCanvasRectToImageCropRect(
            canvasLeft = 250f,
            canvasTop = 500f,
            canvasRight = 750f,
            canvasBottom = 1500f,
            canvasWidth = canvasW,
            canvasHeight = canvasH,
            imageWidth = imgW,
            imageHeight = imgH,
            scale = scale,
            offsetX = 0f,
            offsetY = 0f,
            rotationDegrees = 0f
        )

        assertEquals(0f, cropOnCanvas.left, 0.1f)
        assertEquals(0f, cropOnCanvas.top, 0.1f)
        assertEquals(1000f, cropOnCanvas.right, 0.1f)
        assertEquals(2000f, cropOnCanvas.bottom, 0.1f)
    }

    @Test
    fun normalizedCropWithTransformState_integratesCorrectly() {
        val transformState = GestureTransformState(initialScale = 1.0f)
        transformState.onTransform(panChangeX = 100f, panChangeY = 0f, zoomChange = 1.0f, rotationChange = 0f)

        val normRect = CropRect(left = 0.1f, top = 0.1f, right = 0.9f, bottom = 0.9f)
        val imageCrop = CoordinateTransformer.mapNormalizedCropToImage(
            normCrop = normRect,
            canvasWidth = 1000f,
            canvasHeight = 1000f,
            imageWidth = 1000f,
            imageHeight = 1000f,
            transformState = transformState
        )

        // With pan of +100 on canvas, the image has shifted right, so a canvas window of 100..900
        // maps to 0..800 on image
        assertEquals(0f, imageCrop.left, 0.1f)
        assertEquals(100f, imageCrop.top, 0.1f)
        assertEquals(800f, imageCrop.right, 0.1f)
        assertEquals(900f, imageCrop.bottom, 0.1f)
    }
}
