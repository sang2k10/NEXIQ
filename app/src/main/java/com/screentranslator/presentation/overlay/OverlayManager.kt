package com.screentranslator.presentation.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.screentranslator.ScreenTranslatorApp
import com.screentranslator.domain.model.Language
import com.screentranslator.presentation.overlay.ui.SessionOverlayScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * OverlayManager attaches and manages the interactive Compose translation overlay
 * directly to WindowManager using TYPE_APPLICATION_OVERLAY.
 */
class OverlayManager(
    private val context: Context
) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: ComposeView? = null
    private var lifecycleOwner: OverlayLifecycleOwner? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun showOverlay() {
        if (overlayView != null) return

        // Temporarily hide floating bubble so it doesn't overlap the translation canvas
        ScreenTranslatorApp.instance.container.floatingBubbleManager.hideBubble()

        val sessionRepo = ScreenTranslatorApp.instance.container.sessionRepository
        val settingsRepo = ScreenTranslatorApp.instance.container.settingsRepository

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        val owner = OverlayLifecycleOwner().apply {
            performRestore(null)
            handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            handleLifecycleEvent(Lifecycle.Event.ON_START)
            handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }
        lifecycleOwner = owner

        val composeView = ComposeView(context).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)

            setContent {
                val session by sessionRepo.activeSession.collectAsState()
                val settings by settingsRepo.settingsFlow.collectAsState(
                    initial = com.screentranslator.domain.model.AppSettings()
                )

                session?.let { activeSession ->
                    SessionOverlayScreen(
                        session = activeSession,
                        overlayOpacity = settings.overlayOpacity,
                        initialScale = settings.initialScale,
                        onSourceLanguageChanged = { newSource ->
                            sessionRepo.updateSession { it.copy(sourceLanguage = newSource) }
                            scope.launch {
                                val container = ScreenTranslatorApp.instance.container
                                container.executeOcrUseCase()
                                container.translateBlocksUseCase()
                            }
                        },
                        onTargetLanguageChanged = { newTarget ->
                            sessionRepo.updateSession { it.copy(targetLanguage = newTarget) }
                            scope.launch {
                                ScreenTranslatorApp.instance.container.translateBlocksUseCase()
                            }
                        },
                        onToggleCropMode = {
                            sessionRepo.updateSession { it.copy(isCropMode = !it.isCropMode) }
                        },
                        onConfirmCrop = { pixelRect ->
                            scope.launch {
                                ScreenTranslatorApp.instance.container.retranslateCropUseCase(pixelRect)
                            }
                        },
                        onDismissError = {
                            sessionRepo.updateSession { it.copy(errorMessage = null) }
                        },
                        onCloseSession = {
                            hideOverlay()
                        }
                    )
                }
            }
        }

        overlayView = composeView
        windowManager.addView(composeView, layoutParams)
    }

    fun hideOverlay() {
        overlayView?.let { view ->
            try {
                windowManager.removeView(view)
            } catch (_: Exception) {}
            overlayView = null
        }
        lifecycleOwner?.let { owner ->
            owner.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            owner.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            owner.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            lifecycleOwner = null
        }
        ScreenTranslatorApp.instance.container.sessionRepository.endSession()

        // Deterministically stop foreground capture service and clear ongoing notification
        try {
            val stopIntent = android.content.Intent(context, com.screentranslator.service.ScreenCaptureService::class.java).apply {
                action = com.screentranslator.service.ScreenCaptureService.ACTION_STOP
            }
            context.startService(stopIntent)
        } catch (_: Exception) {}

        // Restore floating bubble if user has it enabled
        scope.launch {
            val settings = ScreenTranslatorApp.instance.container.settingsRepository.getSettings()
            if (settings.showFloatingButton) {
                ScreenTranslatorApp.instance.container.floatingBubbleManager.showBubble()
            }
        }
    }

    fun destroy() {
        hideOverlay()
        scope.cancel()
    }

    /**
     * Minimal Lifecycle and SavedState registry owner for WindowManager ComposeView.
     */
    private class OverlayLifecycleOwner : LifecycleOwner, SavedStateRegistryOwner {
        private val lifecycleRegistry = LifecycleRegistry(this)
        private val savedStateRegistryController = SavedStateRegistryController.create(this)

        override val lifecycle: Lifecycle get() = lifecycleRegistry
        override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

        fun performRestore(savedState: android.os.Bundle?) {
            savedStateRegistryController.performRestore(savedState)
        }

        fun handleLifecycleEvent(event: Lifecycle.Event) {
            lifecycleRegistry.handleLifecycleEvent(event)
        }
    }
}
