package com.seeker.app.ui.discovery

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seeker.app.core.model.LanDevice
import com.seeker.app.core.model.PortInfo
import com.seeker.app.core.util.ConnectionMonitor
import com.seeker.app.data.network.NetworkRepository
import com.seeker.app.data.network.ScanProgress
import com.seeker.app.data.wifi.WifiRepository
import com.seeker.app.core.model.ConnectedNetwork
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LanDiscoveryUiState(
    val devices: List<LanDevice> = emptyList(),
    val isScanning: Boolean = false,
    val scanMessage: String? = null,
    val error: String? = null,
    val scanningDeviceIp: String? = null,  // IP del dispositivo in port scan
    val gatewayIp: String? = null,          // IP del gateway dinamico
    val portScanProgress: PortScanProgress? = null,  // Progresso scansione porte
    val currentNetwork: ConnectedNetwork? = null     // AP a cui siamo connessi
)

data class PortScanProgress(
    val ipAddress: String,
    val currentPort: Int,
    val totalPorts: Int,
    val transport: String
) {
    val progressPercent: Float
        get() = if (totalPorts > 0) currentPort.toFloat() / totalPorts else 0f
    val message: String
        get() = "Scansione porte su $ipAddress: $currentPort/$totalPorts ($transport)"
}

@HiltViewModel
class LanDiscoveryViewModel @Inject constructor(
    private val networkRepository: NetworkRepository,
    private val wifiRepository: WifiRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(LanDiscoveryUiState())
    val uiState: StateFlow<LanDiscoveryUiState> = _uiState.asStateFlow()

    private var scanJob: Job? = null

    init {
        // Legge il gateway IP e la rete corrente (Wi-Fi o Ethernet)
        val currentConnection = ConnectionMonitor.getActiveNetwork(context)
            ?: wifiRepository.getCurrentConnection()
        val gatewayIp = currentConnection?.defaultGateway
        Log.d("SeekerVM", "Gateway IP: $gatewayIp, Network: ${currentConnection?.displayName}")
        _uiState.update { it.copy(gatewayIp = gatewayIp, currentNetwork = currentConnection) }

        // Osserva risultati incrementali della scansione
        viewModelScope.launch {
            networkRepository.scanResults.collect { devices ->
                Log.d("SeekerVM", "scanResults: ${devices.size} devices")
                _uiState.update { it.copy(devices = devices) }
            }
        }

        // Osserva progresso scansione
        viewModelScope.launch {
            networkRepository.scanProgress.collect { progress ->
                when (progress) {
                    is ScanProgress.Scanning -> _uiState.update { it.copy(scanMessage = progress.message, isScanning = true) }
                    is ScanProgress.Complete -> _uiState.update { it.copy(isScanning = false, scanMessage = null, devices = progress.devices) }
                    is ScanProgress.Error -> _uiState.update { it.copy(isScanning = false, scanMessage = null, error = progress.message) }
                    is ScanProgress.PortScanning -> {
                        _uiState.update { it.copy(
                            portScanProgress = PortScanProgress(
                                ipAddress = progress.ipAddress,
                                currentPort = progress.port,
                                totalPorts = progress.total,
                                transport = progress.transport
                            )
                        )}
                    }
                    is ScanProgress.PortScanComplete -> _uiState.update { it.copy(scanningDeviceIp = null, portScanProgress = null) }
                    else -> _uiState.update { it.copy(isScanning = false, scanMessage = null) }
                }
            }
        }
    }

    fun startScan() {
        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true, error = null) }
            networkRepository.scanLocalNetwork()
        }
    }

    fun scanPorts(device: LanDevice) {
        viewModelScope.launch {
            _uiState.update { it.copy(scanningDeviceIp = device.ipAddress, portScanProgress = null) }
            networkRepository.scanDevicePorts(device)
        }
    }

    fun getDevicePorts(ipAddress: String): List<PortInfo> {
        return _uiState.value.devices.find { it.ipAddress == ipAddress }?.ports ?: emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        scanJob?.cancel()
    }
}

private fun <T> MutableStateFlow<T>.update(block: (T) -> T) {
    value = block(value)
}
