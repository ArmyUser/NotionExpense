package it.speses22.app

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.os.Build
import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.material3.Surface

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.WindowCompat

import it.speses22.app.data.Expense
import it.speses22.app.data.NotionRef
import it.speses22.app.data.RefSources
import it.speses22.app.data.RefStore
import it.speses22.app.data.appendDigit
import it.speses22.app.data.backspace
import it.speses22.app.data.parseAmount
import it.speses22.app.ui.ExpenseFlow
import it.speses22.app.ui.ExpenseStep
import it.speses22.app.ui.theme.SpeseTheme
import it.speses22.app.work.SubmitExpenseWorker

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import java.time.LocalDate


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Finestra trasparente
        window.setBackgroundDrawableResource(
            android.R.color.transparent
        )

        window.decorView.setBackgroundColor(
            AndroidColor.TRANSPARENT
        )

        // Serve perché imePadding/systemBars seguano davvero la tastiera
        WindowCompat.setDecorFitsSystemWindows(window, false)

        createNotificationChannel(this)

        setContent {
            SpeseApp()
        }

        // Permesso notifiche Android 13+
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                100
            )
        }
    }
}


fun createNotificationChannel(context: Context) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

        val channel = NotificationChannel(
            "transactions",
            "Transactions",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for new transactions"
        }

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

        manager.createNotificationChannel(channel)
    }
}


/** Risultato di una lettura di cache, applicato in un colpo solo sul main. */
private data class CachedRefs(
    val accounts: List<NotionRef>,
    val categories: List<NotionRef>,
    val accountId: String?,
    val categoryId: String?
)


@Composable
fun SpeseApp() {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var amount by rememberSaveable { mutableStateOf("0") }
    var description by rememberSaveable { mutableStateOf("") }
    var categories by remember { mutableStateOf(emptyList<NotionRef>()) }
    var selectedCategoryId by rememberSaveable { mutableStateOf<String?>(null) }

    var accounts by remember { mutableStateOf(emptyList<NotionRef>()) }
    var selectedAccountId by rememberSaveable { mutableStateOf<String?>(null) }

    var dateEpochDay by rememberSaveable {
        mutableStateOf(LocalDate.now().toEpochDay())
    }

    var step by rememberSaveable { mutableStateOf(ExpenseStep.Amount) }

    // Senza memoizzazione ogni ricomposizione crea una LocalDate nuova: essendo
    // un tipo instabile, la sola identita' diversa impedirebbe di saltare
    // ExpenseFlow ed Editor.
    val date = remember(dateEpochDay) { LocalDate.ofEpochDay(dateEpochDay) }

    // Guida le animazioni di apertura e chiusura della card.
    // rememberSaveable: dopo una rotazione la card non rientra da capo e la
    // conferma gia' mostrata non torna indietro all'editor.
    var visible by rememberSaveable { mutableStateOf(false) }
    var submitted by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    // Prima la cache, cosi' i selettori sono popolati anche senza rete; poi un
    // aggiornamento in background se Notion risponde.
    LaunchedEffect(Unit) {

        // SharedPreferences fa I/O su disco al primo accesso: fuori dal main.
        val cached = withContext(Dispatchers.IO) {
            val cachedAccounts = RefStore.Accounts.read(context)
            val cachedCategories = RefStore.Categories.read(context)

            // Riprende l'ultima scelta, se quella riga esiste ancora.
            CachedRefs(
                accounts = cachedAccounts,
                categories = cachedCategories,
                accountId = RefStore.Accounts
                    .preselect(context, cachedAccounts, selectedAccountId),
                categoryId = RefStore.Categories
                    .preselect(context, cachedCategories, selectedCategoryId)
            )
        }

        accounts = cached.accounts
        categories = cached.categories
        selectedAccountId = cached.accountId
        selectedCategoryId = cached.categoryId

        RefSources.accounts().load()?.let { fresh ->
            accounts = fresh
            selectedAccountId = withContext(Dispatchers.IO) {
                RefStore.Accounts.write(context, fresh)
                RefStore.Accounts.preselect(context, fresh, selectedAccountId)
            }
        }

        RefSources.categories().load()?.let { fresh ->
            categories = fresh
            selectedCategoryId = withContext(Dispatchers.IO) {
                RefStore.Categories.write(context, fresh)
                RefStore.Categories.preselect(context, fresh, selectedCategoryId)
            }
        }
    }

    // Chiude il flusso lasciando finire l'animazione di uscita
    val close: () -> Unit = {

        visible = false

        scope.launch {
            delay(SpeseCloseDelayMillis)
            (context as? ComponentActivity)?.finish()
        }
    }

    val submit: () -> Unit = {

        submitted = true

        // Accodato prima della chiusura: l'invio prosegue anche a card chiusa.
        SubmitExpenseWorker.enqueue(
            context = context,
            expense = Expense(
                amount = parseAmount(amount) ?: 0.0,
                description = description,
                date = LocalDate.ofEpochDay(dateEpochDay).toString(),
                category = categories
                    .firstOrNull { it.pageId == selectedCategoryId }
                    ?.name
                    .orEmpty(),
                categoryPageId = selectedCategoryId.orEmpty(),
                accountPageId = selectedAccountId.orEmpty()
            )
        )

        // Nessuna notifica qui: "Expense added" e' l'accettazione locale, la
        // notifica arriva dal Worker solo quando Notion ha davvero salvato.
    }

    // Tempi identici a prima, ma legati allo stato invece che allo scope della
    // callback: una rotazione dopo l'invio non lascia la card aperta per sempre.
    LaunchedEffect(submitted) {

        if (!submitted) return@LaunchedEffect

        delay(SpeseSuccessHoldMillis)
        visible = false
        delay(SpeseCloseDelayMillis)
        (context as? ComponentActivity)?.finish()
    }

    // Indietro: torna al passo precedente, esce solo dal primo
    BackHandler(enabled = !submitted) {

        if (step == ExpenseStep.Amount) {
            close()
        } else {
            step = ExpenseStep.entries[step.ordinal - 1]
        }
    }

    SpeseTheme {

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Transparent
        ) {

            Box(modifier = Modifier.fillMaxSize()) {

                ExpenseFlow(
                    step = step,
                    amount = amount,
                    description = description,
                    date = date,
                    categories = categories,
                    selectedCategoryId = selectedCategoryId,
                    accounts = accounts,
                    selectedAccountId = selectedAccountId,
                    visible = visible,
                    submitted = submitted,

                    onStepChange = { step = it },

                    onAmountDigit = { digit ->
                        amount = appendDigit(amount, digit)
                    },

                    onAmountComma = {
                        if (!amount.contains(",")) {
                            amount += ","
                        }
                    },

                    onAmountBackspace = {
                        amount = backspace(amount)
                    },

                    onAmountClear = {
                        amount = "0"
                    },

                    onDescriptionChange = { description = it },

                    onDateChange = { date ->
                        dateEpochDay = date.toEpochDay()
                    },

                    onCategoryChange = {
                        selectedCategoryId = it.pageId
                        RefStore.Categories.rememberLastUsed(context, it.pageId)
                    },

                    onAccountChange = {
                        selectedAccountId = it.pageId
                        RefStore.Accounts.rememberLastUsed(context, it.pageId)
                    },

                    onCancel = close,
                    onSubmit = submit
                )
            }
        }
    }
}


/** Durata dell'uscita della card, allineata a SpeseMotion.CardExitMillis. */
private const val SpeseCloseDelayMillis = 220L

/** Quanto resta a schermo la conferma prima di chiudere. */
private const val SpeseSuccessHoldMillis = 900L
