package it.speses22.app.data

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import it.speses22.app.data.notion.NotionConfig
import it.speses22.app.data.notion.NotionCurrency

/**
 * Simbolo di valuta mostrato in tutta l'app.
 *
 * Non e' un'impostazione: viene dal formato della colonna importo in Notion,
 * cosi' l'app mostra sempre la stessa valuta che l'utente vede la'. La copia
 * locale tiene il Quick Add istantaneo e utilizzabile offline.
 *
 * Il valore sta in uno stato Compose: quando l'aggiornamento da Notion arriva
 * a schermata gia' aperta, i simboli visibili si aggiornano da soli.
 */
object AppCurrency {

    /** Quello usato finora: vale finche' Notion non dice altro. */
    private const val Default = "€"

    private val state = mutableStateOf(Default)

    @Volatile
    private var loaded = false

    /** Simbolo corrente. Da leggere dopo [load]. */
    val symbol: String get() = state.value

    /** Simbolo corrente, per chi non passa da un tema (notifiche, worker). */
    fun symbol(context: Context): String {
        load(context)
        return state.value
    }

    /**
     * Carica la copia locale, una sola volta per processo. Chiamato dai due
     * temi, quindi prima di qualunque schermata.
     */
    fun load(context: Context) {

        if (loaded) return

        AppPreferences.currencySymbol(context)?.let { state.value = it }
        loaded = true
    }

    /**
     * Rilegge il formato da Notion. Se Notion non risponde resta l'ultimo
     * valore noto; se la colonna non e' formattata come valuta si torna a €.
     */
    suspend fun refresh(context: Context) {

        val format = NotionCurrency.format(NotionConfig.fromBuildConfig()) ?: return

        val symbol = NotionCurrency.symbolOf(format) ?: Default

        load(context)

        if (symbol != state.value) {
            state.value = symbol
            AppPreferences.setCurrencySymbol(context, symbol)
        }
    }
}
