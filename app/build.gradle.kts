plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

/**
 * Segreti e schema Notion.
 *
 * I valori vivono in ~/.gradle/gradle.properties, fuori dal repository: così il
 * token non puo' finire in Git nemmeno per sbaglio. Il fallback vuoto tiene il
 * progetto compilabile anche senza configurazione.
 */
fun secret(name: String): String =
    (project.findProperty(name) as String?).orEmpty().trim()

/** Nome della proprieta' Notion, con un default ragionevole se non configurato. */
fun schema(name: String, fallback: String): String =
    (project.findProperty(name) as String?)
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?: fallback

android {
    namespace = "it.speses22.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "it.speses22.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // --- Credenziali Notion ---
        buildConfigField("String", "NOTION_TOKEN", "\"${secret("notionToken")}\"")
        buildConfigField(
            "String",
            "NOTION_DATA_SOURCE_ID",
            "\"${secret("notionDataSourceId")}\""
        )

        // --- Mappatura sullo schema del database esistente ---
        buildConfigField(
            "String",
            "NOTION_PROP_TITLE",
            "\"${schema("notionPropTitle", "Name")}\""
        )
        buildConfigField(
            "String",
            "NOTION_PROP_AMOUNT",
            "\"${schema("notionPropAmount", "Amount")}\""
        )
        buildConfigField(
            "String",
            "NOTION_PROP_DATE",
            "\"${schema("notionPropDate", "Date")}\""
        )
        buildConfigField(
            "String",
            "NOTION_PROP_CATEGORY",
            "\"${schema("notionPropCategory", "Category")}\""
        )
        // select | multi_select | rich_text | relation
        buildConfigField(
            "String",
            "NOTION_PROP_CATEGORY_TYPE",
            "\"${schema("notionPropCategoryType", "select")}\""
        )
        // Serve solo quando Category e' una relation.
        buildConfigField(
            "String",
            "NOTION_CATEGORY_DATA_SOURCE_ID",
            "\"${secret("notionCategoryDataSourceId")}\""
        )

        // --- Account (relation verso la tabella Accounts) ---
        buildConfigField(
            "String",
            "NOTION_PROP_ACCOUNT",
            "\"${schema("notionPropAccount", "Account")}\""
        )
        buildConfigField(
            "String",
            "NOTION_ACCOUNTS_DATA_SOURCE_ID",
            "\"${secret("notionAccountsDataSourceId")}\""
        )
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    val bom = platform("androidx.compose:compose-bom:2024.12.01")

    implementation(bom)
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Invio in background che sopravvive alla chiusura dell'Activity.
    implementation("androidx.work:work-runtime-ktx:2.10.5")
}
