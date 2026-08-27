package it.speses22.app.ui.dashboard

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.speses22.app.data.notion.NotionConfig
import it.speses22.app.ui.components.SpeseButton
import it.speses22.app.ui.components.SpeseButtonStyle
import it.speses22.app.ui.theme.Spese

/**
 * Impostazioni minime: stato della connessione e scorciatoia verso Notion.
 *
 * Il token non viene mai mostrato, nemmeno parzialmente.
 */
@Composable
fun SettingsSection(data: PeriodData) {

    val context = LocalContext.current
    val config = NotionConfig.fromBuildConfig()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {

        Text(
            text = "NOTION",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            color = Spese.TextTertiary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card {

            SettingRow(
                label = "Connection",
                value = if (config.isConfigured) "Configured" else "Not configured"
            )

            SettingRow(
                label = "Last read",
                value = when {
                    data.loading -> "Reading…"
                    data.failed -> "Failed"
                    else -> "${data.expenses.size} expenses"
                }
            )

            SettingRow(
                label = "Expenses source",
                value = config.dataSourceId.takeIf { it.isNotBlank() }
                    ?.let { "…${it.takeLast(6)}" }
                    ?: "—"
            )

            SettingRow(
                label = "Category / Account",
                value = "${config.categoryProperty} · ${config.accountProperty}"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        SpeseButton(
            text = "OPEN IN NOTION",
            onClick = { openNotion(context, config.databaseId) },
            style = SpeseButtonStyle.Secondary,
            enabled = config.databaseId.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "APP",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            color = Spese.TextTertiary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card {
            SettingRow(label = "Theme", value = "Dark")
            SettingRow(label = "Currency", value = "EUR (€)")
            SettingRow(label = "Quick Add", value = "Launcher entry “Quick Add”")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Sync of new expenses is handled in the background and retried " +
                "automatically; a notification confirms once Notion has saved.",
            fontSize = 12.sp,
            color = Spese.TextTertiary
        )
    }
}


@Composable
private fun SettingRow(label: String, value: String) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {

        Text(text = label, fontSize = 14.sp, color = Spese.TextSecondary)

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Spese.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}


/** Apre il database esistente; non ricostruisce Notion dentro l'app. */
private fun openNotion(context: android.content.Context, databaseId: String) {

    if (databaseId.isBlank()) return

    val compact = databaseId.replace("-", "")

    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.notion.so/$compact"))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    runCatching { context.startActivity(intent) }
}
