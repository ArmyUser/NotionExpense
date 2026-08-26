package it.speses22.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.Until

const val TargetPackage = "it.speses22.app"

private const val Timeout = 5_000L

/**
 * Percorso critico reale: apertura, i cinque passi, i due selettori e il
 * ritorno indietro.
 *
 * Volutamente NON preme DONE: quello invierebbe una spesa vera su Notion a ogni
 * iterazione del benchmark.
 */
fun MacrobenchmarkScope.expenseJourney() {

    // --- Passo 1: importo ---
    tap(By.text("1"))
    tap(By.text("2"))
    tap(By.text(","))
    tap(By.text("5"))

    tap(By.text("NEXT"))

    // --- Passo 2: descrizione (NEXT resta disabilitato se e' vuota) ---
    if (tap(By.textContains("What did you spend on"))) {
        device.executeShellCommand("input text Benchmark")
        device.waitForIdle()
    }

    tap(By.text("NEXT"))

    // --- Passo 3: data, incluso il cambio mese ---
    tap(By.text("›"))
    tap(By.text("‹"))
    tap(By.text("15"))

    tap(By.text("NEXT"))

    // --- Passo 4: selettore conto ---
    device.wait(Until.hasObject(By.text("ACCOUNT")), Timeout)
    firstOf("Cash Casa", "Revolut", "Intesa San Paolo", "Cash In giro")

    tap(By.text("NEXT"))

    // --- Passo 5: selettore categoria, schermata dedicata ---
    device.wait(Until.hasObject(By.text("SUMMARY")), Timeout)
    tap(By.textContains("category"))
    device.wait(Until.hasObject(By.text("CATEGORY")), Timeout)
    firstOf("Subscriptions", "Technology", "Cibo preso in giro")

    // --- Navigazione all'indietro, per coprire le transizioni inverse ---
    repeat(3) { tap(By.text("BACK")) }

    // Fermo qui di proposito: DONE scriverebbe davvero su Notion.
}


/**
 * Tocca un elemento tollerando i nodi che scadono a meta' animazione: durante
 * una transizione l'albero viene ricreato e il riferimento diventa stale.
 */
private fun MacrobenchmarkScope.tap(
    selector: BySelector,
    timeout: Long = Timeout
): Boolean {

    repeat(3) {
        try {
            val target = device.wait(Until.findObject(selector), timeout)
                ?: return false

            target.click()
            device.waitForIdle()
            return true

        } catch (_: StaleObjectException) {
            device.waitForIdle()
        }
    }

    return false
}


/** Le righe sono dati dell'utente: si prende la prima che esiste davvero. */
private fun MacrobenchmarkScope.firstOf(vararg labels: String): Boolean =
    labels.any { label ->
        device.hasObject(By.text(label)) && tap(By.text(label), timeout = 1_000L)
    }
