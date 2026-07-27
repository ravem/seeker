package com.seeker.app.ui.currentconnection

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.seeker.app.core.model.ConnectedNetwork
import com.seeker.app.core.model.MobileNetworksState
import com.seeker.app.core.permissions.PermissionManager
import com.seeker.app.core.util.ConnectionMonitor
import com.seeker.app.core.util.LatencyMonitor
import com.seeker.app.core.util.SpeedTest
import com.seeker.app.core.util.SpeedTestResult
import com.seeker.app.data.telephony.MobileNetworkMonitor
import com.seeker.app.data.wifi.WifiRepository
import com.seeker.app.ui.components.SignalSample
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val POLL_MS = 1000L
private const val MAX_HISTORY_MS = 60_000L  // 60 secondi di storico

data class CurrentConnectionUiState(
    val connectedNetwork: ConnectedNetwork? = null,
    val isLoading: Boolean = true,
    val isWifiEnabled: Boolean = true,
    val hasPermission: Boolean = false,
    val errorMessage: String? = null,
    val mobileNetworks: MobileNetworksState = MobileNetworksState(),
    val signalHistory: List<SignalSample> = emptyList(),
    val speedTestResult: SpeedTestResult? = null,
    val isSpeedTesting: Boolean = false,
    val showSpeedTest: Boolean = true
)

@HiltViewModel
class CurrentConnectionViewModel @Inject constructor(
    application: Application,
    private val wifiRepository: WifiRepository,
    private val latencyMonitor: LatencyMonitor,
    private val speedTest: SpeedTest,
    private val mobileNetworkMonitor: MobileNetworkMonitor
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(CurrentConnectionUiState())
    val uiState: StateFlow<CurrentConnectionUiState> = _uiState.asStateFlow()
    private var pollJob: Job? = null

    init {
        startPolling()
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            val pm = PermissionManager(ctx)
            _uiState.update {
                it.copy(hasPermission = pm.hasRequiredPermissions(), isWifiEnabled = pm.isWifiEnabled())
            }
        }
    }

    private fun startPolling() {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (isActive) {
                // Usa ConnectionMonitor che supporta Wi-Fi ed Ethernet
                val ctx = getApplication<Application>()
                val network = ConnectionMonitor.getActiveNetwork(ctx)
                    ?: wifiRepository.getCurrentConnection()

                // Storico segnale
                val now = System.currentTimeMillis()
                val history = _uiState.value.signalHistory.toMutableList()
                if (network != null) {
                    history.add(SignalSample(timestampMs = now, dbm = network.signalStrengthDbm))
                }
                // Pulisci campioni più vecchi di MAX_HISTORY_MS
                val cutoff = now - MAX_HISTORY_MS
                while (history.isNotEmpty() && history.first().timestampMs < cutoff) {
                    history.removeAt(0)
                }

                // Latenza
                val updatedNetwork = if (network != null) {
                    val gwLatency = latencyMonitor.ping(network.defaultGateway, 2000)
                    val inetLatency = latencyMonitor.ping("1.1.1.1", 3000)
                    network.copy(latencyGatewayMs = gwLatency, latencyInternetMs = inetLatency)
                } else network

                // Rete mobile
                val mobile = mobileNetworkMonitor.getMobileNetworks()

                _uiState.update {
                    it.copy(
                        connectedNetwork = updatedNetwork,
                        isLoading = false,
                        errorMessage = if (updatedNetwork == null) "Non connesso a nessuna rete" else null,
                        mobileNetworks = mobile,
                        signalHistory = history
                    )
                }
                delay(POLL_MS)
            }
        }
    }

    /**
     * Avvia uno speed test.
     */
    fun runSpeedTest() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSpeedTesting = true, speedTestResult = null) }
            val result = speedTest.runFullTest()
            _uiState.update { it.copy(isSpeedTesting = false, speedTestResult = result) }
        }
    }

    /**
     * Nasconde/mostra lo speed test.
     */
    fun toggleSpeedTest() {
        _uiState.update { it.copy(showSpeedTest = !it.showSpeedTest) }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val network = wifiRepository.getCurrentConnection()
            _uiState.update {
                it.copy(
                    connectedNetwork = network,
                    isLoading = false,
                    errorMessage = if (network == null) "Non connesso a nessuna rete" else null
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollJob?.cancel()
    }
}

private fun <T> MutableStateFlow<T>.update(block: (T) -> T) {
    value = block(value)
}
