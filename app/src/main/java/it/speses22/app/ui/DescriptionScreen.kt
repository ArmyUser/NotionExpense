package it.speses22.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.speses22.app.ui.theme.Spese
import it.speses22.app.ui.theme.SpeseMotion
import it.speses22.app.ui.theme.SpeseShapes

/**
 * Passo 2: descrizione.
 *
 * Il campo prende il fuoco all'ingresso, così la tastiera di sistema entra
 * mentre il contenuto della card sta ancora sfumando.
 */
@Composable
fun DescriptionStep(
    description: String,
    onDescriptionChange: (String) -> Unit,
    onSubmit: () -> Unit
) {

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    val borderColor by animateColorAsState(
        targetValue = if (focused) Spese.Accent else Spese.Divider,
        animationSpec = tween(SpeseMotion.StepEnterMillis),
        label = "fieldBorder"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (focused) Spese.Surface else Spese.SurfaceSunken,
        animationSpec = tween(SpeseMotion.StepEnterMillis),
        label = "fieldBackground"
    )

    Column(modifier = Modifier.fillMaxWidth()) {

        BasicTextField(
            value = description,
            onValueChange = onDescriptionChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 104.dp)
                .focusRequester(focusRequester),
            interactionSource = interactionSource,
            textStyle = TextStyle(
                fontSize = 17.sp,
                fontWeight = FontWeight.Normal,
                color = Spese.TextPrimary,
                lineHeight = 24.sp
            ),
            cursorBrush = SolidColor(Spese.Accent),
            maxLines = 4,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (description.isNotBlank()) {
                        keyboardController?.hide()
                        onSubmit()
                    }
                }
            ),
            decorationBox = { innerTextField ->

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 104.dp)
                        .background(backgroundColor, SpeseShapes.Field)
                        .border(1.dp, borderColor, SpeseShapes.Field)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {

                    if (description.isEmpty()) {

                        Text(
                            text = "What did you spend on?",
                            fontSize = 17.sp,
                            color = Spese.TextTertiary
                        )
                    }

                    innerTextField()
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Tip: keep it short — “Groceries at Esselunga”",
            fontSize = 13.sp,
            color = Spese.TextTertiary
        )
    }
}
