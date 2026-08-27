package it.speses22.app.data.notion

import it.speses22.app.data.DashboardSource
import it.speses22.app.data.ExpenseRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDate

/**
 * Lettura delle spese gia' presenti su Notion.
 *
 * Usa lo stesso trasporto e la stessa configurazione della scrittura: nessun
 * secondo database locale, nessuna proprieta' inventata.
 */
class NotionDashboardSource(
    private val config: NotionConfig
) : DashboardSource {

    override suspend fun expenses(
        from: LocalDate,
        to: LocalDate
    ): List<ExpenseRecord>? = withContext(Dispatchers.IO) {

        if (!config.isConfigured) return@withContext null

        try {

            val filter = JSONObject().put(
                "and",
                JSONArray()
                    .put(dateBound("on_or_after", from))
                    .put(dateBound("on_or_before", to))
            )

            val response = NotionHttp.post(
                token = config.token,
                url = "${NotionHttp.DataSourcesUrl}/${config.dataSourceId}/query",
                json = JSONObject()
                    .put("filter", filter)
                    .put(
                        "sorts",
                        JSONArray().put(
                            JSONObject()
                                .put("property", config.dateProperty)
                                .put("direction", "descending")
                        )
                    )
                    .put("page_size", PageSize)
                    .toString()
            )

            if (!response.isSuccess) return@withContext null

            val results = JSONObject(response.body).optJSONArray("results")
                ?: return@withContext emptyList()

            // I nomi delle relazioni arrivano dagli elenchi gia' sincronizzati.
            val categories = NotionRefSource(config, config.categoryDataSourceId)
                .load()
                .orEmpty()
                .associate { it.pageId to it.name }

            val accounts = NotionRefSource(config, config.accountsDataSourceId)
                .load()
                .orEmpty()
                .associate { it.pageId to it.name }

            (0 until results.length()).mapNotNull { index ->
                results.optJSONObject(index)?.let { page ->
                    record(page, categories, accounts)
                }
            }

        } catch (_: IOException) {
            null
        }
    }


    override suspend fun monthlyBudget(): Double? = withContext(Dispatchers.IO) {

        if (!config.isConfigured || config.categoryDataSourceId.isBlank()) {
            return@withContext null
        }

        try {

            val response = NotionHttp.post(
                token = config.token,
                url = "${NotionHttp.DataSourcesUrl}/${config.categoryDataSourceId}/query",
                json = JSONObject().put("page_size", PageSize).toString()
            )

            if (!response.isSuccess) return@withContext null

            val results = JSONObject(response.body).optJSONArray("results")
                ?: return@withContext null

            var total = 0.0
            var found = false

            for (index in 0 until results.length()) {

                val properties = results.optJSONObject(index)?.optJSONObject("properties")
                    ?: continue

                val budget = properties.optJSONObject(BudgetProperty) ?: continue

                if (budget.optString("type") != "number") continue
                if (budget.isNull("number")) continue

                total += budget.optDouble("number", 0.0)
                found = true
            }

            if (found) total else null

        } catch (_: IOException) {
            null
        }
    }


    private fun dateBound(operator: String, day: LocalDate): JSONObject =
        JSONObject()
            .put("property", config.dateProperty)
            .put("date", JSONObject().put(operator, day.toString()))


    private fun record(
        page: JSONObject,
        categories: Map<String, String>,
        accounts: Map<String, String>
    ): ExpenseRecord? {

        val properties = page.optJSONObject("properties") ?: return null

        val date = properties.optJSONObject(config.dateProperty)
            ?.optJSONObject("date")
            ?.optString("start")
            ?.takeIf { it.isNotBlank() }
            ?.let { runCatching { LocalDate.parse(it.take(10)) }.getOrNull() }
            ?: return null

        return ExpenseRecord(
            id = page.optString("id"),
            description = NotionHttp.titleOf(page).orEmpty(),
            amount = properties.optJSONObject(config.amountProperty)
                ?.optDouble("number", 0.0)
                ?: 0.0,
            date = date,
            categoryName = relationName(properties, config.categoryProperty, categories),
            accountName = relationName(properties, config.accountProperty, accounts)
        )
    }


    private fun relationName(
        properties: JSONObject,
        name: String,
        lookup: Map<String, String>
    ): String? {

        val relation = properties.optJSONObject(name)?.optJSONArray("relation")
            ?: return null

        val first = relation.optJSONObject(0)?.optString("id") ?: return null

        return lookup[first]
    }


    private companion object {

        const val PageSize = 100

        /** Nome fisso: e' una proprieta' della tabella Categories dell'utente. */
        const val BudgetProperty = "Monthly Budget"
    }
}
