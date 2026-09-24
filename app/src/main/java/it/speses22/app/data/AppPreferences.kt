package it.speses22.app.data

import android.content.Context

/**
 * Tema dell'applicazione. Due sole scelte, condivise da dashboard e Quick Add.
 */
enum class ThemeMode { Light, Dark }

/**
 * Preferenze locali condivise da dashboard e Quick Add.
 *
 * Il tema e' una scelta dell'utente; la valuta no, e' solo la copia locale di
 * quella letta da Notion (vedi [AppCurrency]).
 */
object AppPreferences {

    private const val Prefs = "app_preferences"
    private const val KeyTheme = "theme_mode"
    private const val KeyCurrency = "currency_symbol"

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

    /** Ultimo simbolo letto da Notion, oppure null se non e' mai stato letto. */
    fun currencySymbol(context: Context): String? =
        prefs(context).getString(KeyCurrency, null)

    fun setCurrencySymbol(context: Context, symbol: String) {
        prefs(context).edit().putString(KeyCurrency, symbol).apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(Prefs, Context.MODE_PRIVATE)
}
