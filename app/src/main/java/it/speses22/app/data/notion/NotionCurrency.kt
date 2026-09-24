package it.speses22.app.data.notion

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

/**
 * Valuta della colonna importo, letta dallo schema di Expenses.
 *
 * Notion non ha una valuta del database: ce l'ha la singola colonna numerica,
 * nel suo formato ("euro", "dollar", ...). E' quella che l'utente vede in
 * Notion, quindi e' quella che l'app deve mostrare.
 */
internal object NotionCurrency {

    /**
     * Formato della colonna importo (es. "euro", "number"), oppure null se lo
     * schema non e' leggibile: rete assente, token o ID sbagliati.
     */
    suspend fun format(config: NotionConfig): String? = withContext(Dispatchers.IO) {

        if (!config.isConfigured) return@withContext null

        try {

            val response = NotionHttp.get(
                token = config.token,
                url = "${NotionHttp.DataSourcesUrl}/${config.dataSourceId}"
            )

            if (!response.isSuccess) return@withContext null

            JSONObject(response.body)
                .optJSONObject("properties")
                ?.optJSONObject(config.amountProperty)
                ?.optJSONObject("number")
                ?.optString("format")
                ?.takeIf { it.isNotBlank() }

        } catch (_: IOException) {
            null
        } catch (_: JSONException) {
            null
        }
    }


    /**
     * Simbolo per un formato Notion, oppure null se il formato non e' una valuta
     * ("number", "percent", ...) o e' uno che questa tabella non conosce ancora.
     */
    fun symbolOf(format: String): String? = Symbols[format]


    /** I formati di valuta documentati dall'API di Notion. */
    private val Symbols = mapOf(
        "euro" to "€",
        "dollar" to "$",
        "pound" to "£",
        "yen" to "¥",
        "yuan" to "CN¥",
        "won" to "₩",
        "rupee" to "₹",
        "rupiah" to "Rp",
        "ruble" to "₽",
        "real" to "R$",
        "lira" to "₺",
        "franc" to "CHF",
        "krona" to "kr",
        "norwegian_krone" to "kr",
        "danish_krone" to "kr",
        "zloty" to "zł",
        "forint" to "Ft",
        "koruna" to "Kč",
        "leu" to "lei",
        "shekel" to "₪",
        "dirham" to "AED",
        "riyal" to "SAR",
        "rand" to "R",
        "baht" to "฿",
        "ringgit" to "RM",
        "philippine_peso" to "₱",
        "australian_dollar" to "A$",
        "canadian_dollar" to "CA$",
        "singapore_dollar" to "S$",
        "hong_kong_dollar" to "HK$",
        "new_zealand_dollar" to "NZ$",
        "new_taiwan_dollar" to "NT$",
        "mexican_peso" to "MX$",
        "chilean_peso" to "CLP$",
        "colombian_peso" to "COL$",
        "argentine_peso" to "ARS$",
        "uruguayan_peso" to "UYU$",
        "peruvian_sol" to "S/"
    )
}
