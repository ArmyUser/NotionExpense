package it.speses22.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.speses22.app.ui.theme.Spese
import it.speses22.app.ui.theme.SpeseMotion
import it.speses22.app.ui.theme.SpeseShapes


/**
 * Micro-scale feedback driven straight off the interaction source.
 *
 * The scale is read inside [graphicsLayer]'s lambda so a press only triggers a
 * redraw, never a recomposition or relayout.
 */
@Composable
fun Modifier.pressScale(
    interactionSource: InteractionSource,
    pressedScale: Float = 0.96f
): Modifier {

    val pressed by interactionSource.collectIsPressedAsState()

    val scale = animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = SpeseMotion.Press,
        label = "pressScale"
    )

    return this.graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }
}


/**
 * The single haptic used across the flow: a short, light tick.
 *
 * Kept in one place so the keypad, the card actions and the category rows all
 * feel identical, and so the feedback type is changed in exactly one spot.
 */
@Composable
private fun rememberHapticTick(): () -> Unit {

    val haptic = LocalHapticFeedback.current

    return remember(haptic) {
        { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) }
    }
}


enum class SpeseButtonStyle {
    Primary,
    Secondary
}


@Composable
fun SpeseButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: SpeseButtonStyle = SpeseButtonStyle.Primary,
    enabled: Boolean = true
) {

    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val tick = rememberHapticTick()

    val targetContainer = when {
        !enabled -> Spese.Disabled

        style == SpeseButtonStyle.Primary ->
            if (pressed) Spese.AccentPressed else Spese.Accent

        else ->
            if (pressed) Spese.SurfaceSunkenPressed else Spese.SurfaceSunken
    }

    val targetContent = when {
        !enabled -> Spese.OnDisabled
        style == SpeseButtonStyle.Primary -> Spese.OnAccent
        else -> Spese.TextPrimary
    }

    val container by animateColorAsState(
        targetValue = targetContainer,
        animationSpec = tween(SpeseMotion.PressMillis),
        label = "buttonContainer"
    )

    val content by animateColorAsState(
        targetValue = targetContent,
        animationSpec = tween(SpeseMotion.PressMillis),
        label = "buttonContent"
    )

    Box(
        modifier = modifier
            .heightIn(min = 52.dp)
            .pressScale(interactionSource)
            .clip(SpeseShapes.Control)
            .background(container)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = {
                    tick()
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {

        // Crossfades "NEXT" -> "DONE" without moving the button.
        AnimatedContent(
            targetState = text,
            transitionSpec = {
                fadeIn(tween(140)) togetherWith fadeOut(tween(100))
            },
            label = "buttonLabel"
        ) { label ->

            Text(
                text = label,
                color = content,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.3.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}


/**
 * A flat, tactile surface used for numpad keys and pickers: colour flash plus a
 * micro-scale, no ripple.
 *
 * [hapticFeedback] is opt-in: this surface backs both keys, which commit a
 * value, and pickers, which only open something. Callers that commit ask for
 * the tick; the rest keep their current, silent behaviour.
 */
@Composable
fun SpeseSurfaceButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = SpeseShapes.Key,
    container: Color = Spese.SurfaceSunken,
    containerPressed: Color = Spese.SurfaceSunkenPressed,
    onLongClick: (() -> Unit)? = null,
    hapticFeedback: Boolean = false,
    content: @Composable () -> Unit
) {

    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val tick = rememberHapticTick()

    val background by animateColorAsState(
        targetValue = if (pressed) containerPressed else container,
        animationSpec = tween(SpeseMotion.PressMillis),
        label = "surfaceButton"
    )

    val click: () -> Unit =
        if (hapticFeedback) {
            {
                tick()
                onClick()
            }
        } else {
            onClick
        }

    // The long press keeps the same tick as a plain key.
    val longClick: (() -> Unit)? =
        if (hapticFeedback) {
            onLongClick?.let { action ->
                {
                    tick()
                    action()
                }
            }
        } else {
            onLongClick
        }

    Box(
        modifier = modifier
            .pressScale(interactionSource, pressedScale = 0.94f)
            .clip(shape)
            .background(background)
            .then(
                if (longClick != null) {
                    Modifier.combinedClickableCompat(
                        interactionSource = interactionSource,
                        onClick = click,
                        onLongClick = longClick
                    )
                } else {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = click
                    )
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}


@OptIn(ExperimentalFoundationApi::class)
private fun Modifier.combinedClickableCompat(
    interactionSource: MutableInteractionSource,
    onClick: () -> Unit,
    onLongClick: () -> Unit
): Modifier = this.combinedClickable(
    interactionSource = interactionSource,
    indication = null,
    onLongClick = onLongClick,
    onClick = onClick
)


/**
 * Progress indicator for the flow. The active step stretches into a pill so the
 * user can read position at a glance without a numeric counter.
 */
@Composable
fun StepDots(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier
) {

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        repeat(total) { index ->

            val active = index == current
            val done = index < current

            val width by animateDpAsState(
                targetValue = if (active) 18.dp else 6.dp,
                animationSpec = tween(SpeseMotion.StepEnterMillis),
                label = "dotWidth"
            )

            val color by animateColorAsState(
                targetValue = when {
                    active -> Spese.Accent
                    done -> Spese.TextTertiary
                    else -> Spese.Divider
                },
                animationSpec = tween(SpeseMotion.StepEnterMillis),
                label = "dotColor"
            )

            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}


/** Small round button used for month navigation. */
@Composable
fun SpeseIconButton(
    glyph: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    SpeseSurfaceButton(
        onClick = onClick,
        modifier = modifier.size(44.dp),
        shape = RoundedCornerShape(22.dp)
    ) {

        Text(
            text = glyph,
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            color = Spese.TextPrimary
        )
    }
}
