package it.speses22.app.data.notion

import it.speses22.app.data.Expense
import it.speses22.app.data.ExpenseSink
import it.speses22.app.data.SendResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

/**
 * Unica classe che conosce Notion.
 *
 * HTTP, JSON e classificazione degli errori restano qui dentro: ne'
 * l'interfaccia ne' il Worker sanno che dall'altra parte c'e' Notion.
 */
class NotionExpenseSink(
    private val config: NotionConfig
) : ExpenseSink {

    override suspend fun send(expense: Expense, isRetry: Boolean): SendResult =
        withContext(Dispatchers.IO) {

            if (!config.isConfigured) {
                return@withContext SendResult.Permanent(
                    "Notion non configurato: token o data source id mancante"
                )
            }

            try {

                // Un tentativo precedente puo' essere arrivato a destinazione
                // senza che la risposta tornasse indietro: prima di ricreare la
                // pagina si verifica che non esista gia'.
                if (isRetry) {
                    when (val existing = findExisting(expense)) {
                        is Lookup.Found -> return@withContext SendResult.Success
                        is Lookup.Failed -> return@withContext existing.result
                        Lookup.Missing -> Unit
                    }
                }

                val response = NotionHttp.post(
                    token = config.token,
                    url = NotionHttp.PagesUrl,
                    json = buildPayload(expense).toString()
                )

                if (response.isSuccess) SendResult.Success else classify(response)

            } catch (error: IOException) {

                // Rete assente, DNS, timeout: transitorio per definizione.
                SendResult.Retryable(error.message ?: "errore di rete")
            }
        }


    // ---- Ricerche ---------------------------------------------------------

    private sealed interface Lookup {

        data class Found(val pageId: String) : Lookup

        data object Missing : Lookup

        data class Failed(val result: SendResult) : Lookup
    }


    /**
     * Cerca una spesa gia' registrata con stessa data, stesso importo e stesso
     * titolo. Notion non offre chiavi di idempotenza, quindi il controllo si fa
     * sui dati, e solo quando si sta ritentando.
     */
    private fun findExisting(expense: Expense): Lookup {

        val filter = JSONObject().put(
            "and",
            JSONArray()
                .put(
                    JSONObject()
                        .put("property", config.dateProperty)
                        .put("date", JSONObject().put("equals", expense.date))
                )
                .put(
                    JSONObject()
                        .put("property", config.amountProperty)
                        .put("number", JSONObject().put("equals", expense.amount))
                )
        )

        val response = NotionHttp.post(
            token = config.token,
            url = "${NotionHttp.DataSourcesUrl}/${config.dataSourceId}/query",
            json = JSONObject()
                .put("filter", filter)
                .put("page_size", QueryPageSize)
                .toString()
        )

        if (!response.isSuccess) return Lookup.Failed(classify(response))

        val results = JSONObject(response.body).optJSONArray("results")
            ?: return Lookup.Missing

        for (index in 0 until results.length()) {

            val page = results.optJSONObject(index) ?: continue

            if (NotionHttp.titleOf(page) == expense.description) {
                return Lookup.Found(page.optString("id"))
            }
        }

        return Lookup.Missing
    }


    // ---- Corpo della richiesta -------------------------------------------

    /**
     * Dalla versione 2025-09-03 il parent di una pagina in un database e' il
     * data source, non piu' il database.
     */
    private fun buildPayload(expense: Expense): JSONObject {

        val properties = JSONObject()
            .put(config.titleProperty, titleValue(expense.description))
            .put(config.amountProperty, JSONObject().put("number", expense.amount))
            .put(config.dateProperty, dateValue(expense.date))

        categoryValue(expense)?.let {
            properties.put(config.categoryProperty, it)
        }

        // Il conto arriva gia' come page id: nessuna ricerca necessaria.
        if (expense.accountPageId.isNotBlank() && config.accountProperty.isNotBlank()) {
            properties.put(config.accountProperty, relationValue(expense.accountPageId))
        }

        return JSONObject()
            .put(
                "parent",
                JSONObject()
                    .put("type", "data_source_id")
                    .put("data_source_id", config.dataSourceId)
            )
            .put("properties", properties)
    }


    private fun titleValue(text: String): JSONObject =
        JSONObject().put(
            "title",
            JSONArray().put(
                JSONObject().put("text", JSONObject().put("content", text))
            )
        )


    private fun dateValue(isoDate: String): JSONObject =
        JSONObject().put("date", JSONObject().put("start", isoDate))


    private fun relationValue(pageId: String): JSONObject =
        JSONObject().put(
            "relation",
            JSONArray().put(JSONObject().put("id", pageId))
        )


    /** La categoria puo' essere Relation, Select, Multi-select o Rich text. */
    private fun categoryValue(expense: Expense): JSONObject? {

        val name = expense.category.takeIf { it.isNotBlank() }

        return when (config.categoryType) {

            // Id gia' risolto dall'elenco sincronizzato da Notion.
            RelationType -> expense.categoryPageId
                .takeIf { it.isNotBlank() }
                ?.let { relationValue(it) }

            "multi_select" -> name?.let {
                JSONObject().put(
                    "multi_select",
                    JSONArray().put(JSONObject().put("name", it))
                )
            }

            "rich_text" -> name?.let {
                JSONObject().put(
                    "rich_text",
                    JSONArray().put(
                        JSONObject().put("text", JSONObject().put("content", it))
                    )
                )
            }

            else -> name?.let {
                JSONObject().put("select", JSONObject().put("name", it))
            }
        }
    }


    /**
     * Traduce la risposta di Notion in "ha senso riprovare" oppure no.
     *
     * 429/409 e 5xx sono transitori; 400/401/403/404 no, e insistere
     * significherebbe solo consumare batteria.
     */
    private fun classify(response: NotionResponse): SendResult {

        val parsed = runCatching { JSONObject(response.body) }.getOrNull()

        val detail = listOfNotNull(
            "HTTP ${response.code}",
            parsed?.optString("code")?.takeIf { it.isNotBlank() },
            parsed?.optString("message")?.takeIf { it.isNotBlank() }
        ).joinToString(" · ")

        return when {
            response.code == 429 -> SendResult.Retryable(detail)
            response.code == 409 -> SendResult.Retryable(detail)
            response.code >= 500 -> SendResult.Retryable(detail)
            else -> SendResult.Permanent(detail)
        }
    }


    private companion object {

        const val RelationType = "relation"
        const val QueryPageSize = 100
    }
}
