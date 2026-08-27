package it.speses22.app.ui.theme

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import it.speses22.app.data.AppPreferences
import it.speses22.app.data.ThemeMode

/**
 * Palette del Quick Add.
 *
 * I nomi delle proprieta' restano identici: le schermate continuano a scrivere
 * `Spese.Surface` e non sanno che il valore ora dipende dal tema scelto.
 */
data class SpesePalette(
    val Background: Color,
    val Surface: Color,
    val SurfaceSunken: Color,
    val SurfaceSunkenPressed: Color,
    val Divider: Color,
    val TextPrimary: Color,
    val TextSecondary: Color,
    val TextTertiary: Color,
    val Accent: Color,
    val AccentPressed: Color,
    val AccentSoft: Color,
    val OnAccent: Color,
    val Disabled: Color,
    val OnDisabled: Color,
    val Success: Color,
    val Scrim: Color,
    val ScrimAlpha: Float,
    val Shadow: Color,
    val ShadowAlpha: Float
)

/** Valori scuri: identici a quelli usati finora. */
private val DarkPalette = SpesePalette(
    Background = Color(0xFF141414),
    Surface = Color(0xFF1F1F1F),
    SurfaceSunken = Color(0xFF2A2A2A),
    SurfaceSunkenPressed = Color(0xFF353535),
    Divider = Color(0xFF383838),
    TextPrimary = Color(0xFFE9E9E7),
    TextSecondary = Color(0xFF9B9B99),
    TextTertiary = Color(0xFF6E6E6C),
    Accent = Color(0xFF2383E2),
    AccentPressed = Color(0xFF1B6FC2),
    AccentSoft = Color(0xFF1D3A5C),
    OnAccent = Color(0xFFFFFFFF),
    Disabled = Color(0xFF333333),
    OnDisabled = Color(0xFF6E6E6C),
    Success = Color(0xFF3DA35D),
    Scrim = Color(0xFF000000),
    ScrimAlpha = 0.62f,
    Shadow = Color(0xFF000000),
    ShadowAlpha = 0.65f
)

/**
 * Valori chiari: stessi ruoli, stesso accento, nessuna modifica al layout.
 * Su fondo chiaro scrim e ombra vanno alleggeriti, altrimenti anneriscono.
 */
private val LightPalette = SpesePalette(
    Background = Color(0xFFF6F6F4),
    Surface = Color(0xFFFFFFFF),
    SurfaceSunken = Color(0xFFF1F0ED),
    SurfaceSunkenPressed = Color(0xFFE5E4E0),
    Divider = Color(0xFFE2E0DB),
    TextPrimary = Color(0xFF191919),
    TextSecondary = Color(0xFF6B6B69),
    TextTertiary = Color(0xFF9B9B99),
    Accent = Color(0xFF2383E2),
    AccentPressed = Color(0xFF1B6FC2),
    AccentSoft = Color(0xFFE8F1FC),
    OnAccent = Color(0xFFFFFFFF),
    Disabled = Color(0xFFEDEDEA),
    OnDisabled = Color(0xFFA9A9A6),
    Success = Color(0xFF2E9E52),
    Scrim = Color(0xFF000000),
    ScrimAlpha = 0.32f,
    Shadow = Color(0xFF000000),
    ShadowAlpha = 0.18f
)

private var activePalette by mutableStateOf(DarkPalette)

/**
 * Token di colore del Quick Add.
 *
 * Le proprieta' sono calcolate, non costanti: cambiando la preferenza cambia
 * il valore letto, senza toccare una sola schermata.
 */
object Spese {

    val Background: Color get() = activePalette.Background

    val Surface: Color get() = activePalette.Surface
    val SurfaceSunken: Color get() = activePalette.SurfaceSunken
    val SurfaceSunkenPressed: Color get() = activePalette.SurfaceSunkenPressed

    val Divider: Color get() = activePalette.Divider

    val TextPrimary: Color get() = activePalette.TextPrimary
    val TextSecondary: Color get() = activePalette.TextSecondary
    val TextTertiary: Color get() = activePalette.TextTertiary

    val Accent: Color get() = activePalette.Accent
    val AccentPressed: Color get() = activePalette.AccentPressed
    val AccentSoft: Color get() = activePalette.AccentSoft
    val OnAccent: Color get() = activePalette.OnAccent

