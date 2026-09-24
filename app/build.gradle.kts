plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("androidx.baselineprofile")
}


fun secret(name: String): String =
    (project.findProperty(name) as String?).orEmpty().trim()

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

        buildConfigField("String", "NOTION_TOKEN", "\"${secret("notionToken")}\"")
        buildConfigField(
            "String",
            "NOTION_DATA_SOURCE_ID",
            "\"${secret("notionDataSourceId")}\""
        )

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

        // Solo per l'azione "Open in Notion".
        buildConfigField(
            "String",
            "NOTION_DATABASE_ID",
            "\"${secret("notionDatabaseId")}\""
        )
    }

    buildTypes {

        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }

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

    lint {
        checkReleaseBuilds = false
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

baselineProfile {
    mergeIntoMain = true
}

dependencies {
    val bom = platform("androidx.compose:compose-bom:2024.12.01")

    implementation(bom)
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    implementation("androidx.work:work-runtime-ktx:2.10.5")

    implementation("androidx.profileinstaller:profileinstaller:1.4.1")

    baselineProfile(project(":baselineprofile"))
}
