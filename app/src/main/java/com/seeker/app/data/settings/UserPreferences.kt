package com.seeker.app.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "seeker_settings")

enum class ThemeMode(val value: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromValue(value: String): ThemeMode =
            entries.find { it.value == value } ?: SYSTEM
    }
}

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val OUI_UPDATE_INTERVAL_DAYS = intPreferencesKey("oui_update_interval_days")
        val SCAN_INTERVAL_SECONDS = intPreferencesKey("scan_interval_seconds")

        // API credentials
        val MERAKI_API_KEY = stringPreferencesKey("meraki_api_key")
        val MERAKI_ORG_ID = stringPreferencesKey("meraki_org_id")

        val UNIFI_URL = stringPreferencesKey("unifi_url")
        val UNIFI_USERNAME = stringPreferencesKey("unifi_username")
        val UNIFI_PASSWORD = stringPreferencesKey("unifi_password")

        val OMADA_URL = stringPreferencesKey("omada_url")
        val OMADA_USERNAME = stringPreferencesKey("omada_username")
        val OMADA_PASSWORD = stringPreferencesKey("omada_password")
    }

    // ── Theme ──

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        ThemeMode.fromValue(prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.value)
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.value
        }
    }

    // ── OUI Update Interval ──

    val ouiUpdateIntervalDays: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.OUI_UPDATE_INTERVAL_DAYS] ?: 7
    }

    suspend fun setOuiUpdateIntervalDays(days: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.OUI_UPDATE_INTERVAL_DAYS] = days
        }
    }

    // ── Scan Interval ──

    val scanIntervalSeconds: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.SCAN_INTERVAL_SECONDS] ?: 30
    }

    suspend fun setScanIntervalSeconds(seconds: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SCAN_INTERVAL_SECONDS] = seconds
        }
    }

    // ── Meraki ──

    val merakiApiKey: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.MERAKI_API_KEY] ?: ""
    }

    val merakiOrgId: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.MERAKI_ORG_ID] ?: ""
    }

    suspend fun setMerakiCredentials(apiKey: String, orgId: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.MERAKI_API_KEY] = apiKey
            prefs[Keys.MERAKI_ORG_ID] = orgId
        }
    }

    // ── UniFi ──

    val unifiUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.UNIFI_URL] ?: ""
    }
    val unifiUsername: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.UNIFI_USERNAME] ?: ""
    }
    val unifiPassword: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.UNIFI_PASSWORD] ?: ""
    }

    suspend fun setUnifiCredentials(url: String, username: String, password: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.UNIFI_URL] = url
            prefs[Keys.UNIFI_USERNAME] = username
            prefs[Keys.UNIFI_PASSWORD] = password
        }
    }

    // ── Omada ──

    val omadaUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.OMADA_URL] ?: ""
    }
    val omadaUsername: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.OMADA_USERNAME] ?: ""
    }
    val omadaPassword: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.OMADA_PASSWORD] ?: ""
    }

    suspend fun setOmadaCredentials(url: String, username: String, password: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.OMADA_URL] = url
            prefs[Keys.OMADA_USERNAME] = username
            prefs[Keys.OMADA_PASSWORD] = password
        }
    }
}
