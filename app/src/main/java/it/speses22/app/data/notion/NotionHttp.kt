package it.speses22.app.data.notion

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

internal data class NotionResponse(val code: Int, val body: String) {

    val isSuccess: Boolean get() = code in 200..299
}


/**
 * Trasporto condiviso verso Notion.
 *
 * Versione dell'API, header e lettura della risposta stanno qui, cosi' il sink
 * e la sorgente dei conti non li duplicano.
 */
internal object NotionHttp {

    /** Versione verificata sulla documentazione Notion. */
    const val Version = "2026-03-11"

    const val PagesUrl = "https://api.notion.com/v1/pages"
    const val DataSourcesUrl = "https://api.notion.com/v1/data_sources"

    private const val TimeoutMillis = 15_000

    fun post(token: String, url: String, json: String): NotionResponse {

        val payload = json.toByteArray(Charsets.UTF_8)

        var connection: HttpURLConnection? = null

        try {

            connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = TimeoutMillis
                readTimeout = TimeoutMillis
                doOutput = true
                setFixedLengthStreamingMode(payload.size)
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("Notion-Version", Version)
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }

            connection.outputStream.use { it.write(payload) }

            val code = connection.responseCode

            val stream =
                if (code in 200..299) connection.inputStream else connection.errorStream

            return NotionResponse(
                code = code,
                body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            )

        } finally {
            connection?.disconnect()
        }
    }


    /**
     * Titolo di una pagina, individuato per tipo di proprieta'.
     *
     * Cercare il tipo invece del nome rende la lettura indipendente da come
     * l'utente ha chiamato la colonna.
     */
    fun titleOf(page: JSONObject): String? {

        val properties = page.optJSONObject("properties") ?: return null

        for (key in properties.keys()) {

            val property = properties.optJSONObject(key) ?: continue

            if (property.optString("type") != "title") continue

            val parts = property.optJSONArray("title") ?: continue

            return buildString {
                for (index in 0 until parts.length()) {
                    append(parts.optJSONObject(index)?.optString("plain_text").orEmpty())
                }
            }
        }

        return null
    }
}
