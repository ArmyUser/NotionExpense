package it.speses22.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import it.speses22.app.data.NotionRef

/**
 * Passo 4: conto.
 *
 * L'elenco arriva dalla tabella Accounts di Notion ed e' una lista normale:
 * si scorre senza rischiare di chiudere nulla.
 */
@Composable
fun AccountStep(
    accounts: List<NotionRef>,
    selectedAccountId: String?,
    onAccountSelected: (NotionRef) -> Unit
) {

    Column(modifier = Modifier.fillMaxWidth()) {

        RefList(
            refs = accounts,
            selectedId = selectedAccountId,
            onSelect = onAccountSelected,
            glyph = "🏦",
            emptyMessage =
                "No accounts available offline.\n" +
                    "The expense will be saved without an account."
        )
    }
}
