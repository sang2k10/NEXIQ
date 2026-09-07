package com.screentranslator

import com.screentranslator.presentation.overlay.matrix.GestureTransformState
import org.junit.Assert.*
import org.junit.Test

class GestureTransformTest {

    @Test
    fun transform_panAndZoom_updatesStateProperly() {
        val state = GestureTransformState(initialScale = 0.9f)
        assertEquals(0.9f, state.scale, 0.001f)
        assertEquals(0f, state.offsetX, 0.001f)
        assertEquals(0f, state.offsetY, 0.001f)

        // Apply pan and zoom
        state.onTransform(panChangeX = 25f, panChangeY = -15f, zoomChange = 1.2f, rotationChange = 10f)
        assertEquals(0.9f * 1.2f, state.scale, 0.001f)
        assertEquals(25f, state.offsetX, 0.001f)
        assertEquals(-15f, state.offsetY, 0.001f)
        assertEquals(10f, state.rotationDegrees, 0.001f)
    }

    @Test
    fun transform_scaleClamping_respectsMinAndMaxLimits() {
        val state = GestureTransformState(initialScale = 1.0f)

        // Exceed maximum scale
        state.onTransform(panChangeX = 0f, panChangeY = 0f, zoomChange = 10.0f, rotationChange = 0f)
        assertEquals(6.0f, state.scale, 0.001f)

        // Below minimum scale
        state.onTransform(panChangeX = 0f, panChangeY = 0f, zoomChange = 0.01f, rotationChange = 0f)
        assertEquals(0.40f, state.scale, 0.001f)
    }

    @Test
    fun reset_restoresInitialState() {
        val state = GestureTransformState(initialScale = 0.9f)
        state.onTransform(panChangeX = 50f, panChangeY = 50f, zoomChange = 2f, rotationChange = 45f)
        state.reset(0.85f)

        assertEquals(0.85f, state.scale, 0.001f)
        assertEquals(0f, state.offsetX, 0.001f)
        assertEquals(0f, state.offsetY, 0.001f)
        assertEquals(0f, state.rotationDegrees, 0.001f)
    }
}
