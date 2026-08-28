package it.speses22.app.ui.dashboard

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.speses22.app.MainActivity
import it.speses22.app.data.AppPreferences
import it.speses22.app.data.DashboardSources
import it.speses22.app.data.ExpenseRecord
import it.speses22.app.data.ThemeMode
import java.time.YearMonth
import java.time.temporal.ChronoUnit

enum class Destination(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Filled.Home),
    Expenses("Expenses", Icons.AutoMirrored.Filled.List),
    Settings("Settings", Icons.Filled.Settings)
}

/** Dati letti da Notion per il periodo selezionato. */
data class PeriodData(
    val expenses: List<ExpenseRecord> = emptyList(),
    val budget: Double? = null,
    val loading: Boolean = true,
    val failed: Boolean = false
)

private val MaxContentWidth = 560.dp


@Composable
fun DashboardApp() {

    val context = LocalContext.current

    var themeMode by remember { mutableStateOf(AppPreferences.themeMode(context)) }

    DashboardTheme(mode = themeMode) {

        var destination by rememberSaveable { mutableStateOf(Destination.Home) }
        var monthOffset by rememberSaveable { mutableStateOf(0) }
        var data by remember { mutableStateOf(PeriodData()) }

        val scroll = rememberScrollState()

        // Ogni scheda riparte dall'alto: lo scorrimento e' condiviso, e
        // arrivare su Expenses a meta' elenco sembrava un salto.
        LaunchedEffect(destination) { scroll.scrollTo(0) }

        // Il mese e' scelto su Home e vale anche per lo storico.
        val month = remember(monthOffset) {
            YearMonth.now().plusMonths(monthOffset.toLong())
        }

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

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = { BottomBar(destination) { destination = it } },
            floatingActionButton = {
                // Solo su Home ed Expenses: in Settings non serve.
                if (destination != Destination.Settings) {
                    FloatingActionButton(
                        onClick = {
                            // Avvia il Quick Add esistente, senza modificarlo.
                            context.startActivity(Intent(context, MainActivity::class.java))
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = CircleShape,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Text(text = "+", fontSize = 26.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {

                Column(
                    modifier = Modifier
                        .widthIn(max = MaxContentWidth)
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                        .verticalScroll(scroll)
                        .padding(horizontal = 18.dp)
                ) {

                    Spacer(modifier = Modifier.height(10.dp))

                    when (destination) {

                        Destination.Home -> {
                            MonthHeader(
                                month = month,
                                onPrevious = { monthOffset -= 1 },
                                onNext = { monthOffset += 1 }
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            HomeScreen(data = data, month = month)
                        }

                        Destination.Expenses -> {
                            ScreenTitle("Expenses", monthLabel(month))
                            Spacer(modifier = Modifier.height(14.dp))
                            ExpensesScreen(
                                data = data,
                                month = month,
                                onMonthChange = { chosen ->
                                    monthOffset = ChronoUnit.MONTHS
                                        .between(YearMonth.now(), chosen)
                                        .toInt()
                                },
                                onDeleted = { id ->
                                    // Notion ha gia' confermato: si toglie la riga
                                    // invece di rileggere tutto il mese.
                                    data = data.copy(
                                        expenses = data.expenses.filterNot { it.id == id }
                                    )
                                }
                            )
                        }

                        Destination.Settings -> {
                            // Nessun selettore di mese qui.
                            ScreenTitle("Settings", null)
                            Spacer(modifier = Modifier.height(14.dp))
                            SettingsScreen(
                                themeMode = themeMode,
                                onThemeChange = { chosen ->
                                    themeMode = chosen
                                    AppPreferences.setThemeMode(context, chosen)
                                }
                            )
                        }
                    }

                    // Spazio perche' il FAB non copra l'ultima riga.
                    Spacer(modifier = Modifier.height(96.dp))
                }
            }
        }
    }
}


@Composable
private fun MonthHeader(
    month: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {

    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(modifier = Modifier.weight(1f)) {

            Text(
                text = monthLabel(month),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground
            )

            Text(
                text = "Notion Expense",
                fontSize = 12.sp,
                color = colors.onSurfaceVariant
            )
        }

        Arrow(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous month", onPrevious)

        Spacer(modifier = Modifier.size(8.dp))

        Arrow(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next month", onNext)
    }
}


@Composable
private fun Arrow(icon: ImageVector, description: String, onClick: () -> Unit) {

    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = colors.onSurfaceVariant
        )
    }
}


@Composable
private fun ScreenTitle(title: String, subtitle: String?) {

    val colors = MaterialTheme.colorScheme

    Column(modifier = Modifier.fillMaxWidth()) {

        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = colors.onBackground
        )

        subtitle?.let {
            Text(text = it, fontSize = 12.sp, color = colors.onSurfaceVariant)
        }
    }
}


@Composable
private fun BottomBar(current: Destination, onSelect: (Destination) -> Unit) {

    val colors = MaterialTheme.colorScheme

    NavigationBar(
        containerColor = colors.surface,
        tonalElevation = 0.dp
    ) {
        Destination.entries.forEach { entry ->

            NavigationBarItem(
                selected = entry == current,
                onClick = { onSelect(entry) },
                icon = {
                    Icon(imageVector = entry.icon, contentDescription = entry.label)
                },
                label = {
                    Text(text = entry.label, fontSize = 11.sp)
                },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colors.onPrimaryContainer,
                    indicatorColor = colors.primaryContainer,
                    unselectedIconColor = colors.onSurfaceVariant,
                    selectedTextColor = colors.onSurface,
                    unselectedTextColor = colors.onSurfaceVariant
                )
            )
        }
    }
}
