package it.speses22.app.data.notion

import it.speses22.app.data.NotionRef
import it.speses22.app.data.RefSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException

/**
 * Legge le righe di una tabella Notion esistente. Sola lettura: non crea,
 * rinomina o elimina nulla.
 *
 * Il titolo viene individuato per tipo di proprieta', non per nome, quindi
 * funziona sia con Accounts ("Account") sia con Categories ("Category") senza
 * che il nome della colonna sia scritto da qualche parte.
 */
class NotionRefSource(
    private val config: NotionConfig,
    private val dataSourceId: String
) : RefSource {

    override suspend fun load(): List<NotionRef>? = withContext(Dispatchers.IO) {

        if (config.token.isBlank() || dataSourceId.isBlank()) {
            return@withContext null
        }

        try {

            val response = NotionHttp.post(
                token = config.token,
                url = "${NotionHttp.DataSourcesUrl}/$dataSourceId/query",
                json = JSONObject().put("page_size", PageSize).toString()
            )

            if (!response.isSuccess) return@withContext null

            val results = JSONObject(response.body).optJSONArray("results")
                ?: return@withContext emptyList()

            (0 until results.length()).mapNotNull { index ->

                val page = results.optJSONObject(index) ?: return@mapNotNull null
                val name = NotionHttp.titleOf(page)?.takeIf { it.isNotBlank() }
                    ?: return@mapNotNull null

                NotionRef(name = name, pageId = page.optString("id"))
            }

        } catch (_: IOException) {
            null
        }
    }

    private companion object {
        const val PageSize = 100
    }
}
