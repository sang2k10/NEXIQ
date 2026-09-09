package com.screentranslator.presentation.overlay

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import com.screentranslator.R
import com.screentranslator.ScreenTranslatorApp
import com.screentranslator.core.util.TranslationSessionCoordinator
import com.screentranslator.presentation.permission.CapturePermissionActivity
import com.screentranslator.service.ScreenCaptureService
import com.screentranslator.service.ScreenTranslatorAccessibilityService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.hypot

/**
 * Manages an optional floating trigger bubble on the screen perimeter.
 * Allows instant one-tap screen translation from any app.
 *
 * Features:
 * - 3-second idle dimming to 0.35 alpha.
 * - Snap-to-edge animation on release.
 * - Drag-to-dismiss bottom target (circle with 'X').
 * - Dynamic customizable bubble themes (Cyan, Indigo, Dark, Sunset).
 */
class FloatingBubbleManager(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val mainHandler = Handler(Looper.getMainLooper())
    private val container get() = ScreenTranslatorApp.instance.container

    private var bubbleView: View? = null
    private var dismissTargetView: View? = null
    private var bubbleLayoutParams: WindowManager.LayoutParams? = null
    private var isBubbleShowing = false
    private var isOverDismissTarget = false
    private var currentTheme: String = "cyan"
    private var currentIconStyle: String = "brand"
    private var currentSizeDp: Int = 52

    // Idle dimming runnable: fades bubble to 0.35 alpha after 3 seconds of inactivity
    private val idleDimRunnable = Runnable {
        bubbleView?.animate()
            ?.alpha(0.35f)
            ?.setDuration(350)
            ?.start()
    }

    @SuppressLint("ClickableViewAccessibility")
    fun showBubble() {
        if (isBubbleShowing || bubbleView != null) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) return

        val container = ScreenTranslatorApp.instance.container

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val settings = container.settingsRepository.getSettings()
                currentTheme = settings.bubbleTheme
                currentIconStyle = settings.bubbleIconStyle
                currentSizeDp = settings.bubbleSizeDp
            } catch (_: Exception) {}

            applyBubbleConfig()
        }
    }

    private fun applyBubbleConfig() {
        if (isBubbleShowing || bubbleView != null) return

        val bubbleSizePx = (currentSizeDp * context.resources.displayMetrics.density).toInt()

        val layoutParams = WindowManager.LayoutParams(
            bubbleSizePx,
            bubbleSizePx,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = (context.resources.displayMetrics.heightPixels * 0.35f).toInt()
        }
        bubbleLayoutParams = layoutParams

        val imageView = ImageView(context).apply {
            setImageResource(getThemeIconRes(currentTheme, currentIconStyle))
            setBackgroundResource(getThemeBackgroundRes(currentTheme))
            val pad = (8 * context.resources.displayMetrics.density).toInt()
            setPadding(pad, pad, pad, pad)
            elevation = 12 * context.resources.displayMetrics.density
            alpha = 1f
        }

        imageView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var touchStartTime = 0L
            private var isDragging = false

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        cancelIdleDimming()
                        v.animate().alpha(1f).scaleX(1.1f).scaleY(1.1f).setDuration(100).start()
                        initialX = layoutParams.x
                        initialY = layoutParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        touchStartTime = System.currentTimeMillis()
                        isDragging = false
                        isOverDismissTarget = false
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = event.rawX - initialTouchX
                        val deltaY = event.rawY - initialTouchY
                        if (!isDragging && (abs(deltaX) > 15 || abs(deltaY) > 15)) {
                            isDragging = true
                            showDismissTarget()
                        }

                        if (isDragging) {
                            layoutParams.x = initialX + deltaX.toInt()
                            layoutParams.y = initialY + deltaY.toInt()
                            try {
                                windowManager.updateViewLayout(v, layoutParams)
                            } catch (_: Exception) {}

                            checkDismissTargetCollision(event.rawX, event.rawY, v)
                        }
                        return true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        hideDismissTarget()

                        val duration = System.currentTimeMillis() - touchStartTime
                        val distMoved = abs(event.rawX - initialTouchX) + abs(event.rawY - initialTouchY)

                        if (isOverDismissTarget) {
                            // User dragged bubble into the dismiss circle: dismiss & disable
                            v.animate().scaleX(0f).scaleY(0f).alpha(0f).setDuration(150).withEndAction {
                                hideBubble()
                                CoroutineScope(Dispatchers.Main).launch {
                                    container.settingsRepository.updateSettings {
                                        it.copy(floatingButtonEnabled = false)
                                    }
                                }
                            }.start()
                        } else if (!isDragging && duration < 250 && distMoved < 20) {
                            // Tap detected: Launch translation flow
                            v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                            v.performClick()
                            onBubbleClicked()
                        } else {
                            // Drag ended: Snap to nearest screen edge
                            v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                            snapToEdge(layoutParams, v)
                            resetIdleDimming()
                        }
                        return true
                    }
                }
                return false
            }
        })

        try {
            windowManager.addView(imageView, layoutParams)
            bubbleView = imageView
            isBubbleShowing = true
            resetIdleDimming()
        } catch (_: Exception) {}
    }

    private fun getThemeBackgroundRes(theme: String): Int {
        return when (theme.lowercase()) {
            "indigo", "purple" -> R.drawable.bg_bubble_indigo
            "dark", "obsidian" -> R.drawable.bg_bubble_dark
            "pearl", "white", "light", "sunset", "coral", "rose" -> R.drawable.bg_bubble_pearl
            else -> R.drawable.bg_bubble_cyan
        }
    }

    fun getThemeIconRes(theme: String, iconStyle: String = "brand"): Int {
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

    fun updateTheme(theme: String) {
        currentTheme = theme
        (bubbleView as? ImageView)?.let { img ->
            img.setBackgroundResource(getThemeBackgroundRes(currentTheme))
            img.setImageResource(getThemeIconRes(currentTheme, currentIconStyle))
        }
    }

    fun updateIconStyle(iconStyle: String) {
        currentIconStyle = iconStyle
        (bubbleView as? ImageView)?.let { img ->
            img.setImageResource(getThemeIconRes(currentTheme, currentIconStyle))
        }
    }

    fun updateSize(sizeDp: Int) {
        currentSizeDp = sizeDp
        val sizePx = (sizeDp * context.resources.displayMetrics.density).toInt()
        bubbleLayoutParams?.let { params ->
            params.width = sizePx
            params.height = sizePx
            bubbleView?.let { view ->
                try {
                    windowManager.updateViewLayout(view, params)
                } catch (_: Exception) {}
            }
        }
    }

    private fun resetIdleDimming() {
        cancelIdleDimming()
        mainHandler.postDelayed(idleDimRunnable, 3000L)
    }

    private fun cancelIdleDimming() {
        mainHandler.removeCallbacks(idleDimRunnable)
    }

    private fun showDismissTarget() {
        if (dismissTargetView != null) return

        val sizePx = (64 * context.resources.displayMetrics.density).toInt()
        val params = WindowManager.LayoutParams(
            sizePx,
            sizePx,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            y = (48 * context.resources.displayMetrics.density).toInt()
        }

        val frame = FrameLayout(context).apply {
            setBackgroundResource(R.drawable.bg_bubble_dismiss)
            elevation = 20 * context.resources.displayMetrics.density
            scaleX = 0f
            scaleY = 0f
            alpha = 0f
        }

        val icon = ImageView(context).apply {
            setImageResource(R.drawable.ic_bubble_dismiss)
            val pad = (18 * context.resources.displayMetrics.density).toInt()
            setPadding(pad, pad, pad, pad)
        }
        frame.addView(icon, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))

        try {
            windowManager.addView(frame, params)
            dismissTargetView = frame
            frame.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(200)
                .setInterpolator(OvershootInterpolator())
                .start()
        } catch (_: Exception) {}
    }

    private fun checkDismissTargetCollision(rawX: Float, rawY: Float, bubble: View) {
        val target = dismissTargetView ?: return
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        // Dismiss target center coordinates
        val targetCenterX = screenWidth / 2f
        val targetCenterY = screenHeight - (48 * displayMetrics.density) - (target.height / 2f)

        val distance = hypot((rawX - targetCenterX).toDouble(), (rawY - targetCenterY).toDouble()).toFloat()
        val triggerDistance = 75 * displayMetrics.density

        if (distance < triggerDistance) {
            if (!isOverDismissTarget) {
                isOverDismissTarget = true
                vibrateTick()
                target.animate().scaleX(1.3f).scaleY(1.3f).setDuration(150).start()
                bubble.animate().scaleX(0.7f).scaleY(0.7f).setDuration(150).start()
            }
        } else {
            if (isOverDismissTarget) {
                isOverDismissTarget = false
                target.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start()
                bubble.animate().scaleX(1.1f).scaleY(1.1f).setDuration(150).start()
            }
        }
    }

    private fun hideDismissTarget() {
        val target = dismissTargetView ?: return
        target.animate()
            .scaleX(0f)
            .scaleY(0f)
            .alpha(0f)
            .setDuration(150)
            .withEndAction {
                try {
                    windowManager.removeView(target)
                } catch (_: Exception) {}
                dismissTargetView = null
            }
            .start()
    }

    private fun vibrateTick() {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(20)
            }
        } catch (_: Exception) {}
    }

    private fun snapToEdge(params: WindowManager.LayoutParams, view: View) {
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val bubbleWidth = view.width

        val targetX = if (params.x + bubbleWidth / 2 < screenWidth / 2) {
            0
        } else {
            screenWidth - bubbleWidth
        }

        val startX = params.x
        val animator = ValueAnimator.ofInt(startX, targetX).apply {
            duration = 200
            interpolator = DecelerateInterpolator()
            addUpdateListener { va ->
                params.x = va.animatedValue as Int
                try {
                    windowManager.updateViewLayout(view, params)
                } catch (_: Exception) {}
            }
        }
        animator.start()
    }

    private fun onBubbleClicked() {
        hideBubble()
        if (ScreenTranslatorAccessibilityService.isRunning()) {
            CoroutineScope(Dispatchers.Main).launch {
                pauseBackgroundMedia()
                val captureResult = ScreenTranslatorAccessibilityService.captureScreenshot()
                captureResult.onSuccess { bitmap ->
                    TranslationSessionCoordinator.startSession(bitmap, context)
                }.onFailure { error ->
                    Log.e("FloatingBubbleManager", "Accessibility screenshot failed, falling back to capture dialog", error)
                    val intent = Intent(context, CapturePermissionActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    }
                    context.startActivity(intent)
                }
            }
        } else {
            val intent = Intent(context, CapturePermissionActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
            }
            context.startActivity(intent)
        }
    }

    private fun pauseBackgroundMedia() {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
            val down = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE)
            audioManager.dispatchMediaKeyEvent(down)
            val up = KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE)
            audioManager.dispatchMediaKeyEvent(up)
        } catch (_: Exception) {}
    }

    fun hideBubble() {
        cancelIdleDimming()
        hideDismissTarget()
        bubbleView?.let { view ->
            try {
                windowManager.removeView(view)
            } catch (_: Exception) {}
            bubbleView = null
            bubbleLayoutParams = null
        }
        isBubbleShowing = false
    }
}
