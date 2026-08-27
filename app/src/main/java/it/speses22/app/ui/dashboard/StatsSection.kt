package it.speses22.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.speses22.app.ui.theme.Spese

/**
 * Statistiche calcolate solo da cio' che Notion restituisce davvero.
 *
 * Niente confronto col periodo precedente: richiederebbe una seconda query e
 * viene lasciato come estensione, non inventato.
 */
@Composable
fun StatsSection(data: PeriodData) {

    val expenses = data.expenses
    val total = expenses.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {

        if (data.loading) {
            Hint("Loading from Notion…")
            return@Column
        }

        if (data.failed) {
            Hint("Could not read from Notion.")
            return@Column
        }

        if (expenses.isEmpty()) {
            Hint("Nothing to analyse in this period yet.")
            return@Column
        }

        Card {
            Row(modifier = Modifier.fillMaxWidth()) {
                Metric("Total", money(total), Modifier.weight(1f))
                Metric("Transactions", expenses.size.toString(), Modifier.weight(1f))
                Metric("Average", money(total / expenses.size), Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "BY CATEGORY",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            color = Spese.TextTertiary
        )

        Spacer(modifier = Modifier.height(8.dp))

        val byCategory = expenses
            .groupBy { it.categoryName ?: "Uncategorised" }
            .map { (name, rows) -> name to rows.sumOf { it.amount } }
            .sortedByDescending { it.second }

        byCategory.forEach { (name, amount) ->
            CategoryBar(
                name = name,
                amount = amount,
                fraction = if (total > 0) (amount / total).toFloat() else 0f
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}


@Composable
private fun CategoryBar(name: String, amount: Double, fraction: Float) {

    Column(modifier = Modifier.fillMaxWidth()) {

        Row(modifier = Modifier.fillMaxWidth()) {

            Text(
                text = name,
                fontSize = 14.sp,
                color = Spese.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = money(amount),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Spese.TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Spese.SurfaceSunken)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Spese.Accent)
            )
        }
    }
}
