package com.screentranslator.presentation.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import android.content.Intent
import android.net.Uri
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

    // Dialog States
    var languagePickerTarget by remember { mutableStateOf<LanguagePickerType?>(null) }
    var enginePickerTarget by remember { mutableStateOf<EnginePickerType?>(null) }
    var showRestrictedHelp by remember { mutableStateOf(false) }
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BrandPrimary,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(R.drawable.ic_bubble_translate),
                                    contentDescription = "NEXIQ Brand Logo",
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "NEXIQ",
                                style = MaterialTheme.typography.headlineSmall,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Understand what's on your screen.",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceBase
                )
            )
        },
        containerColor = SurfaceBase
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // 1. User Guide / Demo Video Card (Replaced hero "Translate Screen Now" per user request)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceContainer,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showVideoGuideDialog = true }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = BrandAccentMuted
                                ) {
                                    Text(
                                        text = "USER GUIDE",
                                        color = BrandAccent,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = "30s Demo",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "How NEXIQ Works",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Tap to watch a visual guide on using the floating bubble & in-place translation.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = BrandAccent,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Watch Demo",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    if (!hasOverlayPermission) {
                        Button(
                            onClick = onGrantOverlayPermission,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandAccent),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Layers, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Grant 'Display Over Other Apps' Permission", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }

            // 2. Hardware Screenshot Status Card (Fixed "RECOMMENDED" text wrapping)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isAccessibilityEnabled) SuccessGreenContainer else SurfaceContainer,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isAccessibilityEnabled) SuccessGreen.copy(alpha = 0.5f) else BorderSubtle
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isAccessibilityEnabled) SuccessGreen.copy(alpha = 0.2f) else BorderMedium,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isAccessibilityEnabled) Icons.Default.CheckCircle else Icons.Outlined.CameraAlt,
                                    contentDescription = null,
                                    tint = if (isAccessibilityEnabled) SuccessGreen else TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Instant Screenshot Access",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isAccessibilityEnabled) SuccessGreen else BrandAccent,
                                    modifier = Modifier.size(7.dp)
                                ) {}
                                Text(
                                    text = if (isAccessibilityEnabled) "Active (Static screenshots ready)" else "Setup Recommended (One-Time)",
                                    color = if (isAccessibilityEnabled) SuccessGreen else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isAccessibilityEnabled) {
                            "Captures clean static screenshots directly without system recording prompts or video stutter."
                        } else {
                            "Grant once in Accessibility Settings to take static screenshots without system screen recording prompts."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    if (!isAccessibilityEnabled) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onOpenAccessibilitySettings,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open Accessibility Settings", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Progressive disclosure for Android 13+ restricted settings
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showRestrictedHelp = !showRestrictedHelp }
                                .padding(vertical = 4.dp, horizontal = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Outlined.HelpOutline, contentDescription = null, tint = BrandAccent, modifier = Modifier.size(14.dp))
                                Text(
                                    text = "Phone blocks permission ('Restricted setting')?",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BrandAccent,
                                    fontSize = 12.sp
                                )
                            }
                            Icon(
                                imageVector = if (showRestrictedHelp) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = BrandAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        AnimatedVisibility(
                            visible = showRestrictedHelp,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SurfaceCardSubtle,
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Android blocks accessibility for sideloaded apps. Unlock it in 3 steps:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "1. Tap 'Open App Info' below\n2. Tap the ⋮ (three dots) in top-right corner\n3. Tap 'Allow restricted settings' and enter your PIN\n4. Return here and grant the permission",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimary,
                                        lineHeight = 18.sp,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedButton(
                                        onClick = onOpenAppDetails,
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandPrimaryLighter),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderMedium),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(36.dp)
                                    ) {
                                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Open App Info", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Section: Translation & Languages
            SectionHeader(title = "TRANSLATION & RECOGNITION")

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceContainer,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    SettingNavigationRow(
                        title = "Default Source Language",
                        subtitle = settings.defaultSourceLanguage.displayName,
                        leadingIcon = Icons.Outlined.Language,
                        onClick = { languagePickerTarget = LanguagePickerType.SOURCE }
                    )

                    HorizontalDivider(color = BorderSubtle, modifier = Modifier.padding(horizontal = 16.dp))

                    SettingNavigationRow(
                        title = "Default Target Language",
                        subtitle = settings.defaultTargetLanguage.displayName,
                        leadingIcon = Icons.Outlined.Translate,
                        onClick = { languagePickerTarget = LanguagePickerType.TARGET }
                    )

                    HorizontalDivider(color = BorderSubtle, modifier = Modifier.padding(horizontal = 16.dp))

                    SettingNavigationRow(
                        title = "Translation Engine",
                        subtitle = getTranslationEngineName(settings.selectedTranslationEngineId),
                        leadingIcon = Icons.Outlined.AutoAwesome,
                        onClick = { enginePickerTarget = EnginePickerType.TRANSLATION }
                    )

                    HorizontalDivider(color = BorderSubtle, modifier = Modifier.padding(horizontal = 16.dp))

                    SettingNavigationRow(
                        title = "OCR Recognition Engine",
                        subtitle = getOcrEngineName(settings.selectedOcrEngineId),
                        leadingIcon = Icons.Outlined.DocumentScanner,
                        onClick = { enginePickerTarget = EnginePickerType.OCR }
                    )
                }
            }

            // 4. Section: Interface & Display (Includes Theme Mode: System / Light / Dark)
            SectionHeader(title = "INTERFACE & DISPLAY")

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceContainer,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Theme Mode Selector
                    Column {
                        Text("App Color Theme", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Choose between Light, Dark, or System Default", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "system" to "System",
                                "light" to "Light",
                                "dark" to "Dark"
                            ).forEach { (mode, label) ->
                                val isSelected = settings.themeMode.equals(mode, ignoreCase = true)
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) BrandPrimaryMuted else SurfaceCardSubtle,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) BrandPrimary else BorderSubtle
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onUpdateThemeMode(mode) }
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(vertical = 10.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (isSelected) BrandPrimary else TextSecondary,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = BorderSubtle)

                    // Overlay Background Opacity
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Overlay Dimming", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SurfaceCardSubtle,
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                            ) {
                                Text(
                                    text = "${(settings.overlayOpacity * 100).toInt()}%",
                                    color = BrandPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Slider(
                            value = settings.overlayOpacity,
                            onValueChange = onUpdateOverlayOpacity,
                            valueRange = 0.5f..1.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = BrandPrimary,
                                activeTrackColor = BrandPrimary,
                                inactiveTrackColor = BorderMedium
                            )
                        )
                    }

                    HorizontalDivider(color = BorderSubtle)

                    // Initial Scale
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Initial Image Scale", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SurfaceCardSubtle,
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                            ) {
                                Text(
                                    text = "${(settings.initialScale * 100).toInt()}%",
                                    color = BrandPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Slider(
                            value = settings.initialScale,
                            onValueChange = onUpdateInitialScale,
                            valueRange = 0.7f..1.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = BrandPrimary,
                                activeTrackColor = BrandPrimary,
                                inactiveTrackColor = BorderMedium
                            )
                        )
                    }
                }
            }

            // 5. Section: Floating Bubble (Theme, Size, Icon Style)
            SectionHeader(title = "FLOATING BUBBLE & SHORTCUTS")

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceContainer,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SettingToggleRow(
                        title = "Floating Shortcut Bubble",
                        subtitle = "Persistent trigger on screen edge for one-tap translation",
                        checked = settings.floatingButtonEnabled,
                        onCheckedChange = onToggleFloatingButton
                    )

                    if (settings.floatingButtonEnabled) {
                        HorizontalDivider(color = BorderSubtle)

                        // 1. Bubble Color Theme (Synchronized preview!)
                        BubbleThemeSelector(
                            selectedTheme = settings.bubbleTheme,
                            selectedIconStyle = settings.bubbleIconStyle,
                            onSelectTheme = onUpdateBubbleTheme
                        )

                        HorizontalDivider(color = BorderSubtle)

                        // 2. Bubble Icon Style (Simpler / Alternative options)
                        BubbleIconStyleSelector(
                            selectedIconStyle = settings.bubbleIconStyle,
                            selectedTheme = settings.bubbleTheme,
                            onSelectIconStyle = onUpdateBubbleIconStyle
                        )

                        HorizontalDivider(color = BorderSubtle)

                        // 3. Bubble Size Customization Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Bubble Size", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SurfaceCardSubtle,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                                ) {
                                    Text(
                                        text = "${settings.bubbleSizeDp} dp",
                                        color = BrandPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Slider(
                                value = settings.bubbleSizeDp.toFloat(),
                                onValueChange = { onUpdateBubbleSize(it.toInt()) },
                                valueRange = 40f..64f,
                                steps = 11,
                                colors = SliderDefaults.colors(
                                    thumbColor = BrandPrimary,
                                    activeTrackColor = BrandPrimary,
                                    inactiveTrackColor = BorderMedium
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        OutlinedButton(
                            onClick = onReopenBubble,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandPrimary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderMedium),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reopen Bubble on Screen", fontSize = 13.sp)
                        }
                    }

                    HorizontalDivider(color = BorderSubtle)

                    SettingToggleRow(
                        title = "Auto-start Translation",
                        subtitle = "Run OCR and translation immediately upon capture",
                        checked = settings.autoStartTranslation,
                        onCheckedChange = onToggleAutoStart
                    )

                    HorizontalDivider(color = BorderSubtle)

                    SettingToggleRow(
                        title = "Remember Language Pair",
                        subtitle = "Preserve the last used language selection across app sessions",
                        checked = settings.rememberLastLanguagePair,
                        onCheckedChange = onToggleRememberPair
                    )

                    HorizontalDivider(color = BorderSubtle)

                    SettingToggleRow(
                        title = "Haptic Vibration",
                        subtitle = "Subtle tactile feedback on capture and completion",
                        checked = settings.hapticFeedback,
                        onCheckedChange = onToggleHaptic
                    )
                }
            }

            // 6. Section: Privacy & Storage
            SectionHeader(title = "PRIVACY & STORAGE")

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceContainer,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SuccessGreen.copy(alpha = 0.15f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                            }
                        }
                        Column {
                            Text("Private by Design & Transparent Processing", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            Text(
                                text = "Screen content is processed in transient memory during translation sessions. No background tracking, profiling, or telemetry.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    HorizontalDivider(color = BorderSubtle)

                    // Button to review welcome guide & privacy disclosures anytime
                    OutlinedButton(
                        onClick = { showOnboardingDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderMedium),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    ) {
                        Icon(Icons.Outlined.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Review Privacy Disclosures & Guide", fontSize = 13.sp)
                    }

                    HorizontalDivider(color = BorderSubtle)

                    SettingToggleRow(
                        title = "Save Translation History",
                        subtitle = "Store text translation queries locally on this device",
                        checked = settings.saveHistory,
                        onCheckedChange = onToggleHistory
                    )
                }
            }

            // Quick Access Tips
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = SurfaceBase,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Outlined.TipsAndUpdates, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(16.dp))
                        Text("Quick Access Tips", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Quick Settings Tile: Pull down status shade and tap 'Edit' to add the 'Translate Screen' tile.\n• Notification Shortcut: Use the persistent service notification to trigger translation from any app.\n• Floating Bubble: Keep a draggable shortcut floating at screen edge for single-tap translation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 18.sp,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Modal Dialog for Demo Video Walkthrough
    if (showVideoGuideDialog) {
        DemoVideoGuideDialog(
            onDismiss = { showVideoGuideDialog = false }
        )
    }

    // Modal Dialog for Onboarding & Privacy Guarantee (First Launch + Reviewable)
    if (showOnboardingDialog) {
        OnboardingPrivacyDialog(
            onAccept = {
                onAcceptOnboarding()
                showOnboardingDialog = false
            },
            onDismiss = { showOnboardingDialog = false }
        )
    }

    // Modal Dialog for Language Picker
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

    // Modal Dialog for Engine Picker
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
                "mlkit" to "Google ML Kit (Latin, Japanese, Chinese, Korean)",
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

    if (showCloudConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showCloudConfirmationDialog = false },
            title = {
                Text("Enable Cloud Translation?", fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Selecting Experimental Cloud Translation sends extracted text over HTTPS to Google Translate web servers.",
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
            containerColor = SurfaceContainer
        )
    }
}

private enum class LanguagePickerType { SOURCE, TARGET }
private enum class EnginePickerType { TRANSLATION, OCR }

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = TextTertiary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
    )
}

