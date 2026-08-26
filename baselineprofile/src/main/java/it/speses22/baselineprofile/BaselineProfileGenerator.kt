package it.speses22.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import org.junit.Rule
import org.junit.Test

/**
 * Genera il profilo di baseline dal percorso critico reale.
 *
 * ART precompila cosi' le classi e i metodi che servono al primo avvio e alla
 * prima interazione, invece di arrivarci a caldo dopo che l'utente ha gia'
 * usato l'app.
 */
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(
        packageName = TargetPackage,
        includeInStartupProfile = true
    ) {
        pressHome()
        startActivityAndWait()
        expenseJourney()
    }
}
