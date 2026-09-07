package com.screentranslator.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.screentranslator.domain.model.AppSettings
import com.screentranslator.domain.model.Language

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    hasOverlayPermission: Boolean,
    onGrantOverlayPermission: () -> Unit,
    onStartTranslateNow: () -> Unit,
    onUpdateSourceLanguage: (Language) -> Unit,
    onUpdateTargetLanguage: (Language) -> Unit,
    onUpdateOcrEngine: (String) -> Unit,
    onUpdateTranslationEngine: (String) -> Unit,
    onUpdateOverlayOpacity: (Float) -> Unit,
    onUpdateInitialScale: (Float) -> Unit,
    onToggleAutoStart: (Boolean) -> Unit,
    onToggleRememberPair: (Boolean) -> Unit,
    onToggleHaptic: (Boolean) -> Unit,
    onToggleFloatingButton: (Boolean) -> Unit,
    onToggleHistory: (Boolean) -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Screen Translator",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A)
                )
            )
        },
        containerColor = Color(0xFF0B1120)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero / Action Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Instant Screen Translation",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Capture and translate foreign text in-place using Google Lens-style replacement.",
                                fontSize = 13.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!hasOverlayPermission) {
                        Button(
                            onClick = onGrantOverlayPermission,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Grant Overlay Permission", fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Button(
                            onClick = onStartTranslateNow,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Translate Screen Now", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Translation Settings
            SettingsSectionHeader(title = "Translation", icon = Icons.Default.GTranslate)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    LanguagePickerRow(
                        label = "Default Source Language",
                        currentLanguage = settings.defaultSourceLanguage,
                        availableLanguages = Language.SUPPORTED_SOURCE_LANGUAGES,
                        onLanguageSelected = onUpdateSourceLanguage
                    )

                    HorizontalDivider(color = Color(0xFF334155))

                    LanguagePickerRow(
                        label = "Default Target Language",
                        currentLanguage = settings.defaultTargetLanguage,
                        availableLanguages = Language.SUPPORTED_TARGET_LANGUAGES,
                        onLanguageSelected = onUpdateTargetLanguage
                    )

                    HorizontalDivider(color = Color(0xFF334155))

                    EngineSelectorRow(
                        label = "Translation Engine",
                        selectedId = settings.selectedTranslationEngineId,
                        options = listOf(
                            "mlkit" to "Google ML Kit (On-Device, Offline)",
                            "google_web" to "Google Translate (Cloud)",
                            "deepl" to "DeepL (Planned)",
                            "llm" to "On-Device / Cloud LLM (Planned)"
                        ),
                        onSelect = onUpdateTranslationEngine
                    )
                }
            }

            // OCR Settings
            SettingsSectionHeader(title = "Optical Character Recognition (OCR)", icon = Icons.Default.DocumentScanner)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    EngineSelectorRow(
                        label = "OCR Engine",
                        selectedId = settings.selectedOcrEngineId,
                        options = listOf(
                            "mlkit" to "Google ML Kit (Latin, Japanese, Chinese, Korean)",
                            "tesseract" to "Tesseract OCR (Planned)"
                        ),
                        onSelect = onUpdateOcrEngine
                    )
                }
            }

            // Appearance Settings
            SettingsSectionHeader(title = "Appearance & Overlay", icon = Icons.Default.Palette)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Overlay Background Opacity", color = Color.White, fontSize = 14.sp)
                            Text("${(settings.overlayOpacity * 100).toInt()}%", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = settings.overlayOpacity,
                            onValueChange = onUpdateOverlayOpacity,
                            valueRange = 0.5f..1.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF38BDF8),
                                activeTrackColor = Color(0xFF38BDF8),
                                inactiveTrackColor = Color(0xFF334155)
                            )
                        )
                    }

                    HorizontalDivider(color = Color(0xFF334155))

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Initial Image Scale", color = Color.White, fontSize = 14.sp)
                            Text("${(settings.initialScale * 100).toInt()}%", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = settings.initialScale,
                            onValueChange = onUpdateInitialScale,
                            valueRange = 0.7f..1.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF38BDF8),
                                activeTrackColor = Color(0xFF38BDF8),
                                inactiveTrackColor = Color(0xFF334155)
                            )
                        )
                    }
                }
            }

            // Behavior & Shortcut Settings
            SettingsSectionHeader(title = "Behavior & Shortcuts", icon = Icons.Default.Bolt)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SettingsSwitchRow(
                        title = "Auto-start Translation",
                        subtitle = "Automatically run OCR and translation immediately after capture",
                        checked = settings.autoStartTranslation,
                        onCheckedChange = onToggleAutoStart
                    )

                    HorizontalDivider(color = Color(0xFF334155))

                    SettingsSwitchRow(
                        title = "Remember Language Pair",
                        subtitle = "Save the last used source and target language across sessions",
                        checked = settings.rememberLastLanguagePair,
                        onCheckedChange = onToggleRememberPair
                    )

                    HorizontalDivider(color = Color(0xFF334155))

                    SettingsSwitchRow(
                        title = "Haptic Feedback",
                        subtitle = "Vibrate subtly on capture start and translation completion",
                        checked = settings.hapticFeedback,
                        onCheckedChange = onToggleHaptic
                    )

                    HorizontalDivider(color = Color(0xFF334155))

                    SettingsSwitchRow(
                        title = "Floating Shortcut Bubble",
                        subtitle = "Show a floating button on screen edges to trigger translation anywhere",
                        checked = settings.floatingButtonEnabled,
                        onCheckedChange = onToggleFloatingButton
                    )
                }
            }

            // Privacy Section
            SettingsSectionHeader(title = "Privacy & Security", icon = Icons.Default.Security)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Zero Storage Retention", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            Text("Temporary screenshots are stored in private RAM only and wiped when the session closes.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                    }

                    HorizontalDivider(color = Color(0xFF334155))

                    SettingsSwitchRow(
                        title = "Save Translation History",
                        subtitle = "Store recognized text queries locally (default disabled for maximum privacy)",
                        checked = settings.saveHistory,
                        onCheckedChange = onToggleHistory
                    )
                }
            }

            // Quick Access Help Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Quick Access Tips", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("1. Pull down the Quick Settings notification shade and tap 'Edit' to add the 'Translate Screen' tile.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("2. Use the persistent notification shortcut to translate without leaving any active app.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("3. Enable the Floating Bubble above to keep a single-tap translator at your fingertips.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color(0xFFE2E8F0)
        )
    }
}

