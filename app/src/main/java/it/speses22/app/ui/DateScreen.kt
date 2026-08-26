package it.speses22.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.speses22.app.ui.components.SpeseIconButton
import it.speses22.app.ui.theme.Spese
import it.speses22.app.ui.theme.SpeseMotion
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * Passo 3: data.
 *
 * Il mese cambia con uno scorrimento orizzontale corto, così la direzione della
 * navigazione resta leggibile senza spostare l'intera card.
 */
@Composable
fun DateStep(
    selectedDate: LocalDate,
    onDateChange: (LocalDate) -> Unit
) {

    var displayedMonth by remember {
        mutableStateOf(YearMonth.from(selectedDate))
    }

    // Se la data selezionata cambia da fuori, segui il suo mese.
    LaunchedEffect(selectedDate) {
        displayedMonth = YearMonth.from(selectedDate)
    }

    val slidePx = with(LocalDensity.current) { 24.dp.roundToPx() }

    Column(modifier = Modifier.fillMaxWidth()) {

        // Data selezionata
        AnimatedContent(
            targetState = selectedDate,
            transitionSpec = {
                fadeIn(tween(SpeseMotion.ValueEnterMillis)) togetherWith
                    fadeOut(tween(SpeseMotion.ValueExitMillis))
            },
            label = "selectedDate"
        ) { date ->

            Text(
                text = formatSelectedDate(date),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Spese.TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Navigazione mese
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            SpeseIconButton(
                glyph = "‹",
                onClick = { displayedMonth = displayedMonth.minusMonths(1) }
            )

            Text(
                text = monthLabel(displayedMonth),
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Spese.TextPrimary
            )

            SpeseIconButton(
                glyph = "›",
                onClick = { displayedMonth = displayedMonth.plusMonths(1) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        WeekDayHeader()

        AnimatedContent(
            targetState = displayedMonth,
            transitionSpec = {

                val forward = targetState > initialState
                val enterFrom = if (forward) slidePx else -slidePx

                (
                    slideInHorizontally(tween(SpeseMotion.StepEnterMillis)) { enterFrom } +
                        fadeIn(tween(SpeseMotion.StepEnterMillis))
                    ).togetherWith(
                    slideOutHorizontally(tween(SpeseMotion.StepExitMillis)) { -enterFrom } +
                        fadeOut(tween(SpeseMotion.StepExitMillis))
                ) using SizeTransform(clip = false) { _, _ ->
                    SpeseMotion.ContainerSize
                }
            },
            label = "calendarMonth"
        ) { month ->

            CalendarGrid(
                displayedMonth = month,
                selectedDate = selectedDate,
                onDateSelected = onDateChange
            )
        }
    }
}


@Composable
private fun WeekDayHeader() {

    // getDisplayName consulta i dati di locale: si calcola una volta sola.
    val weekDays = remember {
        listOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY
        ).map { day ->
            day.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                .take(2)
                .uppercase(Locale.getDefault())
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
    ) {

        weekDays.forEach { label ->

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Spese.TextTertiary
                )
            }
        }
    }
}


@Composable
private fun CalendarGrid(
    displayedMonth: YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {

    val firstDayOffset = displayedMonth.atDay(1).dayOfWeek.value - 1
    val daysInMonth = displayedMonth.lengthOfMonth()

    val rows = (firstDayOffset + daysInMonth + 6) / 7
    val today = remember { LocalDate.now() }

    Column(modifier = Modifier.fillMaxWidth()) {

        repeat(rows) { rowIndex ->

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {

                repeat(7) { columnIndex ->

                    val cellIndex = rowIndex * 7 + columnIndex
                    val dayNumber = cellIndex - firstDayOffset + 1

                    if (dayNumber in 1..daysInMonth) {

                        val date = displayedMonth.atDay(dayNumber)

                        DayCell(
                            dayNumber = dayNumber,
                            selected = date == selectedDate,
                            isToday = date == today,
                            onClick = { onDateSelected(date) },
                            modifier = Modifier.weight(1f)
                        )

                    } else {

                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}


@Composable
private fun DayCell(
    dayNumber: Int,
    selected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val interactionSource = remember { MutableInteractionSource() }

    // L'indicatore cresce dal centro invece di comparire di colpo.
    val selection = animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(SpeseMotion.StepEnterMillis),
        label = "daySelection"
    )

    val textColor by animateColorAsState(
        targetValue = when {
            selected -> Spese.OnAccent
            isToday -> Spese.Accent
            else -> Spese.TextPrimary
        },
        animationSpec = tween(SpeseMotion.StepEnterMillis),
        label = "dayText"
    )

    Box(
        modifier = modifier
            .height(44.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier
                .size(38.dp)
                .graphicsLayer {
                    val scale = 0.6f + 0.4f * selection.value
                    scaleX = scale
                    scaleY = scale
                    alpha = selection.value
                }
                .clip(CircleShape)
                .background(Spese.Accent)
        )

        Text(
            text = dayNumber.toString(),
            fontSize = 15.sp,
            fontWeight = if (selected || isToday) {
                FontWeight.SemiBold
            } else {
                FontWeight.Normal
            },
            color = textColor
        )
    }
}


private fun monthLabel(month: YearMonth): String {

    val name = month.month
        .getDisplayName(TextStyle.FULL, Locale.getDefault())
        .replaceFirstChar { it.uppercase() }

    return "$name ${month.year}"
}


fun formatSelectedDate(date: LocalDate): String {

    val monthName = date.month
        .getDisplayName(TextStyle.FULL, Locale.getDefault())
        .replaceFirstChar { it.uppercase() }

    return "${date.dayOfMonth} $monthName ${date.year}"
}
