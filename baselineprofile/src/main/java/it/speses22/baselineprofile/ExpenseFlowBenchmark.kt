package it.speses22.baselineprofile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import org.junit.Rule
import org.junit.Test

/**
 * Fluidita' della PRIMA interazione: ogni iterazione parte da un avvio a
 * freddo, quindi misura il percorso mai visitato prima, non quello gia' caldo.
 */
class ExpenseFlowBenchmark {

    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun flowNoCompilation() = flow(CompilationMode.None())

    @Test
    fun flowBaselineProfile() = flow(
        CompilationMode.Partial(baselineProfileMode = BaselineProfileMode.Require)
    )

    private fun flow(mode: CompilationMode) = rule.measureRepeated(
        packageName = TargetPackage,
        metrics = listOf(FrameTimingMetric()),
        compilationMode = mode,
        startupMode = StartupMode.COLD,
        iterations = 6,
        setupBlock = {
            pressHome()
            startActivityAndWait()
        }
    ) {
        expenseJourney()
    }
}
