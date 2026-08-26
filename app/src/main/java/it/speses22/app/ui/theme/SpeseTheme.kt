package it.speses22.app.ui.theme

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

/**
 * Notion-inspired neutral palette: warm greys, a single blue accent for the
 * primary action, no heavy chrome.
 */
object Spese {

    val Surface = Color(0xFF1F1F1F)
    val SurfaceSunken = Color(0xFF2A2A2A)
    val SurfaceSunkenPressed = Color(0xFF353535)

    val Divider = Color(0xFF383838)

    val TextPrimary = Color(0xFFE9E9E7)
    val TextSecondary = Color(0xFF9B9B99)
    val TextTertiary = Color(0xFF6E6E6C)

    val Accent = Color(0xFF2383E2)
    val AccentPressed = Color(0xFF1B6FC2)
    val AccentSoft = Color(0xFF1D3A5C)
    val OnAccent = Color(0xFFFFFFFF)

    val Disabled = Color(0xFF333333)
    val OnDisabled = Color(0xFF6E6E6C)

    val Success = Color(0xFF3DA35D)

    val Scrim = Color(0xFF000000)
    const val ScrimAlpha = 0.62f

    /** Ombra della card: su fondo scuro serve piu' opaca per restare leggibile. */
    val Shadow = Color(0xFF000000)
    const val ShadowAlpha = 0.65f
}


object SpeseShapes {

    val Card = RoundedCornerShape(28.dp)
    val Sheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val Control = RoundedCornerShape(14.dp)
    val Key = RoundedCornerShape(16.dp)
    val Field = RoundedCornerShape(16.dp)
}


/**
 * Every duration and spring used in the flow lives here, so the motion stays
 * one system instead of drifting per screen.
 */
/**
 * Vincoli di dimensione del contenitore.
 *
 * Non toccano l'aspetto su un telefono normale: servono solo quando la
 * finestra e' molto piu' larga o molto piu' bassa del previsto.
 */
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


private val SpeseColorScheme = darkColorScheme(
    primary = Spese.Accent,
    onPrimary = Spese.OnAccent,
    surface = Spese.Surface,
    onSurface = Spese.TextPrimary,
    surfaceVariant = Spese.SurfaceSunken,
    onSurfaceVariant = Spese.TextSecondary,
    outline = Spese.Divider,
    scrim = Spese.Scrim
)


@Composable
fun SpeseTheme(content: @Composable () -> Unit) {

    MaterialTheme(
        colorScheme = SpeseColorScheme,
        content = content
    )
}
