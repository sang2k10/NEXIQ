package com.screentranslator.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.screentranslator.presentation.ui.theme.*

/**
 * Clean, technical section header.
 * Uses typographic scale, generous top spacing, and subtle tracking instead of card containers.
 */
@Composable
fun NexiqSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = TextTertiary,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = NexiqSpacing.xxl, bottom = NexiqSpacing.sm, start = NexiqSpacing.xs)
    )
}

/**
 * Native Android list row for settings.
 * Secondary icons remain muted neutral; brand color is reserved for active selection.
 */
@Composable
fun NexiqSettingRow(
    title: String,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    trailingText: String? = null,
    trailingTextColor: Color = TextSecondary,
    showChevron: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(),
            onClick = onClick
        )
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(clickableModifier)
            .padding(horizontal = NexiqSpacing.xs, vertical = NexiqSpacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(NexiqSpacing.md))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(NexiqSpacing.xxs))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        if (!trailingText.isNullOrBlank()) {
            Spacer(modifier = Modifier.width(NexiqSpacing.sm))
            Text(
                text = trailingText,
                style = MaterialTheme.typography.bodyMedium,
                color = trailingTextColor,
                fontSize = 13.sp,
                fontWeight = if (trailingTextColor != TextSecondary) FontWeight.Medium else FontWeight.Normal
            )
        }

        if (showChevron) {
            Spacer(modifier = Modifier.width(NexiqSpacing.xs))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Clean Android list row with a native Switch.
 */
@Composable
fun NexiqSwitchRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    leadingIcon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = NexiqSpacing.xs, vertical = NexiqSpacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(NexiqSpacing.md))
        }

        Column(modifier = Modifier.weight(1f).padding(end = NexiqSpacing.md)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(NexiqSpacing.xxs))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = BrandPrimary,
                uncheckedThumbColor = TextTertiary,
                uncheckedTrackColor = BorderMedium
            )
        )
    }
}

/**
 * Quiet slider row without massive synthesizer badges.
 * Value text is rendered quietly next to the title.
 */
@Composable
fun NexiqSliderRow(
    title: String,
    valueText: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = NexiqSpacing.xs, vertical = NexiqSpacing.sm)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Text(
                text = valueText,
                style = MaterialTheme.typography.bodySmall,
                color = BrandPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (!subtitle.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(NexiqSpacing.xxs))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = BrandPrimary,
                activeTrackColor = BrandPrimary,
                inactiveTrackColor = BorderMedium
            ),
            modifier = Modifier.padding(top = NexiqSpacing.xs)
        )
    }
}

/**
 * Informative system status banner.
 * Calm, technical, and restrained. Avoids stacking green-on-green boxes.
 * Uses neutral surface with one strong status signal (✓ Active).
 */
@Composable
fun NexiqStatusBanner(
    title: String,
    description: String,
    isActive: Boolean,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = ContainerShape,
        color = SurfaceElevated,
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(NexiqSpacing.lg)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(NexiqSpacing.md)
            ) {
                // Single clear status dot / indicator
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isActive) SuccessGreen else TextTertiary)
                )

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = if (isActive) "✓ Active" else "Inactive",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isActive) SuccessGreen else TextTertiary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(NexiqSpacing.xs))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(start = NexiqSpacing.lg)
            )

            if (!isActive && actionText != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(NexiqSpacing.md))
                Button(
                    onClick = onActionClick,
                    shape = ControlShape,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    contentPadding = PaddingValues(horizontal = NexiqSpacing.md, vertical = NexiqSpacing.sm),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = NexiqSpacing.lg)
                ) {
                    Text(
                        text = actionText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Hairline subtle divider for clean list grouping.
 */
@Composable
fun NexiqDivider(
    modifier: Modifier = Modifier
) {
    HorizontalDivider(
        color = BorderSubtle,
        thickness = 1.dp,
        modifier = modifier.padding(vertical = NexiqSpacing.xs)
    )
}
