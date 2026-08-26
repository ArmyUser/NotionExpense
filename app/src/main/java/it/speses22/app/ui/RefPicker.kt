package it.speses22.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.speses22.app.data.NotionRef
import it.speses22.app.ui.components.SpeseSurfaceButton
import it.speses22.app.ui.theme.Spese
import it.speses22.app.ui.theme.SpeseMotion
import it.speses22.app.ui.theme.SpeseShapes

/**
 * Elenco selezionabile condiviso da conti e categorie.
 *
 * E' una lista normale dentro la card, non un foglio trascinabile: scorrere non
 * puo' chiuderla per sbaglio.
 */
@Composable
fun RefList(
    refs: List<NotionRef>,
    selectedId: String?,
    onSelect: (NotionRef) -> Unit,
    glyph: String,
    modifier: Modifier = Modifier,
    emptyMessage: String
) {

    if (refs.isEmpty()) {

        Text(
            text = emptyMessage,
            fontSize = 15.sp,
            color = Spese.TextSecondary,
            modifier = modifier.padding(vertical = 12.dp)
        )

        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 340.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {

        items(items = refs, key = { it.pageId }) { ref ->

            RefRow(
                ref = ref,
                glyph = glyph,
                selected = ref.pageId == selectedId,
                onClick = { onSelect(ref) }
            )
        }
    }
}


@Composable
private fun RefRow(
    ref: NotionRef,
    glyph: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    val container by animateColorAsState(
        targetValue = if (selected) Spese.AccentSoft else Spese.SurfaceSunken,
        animationSpec = tween(SpeseMotion.StepEnterMillis),
        label = "refContainer"
    )

    SpeseSurfaceButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(14.dp),
        container = container,
        containerPressed = Spese.SurfaceSunkenPressed,
        hapticFeedback = true
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(
                        color = if (selected) Spese.Surface else Spese.Divider,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {

                Text(text = glyph, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.size(14.dp))

            Text(
                text = ref.name,
                fontSize = 16.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = Spese.TextPrimary,
                maxLines = 1
            )

            Spacer(modifier = Modifier.weight(1f))

            if (selected) {

                Text(
                    text = "✓",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Spese.Accent
                )
            }
        }
    }
}


/** Campo di ricerca, mostrato solo quando l'elenco e' abbastanza lungo. */
@Composable
fun RefSearchField(
    query: String,
    onQueryChange: (String) -> Unit
) {

    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        textStyle = TextStyle(
            fontSize = 16.sp,
            color = Spese.TextPrimary
        ),
        cursorBrush = SolidColor(Spese.Accent),
        singleLine = true,
        decorationBox = { innerTextField ->

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(Spese.SurfaceSunken, SpeseShapes.Field)
                    .border(1.dp, Spese.Divider, SpeseShapes.Field)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {

                if (query.isEmpty()) {

                    Text(
                        text = "Search",
                        fontSize = 16.sp,
                        color = Spese.TextTertiary
                    )
                }

                innerTextField()
            }
        }
    )
}
