package it.speses22.app.data.notion

import it.speses22.app.BuildConfig

/**
 * Credenziali e mappatura verso il database Notion gia' esistente.
 *
 * I nomi delle proprieta' arrivano dalla configurazione Gradle, non dal codice:
 * lo schema del database e' la fonte di verita' e si adatta senza ricompilare
 * logica.
 */
data class NotionConfig(
    val token: String,
    val dataSourceId: String,
    val titleProperty: String,
    val amountProperty: String,
    val dateProperty: String,
    val categoryProperty: String,
    val categoryType: String,
    val categoryDataSourceId: String,
    val accountProperty: String,
    val accountsDataSourceId: String,
    val databaseId: String
) {

    val isConfigured: Boolean
        get() = token.isNotBlank() && dataSourceId.isNotBlank()

    companion object {

        fun fromBuildConfig(): NotionConfig = NotionConfig(
            token = BuildConfig.NOTION_TOKEN,
            dataSourceId = BuildConfig.NOTION_DATA_SOURCE_ID,
            titleProperty = BuildConfig.NOTION_PROP_TITLE,
            amountProperty = BuildConfig.NOTION_PROP_AMOUNT,
            dateProperty = BuildConfig.NOTION_PROP_DATE,
            categoryProperty = BuildConfig.NOTION_PROP_CATEGORY,
            categoryType = BuildConfig.NOTION_PROP_CATEGORY_TYPE,
            categoryDataSourceId = BuildConfig.NOTION_CATEGORY_DATA_SOURCE_ID,
            accountProperty = BuildConfig.NOTION_PROP_ACCOUNT,
            accountsDataSourceId = BuildConfig.NOTION_ACCOUNTS_DATA_SOURCE_ID,
            databaseId = BuildConfig.NOTION_DATABASE_ID
        )
    }
}
