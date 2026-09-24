package it.speses22.app.ui.dashboard

import it.speses22.app.data.AppCurrency
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * L'interfaccia della dashboard e' scritta in inglese: anche le date lo sono,
 * invece di seguire la lingua del telefono. Mescolare "Agosto 2026" a "Total
 * spent" era l'unico punto in cui comparivano due lingue.
 */
private val Language: Locale = Locale.ENGLISH

private val DayMonth = DateTimeFormatter.ofPattern("d MMM", Language)
private val WeekdayDayMonth = DateTimeFormatter.ofPattern("EEEE d MMMM", Language)
private val CompactDate = DateTimeFormatter.ofPattern("EEE d MMM", Language)

fun monthLabel(month: YearMonth): String {

    val name = month.month.getDisplayName(TextStyle.FULL, Language)

    return "$name ${month.year}"
}

/** Simbolo letto da Notion, come nel Quick Add; i separatori seguono il telefono. */
fun money(value: Double): String =
    "${AppCurrency.symbol} %,.2f".format(Locale.getDefault(), value)

fun shortDate(date: LocalDate): String = date.format(DayMonth)

fun longDate(date: LocalDate): String = date.format(WeekdayDayMonth)

/** Giorno con il nome abbreviato: sta in un chip senza andare a capo. */
fun compactDate(date: LocalDate): String = date.format(CompactDate)
