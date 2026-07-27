package com.seeker.app

import android.app.Application
import android.util.Log
import com.seeker.app.data.oui.OuiRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Application class per Seeker.
 * Inizializza Hilt dependency injection e componenti all'avvio.
 */
@HiltAndroidApp
class SeekerApp : Application() {

    @Inject lateinit var ouiRepository: OuiRepository

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        applicationScope.launch {
            initializeOuiDatabase()
        }
    }

    /**
     * Inizializza il database OUI in background:
     * 1. Carica il database in memoria (da cache o asset bundled)
     * 2. Se è obsoleto (>7 giorni), avvia il download della versione aggiornata
     *
     * L'aggiornamento remoto è silenzioso: se fallisce, il database bundled
     * o la cache esistente rimangono in uso senza disturbare l'utente.
     */
    private suspend fun initializeOuiDatabase() {
        Log.i("SeekerApp", "Inizializzazione database OUI...")

        // 1. Carica il database (da cache o asset)
        val loadResult = ouiRepository.initialize()
        if (loadResult.isFailure) {
            Log.w("SeekerApp", "Caricamento OUI database fallito: ${loadResult.exceptionOrNull()?.message}")
        }

        // 2. Controlla se è obsoleto e aggiorna silenziosamente
        val needsUpdate = ouiRepository.checkAndUpdateIfStale(staleThresholdDays = 7)
        if (needsUpdate) {
            Log.i("SeekerApp", "OUI database aggiornato con successo")
        }
    }
}
