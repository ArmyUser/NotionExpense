package it.speses22.app.data

import it.speses22.app.data.notion.NotionConfig
import it.speses22.app.data.notion.NotionRefSource

/**
 * Sorgente di un elenco di relazioni.
 *
 * Speculare a [ExpenseSink]: la UI non sa da dove arrivano conti e categorie,
 * quindi il giorno in cui li servira' un server cambia solo l'implementazione.
 */
interface RefSource {

    /** Righe disponibili, oppure null se la lettura non e' riuscita. */
    suspend fun load(): List<NotionRef>?
}


object RefSources {

    fun accounts(): RefSource =
        NotionConfig.fromBuildConfig().let { NotionRefSource(it, it.accountsDataSourceId) }

    fun categories(): RefSource =
        NotionConfig.fromBuildConfig().let { NotionRefSource(it, it.categoryDataSourceId) }
}
