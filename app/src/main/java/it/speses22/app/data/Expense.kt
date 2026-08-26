package it.speses22.app.data

data class Expense(
    val amount: Double,
    val description: String,
    val date: String,
    /** Nome leggibile, usato per la conferma e per le notifiche. */
    val category: String,
    /** Gia' risolti a page id: gli elenchi arrivano da Notion con gli id. */
    val categoryPageId: String = "",
    val accountPageId: String = ""
)
