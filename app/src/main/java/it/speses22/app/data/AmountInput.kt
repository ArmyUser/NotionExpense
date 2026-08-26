package it.speses22.app.data

fun appendDigit(
    current: String,
    digit: String
): String {

    // Parte decimale
    if (current.contains(",")) {

        val parts = current.split(",")

        val integerPart = parts[0]
        val decimalPart = parts.getOrElse(1) { "" }

        // Massimo 2 cifre decimali
        if (decimalPart.length >= 2) {
            return current
        }

        return "$integerPart,$decimalPart$digit"
    }

    // Il primo numero sostituisce lo zero
    if (current == "0") {
        return digit
    }

    // Massimo 8 cifre intere
    if (current.length >= 8) {
        return current
    }

    return current + digit
}


fun backspace(
    current: String
): String {

    // Parte decimale
    if (current.contains(",")) {

        val parts = current.split(",")

        val integerPart = parts[0]
        val decimalPart = parts.getOrElse(1) { "" }

        // Cancella prima i decimali
        if (decimalPart.isNotEmpty()) {

            val newDecimalPart =
                decimalPart.dropLast(1)

            return if (newDecimalPart.isEmpty()) {
                "$integerPart,"
            } else {
                "$integerPart,$newDecimalPart"
            }
        }

        // Cancella la virgola
        return integerPart
    }

    // Parte intera
    if (current.length <= 1) {
        return "0"
    }

    return current.dropLast(1)
}


/** L'importo inserito, o null se non è ancora un valore valido. */
fun parseAmount(current: String): Double? {

    val normalized = current
        .replace(",", ".")
        .trimEnd('.')

    return normalized.toDoubleOrNull()
}


fun isAmountValid(current: String): Boolean {

    val value = parseAmount(current)

    return value != null && value > 0.0
}