@Composable
fun LanguagePickerRow(
    label: String,
    currentLanguage: Language,
    availableLanguages: List<Language>,
    onLanguageSelected: (Language) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(label, color = Color.White, fontSize = 14.sp)
            Text(currentLanguage.displayName, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF94A3B8))

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color(0xFF1E293B))
        ) {
            availableLanguages.forEach { lang ->
                DropdownMenuItem(
                    text = {
                        Text(
                            lang.displayName,
                            color = if (lang.code == currentLanguage.code) Color(0xFF38BDF8) else Color.White,
                            fontWeight = if (lang.code == currentLanguage.code) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onLanguageSelected(lang)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun EngineSelectorRow(
    label: String,
    selectedId: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val currentTitle = options.find { it.first == selectedId }?.second ?: selectedId

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = Color.White, fontSize = 14.sp)
            Text(currentTitle, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF94A3B8))

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color(0xFF1E293B))
        ) {
            options.forEach { (id, title) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            title,
                            color = if (id == selectedId) Color(0xFF38BDF8) else Color.White,
                            fontWeight = if (id == selectedId) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onSelect(id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, color = Color(0xFF94A3B8), fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF0284C7),
                uncheckedThumbColor = Color(0xFF94A3B8),
                uncheckedTrackColor = Color(0xFF334155)
            )
        )
    }
}
