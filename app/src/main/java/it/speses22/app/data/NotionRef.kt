package it.speses22.app.data

/**
 * Riga di una tabella Notion usata come relazione: conto o categoria.
 *
 * Porta con se' il page id, cosi' la selezione arriva al Worker gia' risolta e
 * l'invio non deve piu' cercare nulla.
 */
data class NotionRef(
    val name: String,
    val pageId: String
)
