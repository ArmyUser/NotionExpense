package it.speses22.app.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Una fetta del grafico: nome, importo e quota sul totale. */
data class Slice(
    val label: String,
    val amount: Double,
    val fraction: Float,
    val color: Color
)

/**
 * Ciambella disegnata su Canvas: nessuna libreria di grafici aggiunta.
 *
 * Con zero spese resta un anello vuoto, invece di inventare dati.
 */
@Composable
fun DonutChart(
    slices: List<Slice>,
    total: String,
    modifier: Modifier = Modifier,
    diameter: androidx.compose.ui.unit.Dp = 172.dp
) {

    val colors = MaterialTheme.colorScheme
    val emptyRing = colors.surfaceVariant

    Box(modifier = modifier.size(diameter), contentAlignment = Alignment.Center) {

        Canvas(modifier = Modifier.size(diameter)) {

            val thickness = size.minDimension * 0.17f
            val inset = thickness / 2f
            val arcSize = Size(size.width - thickness, size.height - thickness)
            val topLeft = Offset(inset, inset)

            if (slices.isEmpty()) {

                drawArc(
                    color = emptyRing,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = thickness)
                )

                return@Canvas
            }

            // Piccolo stacco fra le fette per leggerle anche quando sono sottili.
            val gap = if (slices.size > 1) 2f else 0f
            var start = -90f

            slices.forEach { slice ->

                val sweep = (slice.fraction * 360f) - gap

                if (sweep > 0f) {
                    drawArc(
                        color = slice.color,
                        startAngle = start,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = thickness)
                    )
                }

                start += slice.fraction * 360f
            }
        }

        Text(
            text = total,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface
        )
    }
}
