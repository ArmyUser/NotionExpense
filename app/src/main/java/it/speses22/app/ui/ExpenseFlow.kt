package it.speses22.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.speses22.app.data.NotionRef
import it.speses22.app.data.isAmountValid
import it.speses22.app.ui.components.SpeseButton
import it.speses22.app.ui.components.SpeseButtonStyle
import it.speses22.app.ui.components.StepDots
import it.speses22.app.ui.theme.Spese
import it.speses22.app.ui.theme.SpeseMotion
import it.speses22.app.ui.theme.SpeseShapes
import java.time.LocalDate


private enum class FlowPhase { Editor, Picker, Success }


enum class ExpenseStep(val title: String) {

    Amount("AMOUNT"),
    Description("DESCRIPTION"),
    Date("DATE"),
    Account("ACCOUNT"),
    Category("CATEGORY")
}


/**
 * Contenitore persistente del flusso.
 *
 * Lo scrim, la card, l'intestazione e la riga di azioni restano montati per
 * tutto il flusso: cambia solo il contenuto centrale, e la card adatta la
 * propria altezza con una molla invece di saltare.
 */
@Composable
fun ExpenseFlow(
    step: ExpenseStep,
    amount: String,
    description: String,
    date: LocalDate,
    categories: List<NotionRef>,
    selectedCategoryId: String?,
    accounts: List<NotionRef>,
    selectedAccountId: String?,
    visible: Boolean,
    submitted: Boolean,
    onStepChange: (ExpenseStep) -> Unit,
    onAmountDigit: (String) -> Unit,
    onAmountComma: () -> Unit,
    onAmountBackspace: () -> Unit,
    onAmountClear: () -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onCategoryChange: (NotionRef) -> Unit,
    onAccountChange: (NotionRef) -> Unit,
    onCancel: () -> Unit,
    onSubmit: () -> Unit
) {

    val keyboardController = LocalSoftwareKeyboardController.current

    var pickerOpen by remember { mutableStateOf(false) }

    val selectedCategoryName = remember(categories, selectedCategoryId) {
        categories.firstOrNull { it.pageId == selectedCategoryId }?.name
    }

    // Con il selettore aperto, BACK torna al flusso invece di chiudere la card.
    BackHandler(enabled = pickerOpen) {
        pickerOpen = false
    }

    val phase = when {
        submitted -> FlowPhase.Success
        pickerOpen -> FlowPhase.Picker
        else -> FlowPhase.Editor
    }

    val scrimAlpha by animateFloatAsState(
        targetValue = if (visible) Spese.ScrimAlpha else 0f,
        animationSpec = tween(
            durationMillis = if (visible) {
                SpeseMotion.CardEnterMillis
            } else {
                SpeseMotion.CardExitMillis
            }
        ),
        label = "scrimAlpha"
    )

    val slidePx = with(LocalDensity.current) {
        SpeseMotion.StepOffset.roundToPx()
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Spese.Scrim.copy(alpha = scrimAlpha))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.ime))
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.Bottom
        ) {

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(SpeseMotion.CardEnterMillis)) +
                    scaleIn(
                        initialScale = 0.92f,
                        animationSpec = SpeseMotion.CardScale
                    ) +
                    slideInVertically(SpeseMotion.CardSlide) { it / 6 },
                exit = fadeOut(tween(SpeseMotion.CardExitMillis)) +
                    scaleOut(
                        targetScale = 0.96f,
                        animationSpec = tween(SpeseMotion.CardExitMillis)
                    )
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 24.dp,
                            shape = SpeseShapes.Card,
                            ambientColor = Spese.Shadow.copy(alpha = Spese.ShadowAlpha),
                            spotColor = Spese.Shadow.copy(alpha = Spese.ShadowAlpha)
                        )
                        .clip(SpeseShapes.Card)
                        .background(Spese.Surface)
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {

                    AnimatedContent(
                        targetState = phase,
                        transitionSpec = {
                            (
                                fadeIn(tween(220, delayMillis = 60)) +
                                    scaleIn(initialScale = 0.94f)
                                ).togetherWith(
                                fadeOut(tween(140))
                            ) using SizeTransform(clip = false) { _, _ ->
                                SpeseMotion.ContainerSize
                            }
                        },
                        label = "cardPhase"
                    ) { current ->

                        when (current) {

                            FlowPhase.Success -> SuccessContent(
                                amount = amount,
                                category = selectedCategoryName
                            )

                            FlowPhase.Picker -> CategoryPickerScreen(
                                categories = categories,
                                selectedCategoryId = selectedCategoryId,
                                onSelect = { selected ->
                                    onCategoryChange(selected)
                                    pickerOpen = false
                                },
                                onBack = { pickerOpen = false }
                            )

                            FlowPhase.Editor -> Editor(
                                step = step,
                                amount = amount,
                                description = description,
                                date = date,
                                selectedCategoryName = selectedCategoryName,
                                categoriesLoaded = categories.isNotEmpty(),
                                accounts = accounts,
                                selectedAccountId = selectedAccountId,
                                slidePx = slidePx,
                                onStepChange = onStepChange,
                                onAmountDigit = onAmountDigit,
                                onAmountComma = onAmountComma,
                                onAmountBackspace = onAmountBackspace,
                                onAmountClear = onAmountClear,
                                onDescriptionChange = onDescriptionChange,
                                onDateChange = onDateChange,
                                onAccountChange = onAccountChange,
                                onOpenPicker = {
                                    keyboardController?.hide()
                                    pickerOpen = true
                                },
                                onCancel = onCancel,
                                onSubmit = onSubmit
                            )
                        }
                    }
                }
            }
        }
    }

}


