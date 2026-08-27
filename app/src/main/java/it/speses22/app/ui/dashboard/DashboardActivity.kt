package it.speses22.app.ui.dashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import it.speses22.app.ui.theme.SpeseTheme

/**
 * Punto di ingresso dell'icona del launcher.
 *
 * Separata da MainActivity di proposito: la dashboard e' una schermata piena e
 * opaca, il Quick Add e' una card su finestra traslucida. Sono due temi di
 * finestra diversi, quindi due Activity.
 */
class DashboardActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            SpeseTheme {
                DashboardApp()
            }
        }
    }
}
