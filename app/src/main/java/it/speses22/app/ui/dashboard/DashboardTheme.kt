package it.speses22.app.ui.dashboard

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import it.speses22.app.data.ThemeMode

/**
 * Tema della sola dashboard.
 *
 * Deliberatamente separato da SpeseTheme: il Quick Add continua a usare la sua
 * palette scura fissa, quindi scegliere "Light" qui non ne cambia l'aspetto.
 */

private val Accent = Color(0xFF2383E2)

private val DarkScheme = darkColorScheme(
    primary = Accent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1D3A5C),
    onPrimaryContainer = Color(0xFFD6E7FA),
    background = Color(0xFF141414),
    onBackground = Color(0xFFE9E9E7),
    surface = Color(0xFF1F1F1F),
    onSurface = Color(0xFFE9E9E7),
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFF9B9B99),
    outline = Color(0xFF383838)
)

private val LightScheme = lightColorScheme(
    primary = Accent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCEAFB),
    onPrimaryContainer = Color(0xFF0B3A66),
    background = Color(0xFFF6F6F4),
    onBackground = Color(0xFF191919),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF191919),
    surfaceVariant = Color(0xFFEDEDEA),
    onSurfaceVariant = Color(0xFF5F5F5D),
    outline = Color(0xFFDCDCD8)
)

/** Tinte del grafico: leggibili su entrambi gli sfondi. */
val CategoryPalette = listOf(
    Color(0xFF2383E2),
    Color(0xFF3DA35D),
    Color(0xFFE0913A),
    Color(0xFFB55BC4),
    Color(0xFFDD5C64),
    Color(0xFF2FA8A8),
    Color(0xFF8C7BD8),
    Color(0xFF9AA23A)
)

@Composable
fun DashboardTheme(
    mode: ThemeMode,
    content: @Composable () -> Unit
) {

    val dark = mode == ThemeMode.Dark

    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            // Icone della status bar leggibili anche sul tema chiaro.
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !dark
        }
    }

    MaterialTheme(
        colorScheme = if (dark) DarkScheme else LightScheme,
        content = content
    )
}
