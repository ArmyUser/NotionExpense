package it.speses22.app.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.speses22.app.data.ExpenseRecord
import it.speses22.app.ui.components.SpeseButton
import it.speses22.app.ui.theme.Spese
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun HomeSection(
    month: YearMonth,
    data: PeriodData,
    onAddExpense: () -> Unit
) {

    val total = data.expenses.sumOf { it.amount }

    Column(modifier = Modifier.fillMaxWidth()) {

        Card {
            Text(
                text = "SPENT IN ${monthLabel(month).uppercase()}",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                color = Spese.TextTertiary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = money(total),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Spese.TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {

                Metric("Expenses", data.expenses.size.toString(), Modifier.weight(1f))

                // Il budget si mostra solo se Notion lo definisce davvero.
                data.budget?.let { budget ->
                    Metric("Budget", money(budget), Modifier.weight(1f))
                    Metric("Left", money(budget - total), Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "RECENT",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            color = Spese.TextTertiary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.weight(1f)) {

            when {
                data.loading -> Hint("Loading from Notion…")

                data.failed -> Hint(
                    "Could not read from Notion.\n" +
                        "Check the token and that the database is shared with the integration."
                )

                data.expenses.isEmpty() -> Hint("No expenses recorded in this period.")

                else -> LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(items = data.expenses, key = { it.id }) { ExpenseRow(it) }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        SpeseButton(
            text = "+  ADD EXPENSE",
            onClick = onAddExpense,
            modifier = Modifier.fillMaxWidth()
        )
    }
}


@Composable
private fun ExpenseRow(record: ExpenseRecord) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(modifier = Modifier.weight(1f)) {

            Text(
                text = record.description.ifBlank { "—" },
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Spese.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = listOfNotNull(
                    record.date.format(DateTimeFormatter.ofPattern("d MMM")),
                    record.categoryName,
                    record.accountName
                ).joinToString(" · "),
                fontSize = 12.sp,
                color = Spese.TextTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = money(record.amount),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Spese.TextPrimary
        )
    }
}
