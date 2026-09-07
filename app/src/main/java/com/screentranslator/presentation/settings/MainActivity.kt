package com.screentranslator.presentation.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.screentranslator.ScreenTranslatorApp
import com.screentranslator.presentation.permission.CapturePermissionActivity

class MainActivity : ComponentActivity() {

    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModel.provideFactory(ScreenTranslatorApp.instance.container.settingsRepository)
    }

    private var hasOverlayPermission by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateOverlayPermissionState()

        setContent {
            val settings by viewModel.settings.collectAsState()

            androidx.compose.runtime.LaunchedEffect(settings.showFloatingButton, hasOverlayPermission) {
                if (settings.showFloatingButton && hasOverlayPermission) {
                    ScreenTranslatorApp.instance.container.floatingBubbleManager.showBubble()
                } else {
                    ScreenTranslatorApp.instance.container.floatingBubbleManager.hideBubble()
                }
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
                onToggleHistory = { viewModel.setSaveHistory(it) }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        updateOverlayPermissionState()
    }

    private fun updateOverlayPermissionState() {
        hasOverlayPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
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
        val intent = Intent(this, CapturePermissionActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }
}
