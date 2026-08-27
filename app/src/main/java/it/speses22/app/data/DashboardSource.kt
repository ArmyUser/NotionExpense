package it.speses22.app.data

import it.speses22.app.data.notion.NotionConfig
import it.speses22.app.data.notion.NotionDashboardSource
import java.time.LocalDate

/** Dati di sola lettura richiesti dalla dashboard. Notion resta la fonte di verita'. */
interface DashboardSource {

    /** Spese comprese nell'intervallo, ordinate dalla piu' recente. */
    suspend fun expenses(from: LocalDate, to: LocalDate): List<ExpenseRecord>?

    /**
     * Somma dei budget mensili definiti sulle categorie, oppure null se la
     * proprieta' non e' configurata: meglio non mostrare nulla che inventare.
     */
    suspend fun monthlyBudget(): Double?
}


object DashboardSources {

    fun default(): DashboardSource =
        NotionDashboardSource(NotionConfig.fromBuildConfig())
}
