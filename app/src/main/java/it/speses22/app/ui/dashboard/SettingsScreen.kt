package it.speses22.app.ui.dashboard

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.speses22.app.data.ThemeMode
import it.speses22.app.data.notion.NotionConfig

@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit
) {

    val context = LocalContext.current
    val config = NotionConfig.fromBuildConfig()

    Column(modifier = Modifier.fillMaxWidth()) {

        Group("NOTION") {

            Line("Connection", if (config.isConfigured) "Configured" else "Not configured")

            if (!config.isConfigured) {

                Spacer(modifier = Modifier.height(4.dp))

                // Le credenziali sono un valore di build, non un campo dell'app:
                // dirlo qui evita di cercare un'impostazione che non esiste.
                Body(
                    "The Notion token and data source IDs are build settings. " +
                        "Add them to gradle.properties and rebuild the app."
                )
            }

            Action(
                label = "Open in Notion",
                enabled = config.databaseId.isNotBlank(),
                onClick = { openNotion(context, config.databaseId) }
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ---- Quick Add: solo spiegazioni, nessun controllo sul flusso ----
        Group("QUICK ADD") {

            Text(
                text = "Tapping the app icon opens this Dashboard. Quick Add is a " +
                    "separate entry point in the same app, meant to be launched by a " +
                    "shortcut so you can record an expense without opening the Dashboard.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Card {
            Heading("RegiStar — Back Tap (optional)")

            Body(
                "If your phone has Good Lock with RegiStar, you can launch Quick Add " +
                    "with a double tap on the back of the phone.\n\n" +
                    "1. Open Good Lock, then RegiStar.\n" +
                    "2. Open the Back-Tap action setting.\n" +
                    "3. Choose Double Tap.\n" +
                    "4. Pick the action that launches an app shortcut or activity.\n" +
                    "5. Select Notion Expense → Quick Add.\n\n" +
                    "Menu names vary by One UI version. RegiStar is optional — the app " +
                    "does not depend on it."
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Card {
            Heading("Android & Samsung shortcuts")

            Body(
                "Depending on your device and Android version, the Quick Add shortcut " +
                    "can also be placed on the home screen by long-pressing the app icon, " +
                    "or bound to a system gesture where your device supports it."
            )

            Spacer(modifier = Modifier.height(10.dp))

            Action(
                label = "Open app settings",
                enabled = true,
                onClick = { openAppSettings(context) }
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Group("APPEARANCE") {

            Text(
                text = "Theme",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            ThemeMode.entries.forEach { mode ->
                ThemeChoice(
                    label = mode.name,
                    selected = mode == themeMode,
                    onClick = { onThemeChange(mode) }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Body("Applies to the whole app, including Quick Add.")
        }

        Spacer(modifier = Modifier.height(18.dp))

        Group("GENERAL") {

            Action(
                label = "Notification settings",
                enabled = true,
                onClick = { openNotificationSettings(context) }
            )

            Spacer(modifier = Modifier.height(6.dp))

            Body(
                "Expenses sync in the background and are retried automatically. " +
                    "A notification confirms once Notion has saved."
            )
        }
    }
}


// ---- Pezzi di UI -------------------------------------------------------

@Composable
private fun Group(title: String, content: @Composable ColumnScope.() -> Unit) {

    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(8.dp))

    Card(content = content)
}

@Composable
private fun Card(content: @Composable ColumnScope.() -> Unit) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        content = content
    )
}

@Composable
private fun Heading(text: String) {
    Text(
        text = text,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun Body(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = 19.sp
    )
}

@Composable
private fun Line(label: String, value: String) {

    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 14.sp, color = colors.onSurfaceVariant)

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun Action(label: String, enabled: Boolean, onClick: () -> Unit) {

    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (enabled) colors.primaryContainer else colors.surfaceVariant)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (enabled) colors.onPrimaryContainer else colors.onSurfaceVariant
        )
    }
}

@Composable
private fun ThemeChoice(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick
            )
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = colors.primary,
                unselectedColor = colors.onSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = colors.onSurface
        )
    }
}


// ---- Intent verso il sistema ------------------------------------------

private fun openNotion(context: Context, databaseId: String) {
    if (databaseId.isBlank()) return
    val compact = databaseId.replace("-", "")
    launch(context, Intent(Intent.ACTION_VIEW, Uri.parse("https://www.notion.so/$compact")))
}

private fun openAppSettings(context: Context) {
    launch(
        context,
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        )
    )
}

private fun openNotificationSettings(context: Context) {
    launch(
        context,
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    )
}

private fun launch(context: Context, intent: Intent) {
    runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
}
