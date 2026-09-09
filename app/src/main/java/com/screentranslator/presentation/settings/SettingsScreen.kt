package com.screentranslator.presentation.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.screentranslator.R
import com.screentranslator.domain.model.AppSettings
import com.screentranslator.domain.model.Language
import com.screentranslator.presentation.ui.components.*
import com.screentranslator.presentation.ui.theme.*

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
    onToggleHistory: (Boolean) -> Unit,
    onUpdateBubbleTheme: (String) -> Unit = {},
    onUpdateBubbleSize: (Int) -> Unit = {},
    onUpdateBubbleIconStyle: (String) -> Unit = {},
    onUpdateThemeMode: (String) -> Unit = {},
    onAcceptOnboarding: () -> Unit = {},
    onReopenBubble: () -> Unit = {},
    isAccessibilityEnabled: Boolean = false,
    onOpenAccessibilitySettings: () -> Unit = {},
    onOpenAppDetails: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Dialog states
    var languagePickerTarget by remember { mutableStateOf<LanguagePickerType?>(null) }
    var enginePickerTarget by remember { mutableStateOf<EnginePickerType?>(null) }
    var showVideoGuideDialog by remember { mutableStateOf(false) }
    var showOnboardingDialog by remember { mutableStateOf(false) }
    var showCloudConfirmationDialog by remember { mutableStateOf(false) }

    // Trigger onboarding on first launch
    LaunchedEffect(settings.hasAcceptedOnboarding) {
        if (!settings.hasAcceptedOnboarding) {
            showOnboardingDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(NexiqSpacing.md)
                    ) {
                        Surface(
                            shape = ControlShape,
                            color = SurfaceElevated,
                            border = BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(R.drawable.ic_bubble_translate),
                                    contentDescription = "NEXIQ Viewframe Logo",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "NEXIQ",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Screen Understanding & Translation",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceBase)
            )
        },
        containerColor = SurfaceBase
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = NexiqSpacing.screenHorizontal, vertical = NexiqSpacing.sm)
        ) {

            // 1. Optional Permission Alert (Clean banner, only when missing)
            if (!hasOverlayPermission) {
                Surface(
                    shape = ContainerShape,
                    color = SurfaceElevated,
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(NexiqSpacing.lg),
                        verticalArrangement = Arrangement.spacedBy(NexiqSpacing.sm)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(NexiqSpacing.sm)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Layers,
                                contentDescription = null,
                                tint = BrandPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Overlay Permission Required",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "To display translated text directly on top of your screen, grant 'Display Over Other Apps' permission.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Button(
                            onClick = onGrantOverlayPermission,
                            shape = ControlShape,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                            contentPadding = PaddingValues(horizontal = NexiqSpacing.md, vertical = NexiqSpacing.sm),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Grant Permission",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(NexiqSpacing.md))
            }

            // 2. Assisted Screen Capture Status (Clean, quiet banner)
            NexiqStatusBanner(
                title = "Assisted Static Screen Capture",
                description = if (isAccessibilityEnabled) {
                    "Accessibility screenshot service is active for instantaneous, zero-prompt captures."
                } else {
                    "Enable the optional Accessibility service to capture clean screenshots without repeated recording prompts."
                },
                isActive = isAccessibilityEnabled,
                actionText = "Open Accessibility Settings",
                onActionClick = onOpenAccessibilitySettings
            )

            // 3. Section: General Translation
            NexiqSectionHeader(title = "General Translation")

            NexiqSettingRow(
                title = "Source Language",
                subtitle = "Language visible on screen",
                trailingText = settings.defaultSourceLanguage.displayName,
                showChevron = true,
                onClick = { languagePickerTarget = LanguagePickerType.SOURCE }
            )
            NexiqDivider()

            NexiqSettingRow(
                title = "Target Language",
                subtitle = "Language to translate into",
                trailingText = settings.defaultTargetLanguage.displayName,
                showChevron = true,
                onClick = { languagePickerTarget = LanguagePickerType.TARGET }
            )
            NexiqDivider()

            NexiqSettingRow(
                title = "Translation Engine",
                subtitle = "Processing provider",
                trailingText = getTranslationEngineName(settings.selectedTranslationEngineId),
                showChevron = true,
                onClick = { enginePickerTarget = EnginePickerType.TRANSLATION }
            )
            NexiqDivider()

            NexiqSettingRow(
                title = "OCR Recognition Engine",
                subtitle = "Text detection model",
                trailingText = getOcrEngineName(settings.selectedOcrEngineId),
                showChevron = true,
                onClick = { enginePickerTarget = EnginePickerType.OCR }
            )
            NexiqDivider()

            NexiqSwitchRow(
                title = "Auto-start Translation",
                subtitle = "Execute OCR and translation immediately upon screen capture",
                checked = settings.autoStartTranslation,
                onCheckedChange = onToggleAutoStart
            )
            NexiqDivider()

            NexiqSwitchRow(
                title = "Remember Language Pair",
                subtitle = "Preserve the last used language selection across sessions",
                checked = settings.rememberLastLanguagePair,
                onCheckedChange = onToggleRememberPair
            )

            // 4. Section: Appearance & Display
            NexiqSectionHeader(title = "Appearance & Display")

            // Theme Mode Selector (Segmented Row)
            Column(modifier = Modifier.padding(horizontal = NexiqSpacing.xs, vertical = NexiqSpacing.sm)) {
                Text(
                    text = "Theme Mode",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(NexiqSpacing.xxs))
                Text(
                    text = "System default, light, or dark appearance",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(NexiqSpacing.md))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(NexiqSpacing.sm)
                ) {
                    listOf(
                        "system" to "System",
                        "light" to "Light",
                        "dark" to "Dark"
                    ).forEach { (mode, label) ->
                        val isSelected = settings.themeMode.equals(mode, ignoreCase = true)
                        Surface(
                            shape = ControlShape,
                            color = if (isSelected) BrandPrimaryContainer else SurfaceElevated,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) BrandPrimary else BorderSubtle
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(ControlShape)
                                .clickable { onUpdateThemeMode(mode) }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = NexiqSpacing.md)
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) BrandPrimary else TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
            NexiqDivider()

            NexiqSliderRow(
                title = "Overlay Dimming",
                valueText = "${(settings.overlayOpacity * 100).toInt()}%",
                value = settings.overlayOpacity,
                onValueChange = onUpdateOverlayOpacity,
                valueRange = 0.5f..1.0f,
                subtitle = "Background dimming level behind translated text"
            )
            NexiqDivider()

            NexiqSliderRow(
                title = "Initial Image Scale",
                valueText = "${(settings.initialScale * 100).toInt()}%",
                value = settings.initialScale,
                onValueChange = onUpdateInitialScale,
                valueRange = 0.7f..1.0f,
                subtitle = "Initial canvas viewport scale upon capture"
            )

            // 5. Section: Floating Shortcut
            NexiqSectionHeader(title = "Floating Shortcut")

            NexiqSwitchRow(
                title = "Floating Shortcut Bubble",
                subtitle = "Persistent screen-edge trigger for one-tap capture",
                checked = settings.floatingButtonEnabled,
                onCheckedChange = onToggleFloatingButton
            )

            if (settings.floatingButtonEnabled) {
                NexiqDivider()

                // Bubble Color Theme (Visual circular swatches)
                BubbleColorThemePicker(
                    selectedTheme = settings.bubbleTheme,
                    onSelectTheme = onUpdateBubbleTheme
                )

                NexiqDivider()

                // Bubble Icon Style (Visual icon preview tiles)
                BubbleIconStylePicker(
                    selectedIconStyle = settings.bubbleIconStyle,
                    selectedTheme = settings.bubbleTheme,
                    onSelectIconStyle = onUpdateBubbleIconStyle
                )

                NexiqDivider()

                NexiqSliderRow(
                    title = "Bubble Size",
                    valueText = "${settings.bubbleSizeDp} dp",
                    value = settings.bubbleSizeDp.toFloat(),
                    onValueChange = { onUpdateBubbleSize(it.toInt()) },
                    valueRange = 40f..64f,
                    steps = 11,
                    subtitle = "Diameter of the floating trigger icon"
                )

                Spacer(modifier = Modifier.height(NexiqSpacing.xs))

                OutlinedButton(
                    onClick = onReopenBubble,
                    shape = ControlShape,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandPrimary),
                    border = BorderStroke(1.dp, BorderMedium),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = NexiqSpacing.xs)
                        .height(40.dp)
                ) {
                    Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(NexiqSpacing.sm))
                    Text("Reopen Bubble on Screen", fontSize = 13.sp)
                }
            }

            NexiqDivider()

            NexiqSwitchRow(
                title = "Haptic Vibration",
                subtitle = "Tactile feedback on capture and completion",
                checked = settings.hapticFeedback,
                onCheckedChange = onToggleHaptic
            )

            // 6. Section: Privacy & Data
            NexiqSectionHeader(title = "Privacy & Data")

            NexiqSettingRow(
                title = "Private by Design",
                subtitle = "Screen content is processed in transient memory during translation sessions. No background tracking or advertising telemetry.",
                leadingIcon = Icons.Outlined.Shield
            )
            NexiqDivider()

            NexiqSwitchRow(
                title = "Save Translation History",
                subtitle = "Store text translation queries locally on this device",
                checked = settings.saveHistory,
                onCheckedChange = onToggleHistory
            )
            NexiqDivider()

            OutlinedButton(
                onClick = { showOnboardingDialog = true },
                shape = ControlShape,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandPrimary),
                border = BorderStroke(1.dp, BorderMedium),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = NexiqSpacing.xs, vertical = NexiqSpacing.xs)
                    .height(40.dp)
            ) {
                Icon(Icons.Outlined.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(NexiqSpacing.sm))
                Text("Review Privacy Disclosures & Guide", fontSize = 13.sp)
            }

            // 7. Section: Help & About
            NexiqSectionHeader(title = "Help & About")

            NexiqSettingRow(
                title = "How NEXIQ Works",
                subtitle = "30-second quick guide on screen translation",
                leadingIcon = Icons.AutoMirrored.Filled.HelpOutline,
                showChevron = true,
                onClick = { showVideoGuideDialog = true }
            )
            NexiqDivider()

            NexiqSettingRow(
                title = "App Info",
                subtitle = "NEXIQ v1.0.0 (Build 1)",
                leadingIcon = Icons.Outlined.Info,
                showChevron = true,
                onClick = onOpenAppDetails
            )
            NexiqDivider()

            NexiqSettingRow(
                title = "View Source on GitHub",
                subtitle = "https://github.com/sang2k10/NEXIQ",
                leadingIcon = Icons.AutoMirrored.Filled.OpenInNew,
                showChevron = true,
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/sang2k10/NEXIQ")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    try {
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                }
            )

            Spacer(modifier = Modifier.height(NexiqSpacing.section))
        }
    }

    // Modal: Language Selection
    if (languagePickerTarget != null) {
        val isSource = languagePickerTarget == LanguagePickerType.SOURCE
        val currentLang = if (isSource) settings.defaultSourceLanguage else settings.defaultTargetLanguage
        val availableLangs = if (isSource) Language.SUPPORTED_SOURCE_LANGUAGES else Language.SUPPORTED_TARGET_LANGUAGES

        LanguageSelectionDialog(
            title = if (isSource) "Select Source Language" else "Select Target Language",
            currentLanguage = currentLang,
            languages = availableLangs,
            onSelect = { selected ->
                if (isSource) onUpdateSourceLanguage(selected) else onUpdateTargetLanguage(selected)
                languagePickerTarget = null
            },
            onDismiss = { languagePickerTarget = null }
        )
    }

    // Modal: Engine Selection
    if (enginePickerTarget != null) {
        val isTranslation = enginePickerTarget == EnginePickerType.TRANSLATION
        val title = if (isTranslation) "Select Translation Engine" else "Select OCR Engine"
        val selectedId = if (isTranslation) settings.selectedTranslationEngineId else settings.selectedOcrEngineId
        val options = if (isTranslation) {
            listOf(
                "mlkit" to "Google ML Kit (On-Device, Offline)",
                "google_web" to "Experimental Cloud (Web Fallback)",
                "deepl" to "DeepL API (Planned)",
                "llm" to "On-Device / Cloud LLM (Planned)"
            )
        } else {
            listOf(
                "mlkit" to "Google ML Kit Multi-script",
                "tesseract" to "Tesseract OCR (Planned)"
            )
        }

        EngineSelectionDialog(
            title = title,
            selectedId = selectedId,
            options = options,
            onSelect = { id ->
                if (isTranslation) {
                    if (id == "google_web") {
                        showCloudConfirmationDialog = true
                    } else {
                        onUpdateTranslationEngine(id)
                    }
                } else {
                    onUpdateOcrEngine(id)
                }
                enginePickerTarget = null
            },
            onDismiss = { enginePickerTarget = null }
        )
    }

    // Modal: Cloud Confirmation Warning
    if (showCloudConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showCloudConfirmationDialog = false },
            title = {
                Text("Enable Cloud Translation?", fontWeight = FontWeight.SemiBold, color = TextPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(NexiqSpacing.sm)) {
                    Text(
                        text = "Selecting Experimental Cloud Translation transmits extracted text over HTTPS to Google Translate web servers.",
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "While no images, user identifiers, or advertising profiles are transmitted, this sends text off your device. Use on-device ML Kit if you require 100% offline privacy.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateTranslationEngine("google_web")
                        showCloudConfirmationDialog = false
                    },
                    shape = ControlShape,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                ) {
                    Text("Enable Cloud")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloudConfirmationDialog = false }) {
                    Text("Keep On-Device", color = TextSecondary)
                }
            },
            containerColor = SurfaceContainer,
            shape = DialogShape
        )
    }

    // Modal: User Guide Walkthrough
    if (showVideoGuideDialog) {
        DemoVideoGuideDialog(onDismiss = { showVideoGuideDialog = false })
    }

    // Modal: First-Launch Onboarding & Privacy
    if (showOnboardingDialog) {
        OnboardingPrivacyDialog(
            onAccept = {
                onAcceptOnboarding()
                showOnboardingDialog = false
            },
            onDismiss = { showOnboardingDialog = false }
        )
    }
}

