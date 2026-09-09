package com.screentranslator.presentation.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * NEXIQ Shape System — Restrained and Technical
 * Avoids blanket 16dp-20dp bubbly cards.
 */
val NexiqShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),   // Badges, micro-indicators
    small = RoundedCornerShape(8.dp),        // Interactive buttons, chips, text inputs
    medium = RoundedCornerShape(10.dp),      // Sub-containers, option rows, cards
    large = RoundedCornerShape(14.dp),       // Primary containers, grouped lists
    extraLarge = RoundedCornerShape(20.dp)   // Dialogs, bottom sheets
)

val ControlShape = RoundedCornerShape(8.dp)
val ContainerShape = RoundedCornerShape(12.dp)
val DialogShape = RoundedCornerShape(20.dp)