@Composable
private fun SettingNavigationRow(
    title: String,
    subtitle: String,
    leadingIcon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = BrandPrimary,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(text = title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, fontSize = 12.sp)
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SettingToggleRow(
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
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = BrandPrimary,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = BorderMedium
            )
        )
    }
}

// 1. Bubble Theme Selector (Synchronized with exact vector drawables!)
@Composable
private fun BubbleThemeSelector(
    selectedTheme: String,
    selectedIconStyle: String,
    onSelectTheme: (String) -> Unit
) {
    val themes = listOf(
        ThemeItem("cyan", "Brand Azure", Color(0xFF0284C7), Color(0xFF38BDF8)),
        ThemeItem("dark", "Obsidian Dark", Color(0xFF1E293B), Color(0xFF64748B)),
        ThemeItem("pearl", "Pearl Light", Color(0xFFF8FAFC), Color(0xFFCBD5E1)),
        ThemeItem("indigo", "Indigo Royale", Color(0xFF4F46E5), Color(0xFF818CF8))
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Bubble Color Theme",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Matches your device wallpaper and preference",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            themes.forEach { item ->
                val isSelected = selectedTheme.equals(item.id, ignoreCase = true) ||
                        (selectedTheme.equals("sunset", ignoreCase = true) && item.id == "pearl")
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) BrandPrimaryMuted else SurfaceCardSubtle,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) BrandPrimary else BorderSubtle
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectTheme(item.id) }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = item.color,
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, item.ringColor),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                val iconRes = getBubbleDrawableRes(item.id, selectedIconStyle)
                                Image(
                                    painter = painterResource(iconRes),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Text(
                            text = item.label,
                            color = if (isSelected) TextPrimary else TextSecondary,
                            fontSize = 10.sp,
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

// 2. Bubble Icon Style Selector (Simpler & alternative options)
@Composable
private fun BubbleIconStyleSelector(
    selectedIconStyle: String,
    selectedTheme: String,
    onSelectIconStyle: (String) -> Unit
) {
    val styles = listOf(
        IconStyleItem("brand", "NEXIQ Logo", R.drawable.ic_bubble_translate),
        IconStyleItem("lens", "Minimal Lens", R.drawable.ic_bubble_lens),
        IconStyleItem("glyph", "Translate", R.drawable.ic_bubble_glyph),
        IconStyleItem("aperture", "Aperture", R.drawable.ic_bubble_aperture)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Bubble Icon Style",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Choose a simpler or brand-aligned icon inside the bubble",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            styles.forEach { item ->
                val isSelected = selectedIconStyle.equals(item.id, ignoreCase = true)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) BrandPrimaryMuted else SurfaceCardSubtle,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) BrandPrimary else BorderSubtle
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectIconStyle(item.id) }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = BrandPrimary,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(item.drawableRes),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = item.label,
                            color = if (isSelected) TextPrimary else TextSecondary,
                            fontSize = 10.sp,
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

private fun getBubbleDrawableRes(theme: String, iconStyle: String): Int {
    val isDark = when (theme.lowercase()) {
        "pearl", "white", "light", "sunset", "coral", "rose" -> true
        else -> false
    }
    return when (iconStyle.lowercase()) {
        "lens", "search" -> if (isDark) R.drawable.ic_bubble_lens_dark else R.drawable.ic_bubble_lens
        "glyph", "text", "compact" -> if (isDark) R.drawable.ic_bubble_glyph_dark else R.drawable.ic_bubble_glyph
        "aperture", "ring", "dot" -> if (isDark) R.drawable.ic_bubble_aperture_dark else R.drawable.ic_bubble_aperture
        else -> if (isDark) R.drawable.ic_bubble_translate_dark else R.drawable.ic_bubble_translate
    }
}

// User Guide / Demo Dialog Placeholder
@Composable
private fun DemoVideoGuideDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SurfaceContainer,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = BrandPrimaryMuted,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.PlayCircleOutline, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(22.dp))
                        }
                    }
                    Column {
                        Text("How NEXIQ Works", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("30-Second Quick Walkthrough", style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontSize = 11.sp)
                    }
                }

                // Video Player Simulated Frame
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F172A),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderMedium),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Play Video",
                                tint = BrandPrimaryLighter,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Demo video will load here",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // 3-step guide steps
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    GuideStepRow("1", "Tap the Floating Bubble", "Keep the bubble on screen perimeter to trigger instant translation anytime.")
                    GuideStepRow("2", "Automatic In-Place Translation", "Foreign text is recognized via ML Kit and replaced cleanly in-place.")
                    GuideStepRow("3", "Crop & Save Controls", "Use the Scissors tool to crop specific areas or save the translated screenshot.")
                }

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Got It", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun GuideStepRow(step: String, title: String, description: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = BrandPrimaryMuted,
            modifier = Modifier.size(22.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(step, color = BrandPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
        Column {
            Text(title, style = MaterialTheme.typography.labelMedium, color = TextPrimary, fontSize = 13.sp)
            Text(description, style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
        }
    }
}

// First-Launch Onboarding & Transparent Privacy Disclosure Dialog
@Composable
private fun OnboardingPrivacyDialog(
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SurfaceContainer,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header & Brand
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = BrandPrimary,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(R.drawable.ic_bubble_translate),
                                contentDescription = "NEXIQ Brand Symbol",
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Translate what's on your screen.",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Instantly recognize and translate visible text without leaving the app you're using.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "NEXIQ recognizes text directly from your screen and places the translation back where the original text appears — without interrupting what you are doing.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }

                HorizontalDivider(color = BorderSubtle)

                // HOW IT WORKS
                Text(
                    text = "HOW IT WORKS",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FeatureHighlightRow(
                        icon = Icons.Outlined.FitScreen,
                        title = "Screen Translation",
                        description = "Capture and translate visible text while you continue using your phone."
                    )
                    FeatureHighlightRow(
                        icon = Icons.Outlined.Memory,
                        title = "On-Device Intelligence",
                        description = "NEXIQ uses on-device ML Kit OCR and translation when available, keeping recognized content on your device for that processing path."
                    )
                }

                // YOUR PRIVACY, CLEARLY EXPLAINED (Honest, implementation-backed)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceCardSubtle,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.Security,
                                contentDescription = null,
                                tint = BrandPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Your privacy, clearly explained",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            text = "Screen content is processed in memory for translation sessions. Optional cloud translation may send recognized text to the selected translation provider. Images are only saved to shared storage when you explicitly choose to save them.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                        Text(
                            text = "No hidden collection: NEXIQ does not intentionally collect advertising profiles or sell your screen content.",
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                // TRANSPARENCY & GITHUB LINK
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Review the source code and project documentation on GitHub.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/sang2k10/NEXIQ")).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        contentPadding = PaddingValues(vertical = 10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Outlined.Code,
                            contentDescription = "View NEXIQ source code on GitHub",
                            tint = BrandPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "View source on GitHub",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }

                // PRIMARY ACTION
                Button(
                    onClick = onAccept,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Get Started", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun FeatureHighlightRow(icon: ImageVector, title: String, description: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = BrandPrimaryMuted,
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(18.dp))
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(description, style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
        }
    }
}

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
            shape = RoundedCornerShape(18.dp),
            color = SurfaceContainer,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 480.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = BorderSubtle)

                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(languages) { lang ->
                        val isSelected = lang.code == currentLanguage.code
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) BrandPrimaryMuted else Color.Transparent)
                                .clickable { onSelect(lang) }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
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

                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            }
        }
    }
}

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
            shape = RoundedCornerShape(18.dp),
            color = SurfaceContainer,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = BorderSubtle)

                Column(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    options.forEach { (id, label) ->
                        val isSelected = id == selectedId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) BrandPrimaryMuted else Color.Transparent)
                                .clickable { onSelect(id) }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
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

                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            }
        }
    }
}

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
