package com.screentranslator.presentation.overlay.components

import android.graphics.Bitmap
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import com.screentranslator.domain.model.TranslatedBlock
import com.screentranslator.presentation.overlay.matrix.GestureTransformState

@Composable
fun LensInteractiveCanvas(
    bitmap: Bitmap?,
    translatedBlocks: List<TranslatedBlock>,
    transformState: GestureTransformState,
    initialTargetScale: Float = 0.90f,
    modifier: Modifier = Modifier
) {
    // Initial entrance scale animation from 1.0f (full screen) to initialTargetScale (comfortably scaled down)
    val entranceScaleAnim = remember { Animatable(1.0f) }
    LaunchedEffect(bitmap) {
        if (bitmap != null) {
            entranceScaleAnim.snapTo(1.0f)
            entranceScaleAnim.animateTo(
                targetValue = initialTargetScale,
                animationSpec = tween(durationMillis = 350)
            )
            transformState.reset(initialTargetScale)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, rotation ->
                    transformState.onTransform(
                        panChangeX = pan.x,
                        panChangeY = pan.y,
                        zoomChange = zoom,
                        rotationChange = rotation
                    )
                }
            }
    ) {
        if (bitmap != null && !bitmap.isRecycled) {
            val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }

            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                val imgWidth = bitmap.width.toFloat()
                val imgHeight = bitmap.height.toFloat()

                // Calculate center offset for image within viewport
                val baseOffsetX = (canvasWidth - imgWidth) / 2f
                val baseOffsetY = (canvasHeight - imgHeight) / 2f

                val currentScale = transformState.scale
                val currentRotation = transformState.rotationDegrees
                val currentPanX = transformState.offsetX
                val currentPanY = transformState.offsetY

                val pivot = Offset(canvasWidth / 2f, canvasHeight / 2f)

                // Apply affine transform: Pan -> Rotate -> Scale centered around viewport
                translate(left = currentPanX, top = currentPanY) {
                    rotate(degrees = currentRotation, pivot = pivot) {
                        scale(scale = currentScale, pivot = pivot) {
                            // Draw underlying screenshot
                            drawImage(
                                image = imageBitmap,
                                dstOffset = IntOffset(baseOffsetX.toInt(), baseOffsetY.toInt())
                            )

                            // Translated text blocks rendered in-place over original coordinates with background masking
                            val nativeCanvas = drawContext.canvas.nativeCanvas
                            for (block in translatedBlocks) {
                                com.screentranslator.presentation.overlay.render.LensTextRenderer.renderBlock(
                                    canvas = nativeCanvas,
                                    block = block,
                                    baseOffsetX = baseOffsetX,
                                    baseOffsetY = baseOffsetY
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
