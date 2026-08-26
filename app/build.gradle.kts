plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("androidx.baselineprofile")
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

    buildTypes {

        // Il profilo di baseline vale solo per build NON debuggable: per vederne
        // l'effetto va installata una release, non la debug.
        release {
            isMinifyEnabled = false
            // Firma di debug: serve solo per installare in locale una build
            // non-debuggable. Non e' una configurazione di pubblicazione.
            signingConfig = signingConfigs.getByName("debug")
        }

        // Variante usata da Macrobenchmark: come release, ma profilabile.
        create("benchmark") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            isDebuggable = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    // lintVital va in crash con la toolchain locale (AGP 8.6 + JDK del JBR):
    // il controllo resta disponibile via ./gradlew :app:lint.
    lint {
        checkReleaseBuilds = false
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

baselineProfile {
    // Un unico profilo condiviso dalle varianti, invece di uno per variante.
    mergeIntoMain = true
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

    // Installa il profilo di baseline al primo avvio.
    implementation("androidx.profileinstaller:profileinstaller:1.4.1")

    // Sorgente del profilo generato dal modulo :baselineprofile.
    baselineProfile(project(":baselineprofile"))
}
