package it.speses22.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.speses22.app.data.ExpenseRecord

/**
 * Storico del mese selezionato su Home, raggruppato per giorno.
 *
 * Sola lettura: il Dashboard non modifica nulla su Notion.
 */
@Composable
fun ExpensesScreen(data: PeriodData) {

    val colors = MaterialTheme.colorScheme

    Column(modifier = Modifier.fillMaxWidth()) {

        when {
            data.loading -> Info("Loading from Notion…")

            data.failed -> Info("Could not read from Notion. Check the connection in Settings.")

            data.expenses.isEmpty() -> Info(
                "No expenses this month.\n" +
                    "Expenses you add with Quick Add will appear here."
            )

            else -> {

                val byDay = data.expenses
                    .groupBy { it.date }
                    .toSortedMap(compareByDescending { it })

                byDay.forEach { (day, items) ->

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = longDate(day),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.6.sp,
                            color = colors.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = money(items.sumOf { it.amount }),
                            fontSize = 12.sp,
                            color = colors.onSurfaceVariant
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.surface)
                    ) {
                        items.sortedByDescending { it.amount }.forEach { ExpenseRow(it) }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }
    }
}


@Composable
private fun ExpenseRow(record: ExpenseRecord) {

    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(modifier = Modifier.weight(1f)) {

            Text(
                text = record.description.ifBlank { "—" },
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            val meta = listOfNotNull(record.categoryName, record.accountName)

            if (meta.isNotEmpty()) {

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = meta.joinToString(" · "),
                    fontSize = 12.sp,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = money(record.amount),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface
        )
    }
}


@Composable
private fun Info(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 32.dp)
    )
}
