package com.screentranslator.presentation.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.screentranslator.ScreenTranslatorApp
import com.screentranslator.core.util.TranslationSessionCoordinator
import com.screentranslator.presentation.permission.CapturePermissionActivity
import com.screentranslator.presentation.ui.theme.ScreenTranslatorTheme
import com.screentranslator.service.ScreenTranslatorAccessibilityService
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModel.provideFactory(ScreenTranslatorApp.instance.container.settingsRepository)
    }

    private var hasOverlayPermission by mutableStateOf(false)
    private var isAccessibilityEnabled by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updatePermissionStates()

        setContent {
            val settings by viewModel.settings.collectAsState()

            val isDarkTheme = when (settings.themeMode.lowercase()) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            ScreenTranslatorTheme(darkTheme = isDarkTheme) {
                LaunchedEffect(settings.showFloatingButton, hasOverlayPermission) {
                    if (settings.showFloatingButton && hasOverlayPermission) {
                        ScreenTranslatorApp.instance.container.floatingBubbleManager.showBubble()
                    } else {
                        ScreenTranslatorApp.instance.container.floatingBubbleManager.hideBubble()
                    }
                }

                LaunchedEffect(settings.bubbleSizeDp) {
                    ScreenTranslatorApp.instance.container.floatingBubbleManager.updateSize(settings.bubbleSizeDp)
                }

                LaunchedEffect(settings.bubbleIconStyle) {
                    ScreenTranslatorApp.instance.container.floatingBubbleManager.updateIconStyle(settings.bubbleIconStyle)
                }

                SettingsScreen(
                    settings = settings,
                    hasOverlayPermission = hasOverlayPermission,
                    onGrantOverlayPermission = { requestOverlayPermission() },
                    onStartTranslateNow = { startTranslationFlow() },
                    onUpdateSourceLanguage = { viewModel.setDefaultSourceLanguage(it) },
                    onUpdateTargetLanguage = { viewModel.setDefaultTargetLanguage(it) },
                    onUpdateOcrEngine = { viewModel.setSelectedOcrEngine(it) },
                    onUpdateTranslationEngine = { viewModel.setSelectedTranslationEngine(it) },
                    onUpdateOverlayOpacity = { viewModel.setOverlayOpacity(it) },
                    onUpdateInitialScale = { viewModel.setInitialScale(it) },
                    onToggleAutoStart = { viewModel.setAutoStartTranslation(it) },
                    onToggleRememberPair = { viewModel.setRememberLastLanguagePair(it) },
                    onToggleHaptic = { viewModel.setHapticFeedback(it) },
                    onToggleFloatingButton = { viewModel.setFloatingButtonEnabled(it) },
                    onToggleHistory = { viewModel.setSaveHistory(it) },
                    onUpdateBubbleTheme = { theme ->
                        viewModel.setBubbleTheme(theme)
                        ScreenTranslatorApp.instance.container.floatingBubbleManager.updateTheme(theme)
                    },
                    onUpdateBubbleSize = { size ->
                        viewModel.setBubbleSize(size)
                        ScreenTranslatorApp.instance.container.floatingBubbleManager.updateSize(size)
                    },
                    onUpdateBubbleIconStyle = { style ->
                        viewModel.setBubbleIconStyle(style)
                        ScreenTranslatorApp.instance.container.floatingBubbleManager.updateIconStyle(style)
                    },
                    onUpdateThemeMode = { mode ->
                        viewModel.setThemeMode(mode)
                    },
                    onAcceptOnboarding = {
                        viewModel.setHasAcceptedOnboarding(true)
                    },
                    onReopenBubble = {
                        viewModel.setFloatingButtonEnabled(true)
                        if (hasOverlayPermission) {
                            ScreenTranslatorApp.instance.container.floatingBubbleManager.showBubble()
                        } else {
                            requestOverlayPermission()
                        }
                    },
                    isAccessibilityEnabled = isAccessibilityEnabled,
                    onOpenAccessibilitySettings = {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(intent)
                    },
                    onOpenAppDetails = {
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:$packageName")
                        ).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(intent)
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStates()
    }

    private fun updatePermissionStates() {
        hasOverlayPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
        isAccessibilityEnabled = ScreenTranslatorAccessibilityService.isRunning()
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }
    }

    private fun startTranslationFlow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            requestOverlayPermission()
            return
        }

        if (ScreenTranslatorAccessibilityService.isRunning()) {
            lifecycleScope.launch {
                val captureResult = ScreenTranslatorAccessibilityService.captureScreenshot()
                captureResult.onSuccess { bitmap ->
                    TranslationSessionCoordinator.startSession(bitmap, this@MainActivity)
                }.onFailure {
                    val intent = Intent(this@MainActivity, CapturePermissionActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(intent)
                }
            }
        } else {
            val intent = Intent(this, CapturePermissionActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        }
    }
}
