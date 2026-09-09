package com.screentranslator.presentation.overlay.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.screentranslator.domain.model.Language
import com.screentranslator.presentation.ui.theme.*

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
    onSaveOriginal: () -> Unit = {},
    onSaveTranslated: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var sourceExpanded by remember { mutableStateOf(false) }
    var targetExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Language selector capsule
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SurfaceContainer.copy(alpha = 0.94f),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(end = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Source Language Picker
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { sourceExpanded = true }
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val displaySource = if (sourceLanguage.isAutoDetect && detectedLanguage != null) {
                                "Auto (${detectedLanguage.displayName})"
                            } else {
                                sourceLanguage.displayName
                            }
                            Text(
                                text = displaySource,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = sourceExpanded,
                            onDismissRequest = { sourceExpanded = false },
                            modifier = Modifier
                                .background(SurfaceCard)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                        ) {
                            Language.SUPPORTED_SOURCE_LANGUAGES.forEach { lang ->
                                val isSelected = lang.code == sourceLanguage.code
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            lang.displayName,
                                            color = if (isSelected) BrandPrimaryLighter else TextPrimary,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 13.sp
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

                    Text(
                        text = "→",
                        color = BrandPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )

                    // Target Language Picker
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { targetExpanded = true }
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = targetLanguage.displayName,
                                color = BrandPrimaryLighter,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = targetExpanded,
                            onDismissRequest = { targetExpanded = false },
                            modifier = Modifier
                                .background(SurfaceCard)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                        ) {
                            Language.SUPPORTED_TARGET_LANGUAGES.forEach { lang ->
                                val isSelected = lang.code == targetLanguage.code
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            lang.displayName,
                                            color = if (isSelected) BrandPrimaryLighter else TextPrimary,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 13.sp
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

            // Action Controls: Crop, Save, Close
            Row(
                modifier = Modifier.wrapContentWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Crop Tool (Scissors)
                IconButton(
                    onClick = onToggleCropMode,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isCropMode) BrandPrimary else SurfaceContainer.copy(alpha = 0.94f))
                        .border(1.dp, if (isCropMode) BrandPrimaryLighter else BorderSubtle, CircleShape)
                ) {
                    Icon(
                        Icons.Default.ContentCut,
                        contentDescription = "Crop Area",
                        tint = if (isCropMode) Color.White else TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Save Image (Original vs Translated)
                Box {
                    var saveExpanded by remember { mutableStateOf(false) }

                    IconButton(
                        onClick = { saveExpanded = true },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainer.copy(alpha = 0.94f))
                            .border(1.dp, BorderSubtle, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SaveAlt,
                            contentDescription = "Save Screenshot",
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = saveExpanded,
                        onDismissRequest = { saveExpanded = false },
                        modifier = Modifier
                            .widthIn(min = 210.dp, max = 260.dp)
                            .background(SurfaceCard)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    ) {
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(Icons.Default.Image, contentDescription = null, tint = BrandPrimaryLighter, modifier = Modifier.size(18.dp))
                            },
                            text = {
                                Column {
                                    Text("Save Original Screen", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    Text("Unmodified screenshot", color = TextSecondary, fontSize = 11.sp)
                                }
                            },
                            onClick = {
                                saveExpanded = false
                                onSaveOriginal()
                            }
                        )

                        HorizontalDivider(color = BorderSubtle)

                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(Icons.Default.Translate, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                            },
                            text = {
                                Column {
                                    Text("Save Translated Screen", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    Text("With translated text overlay", color = TextSecondary, fontSize = 11.sp)
                                }
                            },
                            onClick = {
                                saveExpanded = false
                                onSaveTranslated()
                            }
                        )
                    }
                }

                // Close Button
                IconButton(
                    onClick = onCloseSession,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainer.copy(alpha = 0.94f))
                        .border(1.dp, BorderSubtle, CircleShape)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Exit Translator",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Subtly animated linear loading indicator at top edge
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp),
                color = BrandPrimaryLighter,
                trackColor = Color.Transparent
            )
        }
    }
}
