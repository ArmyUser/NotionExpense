package it.speses22.app.ui.dashboard

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.speses22.app.data.DashboardSources
import it.speses22.app.data.ExpenseRecord
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset

/** Come ordinare l'elenco. Il piu' caro e il piu' economico sono due ordinamenti. */
private enum class SortOrder(val label: String) {
    Newest("Newest first"),
    Oldest("Oldest first"),
    Highest("Highest amount"),
    Lowest("Lowest amount")
}

private const val NoDay = -1L

/**
 * Storico completo del mese, filtrabile.
 *
 * Il mese e' quello scelto su Home; il calendario qui dentro puo' spostarlo e
 * in piu' restringe la vista a un solo giorno.
 */
@Composable
fun ExpensesScreen(
    data: PeriodData,
    month: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    onDeleted: (String) -> Unit
) {

    val colors = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()

    // LocalDate non e' salvabile in un Bundle: si conserva il giorno epoch.
    var dayEpoch by rememberSaveable { mutableStateOf(NoDay) }
    var category by rememberSaveable { mutableStateOf<String?>(null) }
    var account by rememberSaveable { mutableStateOf<String?>(null) }
    var sort by rememberSaveable { mutableStateOf(SortOrder.Newest) }

    var calendarOpen by remember { mutableStateOf(false) }
    var pending by remember { mutableStateOf<ExpenseRecord?>(null) }
    var deleting by remember { mutableStateOf(false) }
    var failure by remember { mutableStateOf<String?>(null) }

    val day = dayEpoch.takeIf { it != NoDay }?.let { LocalDate.ofEpochDay(it) }

    // Cambiando mese dalle frecce di Home il giorno scelto non c'entra piu'.
    LaunchedEffect(month) {
        if (day != null && YearMonth.from(day) != month) dayEpoch = NoDay
    }

    val categories = data.expenses.mapNotNull { it.categoryName }.distinct().sorted()
    val accounts = data.expenses.mapNotNull { it.accountName }.distinct().sorted()

    val visible = data.expenses
        .filter { day == null || it.date == day }
        .filter { category == null || it.categoryName == category }
        .filter { account == null || it.accountName == account }
        .let { rows ->
            when (sort) {
                SortOrder.Newest -> rows.sortedWith(compareByDescending<ExpenseRecord> { it.date }.thenByDescending { it.amount })
                SortOrder.Oldest -> rows.sortedWith(compareBy<ExpenseRecord> { it.date }.thenByDescending { it.amount })
                SortOrder.Highest -> rows.sortedByDescending { it.amount }
                SortOrder.Lowest -> rows.sortedBy { it.amount }
            }
        }

    val filtered = day != null || category != null || account != null

    Column(modifier = Modifier.fillMaxWidth()) {

        // ---- Filtri --------------------------------------------------------

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Chip(
                value = day?.let { compactDate(it) } ?: "Whole month",
                active = day != null,
                icon = true,
                modifier = Modifier.weight(1f),
                onClick = { calendarOpen = true }
            )

            PickerChip(
                value = sort.label,
                active = sort != SortOrder.Newest,
                options = SortOrder.entries.map { it.label },
                selected = sort.label,
                resetLabel = null,
                onSelect = { chosen ->
                    sort = SortOrder.entries.firstOrNull { it.label == chosen } ?: SortOrder.Newest
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            PickerChip(
                value = category ?: "All categories",
                active = category != null,
                options = categories,
                selected = category,
                resetLabel = "All categories",
                onSelect = { category = it },
                modifier = Modifier.weight(1f)
            )

            PickerChip(
                value = account ?: "All accounts",
                active = account != null,
                options = accounts,
                selected = account,
                resetLabel = "All accounts",
                onSelect = { account = it },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ---- Riepilogo di cio' che si sta guardando -------------------------

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = when (visible.size) {
                    1 -> "1 expense · ${money(visible.sumOf { it.amount })}"
                    else -> "${visible.size} expenses · ${money(visible.sumOf { it.amount })}"
                },
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = colors.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )

            if (filtered) {
                Text(
                    text = "Clear",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            dayEpoch = NoDay
                            category = null
                            account = null
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ---- Elenco --------------------------------------------------------

        when {
            data.loading -> Info("Loading from Notion…")

            data.failed -> Info("Could not read from Notion. Check the connection in Settings.")

            data.expenses.isEmpty() -> Info(
                "No expenses this month.\n" +
                    "Expenses you add with Quick Add will appear here."
            )

            visible.isEmpty() -> Info("No expenses match these filters.")

            // Ordinando per data i separatori di giorno aiutano a leggere;
            // ordinando per importo spezzerebbero la classifica.
            sort == SortOrder.Newest || sort == SortOrder.Oldest -> {

                val byDay = visible.groupBy { it.date }.toList()

                byDay.forEach { (date, rows) ->

                    DayHeader(date, rows.sumOf { it.amount })

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.surface)
                    ) {
                        rows.forEach { record ->
                            ExpenseRow(record, showDate = false) { pending = record }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            else -> {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.surface)
                ) {
                    visible.forEach { record ->
                        ExpenseRow(record, showDate = true) { pending = record }
                    }
                }
            }
        }

        if (visible.isNotEmpty()) {

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Tap an expense to delete it.",
                fontSize = 12.sp,
                color = colors.onSurfaceVariant
            )
        }
    }

    // ---- Calendario --------------------------------------------------------

    if (calendarOpen) {
        DayPicker(
            initial = day ?: month.atDay(1),
            onDismiss = { calendarOpen = false },
            onWholeMonth = {
                dayEpoch = NoDay
                calendarOpen = false
            },
            onPick = { picked ->
                if (YearMonth.from(picked) != month) onMonthChange(YearMonth.from(picked))
                dayEpoch = picked.toEpochDay()
                calendarOpen = false
            }
        )
    }

    // ---- Conferma di cancellazione ----------------------------------------

    pending?.let { record ->

        AlertDialog(
            onDismissRequest = { if (!deleting) pending = null },
            title = { Text("Delete expense?") },
            text = {
                Text(
                    "${record.description.ifBlank { "Untitled" }} · ${money(record.amount)}\n\n" +
                        "It is moved to the Notion trash, where it can still be restored."
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !deleting,
                    onClick = {
                        deleting = true
                        failure = null
                        scope.launch {
                            val done = DashboardSources.default().delete(record.id)
                            deleting = false
                            if (done) {
                                onDeleted(record.id)
                                pending = null
                            } else {
                                failure = "Could not delete it in Notion. " +
                                    "The integration may not have update access."
                                pending = null
                            }
                        }
                    }
                ) {
                    Text(if (deleting) "Deleting…" else "Delete")
                }
            },
            dismissButton = {
                TextButton(enabled = !deleting, onClick = { pending = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    failure?.let { message ->

        AlertDialog(
            onDismissRequest = { failure = null },
            title = { Text("Not deleted") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { failure = null }) { Text("OK") }
            }
        )
    }
}


// ---- Pezzi di UI -------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayPicker(
    initial: LocalDate,
    onDismiss: () -> Unit,
    onWholeMonth: () -> Unit,
    onPick: (LocalDate) -> Unit
) {

    val state = rememberDatePickerState(
        initialSelectedDateMillis = initial.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    state.selectedDateMillis?.let { millis ->
                        onPick(
                            Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        )
                    } ?: onDismiss()
                }
            ) {
                Text("Show day")
            }
        },
        dismissButton = {
            TextButton(onClick = onWholeMonth) { Text("Whole month") }
        }
    ) {
        DatePicker(state = state, showModeToggle = false)
    }
}

@Composable
private fun Chip(
    value: String,
    active: Boolean,
    modifier: Modifier = Modifier,
    icon: Boolean = false,
    onClick: () -> Unit
) {

    val colors = MaterialTheme.colorScheme

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (active) colors.primaryContainer else colors.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        if (icon) {

            Icon(
                imageVector = Icons.Filled.DateRange,
                contentDescription = null,
                tint = if (active) colors.onPrimaryContainer else colors.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.size(8.dp))
        }

        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
            color = if (active) colors.onPrimaryContainer else colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "▾",
            fontSize = 11.sp,
            color = if (active) colors.onPrimaryContainer else colors.onSurfaceVariant
        )
    }
}

/**
 * Chip che apre il proprio menu.
 *
 * [resetLabel] non nullo aggiunge in cima la voce che toglie il filtro; per
 * l'ordinamento, dove una scelta c'e' sempre, resta null.
 */
@Composable
private fun PickerChip(
    value: String,
    active: Boolean,
    options: List<String>,
    selected: String?,
    resetLabel: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier
) {

    var open by remember { mutableStateOf(false) }

    Box(modifier = modifier) {

        Chip(
            value = value,
            active = active,
            modifier = Modifier.fillMaxWidth(),
            onClick = { open = true }
        )

        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {

            if (options.isEmpty() && resetLabel != null) {
                DropdownMenuItem(
                    text = { Text("Nothing to filter", fontSize = 14.sp) },
                    enabled = false,
                    onClick = {}
                )
            }

            resetLabel?.let { label ->
                MenuRow(label, selected == null) {
                    onSelect(null)
                    open = false
                }
            }

            options.forEach { option ->
                MenuRow(option, option == selected) {
                    onSelect(option)
                    open = false
                }
            }
        }
    }
}

@Composable
private fun MenuRow(label: String, checked: Boolean, onClick: () -> Unit) {

    DropdownMenuItem(
        text = { Text(label, fontSize = 14.sp) },
        trailingIcon = {
            if (checked) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        onClick = onClick
    )
}

@Composable
private fun DayHeader(date: LocalDate, total: Double) {

    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = longDate(date),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.6.sp,
            color = colors.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )

        Text(text = money(total), fontSize = 12.sp, color = colors.onSurfaceVariant)
    }
}

@Composable
private fun ExpenseRow(
    record: ExpenseRecord,
    showDate: Boolean,
    onClick: () -> Unit
) {

    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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

            val meta = buildList {
                if (showDate) add(shortDate(record.date))
                record.categoryName?.let { add(it) }
                record.accountName?.let { add(it) }
            }

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

        Spacer(modifier = Modifier.size(10.dp))

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
