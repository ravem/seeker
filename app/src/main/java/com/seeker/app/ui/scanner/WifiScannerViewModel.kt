package com.seeker.app.ui.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seeker.app.core.model.AccessPoint
import com.seeker.app.core.model.AccessPointGroup
import com.seeker.app.core.util.ConnectionMonitor
import com.seeker.app.data.wifi.WifiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val SCAN_COOLDOWN_MS = 30_000L // 30 secondi tra scansioni automatiche (rispetta throttling Android)

enum class SortMode { BY_SIGNAL, BY_NAME }

data class WifiScannerUiState(
    val groups: List<AccessPointGroup> = emptyList(),
    val accessPoints: List<AccessPoint> = emptyList(),
    val isScanning: Boolean = false,
    val sortMode: SortMode = SortMode.BY_SIGNAL,
    val error: String? = null,
    val lastScanCount: Int = 0,
    val isOnWifi: Boolean = false   // false se connesso via Ethernet
)

@HiltViewModel
class WifiScannerViewModel @Inject constructor(
    private val wifiRepository: WifiRepository,
    @ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(WifiScannerUiState())
    val uiState: StateFlow<WifiScannerUiState> = _uiState.asStateFlow()

    private var autoScanJob: Job? = null
    private var lastScanTimeMs = 0L

    init {
        // Rileva il tipo di connessione
        val conn = ConnectionMonitor.getActiveNetwork(context)
        _uiState.update { it.copy(isOnWifi = conn?.isWifi == true) }

        observeScanResults()
        refresh()
    }

    /**
     * Osserva la scansione passiva: quando il sistema pubblica nuovi risultati
     * (es. da altre app o dal sistema), li raccogliamo senza chiamare startScan().
     */
    private fun observeScanResults() {
        viewModelScope.launch {
            wifiRepository.observeAccessPointGroups().collect { groups ->
                if (groups.isNotEmpty()) {
                    val sorted = sortGroups(groups, _uiState.value.sortMode)
                    _uiState.update { it.copy(
                        groups = sorted,
                        accessPoints = sorted.flatMap { it.accessPoints },
                        isScanning = false,
                        error = null,
                        lastScanCount = _uiState.value.lastScanCount + 1
                    )}
                }
            }
        }
    }

    fun toggleSort() {
        val newMode = if (_uiState.value.sortMode == SortMode.BY_SIGNAL) SortMode.BY_NAME else SortMode.BY_SIGNAL
        _uiState.update { it.copy(
            sortMode = newMode,
            groups = sortGroups(it.groups, newMode)
        )}
    }

    /**
     * Avvia una scansione Wi-Fi manuale.
     * Rispetta il throttling: se l'ultima scansione è stata meno di 30 secondi fa,
     * non avvia una nuova scansione ma usa i risultati più recenti.
     */
    fun refresh() {
        val now = System.currentTimeMillis()
        if (now - lastScanTimeMs < SCAN_COOLDOWN_MS && _uiState.value.accessPoints.isNotEmpty()) {
            // Troppo presto, ma abbiamo risultati recenti
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true) }

            val success = wifiRepository.startScan()
            if (!success) {
                // Fallback: prova a leggere i risultati esistenti
                val groups = wifiRepository.getAccessPointGroups()
                if (groups.isNotEmpty()) {
                    val sorted = sortGroups(groups, _uiState.value.sortMode)
                    _uiState.update { it.copy(
                        groups = sorted,
                        accessPoints = sorted.flatMap { it.accessPoints },
                        isScanning = false
                    )}
                } else {
                    _uiState.update { it.copy(
                        isScanning = false,
                        error = "Scansione temporaneamente non disponibile. " +
                                "Android limita le scansioni Wi-Fi a 4 ogni 2 minuti. " +
                                "Riprova tra qualche istante."
                    )}
                }
            } else {
                lastScanTimeMs = now
                // I risultati arriveranno asincroni via observeScanResults()
                // Se dopo 3 secondi non arrivano, forziamo la lettura
                delay(3000)
                val groups = wifiRepository.getAccessPointGroups()
                if (groups.isNotEmpty()) {
                    val sorted = sortGroups(groups, _uiState.value.sortMode)
                    _uiState.update { it.copy(
                        groups = sorted,
                        accessPoints = sorted.flatMap { it.accessPoints },
                        isScanning = false,
                        error = null
                    )}
                } else {
                    _uiState.update { it.copy(isScanning = false) }
                }
            }
        }
    }

    private fun sortGroups(groups: List<AccessPointGroup>, mode: SortMode): List<AccessPointGroup> {
        return when (mode) {
            SortMode.BY_SIGNAL -> groups.sortedByDescending { it.bestSignal }
            SortMode.BY_NAME -> groups.sortedBy { it.ssid.lowercase() }
        }
    }

    override fun onCleared() {
        super.onCleared()
        autoScanJob?.cancel()
    }
}

private fun <T> MutableStateFlow<T>.update(block: (T) -> T) {
    value = block(value)
}
