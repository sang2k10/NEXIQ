package com.screentranslator.presentation.permission

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.screentranslator.ScreenTranslatorApp
import com.screentranslator.service.ScreenCaptureService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Transparent bridge activity to acquire MediaProjection user consent.
 */
class CapturePermissionActivity : ComponentActivity() {

    private var captureStarted = false

    private val captureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            captureStarted = true
            // Pause any running media (e.g. Douyin, TikTok) so playback does not resume upon activity finish
            pauseBackgroundMedia()

            val serviceIntent = ScreenCaptureService.createStartIntent(
                context = this,
                resultCode = result.resultCode,
                data = result.data!!
            )
            startForegroundService(serviceIntent)
        } else {
            // User cancelled or denied permission: immediately restore the floating bubble
            restoreBubbleIfNeeded()
        }
        finish()
        if (Build.VERSION.SDK_INT >= 34) {
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 34) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }

        // Immediately hide floating bubble so it is not visible or captured in the screenshot
        try {
            ScreenTranslatorApp.instance.container.floatingBubbleManager.hideBubble()
        } catch (_: Exception) {}

        val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        captureLauncher.launch(projectionManager.createScreenCaptureIntent())
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!captureStarted) {
            restoreBubbleIfNeeded()
        }
    }

    private fun restoreBubbleIfNeeded() {
        val container = ScreenTranslatorApp.instance.container
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val settings = container.settingsRepository.getSettings()
                if (settings.floatingButtonEnabled) {
                    container.floatingBubbleManager.showBubble()
                }
            } catch (_: Exception) {}
        }
    }

    private fun pauseBackgroundMedia() {
        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
            val down = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE)
            audioManager.dispatchMediaKeyEvent(down)
            val up = KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE)
            audioManager.dispatchMediaKeyEvent(up)
        } catch (_: Exception) {}
    }
}
