package com.seeker.app.data.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestisce le credenziali sensibili (API key, password) in modo sicuro
 * usando EncryptedSharedPreferences (AES-256 GCM).
 *
 * I dati vengono crittografati con una chiave master protetta dall'Android KeyStore,
 * quindi non estraibili neppure con backup o accesso fisico al file system.
 */
@Singleton
class SecurePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "seeker_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // ── Lettura/Scrittura ──

    private fun get(key: String): String = prefs.getString(key, "") ?: ""
    private fun set(key: String, value: String) = prefs.edit().putString(key, value).apply()
    private fun remove(key: String) = prefs.edit().remove(key).apply()

    /**
     * Flow che emette il valore corrente e si aggiorna sui cambiamenti.
     */
    private fun observe(key: String): Flow<String> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, k ->
            if (k == key) trySend(get(key))
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(get(key))
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }.flowOn(Dispatchers.IO)

    // ── Meraki ──

    companion object {
        private const val KEY_MERAKI_API_KEY = "meraki_api_key"
        private const val KEY_MERAKI_ORG_ID = "meraki_org_id"
        private const val KEY_UNIFI_URL = "unifi_url"
        private const val KEY_UNIFI_USERNAME = "unifi_username"
        private const val KEY_UNIFI_PASSWORD = "unifi_password"
        private const val KEY_OMADA_URL = "omada_url"
        private const val KEY_OMADA_USERNAME = "omada_username"
        private const val KEY_OMADA_PASSWORD = "omada_password"
    }

    val merakiApiKey: Flow<String> get() = observe(KEY_MERAKI_API_KEY)
    val merakiOrgId: Flow<String> get() = observe(KEY_MERAKI_ORG_ID)
    val unifiUrl: Flow<String> get() = observe(KEY_UNIFI_URL)
    val unifiUsername: Flow<String> get() = observe(KEY_UNIFI_USERNAME)
    val unifiPassword: Flow<String> get() = observe(KEY_UNIFI_PASSWORD)
    val omadaUrl: Flow<String> get() = observe(KEY_OMADA_URL)
    val omadaUsername: Flow<String> get() = observe(KEY_OMADA_USERNAME)
    val omadaPassword: Flow<String> get() = observe(KEY_OMADA_PASSWORD)

    suspend fun getMerakiApiKey(): String = get(KEY_MERAKI_API_KEY)
    suspend fun getMerakiOrgId(): String = get(KEY_MERAKI_ORG_ID)
    suspend fun getUnifiUrl(): String = get(KEY_UNIFI_URL)
    suspend fun getUnifiUsername(): String = get(KEY_UNIFI_USERNAME)
    suspend fun getUnifiPassword(): String = get(KEY_UNIFI_PASSWORD)
    suspend fun getOmadaUrl(): String = get(KEY_OMADA_URL)
    suspend fun getOmadaUsername(): String = get(KEY_OMADA_USERNAME)
    suspend fun getOmadaPassword(): String = get(KEY_OMADA_PASSWORD)

    suspend fun setMerakiCredentials(apiKey: String, orgId: String) {
        set(KEY_MERAKI_API_KEY, apiKey)
        set(KEY_MERAKI_ORG_ID, orgId)
    }

    suspend fun setUnifiCredentials(url: String, username: String, password: String) {
        set(KEY_UNIFI_URL, url)
        set(KEY_UNIFI_USERNAME, username)
        set(KEY_UNIFI_PASSWORD, password)
    }

    suspend fun setOmadaCredentials(url: String, username: String, password: String) {
        set(KEY_OMADA_URL, url)
        set(KEY_OMADA_USERNAME, username)
        set(KEY_OMADA_PASSWORD, password)
    }
}
