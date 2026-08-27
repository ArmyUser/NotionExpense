package it.speses22.app.data

import java.time.LocalDate

/**
 * Una spesa gia' registrata su Notion, letta per la dashboard.
 *
 * Distinta da [Expense], che e' il modello di *scrittura* usato dal Quick Add:
 * qui i nomi di categoria e conto sono gia' risolti per essere mostrati.
 */
data class ExpenseRecord(
    val id: String,
    val description: String,
    val amount: Double,
    val date: LocalDate,
    val categoryName: String?,
    val accountName: String?
)
