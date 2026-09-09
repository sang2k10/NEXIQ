package com.screentranslator.presentation.overlay.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.screentranslator.domain.model.Language
import com.screentranslator.domain.model.TranslationSession
import com.screentranslator.presentation.overlay.components.LensInteractiveCanvas
import com.screentranslator.presentation.overlay.components.OverlayTopBar
import com.screentranslator.presentation.overlay.matrix.GestureTransformState
import kotlinx.coroutines.launch

@Composable
fun SessionOverlayScreen(
    session: TranslationSession,
    overlayOpacity: Float,
    initialScale: Float,
    onSourceLanguageChanged: (Language) -> Unit,
    onTargetLanguageChanged: (Language) -> Unit,
    onToggleCropMode: () -> Unit,
    onConfirmCrop: (com.screentranslator.domain.model.CropRect) -> Unit,
    onDismissError: () -> Unit,
    onCloseSession: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transformState = remember { GestureTransformState(initialScale) }

    // Auto-dismiss error banner after 3.5 seconds
    LaunchedEffect(session.errorMessage) {
        if (session.errorMessage != null) {
            kotlinx.coroutines.delay(3500)
            onDismissError()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = overlayOpacity))
    ) {
        // Interactive translated canvas with zoom, pan, rotate
        LensInteractiveCanvas(
            bitmap = session.workingBitmap ?: session.originalBitmap,
            translatedBlocks = session.translatedBlocks,
            transformState = transformState,
            initialTargetScale = initialScale
        )

        // Interactive Crop Overlay when in Crop Mode
        if (session.isCropMode) {
            val bmp = session.workingBitmap ?: session.originalBitmap
            com.screentranslator.presentation.overlay.components.CropOverlay(
                onConfirmCrop = { normRect, canvasWidth, canvasHeight ->
                    if (bmp != null) {
                        val pixelCropRect = com.screentranslator.presentation.overlay.matrix.CoordinateTransformer.mapNormalizedCropToImage(
                            normCrop = normRect,
                            canvasWidth = canvasWidth,
                            canvasHeight = canvasHeight,
                            imageWidth = bmp.width.toFloat(),
                            imageHeight = bmp.height.toFloat(),
                            transformState = transformState
                        )
                        onConfirmCrop(pixelCropRect)
                    } else {
                        onConfirmCrop(normRect)
                    }
                },
                onCancel = onToggleCropMode
            )
        }

        val context = androidx.compose.ui.platform.LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        // Top control bar (drawn above CropOverlay so Close button is always accessible)
        OverlayTopBar(
            sourceLanguage = session.sourceLanguage,
            targetLanguage = session.targetLanguage,
            detectedLanguage = session.detectedLanguage,
            isLoading = session.isLoading,
            isCropMode = session.isCropMode,
            onSourceLanguageSelected = onSourceLanguageChanged,
            onTargetLanguageSelected = onTargetLanguageChanged,
            onToggleCropMode = onToggleCropMode,
            onCloseSession = onCloseSession,
            onSaveOriginal = {
                session.originalBitmap?.let { bmp ->
                    coroutineScope.launch {
                        com.screentranslator.core.util.ImageSaver.saveImage(
                            context = context,
                            bitmap = bmp,
                            isTranslated = false
                        )
                    }
                }
            },
            onSaveTranslated = {
                val targetBmp = session.workingBitmap ?: session.originalBitmap
                targetBmp?.let { bmp ->
                    coroutineScope.launch {
                        val composite = com.screentranslator.presentation.overlay.render.TranslatedImageComposer.compose(
                            originalBitmap = bmp,
                            translatedBlocks = session.translatedBlocks
                        )
                        com.screentranslator.core.util.ImageSaver.saveImage(
                            context = context,
                            bitmap = composite,
                            isTranslated = true
                        )
                    }
                }
            },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Error message banner
        AnimatedVisibility(
            visible = session.errorMessage != null,
            enter = slideInVertically(initialOffsetY = { -it }),
            exit = slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
        ) {
            session.errorMessage?.let { error ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = com.screentranslator.presentation.ui.theme.SurfaceContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, com.screentranslator.presentation.ui.theme.ErrorRed),
                    shadowElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            color = Color.White,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = onDismissError,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
