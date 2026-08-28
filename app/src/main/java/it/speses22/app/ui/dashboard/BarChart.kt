package it.speses22.app.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.speses22.app.data.ExpenseRecord
import java.time.LocalDate
import java.time.YearMonth

/**
 * Spesa giorno per giorno del mese, disegnata su Canvas.
 *
 * Nessuna libreria di grafici: la ciambella e' gia' fatta cosi' e restare
 * coerenti costa meno di una dipendenza in piu'.
 */
@Composable
fun DailyBars(
    month: YearMonth,
    expenses: List<ExpenseRecord>,
    modifier: Modifier = Modifier,
    height: Dp = 118.dp
) {

    val colors = MaterialTheme.colorScheme

    val days = month.lengthOfMonth()

    val totals = DoubleArray(days)

    expenses.forEach { record ->
        val index = record.date.dayOfMonth - 1
        if (record.date.year == month.year &&
            record.date.month == month.month &&
            index in 0 until days
        ) {
            totals[index] += record.amount
        }
    }

    val peak = totals.maxOrNull() ?: 0.0

    // Media sui soli giorni con almeno una spesa: la riga tratteggiata deve
    // dire "quanto spendo quando spendo", non diluire sui giorni fermi.
    val active = totals.count { it > 0.0 }
    val mean = if (active > 0) totals.sum() / active else 0.0

    val bar = colors.primary
    val track = colors.surfaceVariant
    val guide = colors.onSurfaceVariant.copy(alpha = 0.35f)

    val today = LocalDate.now()
    val currentDay = if (YearMonth.from(today) == month) today.dayOfMonth else -1

    Column(modifier = modifier.fillMaxWidth()) {

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
        ) {

            val slot = size.width / days
            val width = (slot * 0.58f).coerceAtLeast(1.5f)
            val radius = CornerRadius(width / 2f, width / 2f)
            val floor = 2f

            if (peak > 0.0 && mean > 0.0) {

                val y = size.height - (mean / peak).toFloat() * (size.height - floor)

                drawLine(
                    color = guide,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f))
                )
            }

            totals.forEachIndexed { index, amount ->

                val filled = peak > 0.0 && amount > 0.0

                val tall =
                    if (filled) (amount / peak).toFloat() * (size.height - floor) + floor
                    else floor

                val left = index * slot + (slot - width) / 2f

                drawRoundRect(
                    color = when {
                        !filled -> track
                        index + 1 == currentDay -> bar
                        else -> bar.copy(alpha = 0.85f)
                    },
                    topLeft = Offset(left, size.height - tall),
                    size = Size(width, tall),
                    cornerRadius = radius
                )
            }
        }

        // Le tre etichette cadono all'inizio, al centro e alla fine della
        // stessa larghezza delle barre, non a un terzo l'una dall'altra.
        Row(modifier = Modifier.fillMaxWidth()) {
            Axis("1", TextAlign.Start, Modifier.weight(1f))
            Axis("${(days + 1) / 2}", TextAlign.Center, Modifier.weight(1f))
            Axis("$days", TextAlign.End, Modifier.weight(1f))
        }
    }
}


@Composable
private fun Axis(text: String, align: TextAlign, modifier: Modifier) {
    Text(
        text = text,
        fontSize = 10.sp,
        textAlign = align,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}


/**
 * Barra orizzontale singola: usata per la ripartizione per conto, dove le fette
 * di una ciambella sarebbero troppo poche per valere il disegno.
 */
@Composable
fun MeterBar(
    fraction: Float,
    color: Color,
    modifier: Modifier = Modifier,
    thickness: Dp = 6.dp
) {

    val track = MaterialTheme.colorScheme.surfaceVariant
    val safe = fraction.coerceIn(0f, 1f)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
    ) {

        val radius = CornerRadius(size.height / 2f, size.height / 2f)

        drawRoundRect(color = track, cornerRadius = radius)

        if (safe > 0f) {
            drawRoundRect(
                color = color,
                size = Size((size.width * safe).coerceAtLeast(size.height), size.height),
                cornerRadius = radius
            )
        }
    }
}
