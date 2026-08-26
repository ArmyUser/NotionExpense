package it.speses22.app.work

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import it.speses22.app.data.Expense
import kotlin.math.abs

/**
 * Notifiche di sincronizzazione.
 *
 * Riguardano solo l'esito su Notion: la conferma immediata all'utente resta la
 * card "Expense added" dentro l'app, che parla di accettazione locale.
 */
internal object SyncNotifications {

    private const val ChannelId = "transactions"

    fun synced(context: Context, expense: Expense) {

        show(
            context = context,
            expense = expense,
            title = "Expense synced",
            text = "€ ${expense.amount} · ${expense.description} saved to Notion."
        )
    }


    fun failed(context: Context, expense: Expense, reason: String) {

        show(
            context = context,
            expense = expense,
            title = "Expense not synced",
            text = "€ ${expense.amount} · ${expense.description} could not be saved. $reason"
        )
    }


    private fun show(
        context: Context,
        expense: Expense,
        title: String,
        text: String
    ) {

        ensureChannel(context)

        val notification = NotificationCompat.Builder(context, ChannelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val allowed =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

        if (!allowed) return

        // Id stabile per spesa: un eventuale riavvio del Worker sostituisce la
        // notifica invece di affiancarne una seconda.
        NotificationManagerCompat
            .from(context)
            .notify(idFor(expense), notification)
    }


    private fun idFor(expense: Expense): Int =
        abs(
            listOf(
                expense.description,
                expense.date,
                expense.amount.toString(),
                expense.category
            ).joinToString("|").hashCode()
        )


    /** Il Worker puo' girare senza che l'Activity sia mai stata creata. */
    private fun ensureChannel(context: Context) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            ChannelId,
            "Transactions",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for new transactions"
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        manager.createNotificationChannel(channel)
    }
}
