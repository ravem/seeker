package com.seeker.app.ui.heatmap

import android.content.Context
import android.net.wifi.WifiManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seeker.app.core.model.*
import com.seeker.app.core.util.HeatmapEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HeatmapUiState(
    val session: HeatmapSession = HeatmapSession(name = "Nuovo rilievo"),
    val cells: List<CellSignal> = emptyList(),
    val availableBssids: List<BssidInfo> = emptyList(),
    val selectedBssid: String? = null,
    val isScanning: Boolean = false,
    val lastScanRssi: Int? = null,
    val message: String? = null,
    val savedSessions: List<HeatmapSession> = emptyList()
)

data class BssidInfo(
    val bssid: String,
    val ssid: String,
    val samplesCount: Int = 0
)

@HiltViewModel
class HeatmapViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(HeatmapUiState())
    val uiState: StateFlow<HeatmapUiState> = _uiState.asStateFlow()

    private val wifiManager: WifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    init {
        loadSessionsList()
    }

    /** Crea una nuova sessione di rilevamento */
    fun newSession(name: String = "Rilievo ${java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}") {
        _uiState.update {
            it.copy(
                session = HeatmapSession(name = name),
                cells = emptyList(),
                selectedBssid = null,
                message = null
            )
        }
        refreshAvailableBssids()
    }

    /** Imposta le dimensioni della griglia */
    fun setGridSize(width: Int, height: Int) {
        _uiState.update {
            it.copy(
                session = it.session.copy(gridWidth = width.coerceIn(3, 50), gridHeight = height.coerceIn(3, 50)),
                cells = emptyList()
            )
        }
    }

    /** Aggiunge un punto di rilevamento nella posizione toccata (coordinate griglia) */
    fun addScanPoint(gridX: Float, gridY: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isScanning = true, message = null) }

            val scanResults = try {
                wifiManager.startScan()
                kotlinx.coroutines.delay(500) // aspetta la scansione
                wifiManager.scanResults ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }

            if (scanResults.isEmpty()) {
                _uiState.update { it.copy(isScanning = false, message = "Nessuna rete Wi-Fi trovata in questo punto") }
                return@launch
            }

            val readings = scanResults.map { result ->
                BssidReading(
                    bssid = result.BSSID ?: "",
                    ssid = result.SSID ?: "<Rete Nascosta>",
                    rssi = result.level,
                    frequencyMhz = result.frequency
                )
            }

            val point = HeatmapScanPoint(
                x = gridX,
                y = gridY,
                readings = readings
            )

            val session = _uiState.value.session
            val updatedSession = session.copy(
                scanPoints = session.scanPoints + point,
                updatedAt = System.currentTimeMillis()
            )

            // Trova il segnale più forte per feedback
            val bestRssi = readings.maxByOrNull { it.rssi }?.rssi

            _uiState.update {
                it.copy(
                    session = updatedSession,
                    isScanning = false,
                    lastScanRssi = bestRssi,
                    message = "Punto (${"%.0f".format(gridX)}, ${"%.0f".format(gridY)}): ${readings.size} reti rilevate"
                )
            }

            refreshAvailableBssids()
            interpolate()
        }
    }

    /** Aggiorna la lista dei BSSID disponibili */
    private fun refreshAvailableBssids() {
        val bssids = mutableMapOf<String, Pair<String, Int>>() // bssid -> (ssid, count)
        for (point in _uiState.value.session.scanPoints) {
            for (reading in point.readings) {
                val current = bssids[reading.bssid]
                bssids[reading.bssid] = Pair(reading.ssid, (current?.second ?: 0) + 1)
            }
        }
        val sorted = bssids.entries
            .sortedByDescending { it.value.second }
            .map { (bssid, info) ->
                BssidInfo(bssid = bssid, ssid = info.first, samplesCount = info.second)
            }

        _uiState.update {
            it.copy(
                availableBssids = sorted,
                selectedBssid = it.selectedBssid ?: sorted.firstOrNull()?.bssid
            )
        }
    }

    /** Seleziona il BSSID da visualizzare */
    fun selectBssid(bssid: String?) {
        _uiState.update { it.copy(selectedBssid = bssid) }
        interpolate()
    }

    /** Esegue l'interpolazione e aggiorna la heatmap */
    private fun interpolate() {
        val state = _uiState.value
        val session = state.session

        if (session.scanPoints.size < 2) {
            _uiState.update { it.copy(cells = emptyList()) }
            return
        }

        val cells = HeatmapEngine.interpolate(
            points = session.scanPoints,
            bssid = state.selectedBssid,
            gridWidth = session.gridWidth,
            gridHeight = session.gridHeight
        )

        _uiState.update { it.copy(cells = cells) }
    }

    /** Salva la sessione corrente nelle preferenze locali (JSON) */
    fun saveSession() {
        viewModelScope.launch(Dispatchers.IO) {
            val session = _uiState.value.session
            // Per ora salviamo in SharedPreferences (poi si puo passare a DataStore/File)
            val prefs = context.getSharedPreferences("heatmap_sessions", Context.MODE_PRIVATE)
            val json = com.seeker.app.data.meraki.MerakiApiClient::class.java // placeholder
            // TODO: serializzare e salvare la sessione
            _uiState.update { it.copy(message = "Sessione salvata") }
            loadSessionsList()
        }
    }

    /** Carica la lista delle sessioni salvate */
    private fun loadSessionsList() {
        // TODO: caricare da storage locale
        _uiState.update { it.copy(savedSessions = emptyList()) }
    }

    /** Carica una sessione salvata */
    fun loadSession(session: HeatmapSession) {
        _uiState.update {
            it.copy(
                session = session,
                cells = emptyList(),
                selectedBssid = null
            )
        }
        refreshAvailableBssids()
        interpolate()
    }

    /** Elimina un punto di rilevamento */
    fun removeLastPoint() {
        val session = _uiState.value.session
        if (session.scanPoints.isEmpty()) return
        val updated = session.copy(
            scanPoints = session.scanPoints.dropLast(1),
            updatedAt = System.currentTimeMillis()
        )
        _uiState.update { it.copy(session = updated) }
        refreshAvailableBssids()
        interpolate()
    }

    override fun onCleared() {
        super.onCleared()
    }
}

private fun <T> MutableStateFlow<T>.update(block: (T) -> T) {
    value = block(value)
}
