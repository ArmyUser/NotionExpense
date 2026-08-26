package it.speses22.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Cache locale degli elenchi che arrivano da Notion.
 *
 * Serve a due cose: popolare i selettori senza attendere la rete, e restare
 * utilizzabili offline, quando Notion non e' raggiungibile.
 */
class RefStore(private val key: String) {

    fun read(context: Context): List<NotionRef> {

        val raw = prefs(context).getString(key, null) ?: return emptyList()

        return runCatching {

            val array = JSONArray(raw)

            (0 until array.length()).mapNotNull { index ->

                val item = array.optJSONObject(index) ?: return@mapNotNull null

                NotionRef(
                    name = item.optString("name"),
                    pageId = item.optString("pageId")
                )
            }

        }.getOrDefault(emptyList())
    }


    fun write(context: Context, refs: List<NotionRef>) {

        val array = JSONArray()

        refs.forEach { ref ->
            array.put(
                JSONObject()
                    .put("name", ref.name)
                    .put("pageId", ref.pageId)
            )
        }

        prefs(context).edit().putString(key, array.toString()).apply()
    }


    // ---- Ultima scelta ----------------------------------------------------

    /**
     * Riporta la selezione da usare all'apertura del flusso.
     *
     * Regole: una scelta gia' fatta in questa sessione vince, ma solo se la
     * riga esiste ancora; altrimenti si riprende l'ultima usata. Un elenco
     * vuoto non prova nessuna cancellazione (puo' solo mancare la rete),
     * quindi in quel caso non si scarta niente.
     */
    fun preselect(
        context: Context,
        refs: List<NotionRef>,
        current: String?
    ): String? {

        if (refs.isEmpty()) return current

        val exists = { id: String -> refs.any { it.pageId == id } }

        if (current != null) {
            return if (exists(current)) current else null
        }

        val saved = prefs(context).getString(lastKey, null) ?: return null

        if (exists(saved)) return saved

        // Riga rimossa da Notion: la preferenza scaduta sparisce in silenzio.
        prefs(context).edit().remove(lastKey).apply()

        return null
    }


    /** Ricorda la scelta appena fatta. Resta sul dispositivo, mai su Notion. */
    fun rememberLastUsed(context: Context, pageId: String) {
        prefs(context).edit().putString(lastKey, pageId).apply()
    }


    private val lastKey get() = "${key}_last"


    private fun prefs(context: Context) =
        context.getSharedPreferences(Prefs, Context.MODE_PRIVATE)


    companion object {

        private const val Prefs = "notion_refs"

        val Accounts = RefStore("accounts")
        val Categories = RefStore("categories")
    }
}
