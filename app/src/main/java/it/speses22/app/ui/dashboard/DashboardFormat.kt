package it.speses22.app.ui.dashboard

import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

fun monthLabel(month: YearMonth): String {

    val name = month.month
        .getDisplayName(TextStyle.FULL, Locale.getDefault())
        .replaceFirstChar { it.uppercase() }

    return "$name ${month.year}"
}

/** Stesso simbolo usato dal Quick Add. */
fun money(value: Double): String = "€ %,.2f".format(Locale.getDefault(), value)
