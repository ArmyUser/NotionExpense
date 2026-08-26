package it.speses22.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.speses22.app.ui.components.SpeseSurfaceButton
import it.speses22.app.ui.theme.Spese

/**
 * Passo 1: importo.
 *
 * Solo contenuto: cornice, intestazione e pulsanti vivono in [ExpenseFlow].
 */
@Composable
fun AmountStep(
    amount: String,
    onDigit: (String) -> Unit,
    onComma: () -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit
) {

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        AmountDisplay(amount = amount)

        Spacer(modifier = Modifier.height(20.dp))

        Keypad(
            onDigit = onDigit,
            onComma = onComma,
            onBackspace = onBackspace,
            onClear = onClear
        )
    }
}


@Composable
private fun AmountDisplay(amount: String) {

    val untouched = amount == "0"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom
    ) {

        Text(
            text = "€",
            fontSize = 26.sp,
            fontWeight = FontWeight.Medium,
            color = if (untouched) Spese.TextTertiary else Spese.TextSecondary,
            modifier = Modifier.padding(bottom = 6.dp, end = 8.dp)
        )

        Text(
            text = amount,
            fontSize = 46.sp,
            fontWeight = FontWeight.Bold,
            color = if (untouched) Spese.TextTertiary else Spese.TextPrimary
        )
    }
}


@Composable
private fun Keypad(
    onDigit: (String) -> Unit,
    onComma: () -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit
) {

    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(",", "0", "⌫")
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        rows.forEach { row ->

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                row.forEach { key ->

                    val isBackspace = key == "⌫"

                    SpeseSurfaceButton(
                        onClick = {
                            when (key) {
                                "," -> onComma()
                                "⌫" -> onBackspace()
                                else -> onDigit(key)
                            }
                        },
                        // Tieni premuto il backspace per azzerare.
                        onLongClick = if (isBackspace) onClear else null,
                        hapticFeedback = true,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 56.dp)
                    ) {

                        Box(contentAlignment = Alignment.Center) {

                            Text(
                                text = key,
                                fontSize = if (isBackspace) 20.sp else 22.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isBackspace) {
                                    Spese.TextSecondary
                                } else {
                                    Spese.TextPrimary
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