private enum class LanguagePickerType { SOURCE, TARGET }
private enum class EnginePickerType { TRANSLATION, OCR }

/**
 * Bubble Color Theme Picker: Displays visual circular swatches instead of bulky cards.
 */
@Composable
private fun BubbleColorThemePicker(
    selectedTheme: String,
    onSelectTheme: (String) -> Unit
) {
    val themes = listOf(
        ThemeItem("cyan", "Azure", Color(0xFF0284C7), Color(0xFF38BDF8)),
        ThemeItem("dark", "Obsidian", Color(0xFF1E293B), Color(0xFF64748B)),
        ThemeItem("pearl", "Pearl", Color(0xFFF8FAFC), Color(0xFFCBD5E1)),
        ThemeItem("indigo", "Indigo", Color(0xFF4F46E5), Color(0xFF818CF8))
    )

    Column(modifier = Modifier.padding(horizontal = NexiqSpacing.xs, vertical = NexiqSpacing.sm)) {
        Text(
            text = "Bubble Color Theme",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(NexiqSpacing.xxs))
        Text(
            text = "Visual styling of the persistent edge bubble",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(NexiqSpacing.md))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(NexiqSpacing.sm)
        ) {
            themes.forEach { item ->
                val isSelected = selectedTheme.equals(item.id, ignoreCase = true) ||
                        (selectedTheme.equals("sunset", ignoreCase = true) && item.id == "pearl")
                Surface(
                    shape = ControlShape,
                    color = if (isSelected) BrandPrimaryContainer else SurfaceElevated,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) BrandPrimary else BorderSubtle
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clip(ControlShape)
                        .clickable { onSelectTheme(item.id) }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(NexiqSpacing.xs),
                        modifier = Modifier.padding(vertical = NexiqSpacing.md, horizontal = NexiqSpacing.xxs)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(item.color)
                                .border(1.dp, item.ringColor, CircleShape)
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = if (item.id == "pearl") Color(0xFF0F172A) else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = item.label,
                            color = if (isSelected) TextPrimary else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * Bubble Icon Style Picker: Clean icon preview tiles.
 */
@Composable
private fun BubbleIconStylePicker(
    selectedIconStyle: String,
    selectedTheme: String,
    onSelectIconStyle: (String) -> Unit
) {
    val styles = listOf(
        IconStyleItem("brand", "Viewframe N", R.drawable.ic_bubble_translate),
        IconStyleItem("lens", "Minimal Lens", R.drawable.ic_bubble_lens),
        IconStyleItem("glyph", "Text Glyph", R.drawable.ic_bubble_glyph),
        IconStyleItem("aperture", "Aperture", R.drawable.ic_bubble_aperture)
    )

    Column(modifier = Modifier.padding(horizontal = NexiqSpacing.xs, vertical = NexiqSpacing.sm)) {
        Text(
            text = "Bubble Icon Style",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(NexiqSpacing.xxs))
        Text(
            text = "Graphic symbol displayed inside the floating bubble",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(NexiqSpacing.md))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(NexiqSpacing.sm)
        ) {
            styles.forEach { item ->
                val isSelected = selectedIconStyle.equals(item.id, ignoreCase = true)
                Surface(
                    shape = ControlShape,
                    color = if (isSelected) BrandPrimaryContainer else SurfaceElevated,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) BrandPrimary else BorderSubtle
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clip(ControlShape)
                        .clickable { onSelectIconStyle(item.id) }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(NexiqSpacing.xs),
                        modifier = Modifier.padding(vertical = NexiqSpacing.md, horizontal = NexiqSpacing.xxs)
                    ) {
                        Image(
                            painter = painterResource(item.drawableRes),
                            contentDescription = item.label,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = item.label,
                            color = if (isSelected) TextPrimary else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

private data class ThemeItem(val id: String, val label: String, val color: Color, val ringColor: Color)
private data class IconStyleItem(val id: String, val label: String, val drawableRes: Int)

private fun getTranslationEngineName(id: String): String {
    return when (id) {
        "google_web" -> "Experimental Cloud (Web Fallback)"
        "deepl" -> "DeepL API"
        "llm" -> "On-Device / Cloud LLM"
        else -> "Google ML Kit (On-Device, Offline)"
    }
}

private fun getOcrEngineName(id: String): String {
    return when (id) {
        "tesseract" -> "Tesseract OCR"
        else -> "Google ML Kit Multi-script"
    }
}

/**
 * Clean User Guide Dialog.
 */
@Composable
private fun DemoVideoGuideDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = DialogShape,
            color = SurfaceContainer,
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(NexiqSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(NexiqSpacing.md)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(NexiqSpacing.md)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = BrandPrimaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(20.dp))
                        }
                    }
                    Column {
                        Text("How NEXIQ Works", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text("30-Second Quick Walkthrough", style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontSize = 11.sp)
                    }
                }

                // Simulated Preview Screen
                Surface(
                    shape = ContainerShape,
                    color = Color(0xFF070B12),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(NexiqSpacing.xs)
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Play Video",
                                tint = BrandPrimary,
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = "Screen Translation Walkthrough",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(NexiqSpacing.sm)) {
                    GuideStepRow("1", "Tap Floating Bubble", "Keep the bubble on your screen edge to trigger instant captures.")
                    GuideStepRow("2", "In-Place Overlay", "Recognized text is replaced directly on top of the original layout.")
                    GuideStepRow("3", "Focus & Crop", "Use the Scissors tool to crop specific areas or save the composite image.")
                }

                Button(
                    onClick = onDismiss,
                    shape = ControlShape,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Got It", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun GuideStepRow(step: String, title: String, description: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(NexiqSpacing.md),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(BrandPrimaryContainer)
        ) {
            Text(step, color = BrandPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontSize = 13.sp)
            Text(description, style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
        }
    }
}

/**
 * Onboarding / Welcome Dialog: Clear, honest, scannable in seconds.
 */
@Composable
private fun OnboardingPrivacyDialog(
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = DialogShape,
            color = SurfaceContainer,
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier.padding(NexiqSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(NexiqSpacing.lg)
            ) {
                // Brand Header
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = ContainerShape,
                        color = SurfaceElevated,
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(R.drawable.ic_bubble_translate),
                                contentDescription = "NEXIQ Viewframe Logo",
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(NexiqSpacing.md))
                    Text(
                        text = "Translate what's on your screen",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(NexiqSpacing.xs))
                    Text(
                        text = "Recognize and translate visible text in-place without switching apps.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }

                NexiqDivider()

                // Key Features (Typographic hierarchy, not cards)
                Column(verticalArrangement = Arrangement.spacedBy(NexiqSpacing.md)) {
                    FeatureHighlightRow(
                        icon = Icons.Outlined.FitScreen,
                        title = "In-Place Overlay",
                        description = "Translations render directly over the original coordinates on your screen."
                    )
                    FeatureHighlightRow(
                        icon = Icons.Outlined.Memory,
                        title = "On-Device Intelligence",
                        description = "ML Kit OCR processes recognized text locally on your device."
                    )
                }

                // Privacy Disclosure Summary (Calm, technical)
                Surface(
                    shape = ControlShape,
                    color = SurfaceElevated,
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(NexiqSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(NexiqSpacing.xs)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(NexiqSpacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.Security,
                                contentDescription = null,
                                tint = BrandPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Your privacy, clearly explained",
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                fontSize = 12.sp
                            )
                        }
                        Text(
                            text = "Screen content exists in transient memory only during active translation sessions. No background tracking or profile collection. Optional cloud engine sends text over HTTPS only when explicitly enabled.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                // GitHub Source Link
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/sang2k10/NEXIQ")).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        try {
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    },
                    shape = ControlShape,
                    border = BorderStroke(1.dp, BorderSubtle),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    contentPadding = PaddingValues(vertical = NexiqSpacing.sm),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Outlined.Code,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(NexiqSpacing.sm))
                    Text(
                        text = "View source code on GitHub",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Primary CTA
                Button(
                    onClick = onAccept,
                    shape = ControlShape,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    contentPadding = PaddingValues(vertical = NexiqSpacing.md),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Get Started",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureHighlightRow(icon: ImageVector, title: String, description: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(NexiqSpacing.md),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(BrandPrimaryContainer)
        ) {
            Icon(icon, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(NexiqSpacing.xxs))
            Text(description, style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
        }
    }
}

/**
 * Language Selection Modal.
 */
@Composable
private fun LanguageSelectionDialog(
    title: String,
    currentLanguage: Language,
    languages: List<Language>,
    onSelect: (Language) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = DialogShape,
            color = SurfaceContainer,
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 480.dp)
        ) {
            Column(modifier = Modifier.padding(NexiqSpacing.lg)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(NexiqSpacing.sm))
                NexiqDivider()

                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(NexiqSpacing.xxs)
                ) {
                    items(languages) { lang ->
                        val isSelected = lang.code == currentLanguage.code
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(ControlShape)
                                .background(if (isSelected) BrandPrimaryContainer else Color.Transparent)
                                .clickable { onSelect(lang) }
                                .padding(horizontal = NexiqSpacing.md, vertical = NexiqSpacing.md),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = lang.displayName,
                                color = if (isSelected) BrandPrimary else TextPrimary,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(NexiqSpacing.sm))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            }
        }
    }
}

/**
 * Engine Selection Modal.
 */
@Composable
private fun EngineSelectionDialog(
    title: String,
    selectedId: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = DialogShape,
            color = SurfaceContainer,
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(NexiqSpacing.lg)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(NexiqSpacing.sm))
                NexiqDivider()

                Column(
                    modifier = Modifier.padding(vertical = NexiqSpacing.xs),
                    verticalArrangement = Arrangement.spacedBy(NexiqSpacing.xs)
                ) {
                    options.forEach { (id, label) ->
                        val isSelected = id == selectedId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(ControlShape)
                                .background(if (isSelected) BrandPrimaryContainer else Color.Transparent)
                                .clickable { onSelect(id) }
                                .padding(horizontal = NexiqSpacing.md, vertical = NexiqSpacing.md),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) BrandPrimary else TextPrimary,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(NexiqSpacing.sm))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            }
        }
    }
}