    val Disabled: Color get() = activePalette.Disabled
    val OnDisabled: Color get() = activePalette.OnDisabled

    val Success: Color get() = activePalette.Success

    val Scrim: Color get() = activePalette.Scrim
    val ScrimAlpha: Float get() = activePalette.ScrimAlpha

    val Shadow: Color get() = activePalette.Shadow
    val ShadowAlpha: Float get() = activePalette.ShadowAlpha
}


object SpeseShapes {

    val Card = RoundedCornerShape(28.dp)
    val Sheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val Control = RoundedCornerShape(14.dp)
    val Key = RoundedCornerShape(16.dp)
    val Field = RoundedCornerShape(16.dp)
}


object SpeseDimens {

    /** Oltre questa larghezza la card resta centrata invece di allargarsi. */
    val MaxCardWidth = 480.dp
}


object SpeseMotion {

    /** Step-to-step content swap. */
    const val StepEnterMillis = 200
    const val StepExitMillis = 130

    /** Card entrance / exit. */
    const val CardEnterMillis = 300
    const val CardExitMillis = 200

    /** Touch feedback. */
    const val PressMillis = 100

    /**
     * Scambio di fase della card (editor / selettore / conferma).
     *
     * Valori gia' in uso, spostati qui senza modificarli: cambiare i numeri
     * cambierebbe il ritmo attuale.
     */
    const val PhaseEnterMillis = 220
    const val PhaseEnterDelayMillis = 60
    const val PhaseExitMillis = 140

    /** Crossfade di un'etichetta dentro un controllo. */
    const val LabelEnterMillis = 140
    const val LabelExitMillis = 100

    /** Crossfade di un valore letto (la data selezionata). */
    const val ValueEnterMillis = 160
    const val ValueExitMillis = 120

    /** Vertical travel of incoming/outgoing step content. */
    val StepOffset = 12.dp

    /** Container resize: critically damped, no visible overshoot. */
    val ContainerSize: FiniteAnimationSpec<IntSize> = spring(
        dampingRatio = 0.9f,
        stiffness = 420f,
        visibilityThreshold = IntSize.VisibilityThreshold
    )

    /** Press scale: stiff and short, so it never lags the finger. */
    val Press: FiniteAnimationSpec<Float> = spring(
        dampingRatio = 0.85f,
        stiffness = 1600f
    )

    /** Card scale on entrance. */
    val CardScale: FiniteAnimationSpec<Float> = spring(
        dampingRatio = 0.88f,
        stiffness = 380f
    )

    /** Card vertical travel on entrance. */
    val CardSlide: FiniteAnimationSpec<IntOffset> = spring(
        dampingRatio = 0.88f,
        stiffness = 380f,
        visibilityThreshold = IntOffset.VisibilityThreshold
    )
}


private fun schemeFor(dark: Boolean) = if (dark) {
    darkColorScheme(
        primary = Spese.Accent,
        onPrimary = Spese.OnAccent,
        surface = Spese.Surface,
        onSurface = Spese.TextPrimary,
        surfaceVariant = Spese.SurfaceSunken,
        onSurfaceVariant = Spese.TextSecondary,
        outline = Spese.Divider,
        scrim = Spese.Scrim
    )
} else {
    lightColorScheme(
        primary = Spese.Accent,
        onPrimary = Spese.OnAccent,
        surface = Spese.Surface,
        onSurface = Spese.TextPrimary,
        surfaceVariant = Spese.SurfaceSunken,
        onSurfaceVariant = Spese.TextSecondary,
        outline = Spese.Divider,
        scrim = Spese.Scrim
    )
}


/**
 * Tema del Quick Add.
 *
 * Firma invariata: legge da solo la preferenza condivisa, cosi' MainActivity
 * non cambia di una riga.
 */
@Composable
fun SpeseTheme(content: @Composable () -> Unit) {

    val context = LocalContext.current
    val dark = AppPreferences.themeMode(context) == ThemeMode.Dark

    // Applicata prima che i figli si compongano.
    remember(dark) {
        activePalette = if (dark) DarkPalette else LightPalette
        dark
    }

    MaterialTheme(
        colorScheme = schemeFor(dark),
        content = content
    )
}
