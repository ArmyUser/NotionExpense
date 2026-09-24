package it.speses22.app.data.notion

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.ProtocolException
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

    fun post(token: String, url: String, json: String): NotionResponse =
        send("POST", token, url, json)

    /**
     * PATCH: serve solo a spostare una pagina nel cestino.
     *
     * HttpURLConnection su Android accetta PATCH (l'implementazione e' quella di
     * OkHttp), mentre la JVM desktop no: se mai venisse rifiutato si ricade
     * sull'header di override, che e' l'unica alternativa senza aggiungere una
     * libreria HTTP al progetto.
     */
    fun patch(token: String, url: String, json: String): NotionResponse =
        send("PATCH", token, url, json)

    /** GET senza corpo: serve a leggere lo schema di una tabella. */
    fun get(token: String, url: String): NotionResponse =
        send("GET", token, url, null)


    private fun send(
        method: String,
        token: String,
        url: String,
        json: String?
    ): NotionResponse {

        // Senza corpo niente doOutput: con doOutput attivo HttpURLConnection
        // trasforma una GET in POST.
        val payload = json?.toByteArray(Charsets.UTF_8)

        var connection: HttpURLConnection? = null

        try {

            connection = (URL(url).openConnection() as HttpURLConnection).apply {

                var override: String? = null

                try {
                    requestMethod = method
                } catch (_: ProtocolException) {
                    requestMethod = "POST"
                    override = method
                }

                connectTimeout = TimeoutMillis
                readTimeout = TimeoutMillis
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("Notion-Version", Version)

                if (payload != null) {
                    doOutput = true
                    setFixedLengthStreamingMode(payload.size)
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                }

                override?.let { setRequestProperty("X-HTTP-Method-Override", it) }
            }

            payload?.let { bytes -> connection.outputStream.use { it.write(bytes) } }

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
