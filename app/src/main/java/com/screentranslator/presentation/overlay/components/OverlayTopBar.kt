package com.screentranslator.presentation.overlay.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.screentranslator.domain.model.Language

@Composable
fun OverlayTopBar(
    sourceLanguage: Language,
    targetLanguage: Language,
    detectedLanguage: Language?,
    isLoading: Boolean,
    isCropMode: Boolean,
    onSourceLanguageSelected: (Language) -> Unit,
    onTargetLanguageSelected: (Language) -> Unit,
    onToggleCropMode: () -> Unit,
    onCloseSession: () -> Unit,
    modifier: Modifier = Modifier
) {
    var sourceExpanded by remember { mutableStateOf(false) }
    var targetExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Language selector capsule
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xDD0F172A),
                shadowElevation = 8.dp,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Source Language Picker
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { sourceExpanded = true }
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val displaySource = if (sourceLanguage.isAutoDetect && detectedLanguage != null) {
                                "Auto (${detectedLanguage.displayName})"
                            } else {
                                sourceLanguage.displayName
                            }
                            Text(
                                text = displaySource,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = sourceExpanded,
                            onDismissRequest = { sourceExpanded = false },
                            modifier = Modifier.background(Color(0xFF1E293B))
                        ) {
                            Language.SUPPORTED_SOURCE_LANGUAGES.forEach { lang ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            lang.displayName,
                                            color = if (lang.code == sourceLanguage.code) Color(0xFF38BDF8) else Color.White
                                        )
                                    },
                                    onClick = {
                                        onSourceLanguageSelected(lang)
                                        sourceExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(14.dp)
                    )

                    // Target Language Picker
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { targetExpanded = true }
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = targetLanguage.displayName,
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = targetExpanded,
                            onDismissRequest = { targetExpanded = false },
                            modifier = Modifier.background(Color(0xFF1E293B))
                        ) {
                            Language.SUPPORTED_TARGET_LANGUAGES.forEach { lang ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            lang.displayName,
                                            color = if (lang.code == targetLanguage.code) Color(0xFF38BDF8) else Color.White
                                        )
                                    },
                                    onClick = {
                                        onTargetLanguageSelected(lang)
                                        targetExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Action Buttons: Crop & Close
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Crop Button (Scissors)
                IconButton(
                    onClick = onToggleCropMode,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isCropMode) Color(0xFF0284C7) else Color(0xDD0F172A))
                ) {
                    Icon(
                        Icons.Default.ContentCut,
                        contentDescription = "Crop Area",
                        tint = if (isCropMode) Color.White else Color(0xFFE2E8F0),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Close Button (Circular X)
                IconButton(
                    onClick = onCloseSession,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xDD0F172A))
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close Translation Session",
                        tint = Color(0xFFF43F5E),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Animated subtle progress indicator
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = Color(0xFF38BDF8),
                trackColor = Color.Transparent
            )
        }
    }
}
