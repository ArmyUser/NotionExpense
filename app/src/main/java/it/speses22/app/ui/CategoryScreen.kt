package it.speses22.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.speses22.app.data.NotionRef
import it.speses22.app.ui.components.SpeseButton
import it.speses22.app.ui.components.SpeseButtonStyle
import it.speses22.app.ui.components.SpeseSurfaceButton
import it.speses22.app.ui.components.pressScale
import it.speses22.app.ui.theme.Spese
import it.speses22.app.ui.theme.SpeseMotion
import it.speses22.app.ui.theme.SpeseShapes
import java.time.LocalDate

/** Glifo unico: le categorie Notion usano icone interne, non emoji. */
const val CategoryGlyph = "🏷️"

/** Sotto questa soglia la ricerca sarebbe solo rumore. */
private const val SearchThreshold = 8


/**
 * Passo 5: categoria e riepilogo.
 *
 * Il campo apre una schermata dedicata invece di un foglio: cosi' scorrere
 * l'elenco non puo' chiuderlo per sbaglio.
 */
@Composable
fun CategoryStep(
    amount: String,
    description: String,
    date: LocalDate,
    selectedCategoryName: String?,
    onOpenPicker: () -> Unit,
    onEditAmount: () -> Unit,
    onEditDescription: () -> Unit,
    onEditDate: () -> Unit
) {

    Column(modifier = Modifier.fillMaxWidth()) {

        CategoryPickerButton(
            selectedCategoryName = selectedCategoryName,
            onClick = onOpenPicker
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "SUMMARY",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            color = Spese.TextTertiary
        )

        Spacer(modifier = Modifier.height(8.dp))

        SummaryRow(label = "Amount", value = "€ $amount", onClick = onEditAmount)

        SummaryRow(
            label = "Description",
            value = description.ifBlank { "—" },
            onClick = onEditDescription
        )

        SummaryRow(
            label = "Date",
            value = formatSelectedDate(date),
            onClick = onEditDate
        )
    }
}


/**
 * Schermata dedicata di scelta categoria.
 *
 * Vive dentro la card come gli altri passi, quindi eredita la stessa
 * navigazione: si esce scegliendo, oppure con BACK.
 */
@Composable
fun CategoryPickerScreen(
    categories: List<NotionRef>,
    selectedCategoryId: String?,
    onSelect: (NotionRef) -> Unit,
    onBack: () -> Unit
) {

    var query by remember { mutableStateOf("") }

    val visible = remember(categories, query) {
        if (query.isBlank()) {
            categories
        } else {
            categories.filter { it.name.contains(query, ignoreCase = true) }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {

        Text(
            text = "CATEGORY",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.2.sp,
            color = Spese.TextTertiary
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (categories.size >= SearchThreshold) {

            RefSearchField(query = query, onQueryChange = { query = it })

            Spacer(modifier = Modifier.height(12.dp))
        }

        RefList(
            refs = visible,
            selectedId = selectedCategoryId,
            onSelect = onSelect,
            glyph = CategoryGlyph,
            emptyMessage = if (categories.isEmpty()) {
                "No categories available offline.\n" +
                    "The expense will be saved without a category."
            } else {
                "No category matches “$query”."
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        SpeseButton(
            text = "BACK",
            onClick = onBack,
            style = SpeseButtonStyle.Secondary,
            modifier = Modifier.fillMaxWidth()
        )
    }
}


@Composable
private fun CategoryPickerButton(
    selectedCategoryName: String?,
    onClick: () -> Unit
) {

    val chosen = selectedCategoryName != null

    val container by animateColorAsState(
        targetValue = if (chosen) Spese.AccentSoft else Spese.SurfaceSunken,
        animationSpec = tween(SpeseMotion.StepEnterMillis),
        label = "pickerContainer"
    )

    SpeseSurfaceButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = SpeseShapes.Field,
        container = container,
        containerPressed = if (chosen) Spese.AccentSoft else Spese.SurfaceSunkenPressed
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            AnimatedContent(
                targetState = selectedCategoryName,
                transitionSpec = {
                    fadeIn(tween(SpeseMotion.StepEnterMillis)) togetherWith
                        fadeOut(tween(SpeseMotion.StepExitMillis))
                },
                label = "pickerLabel"
            ) { name ->

                Row(verticalAlignment = Alignment.CenterVertically) {

                    Text(text = CategoryGlyph, fontSize = 20.sp)

                    Spacer(modifier = Modifier.size(12.dp))

                    Text(
                        text = name ?: "Choose a category",
                        fontSize = 17.sp,
                        fontWeight = if (name != null) {
                            FontWeight.SemiBold
                        } else {
                            FontWeight.Normal
                        },
                        color = if (name != null) {
                            Spese.TextPrimary
                        } else {
                            Spese.TextSecondary
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(text = "›", fontSize = 22.sp, color = Spese.TextTertiary)
        }
    }
}


@Composable
private fun SummaryRow(
    label: String,
    value: String,
    onClick: () -> Unit
) {

    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interactionSource, pressedScale = 0.985f)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(text = label, fontSize = 15.sp, color = Spese.TextSecondary)

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Spese.TextPrimary,
            maxLines = 1
        )

        Spacer(modifier = Modifier.size(8.dp))

        Text(text = "›", fontSize = 17.sp, color = Spese.TextTertiary)
    }
}
