package it.speses22.app.data

import android.content.Context

/**
 * Tema dell'applicazione. Due sole scelte, condivise da dashboard e Quick Add.
 */
enum class ThemeMode { Light, Dark }

/**
 * Preferenze locali condivise.
 *
 * Un solo valore persistito: non esistono preferenze separate per dashboard e
 * Quick Add.
 */
object AppPreferences {

    private const val Prefs = "app_preferences"
    private const val KeyTheme = "theme_mode"

    /** Default scuro: e' l'aspetto con cui l'app e' stata usata finora. */
    private val Default = ThemeMode.Dark

    fun themeMode(context: Context): ThemeMode {

        val stored = prefs(context).getString(KeyTheme, null) ?: return Default

        // "System" non esiste piu': una preferenza vecchia ricade sul default.
        return runCatching { ThemeMode.valueOf(stored) }.getOrDefault(Default)
    }

    fun setThemeMode(context: Context, mode: ThemeMode) {
        prefs(context).edit().putString(KeyTheme, mode.name).apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(Prefs, Context.MODE_PRIVATE)
}
