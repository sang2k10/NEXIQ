package com.screentranslator.service

import android.app.Activity
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import com.screentranslator.R
import com.screentranslator.ScreenTranslatorApp
import com.screentranslator.domain.model.TranslationSession
import com.screentranslator.presentation.permission.CapturePermissionActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Foreground Service for managing screen capture projection and translation session.
 */
class ScreenCaptureService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                ScreenTranslatorApp.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(ScreenTranslatorApp.NOTIFICATION_ID, notification)
        }

        when (intent?.action) {
            ACTION_START -> {
                val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED)
                val data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(EXTRA_DATA, Intent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(EXTRA_DATA)
                }

                if (resultCode == Activity.RESULT_OK && data != null) {
                    executeCapture(resultCode, data)
                }
            }
            ACTION_STOP -> {
                ScreenTranslatorApp.instance.container.sessionRepository.endSession()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    private fun executeCapture(resultCode: Int, data: Intent) {
        serviceScope.launch {
            val container = ScreenTranslatorApp.instance.container
            val settings = container.settingsRepository.getSettings()

            val captureResult = container.screenCaptureManager.captureScreen(resultCode, data)
            captureResult.onSuccess { bitmap ->
                val session = TranslationSession(
                    originalBitmap = bitmap,
                    workingBitmap = bitmap,
                    sourceLanguage = settings.defaultSourceLanguage,
                    targetLanguage = settings.defaultTargetLanguage,
                    isOcrRunning = settings.autoStartTranslation
                )
                container.sessionRepository.startSession(session)

                if (settings.hapticFeedback) {
                    triggerHapticFeedback()
                }

                // Show the interactive overlay
                container.overlayManager.showOverlay()

                // Auto-start OCR and translation pipeline
                if (settings.autoStartTranslation) {
                    container.executeOcrUseCase()
                    container.translateBlocksUseCase()
                }
            }.onFailure { error ->
                Log.e("ScreenCaptureService", "Failed to capture screen", error)
            }
        }
    }

    private fun triggerHapticFeedback() {
        try {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(50)
            }
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun createNotification(): Notification {
        val captureIntent = Intent(this, CapturePermissionActivity::class.java)
        val capturePendingIntent = PendingIntent.getActivity(
            this, 0, captureIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, ScreenCaptureService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, ScreenTranslatorApp.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_translate_tile)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .addAction(R.drawable.ic_translate_tile, getString(R.string.action_translate), capturePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.action_stop), stopPendingIntent)
            .build()
    }

    companion object {
        const val ACTION_START = "com.screentranslator.action.START"
        const val ACTION_STOP = "com.screentranslator.action.STOP"
        const val EXTRA_RESULT_CODE = "extra_result_code"
        const val EXTRA_DATA = "extra_data"

        fun createStartIntent(context: Context, resultCode: Int, data: Intent): Intent {
            return Intent(context, ScreenCaptureService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_RESULT_CODE, resultCode)
                putExtra(EXTRA_DATA, data)
            }
        }
    }
}
