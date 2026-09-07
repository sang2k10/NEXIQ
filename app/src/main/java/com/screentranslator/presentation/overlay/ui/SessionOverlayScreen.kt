package com.screentranslator.presentation.overlay.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

@Composable
fun SessionOverlayScreen(
    session: TranslationSession,
    overlayOpacity: Float,
    initialScale: Float,
    onSourceLanguageChanged: (Language) -> Unit,
    onTargetLanguageChanged: (Language) -> Unit,
    onToggleCropMode: () -> Unit,
    onConfirmCrop: (com.screentranslator.domain.model.CropRect) -> Unit,
    onCloseSession: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transformState = remember { GestureTransformState(initialScale) }

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

        // Top control bar
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
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Interactive Crop Overlay when in Crop Mode
        if (session.isCropMode) {
            com.screentranslator.presentation.overlay.components.CropOverlay(
                onConfirmCrop = onConfirmCrop,
                onCancel = onToggleCropMode
            )
        }

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
                    color = Color(0xFFE11D48),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
