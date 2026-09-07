package com.screentranslator.presentation.overlay.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.screentranslator.domain.model.CropRect

private enum class DragHandle {
    NONE, INSIDE, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT,
    TOP, BOTTOM, LEFT, RIGHT
}

@Composable
fun CropOverlay(
    onConfirmCrop: (CropRect) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Crop box coordinates normalized (0f..1f)
    var leftNorm by remember { mutableFloatStateOf(0.10f) }
    var topNorm by remember { mutableFloatStateOf(0.20f) }
    var rightNorm by remember { mutableFloatStateOf(0.90f) }
    var bottomNorm by remember { mutableFloatStateOf(0.80f) }

    var activeHandle by remember { mutableStateOf(DragHandle.NONE) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
    ) {
        val totalWidth = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val totalHeight = constraints.maxHeight.toFloat().coerceAtLeast(1f)

        // Pixel bounds of crop box
        val leftPx = leftNorm * totalWidth
        val topPx = topNorm * totalHeight
        val rightPx = rightNorm * totalWidth
        val bottomPx = bottomNorm * totalHeight

        val minSizePx = 60.dp.value * 2

        // Canvas for dim scrim and crop rectangle border
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(totalWidth, totalHeight) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val touchRadius = 40.dp.toPx()
                            val x = offset.x
                            val y = offset.y

                            val curLeft = leftNorm * totalWidth
                            val curTop = topNorm * totalHeight
                            val curRight = rightNorm * totalWidth
                            val curBottom = bottomNorm * totalHeight

                            activeHandle = when {
                                // Corners
                                (x - curLeft).let { dx -> (y - curTop).let { dy -> dx * dx + dy * dy <= touchRadius * touchRadius } } -> DragHandle.TOP_LEFT
                                (x - curRight).let { dx -> (y - curTop).let { dy -> dx * dx + dy * dy <= touchRadius * touchRadius } } -> DragHandle.TOP_RIGHT
                                (x - curLeft).let { dx -> (y - curBottom).let { dy -> dx * dx + dy * dy <= touchRadius * touchRadius } } -> DragHandle.BOTTOM_LEFT
                                (x - curRight).let { dx -> (y - curBottom).let { dy -> dx * dx + dy * dy <= touchRadius * touchRadius } } -> DragHandle.BOTTOM_RIGHT

                                // Edges
                                kotlin.math.abs(x - curLeft) <= touchRadius && y in curTop..curBottom -> DragHandle.LEFT
                                kotlin.math.abs(x - curRight) <= touchRadius && y in curTop..curBottom -> DragHandle.RIGHT
                                kotlin.math.abs(y - curTop) <= touchRadius && x in curLeft..curRight -> DragHandle.TOP
                                kotlin.math.abs(y - curBottom) <= touchRadius && x in curLeft..curRight -> DragHandle.BOTTOM

                                // Inside pan
                                x in curLeft..curRight && y in curTop..curBottom -> DragHandle.INSIDE
                                else -> DragHandle.NONE
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val dx = dragAmount.x
                            val dy = dragAmount.y

                            var curLeft = leftNorm * totalWidth
                            var curTop = topNorm * totalHeight
                            var curRight = rightNorm * totalWidth
                            var curBottom = bottomNorm * totalHeight

                            when (activeHandle) {
                                DragHandle.INSIDE -> {
                                    val w = curRight - curLeft
                                    val h = curBottom - curTop
                                    curLeft = (curLeft + dx).coerceIn(0f, totalWidth - w)
                                    curRight = curLeft + w
                                    curTop = (curTop + dy).coerceIn(0f, totalHeight - h)
                                    curBottom = curTop + h
                                }
                                DragHandle.TOP_LEFT -> {
                                    curLeft = (curLeft + dx).coerceIn(0f, curRight - minSizePx)
                                    curTop = (curTop + dy).coerceIn(0f, curBottom - minSizePx)
                                }
                                DragHandle.TOP_RIGHT -> {
                                    curRight = (curRight + dx).coerceIn(curLeft + minSizePx, totalWidth)
                                    curTop = (curTop + dy).coerceIn(0f, curBottom - minSizePx)
                                }
                                DragHandle.BOTTOM_LEFT -> {
                                    curLeft = (curLeft + dx).coerceIn(0f, curRight - minSizePx)
                                    curBottom = (curBottom + dy).coerceIn(curTop + minSizePx, totalHeight)
                                }
                                DragHandle.BOTTOM_RIGHT -> {
                                    curRight = (curRight + dx).coerceIn(curLeft + minSizePx, totalWidth)
                                    curBottom = (curBottom + dy).coerceIn(curTop + minSizePx, totalHeight)
                                }
                                DragHandle.LEFT -> {
                                    curLeft = (curLeft + dx).coerceIn(0f, curRight - minSizePx)
                                }
                                DragHandle.RIGHT -> {
                                    curRight = (curRight + dx).coerceIn(curLeft + minSizePx, totalWidth)
                                }
                                DragHandle.TOP -> {
                                    curTop = (curTop + dy).coerceIn(0f, curBottom - minSizePx)
                                }
                                DragHandle.BOTTOM -> {
                                    curBottom = (curBottom + dy).coerceIn(curTop + minSizePx, totalHeight)
                                }
                                DragHandle.NONE -> {}
                            }

                            leftNorm = curLeft / totalWidth
                            topNorm = curTop / totalHeight
                            rightNorm = curRight / totalWidth
                            bottomNorm = curBottom / totalHeight
                        },
                        onDragEnd = {
                            activeHandle = DragHandle.NONE
                        },
                        onDragCancel = {
                            activeHandle = DragHandle.NONE
                        }
                    )
                }
        ) {
            // Cutout scrim
            val cropPath = Path().apply {
                addRect(Rect(leftPx, topPx, rightPx, bottomPx))
            }

            clipPath(cropPath, clipOp = ClipOp.Difference) {
                drawRect(Color(0x99000000))
            }

            // Outline of the crop area
            drawRect(
                color = Color.White.copy(alpha = 0.85f),
                topLeft = Offset(leftPx, topPx),
                size = Size(rightPx - leftPx, bottomPx - topPx),
                style = Stroke(width = 2.dp.toPx())
            )

            // Lens-style corner L-brackets
            val bracketLen = 20.dp.toPx()
            val bracketStroke = 4.dp.toPx()
            val bracketColor = Color(0xFF38BDF8) // Sky blue accent

            // Top-Left
            drawLine(bracketColor, Offset(leftPx - 2, topPx), Offset(leftPx + bracketLen, topPx), bracketStroke)
            drawLine(bracketColor, Offset(leftPx, topPx - 2), Offset(leftPx, topPx + bracketLen), bracketStroke)

            // Top-Right
            drawLine(bracketColor, Offset(rightPx + 2, topPx), Offset(rightPx - bracketLen, topPx), bracketStroke)
            drawLine(bracketColor, Offset(rightPx, topPx - 2), Offset(rightPx, topPx + bracketLen), bracketStroke)

            // Bottom-Left
            drawLine(bracketColor, Offset(leftPx - 2, bottomPx), Offset(leftPx + bracketLen, bottomPx), bracketStroke)
            drawLine(bracketColor, Offset(leftPx, bottomPx + 2), Offset(leftPx, bottomPx - bracketLen), bracketStroke)

            // Bottom-Right
            drawLine(bracketColor, Offset(rightPx + 2, bottomPx), Offset(rightPx - bracketLen, bottomPx), bracketStroke)
            drawLine(bracketColor, Offset(rightPx, bottomPx + 2), Offset(rightPx, bottomPx - bracketLen), bracketStroke)
        }

        // Bottom action toolbar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp, start = 24.dp, end = 24.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xEE1E293B),
            shadowElevation = 10.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cancel
                IconButton(
                    onClick = onCancel,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel Crop",
                        tint = Color(0xFFCBD5E1)
                    )
                }

                // Reset to default
                IconButton(
                    onClick = {
                        leftNorm = 0.10f
                        topNorm = 0.20f
                        rightNorm = 0.90f
                        bottomNorm = 0.80f
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Crop",
                        tint = Color(0xFFCBD5E1)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Confirm button
                Button(
                    onClick = {
                        onConfirmCrop(
                            CropRect(
                                left = leftNorm,
                                top = topNorm,
                                right = rightNorm,
                                bottom = bottomNorm
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0284C7),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Translate Region",
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
