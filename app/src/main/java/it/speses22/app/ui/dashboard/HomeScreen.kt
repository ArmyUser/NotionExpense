package it.speses22.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import java.time.LocalDate
import java.time.YearMonth

/**
 * Home: solo numeri e grafici del mese scelto.
 *
 * L'elenco delle singole spese vive in Expenses, dove ci sono i filtri: qui
 * ripeterlo voleva dire avere due storici leggermente diversi.
 */
@Composable
fun HomeScreen(data: PeriodData, month: YearMonth) {

    val total = data.expenses.sumOf { it.amount }

    Column(modifier = Modifier.fillMaxWidth()) {

        Card {

            Text(
                text = "Total spent",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = money(total),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            data.budget?.let { budget ->

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Figure("Budget", money(budget), Modifier.weight(1f))
                    Figure(
                        label = if (budget - total < 0) "Over budget" else "Remaining",
                        value = money(kotlin.math.abs(budget - total)),
                        modifier = Modifier.weight(1f),
                        highlight = budget - total < 0
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                BudgetBar(fraction = if (budget > 0) (total / budget).toFloat() else 0f)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (data.loading) {
            Note("Loading from Notion…")
            return@Column
        }

        if (data.failed) {
            Note("Could not read from Notion. Check the connection in Settings.")
            return@Column
        }

        if (data.expenses.isEmpty()) {
            EmptyState()
            return@Column
        }

        // ---- Numeri secchi -------------------------------------------------

        val amounts = data.expenses.map { it.amount }
        val elapsed = daysElapsed(month)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Stat("Expenses", "${data.expenses.size}", Modifier.weight(1f))
            Stat("Average", money(total / data.expenses.size), Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Stat("Largest", money(amounts.max()), Modifier.weight(1f))
            Stat(
                label = "Per day",
                value = money(if (elapsed > 0) total / elapsed else 0.0),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ---- Andamento giornaliero ----------------------------------------

        Card {

            SectionTitle("DAILY SPENDING")

            Spacer(modifier = Modifier.height(16.dp))

            DailyBars(month = month, expenses = data.expenses)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ---- Ripartizione per categoria -----------------------------------

        val categorySlices = slices(data.expenses) { it.categoryName ?: "Uncategorised" }

        Card {

            SectionTitle("BY CATEGORY")

            Spacer(modifier = Modifier.height(14.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                DonutChart(slices = categorySlices, total = money(total))
            }

            Spacer(modifier = Modifier.height(16.dp))

            categorySlices.forEachIndexed { index, slice ->
                if (index > 0) Spacer(modifier = Modifier.height(10.dp))
                LegendRow(slice)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ---- Ripartizione per conto ---------------------------------------

        val accountSlices = slices(data.expenses) { it.accountName ?: "No account" }

        Card {

            SectionTitle("BY ACCOUNT")

            Spacer(modifier = Modifier.height(14.dp))

            accountSlices.forEachIndexed { index, slice ->
                if (index > 0) Spacer(modifier = Modifier.height(14.dp))
                MeterRow(slice)
            }
        }
    }
}


// ---- Aggregazioni ------------------------------------------------------

/** Giorni gia' trascorsi: per il mese in corso la media si ferma a oggi. */
private fun daysElapsed(month: YearMonth): Int {

    val today = LocalDate.now()

    return when {
        YearMonth.from(today) == month -> today.dayOfMonth
        month.isAfter(YearMonth.from(today)) -> 0
        else -> month.lengthOfMonth()
    }
}

private fun slices(
    expenses: List<ExpenseRecord>,
    key: (ExpenseRecord) -> String
): List<Slice> {

    val total = expenses.sumOf { it.amount }
    if (total <= 0.0) return emptyList()

    return expenses
        .groupBy(key)
        .map { (name, rows) -> name to rows.sumOf { it.amount } }
        .sortedByDescending { it.second }
        .mapIndexed { index, (name, amount) ->
            Slice(
                label = name,
                amount = amount,
                fraction = (amount / total).toFloat(),
                color = CategoryPalette[index % CategoryPalette.size]
            )
        }
}


// ---- Pezzi di UI -------------------------------------------------------

@Composable
private fun Card(content: @Composable ColumnScope.() -> Unit) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        content = content
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/** Riquadro con un solo numero: quattro di questi formano la griglia. */
@Composable
private fun Stat(label: String, value: String, modifier: Modifier = Modifier) {

    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {

        Text(text = label, fontSize = 11.sp, color = colors.onSurfaceVariant)

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun Figure(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false
) {
    val colors = MaterialTheme.colorScheme

    Column(modifier = modifier) {
        Text(text = label, fontSize = 12.sp, color = colors.onSurfaceVariant)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (highlight) colors.error else colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun BudgetBar(fraction: Float) {
    val colors = MaterialTheme.colorScheme
    val safe = fraction.coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(colors.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(safe)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(if (fraction > 1f) colors.error else colors.primary)
        )
    }
}

@Composable
private fun LegendRow(slice: Slice) {
    val colors = MaterialTheme.colorScheme

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {

        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(slice.color)
        )

        Spacer(modifier = Modifier.size(10.dp))

        Text(
            text = slice.label,
            fontSize = 14.sp,
            color = colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = money(slice.amount),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = colors.onSurface
        )

        Spacer(modifier = Modifier.size(10.dp))

        Text(
            text = "${(slice.fraction * 100).toInt()}%",
            fontSize = 13.sp,
            color = colors.onSurfaceVariant
        )
    }
}

@Composable
private fun MeterRow(slice: Slice) {

    val colors = MaterialTheme.colorScheme

    Column(modifier = Modifier.fillMaxWidth()) {

        Row(verticalAlignment = Alignment.CenterVertically) {

            Text(
                text = slice.label,
                fontSize = 14.sp,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = money(slice.amount),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface
            )
        }

        Spacer(modifier = Modifier.height(7.dp))

        MeterBar(fraction = slice.fraction, color = slice.color)
    }
}

@Composable
private fun Note(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 24.dp)
    )
}

@Composable
private fun EmptyState() {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No expenses this month",
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Expenses you add with Quick Add will appear here.",
            fontSize = 14.sp,
            color = colors.onSurfaceVariant
        )
    }
}
