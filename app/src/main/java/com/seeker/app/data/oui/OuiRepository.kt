package com.seeker.app.data.oui

import android.util.Log
import com.seeker.app.core.util.OuiDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "SeekerOuiRepo"

/**
 * Repository per l'accesso al database OUI.
 * Astrae [OuiDatabase] e fornisce API a livello applicativo con
 * supporto per aggiornamento periodico.
 */
@Singleton
class OuiRepository @Inject constructor(
    private val ouiDatabase: OuiDatabase
) {
    private val _lastUpdateTimestamp = MutableStateFlow(ouiDatabase.lastUpdateTimestamp())
    val lastUpdateTimestamp: Flow<Long> = _lastUpdateTimestamp.asStateFlow()

    private val _entryCount = MutableStateFlow(ouiDatabase.entryCount())
    val entryCount: Flow<Int> = _entryCount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: Flow<Boolean> = _isLoading.asStateFlow()

    /**
     * Inizializza il database OUI (carica in memoria).
     */
    suspend fun initialize(): Result<Unit> {
        _isLoading.value = true
        val result = ouiDatabase.load()
        _isLoading.value = false
        if (result.isSuccess) {
            _lastUpdateTimestamp.value = ouiDatabase.lastUpdateTimestamp()
            _entryCount.value = ouiDatabase.entryCount()
            Log.d(TAG, "OUI database caricato: ${_entryCount.value} entry")
        } else {
            Log.w(TAG, "Errore caricamento OUI database: ${result.exceptionOrNull()?.message}")
        }
        return result
    }

    /**
     * Cerca il vendor per un dato MAC address.
     */
    fun lookupVendor(macAddress: String): String? =
        ouiDatabase.lookup(macAddress)

    /**
     * Aggiorna il database OUI dalla fonte remota IEEE.
     */
    suspend fun updateFromRemote(): Result<Unit> {
        _isLoading.value = true
        val result = ouiDatabase.updateFromRemote()
        _isLoading.value = false
        if (result.isSuccess) {
            _lastUpdateTimestamp.value = ouiDatabase.lastUpdateTimestamp()
            _entryCount.value = ouiDatabase.entryCount()
            Log.i(TAG, "OUI database aggiornato: ${_entryCount.value} entry")
        } else {
            Log.w(TAG, "Aggiornamento OUI fallito: ${result.exceptionOrNull()?.message}")
        }
        return result
    }

    /**
     * Controlla se il database OUI è obsoleto e lo aggiorna se necessario.
     *
     * @param staleThresholdDays Età massima del database prima di considerarlo obsoleto (default: 7)
     * @return true se l'aggiornamento è stato eseguito con successo, false altrimenti
     */
    suspend fun checkAndUpdateIfStale(staleThresholdDays: Int = 7): Boolean {
        val lastUpdate = ouiDatabase.lastUpdateTimestamp()
        val now = System.currentTimeMillis()
        val ageMs = now - lastUpdate
        val staleThresholdMs = staleThresholdDays * 24L * 60L * 60L * 1000L

        if (lastUpdate == 0L) {
            Log.d(TAG, "OUI database mai aggiornato: avvio download remoto")
        } else if (ageMs < staleThresholdMs) {
            val ageDays = ageMs / (24L * 60L * 60L * 1000L)
            Log.d(TAG, "OUI database aggiornato $ageDays giorni fa, non ancora obsoleto")
            return false
        } else {
            val ageDays = ageMs / (24L * 60L * 60L * 1000L)
            Log.d(TAG, "OUI database obsoleto ($ageDays giorni): aggiornamento...")
        }

        val result = updateFromRemote()
        return result.isSuccess
    }

    /**
     * Numero di entry nel database.
     */
    fun entryCount(): Int = ouiDatabase.entryCount()
}
