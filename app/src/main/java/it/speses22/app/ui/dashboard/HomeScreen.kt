package it.speses22.app.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.speses22.app.data.ExpenseRecord

private enum class Grouping(val label: String) {
    Categories("Categories"),
    Accounts("Accounts")
}

/** Un gruppo esplorabile: categoria o conto, con le sue spese. */
private data class Group(
    val name: String,
    val total: Double,
    val fraction: Float,
    val items: List<ExpenseRecord>
)

@Composable
fun HomeScreen(data: PeriodData) {

    val colors = MaterialTheme.colorScheme
    val total = data.expenses.sumOf { it.amount }

    var grouping by rememberSaveable { mutableStateOf(Grouping.Categories) }
    var expanded by rememberSaveable { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {

        // ---- Totale e budget ----
        Surface {

            Text(
                text = "Total spent",
                fontSize = 13.sp,
                color = colors.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = money(total),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )

            data.budget?.let { budget ->

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Figure("Budget", money(budget), Modifier.weight(1f))
                    Figure(
                        label = "Remaining",
                        value = money(budget - total),
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

        // ---- Ripartizione per categoria ----
        val categorySlices = slices(data.expenses) { it.categoryName }

        Surface {

            SectionTitle("EXPENSE BY CATEGORY")

            Spacer(modifier = Modifier.height(14.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                DonutChart(slices = categorySlices, total = money(total))
            }

            Spacer(modifier = Modifier.height(16.dp))

            categorySlices.forEach { slice ->
                LegendRow(slice)
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ---- Esplorazione per categoria o conto ----
        SectionTitle("EXPENSES")

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Grouping.entries.forEach { entry ->
                Toggle(
                    label = entry.label,
                    selected = entry == grouping,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        grouping = entry
                        expanded = null
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val groups = groups(data.expenses, grouping)

        groups.forEach { group ->

            GroupRow(
                group = group,
                expanded = expanded == group.name,
                onClick = { expanded = if (expanded == group.name) null else group.name }
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}


// ---- Aggregazioni ------------------------------------------------------

private fun slices(
    expenses: List<ExpenseRecord>,
    key: (ExpenseRecord) -> String?
): List<Slice> {

    val total = expenses.sumOf { it.amount }
    if (total <= 0.0) return emptyList()

    return expenses
        .groupBy { key(it) ?: "Uncategorised" }
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

private fun groups(expenses: List<ExpenseRecord>, grouping: Grouping): List<Group> {

    val total = expenses.sumOf { it.amount }

    return expenses
        .groupBy {
            when (grouping) {
                Grouping.Categories -> it.categoryName ?: "Uncategorised"
                Grouping.Accounts -> it.accountName ?: "No account"
            }
        }
        .map { (name, rows) ->
            val sum = rows.sumOf { it.amount }
            Group(
                name = name,
                total = sum,
                fraction = if (total > 0) (sum / total).toFloat() else 0f,
                items = rows.sortedByDescending { it.date }
            )
        }
        .sortedByDescending { it.total }
}


// ---- Pezzi di UI -------------------------------------------------------

@Composable
private fun Surface(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {

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
private fun Toggle(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) colors.primaryContainer else colors.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant
        )
    }
}

@Composable
private fun GroupRow(group: Group, expanded: Boolean, onClick: () -> Unit) {

    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {

            Text(
                text = group.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = money(group.total),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface
            )

            Spacer(modifier = Modifier.size(8.dp))

            Text(
                text = if (expanded) "▾" else "▸",
                fontSize = 13.sp,
                color = colors.onSurfaceVariant
            )
        }

        AnimatedVisibility(visible = expanded) {

            Column(modifier = Modifier.padding(top = 10.dp)) {

                group.items.forEach { item ->

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                    ) {

                        Text(
                            text = money(item.amount),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.onSurface
                        )

                        Spacer(modifier = Modifier.size(12.dp))

                        Text(
                            text = item.description.ifBlank { "—" },
                            fontSize = 13.sp,
                            color = colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = shortDate(item.date),
                            fontSize = 12.sp,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            }
        }
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
