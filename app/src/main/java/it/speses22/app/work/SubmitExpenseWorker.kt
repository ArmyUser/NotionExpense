package it.speses22.app.work

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import it.speses22.app.data.Expense
import it.speses22.app.data.ExpenseSinks
import it.speses22.app.data.SendResult
import java.util.concurrent.TimeUnit

/**
 * Invio della spesa fuori dal ciclo di vita dell'Activity.
 *
 * L'Activity si chiude circa un secondo dopo il tocco su DONE: WorkManager
 * persiste la richiesta su disco prima di eseguirla, quindi l'invio sopravvive
 * alla chiusura della card, alla mancanza di rete e alla morte del processo.
 */
class SubmitExpenseWorker(
    context: Context,
    parameters: WorkerParameters
) : CoroutineWorker(context, parameters) {

    override suspend fun doWork(): Result {

        val expense = Expense(
            amount = inputData.getDouble(KeyAmount, 0.0),
            description = inputData.getString(KeyDescription).orEmpty(),
            date = inputData.getString(KeyDate).orEmpty(),
            category = inputData.getString(KeyCategory).orEmpty(),
            // Senza questo la relazione Category resta vuota: il sink lo
            // richiede non vuoto per costruire il payload della relation.
            categoryPageId = inputData.getString(KeyCategoryPageId).orEmpty(),
            accountPageId = inputData.getString(KeyAccountPageId).orEmpty()
        )

        val outcome = ExpenseSinks.default().send(
            expense = expense,
            isRetry = runAttemptCount > 0
        )

        return when (outcome) {

            is SendResult.Success -> {
                SyncNotifications.synced(applicationContext, expense)
                Result.success()
            }

            is SendResult.Permanent -> giveUp(expense, outcome.reason)

            is SendResult.Retryable ->
                // Finche' si ritenta non si notifica nulla: l'utente sapra'
                // l'esito solo quando c'e' un esito.
                if (runAttemptCount >= MaxAttempts) {
                    giveUp(expense, "${outcome.reason} (dopo $MaxAttempts tentativi)")
                } else {
                    Result.retry()
                }
        }
    }


    private fun giveUp(expense: Expense, reason: String): Result {

        SyncNotifications.failed(applicationContext, expense, reason)

        return Result.failure(workDataOf(KeyError to reason))
    }


    companion object {

        private const val KeyAmount = "amount"
        private const val KeyDescription = "description"
        private const val KeyDate = "date"
        private const val KeyCategory = "category"
        private const val KeyCategoryPageId = "categoryPageId"
        private const val KeyAccountPageId = "accountPageId"

        /** Leggibile da WorkManager per capire perche' un invio e' fallito. */
        const val KeyError = "error"

        private const val MaxAttempts = 5

        /** Accoda l'invio e ritorna subito: l'animazione di uscita non viene toccata. */
        fun enqueue(context: Context, expense: Expense) {

            val request = OneTimeWorkRequestBuilder<SubmitExpenseWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    30,
                    TimeUnit.SECONDS
                )
                .setInputData(
                    workDataOf(
                        KeyAmount to expense.amount,
                        KeyDescription to expense.description,
                        KeyDate to expense.date,
                        KeyCategory to expense.category,
                        KeyCategoryPageId to expense.categoryPageId,
                        KeyAccountPageId to expense.accountPageId
                    )
                )
                .build()

            WorkManager.getInstance(context.applicationContext).enqueue(request)
        }
    }
}
