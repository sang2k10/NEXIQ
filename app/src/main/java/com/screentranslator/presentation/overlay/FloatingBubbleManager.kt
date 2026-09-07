package com.screentranslator.presentation.overlay

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.screentranslator.R
import com.screentranslator.presentation.permission.CapturePermissionActivity
import kotlin.math.abs

/**
 * Manages an optional floating trigger bubble on the screen perimeter.
 * Allows instant one-tap screen translation from any app.
 */
class FloatingBubbleManager(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var bubbleView: View? = null
    private var isBubbleShowing = false

    @SuppressLint("ClickableViewAccessibility")
    fun showBubble() {
        if (isBubbleShowing || bubbleView != null) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) return

        val bubbleSizePx = (52 * context.resources.displayMetrics.density).toInt()

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
            y = (context.resources.displayMetrics.heightPixels * 0.4f).toInt()
        }

        val imageView = ImageView(context).apply {
            setImageResource(R.drawable.ic_translate_tile)
            setBackgroundResource(R.drawable.bg_bubble)
            val pad = (10 * context.resources.displayMetrics.density).toInt()
            setPadding(pad, pad, pad, pad)
            elevation = 12 * context.resources.displayMetrics.density
        }

        imageView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var touchStartTime = 0L

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = layoutParams.x
                        initialY = layoutParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        touchStartTime = System.currentTimeMillis()
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        layoutParams.x = initialX + (event.rawX - initialTouchX).toInt()
                        layoutParams.y = initialY + (event.rawY - initialTouchY).toInt()
                        try {
                            windowManager.updateViewLayout(v, layoutParams)
                        } catch (_: Exception) {}
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        val duration = System.currentTimeMillis() - touchStartTime
                        val distMoved = abs(event.rawX - initialTouchX) + abs(event.rawY - initialTouchY)

                        if (duration < 250 && distMoved < 15) {
                            // Tap detected: Launch translation flow
                            v.performClick()
                            onBubbleClicked()
                        } else {
                            // Snap to nearest screen edge
                            snapToEdge(layoutParams, v)
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
        val intent = Intent(context, CapturePermissionActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(intent)
    }

    fun hideBubble() {
        bubbleView?.let { view ->
            try {
                windowManager.removeView(view)
            } catch (_: Exception) {}
            bubbleView = null
        }
        isBubbleShowing = false
    }
}
