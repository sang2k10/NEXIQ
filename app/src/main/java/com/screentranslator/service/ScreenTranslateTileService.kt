package com.screentranslator.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.screentranslator.core.util.TranslationSessionCoordinator
import com.screentranslator.presentation.permission.CapturePermissionActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Android Quick Settings Tile to trigger translation directly from status shade.
 * Uses active accessibility capture when available to bypass system permission prompts.
 */
@RequiresApi(Build.VERSION_CODES.N)
class ScreenTranslateTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        qsTile?.apply {
            state = Tile.STATE_INACTIVE
            updateTile()
        }
    }

    override fun onClick() {
        super.onClick()

        if (ScreenTranslatorAccessibilityService.isRunning()) {
            collapseShade()
            CoroutineScope(Dispatchers.Main).launch {
                // Short delay to let the status bar shade animate away before screenshot
                delay(350L)
                val captureResult = ScreenTranslatorAccessibilityService.captureScreenshot()
                captureResult.onSuccess { bitmap ->
                    TranslationSessionCoordinator.startSession(bitmap, this@ScreenTranslateTileService)
                }
            }
        } else {
            val intent = Intent(this, CapturePermissionActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                startActivityAndCollapse(pendingIntent)
            } else {
                @Suppress("DEPRECATION")
                startActivityAndCollapse(intent)
            }
        }
    }

    private fun collapseShade() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val dummyIntent = Intent(this, ScreenTranslateTileService::class.java)
            val pendingIntent = PendingIntent.getService(
                this, 0, dummyIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            startActivityAndCollapse(pendingIntent)
        } else {
            @Suppress("DEPRECATION")
            val closeIntent = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
            sendBroadcast(closeIntent)
        }
    }
}
