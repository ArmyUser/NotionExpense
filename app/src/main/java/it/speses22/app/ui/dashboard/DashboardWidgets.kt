package it.speses22.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.speses22.app.ui.theme.Spese
import it.speses22.app.ui.theme.SpeseShapes

/** Contenitore condiviso: stessa superficie della card del Quick Add. */
@Composable
fun Card(content: @Composable ColumnScope.() -> Unit) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SpeseShapes.Card)
            .background(Spese.Surface)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        content = content
    )
}


@Composable
fun Metric(label: String, value: String, modifier: Modifier = Modifier) {

    Column(modifier = modifier) {

        Text(
            text = label,
            fontSize = 11.sp,
            color = Spese.TextTertiary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Spese.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}


/** Messaggio neutro per stati vuoti, caricamento ed errore. */
@Composable
fun Hint(text: String) {

    Text(
        text = text,
        fontSize = 14.sp,
        color = Spese.TextSecondary,
        modifier = Modifier.padding(vertical = 10.dp)
    )
}