@Composable
private fun Editor(
    step: ExpenseStep,
    amount: String,
    description: String,
    date: LocalDate,
    selectedCategoryName: String?,
    categoriesLoaded: Boolean,
    accounts: List<NotionRef>,
    selectedAccountId: String?,
    slidePx: Int,
    onStepChange: (ExpenseStep) -> Unit,
    onAmountDigit: (String) -> Unit,
    onAmountComma: () -> Unit,
    onAmountBackspace: () -> Unit,
    onAmountClear: () -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onAccountChange: (NotionRef) -> Unit,
    onOpenPicker: () -> Unit,
    onCancel: () -> Unit,
    onSubmit: () -> Unit
) {

    val keyboardController = LocalSoftwareKeyboardController.current

    val canAdvance = when (step) {
        ExpenseStep.Amount -> isAmountValid(amount)
        ExpenseStep.Description -> description.isNotBlank()
        ExpenseStep.Date -> true
        // Se non c'e' nessun conto (offline, cache vuota) non si resta bloccati.
        ExpenseStep.Account -> accounts.isEmpty() || selectedAccountId != null
        // Se Notion non ha mandato categorie non si resta bloccati.
        ExpenseStep.Category -> !categoriesLoaded || selectedCategoryName != null
    }

    val advance: () -> Unit = {

        keyboardController?.hide()

        when (step) {
            ExpenseStep.Amount -> onStepChange(ExpenseStep.Description)
            ExpenseStep.Description -> onStepChange(ExpenseStep.Date)
            ExpenseStep.Date -> onStepChange(ExpenseStep.Account)
            ExpenseStep.Account -> onStepChange(ExpenseStep.Category)
            ExpenseStep.Category -> onSubmit()
        }
    }

    val retreat: () -> Unit = {

        keyboardController?.hide()

        when (step) {
            ExpenseStep.Amount -> onCancel()
            ExpenseStep.Description -> onStepChange(ExpenseStep.Amount)
            ExpenseStep.Date -> onStepChange(ExpenseStep.Description)
            ExpenseStep.Account -> onStepChange(ExpenseStep.Date)
            ExpenseStep.Category -> onStepChange(ExpenseStep.Account)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {

        // ---- Intestazione persistente ----
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    fadeIn(tween(SpeseMotion.StepEnterMillis)) togetherWith
                        fadeOut(tween(SpeseMotion.StepExitMillis))
                },
                label = "stepTitle"
            ) { current ->

                Text(
                    text = current.title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.2.sp,
                    color = Spese.TextTertiary
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            StepDots(
                current = step.ordinal,
                total = ExpenseStep.entries.size
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ---- Contenuto del passo ----
        AnimatedContent(
            targetState = step,
            transitionSpec = {

                val forward = targetState.ordinal > initialState.ordinal
                val enterFrom = if (forward) slidePx else -slidePx

                (
                    slideInVertically(
                        tween(SpeseMotion.StepEnterMillis)
                    ) { enterFrom } + fadeIn(
                        tween(SpeseMotion.StepEnterMillis)
                    )
                    ).togetherWith(
                    slideOutVertically(
                        tween(SpeseMotion.StepExitMillis)
                    ) { -enterFrom } + fadeOut(
                        tween(SpeseMotion.StepExitMillis)
                    )
                ) using SizeTransform(clip = false) { _, _ ->
                    SpeseMotion.ContainerSize
                }
            },
            label = "stepContent"
        ) { current ->

            when (current) {

                ExpenseStep.Amount -> AmountStep(
                    amount = amount,
                    onDigit = onAmountDigit,
                    onComma = onAmountComma,
                    onBackspace = onAmountBackspace,
                    onClear = onAmountClear
                )

                ExpenseStep.Description -> DescriptionStep(
                    description = description,
                    onDescriptionChange = onDescriptionChange,
                    onSubmit = advance
                )

                ExpenseStep.Date -> DateStep(
                    selectedDate = date,
                    onDateChange = onDateChange
                )

                ExpenseStep.Account -> AccountStep(
                    accounts = accounts,
                    selectedAccountId = selectedAccountId,
                    onAccountSelected = onAccountChange
                )

                ExpenseStep.Category -> CategoryStep(
                    amount = amount,
                    description = description,
                    date = date,
                    selectedCategoryName = selectedCategoryName,
                    onOpenPicker = onOpenPicker,
                    onEditAmount = { onStepChange(ExpenseStep.Amount) },
                    onEditDescription = { onStepChange(ExpenseStep.Description) },
                    onEditDate = { onStepChange(ExpenseStep.Date) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ---- Azioni persistenti ----
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            SpeseButton(
                text = if (step == ExpenseStep.Amount) "CANCEL" else "BACK",
                onClick = retreat,
                style = SpeseButtonStyle.Secondary,
                modifier = Modifier.weight(1f)
            )

            SpeseButton(
                text = if (step == ExpenseStep.Category) "DONE" else "NEXT",
                onClick = advance,
                enabled = canAdvance,
                modifier = Modifier.weight(1f)
            )
        }
    }
}


@Composable
private fun SuccessContent(
    amount: String,
    category: String?
) {

    var appear by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        appear = true
    }

    val progress by animateFloatAsState(
        targetValue = if (appear) 1f else 0f,
        animationSpec = SpeseMotion.CardScale,
        label = "successMark"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .size(56.dp)
                .graphicsLayer {
                    val scale = 0.5f + 0.5f * progress
                    scaleX = scale
                    scaleY = scale
                    alpha = progress
                }
                .clip(CircleShape)
                .background(Spese.Success),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = "✓",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Spese.OnAccent
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Expense added",
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = Spese.TextPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "€ $amount · ${category ?: ""}",
            fontSize = 14.sp,
            color = Spese.TextSecondary
        )
    }
}
