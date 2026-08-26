package it.speses22.app.data

import it.speses22.app.data.notion.NotionConfig
import it.speses22.app.data.notion.NotionExpenseSink

/**
 * Esito di un invio.
 *
 * Distingue cio' che ha senso ritentare da cio' che non si risolvera' mai da
 * solo: e' questa distinzione a evitare i retry infiniti.
 */
sealed interface SendResult {

    data object Success : SendResult

    /** Rete assente, throttling, errore lato server: riprovare ha senso. */
    data class Retryable(val reason: String) : SendResult

    /** Token errato, database non condiviso, schema non corrispondente. */
    data class Permanent(val reason: String) : SendResult
}


/**
 * Destinazione di una spesa.
 *
 * Il Worker conosce solo questa interfaccia, mai Notion: il giorno in cui la
 * spesa passera' da un server, cambia l'implementazione e non il flusso.
 */
interface ExpenseSink {

    /**
     * @param isRetry se true l'invio precedente potrebbe essere andato a buon
     *   fine senza che la risposta sia arrivata: l'implementazione deve
     *   assicurarsi di non creare un duplicato.
     */
    suspend fun send(expense: Expense, isRetry: Boolean): SendResult
}


/**
 * Punto unico di composizione.
 *
 * Per passare a `Android -> Server -> Notion` basta cambiare questa riga con
 * l'implementazione che parla col proprio backend.
 */
object ExpenseSinks {

    fun default(): ExpenseSink =
        NotionExpenseSink(NotionConfig.fromBuildConfig())
}
