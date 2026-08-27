package it.speses22.app.ui.dashboard

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.speses22.app.MainActivity
import it.speses22.app.data.DashboardSources
import it.speses22.app.data.ExpenseRecord
import it.speses22.app.ui.components.SpeseSurfaceButton
import it.speses22.app.ui.theme.Spese
import java.time.YearMonth

enum class DashboardSection(val label: String) {
    Home("HOME"),
    Stats("STATS"),
    Settings("SETTINGS")
}

/** Stato dei dati letti da Notion per il periodo selezionato. */
data class PeriodData(
    val expenses: List<ExpenseRecord> = emptyList(),
    val budget: Double? = null,
    val loading: Boolean = true,
    val failed: Boolean = false
)


@Composable
fun DashboardApp() {

    val context = LocalContext.current

    var section by rememberSaveable { mutableStateOf(DashboardSection.Home) }
    var monthOffset by rememberSaveable { mutableStateOf(0) }
    var data by remember { mutableStateOf(PeriodData()) }

    val month = remember(monthOffset) { YearMonth.now().plusMonths(monthOffset.toLong()) }

    // Notion resta la fonte: nessuna copia locale delle spese.
    LaunchedEffect(month) {

        data = PeriodData(loading = true)

        val source = DashboardSources.default()
        val loaded = source.expenses(month.atDay(1), month.atEndOfMonth())

        data = PeriodData(
            expenses = loaded.orEmpty(),
            budget = source.monthlyBudget(),
            loading = false,
            failed = loaded == null
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Spese.Background)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 20.dp)
        ) {

            Header(
                month = month,
                onPrevious = { monthOffset -= 1 },
                onNext = { monthOffset += 1 }
            )

            Spacer(modifier = Modifier.height(18.dp))

            Box(modifier = Modifier.weight(1f)) {

                when (section) {

                    DashboardSection.Home -> HomeSection(
                        month = month,
                        data = data,
                        onAddExpense = {
                            // Riusa esattamente il flusso esistente.
                            context.startActivity(Intent(context, MainActivity::class.java))
                        }
                    )

                    DashboardSection.Stats -> StatsSection(data = data)

                    DashboardSection.Settings -> SettingsSection(data = data)
                }
            }

            SectionBar(
                current = section,
                onSelect = { section = it }
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}


@Composable
private fun Header(
    month: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {

    Column(modifier = Modifier.padding(top = 20.dp)) {

        Text(
            text = "Notion Expense",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Spese.TextPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {

            MonthArrow(glyph = "‹", onClick = onPrevious)

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = monthLabel(month),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Spese.TextSecondary
            )

            Spacer(modifier = Modifier.weight(1f))

            MonthArrow(glyph = "›", onClick = onNext)
        }
    }
}


@Composable
private fun MonthArrow(glyph: String, onClick: () -> Unit) {

    SpeseSurfaceButton(
        onClick = onClick,
        modifier = Modifier.heightIn(min = 36.dp),
        shape = RoundedCornerShape(10.dp),
        hapticFeedback = true
    ) {
        Box(
            modifier = Modifier
                .heightIn(min = 36.dp)
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = glyph, fontSize = 18.sp, color = Spese.TextSecondary)
        }
    }
}


@Composable
private fun SectionBar(
    current: DashboardSection,
    onSelect: (DashboardSection) -> Unit
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        DashboardSection.entries.forEach { entry ->

            val selected = entry == current

            SpeseSurfaceButton(
                onClick = { onSelect(entry) },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp),
                shape = RoundedCornerShape(12.dp),
                container = if (selected) Spese.AccentSoft else Spese.SurfaceSunken,
                hapticFeedback = true
            ) {
                Box(
                    modifier = Modifier.heightIn(min = 44.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = entry.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        color = if (selected) Spese.TextPrimary else Spese.TextTertiary
                    )
                }
            }
        }
    }
}
