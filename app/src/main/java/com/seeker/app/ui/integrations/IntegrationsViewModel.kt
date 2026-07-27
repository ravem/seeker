package com.seeker.app.ui.integrations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seeker.app.data.meraki.MerakiApiClient
import com.seeker.app.data.meraki.MerakiRepository
import com.seeker.app.data.omada.OmadaApiClient
import com.seeker.app.data.omada.OmadaRepository
import com.seeker.app.data.settings.SecurePreferences
import com.seeker.app.data.unifi.UniFiApiClient
import com.seeker.app.data.unifi.UniFiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IntegrationsUiState(
    val expandedMeraki: Boolean = false,
    val expandedUnifi: Boolean = false,
    val expandedOmada: Boolean = false,
    // Meraki
    val merakiApiKey: String = "",
    val merakiOrgId: String = "",
    val merakiStatus: String? = null,
    val merakiTesting: Boolean = false,
    val merakiOrganizations: List<MerakiOrgOption> = emptyList(),
    val merakiLoadingOrgs: Boolean = false,
    // UniFi
    val unifiUrl: String = "",
    val unifiUsername: String = "",
    val unifiPassword: String = "",
    val unifiStatus: String? = null,
    val unifiTesting: Boolean = false,
    // Omada
    val omadaUrl: String = "",
    val omadaUsername: String = "",
    val omadaPassword: String = "",
    val omadaStatus: String? = null,
    val omadaTesting: Boolean = false
)

data class MerakiOrgOption(
    val id: String,
    val name: String
)

@HiltViewModel
class IntegrationsViewModel @Inject constructor(
    private val securePreferences: SecurePreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(IntegrationsUiState())
    val uiState: StateFlow<IntegrationsUiState> = _uiState.asStateFlow()

    init {
        loadCredentials()
    }

    private fun loadCredentials() {
        viewModelScope.launch {
            // Meraki (crittografate)
            val merakiApiKey = securePreferences.merakiApiKey.first()
            val merakiOrgId = securePreferences.merakiOrgId.first()
            _uiState.update { it.copy(merakiApiKey = merakiApiKey, merakiOrgId = merakiOrgId) }

            // UniFi
            val unifiUrl = securePreferences.unifiUrl.first()
            val unifiUsername = securePreferences.unifiUsername.first()
            val unifiPassword = securePreferences.unifiPassword.first()
            _uiState.update { it.copy(unifiUrl = unifiUrl, unifiUsername = unifiUsername, unifiPassword = unifiPassword) }

            // Omada
            val omadaUrl = securePreferences.omadaUrl.first()
            val omadaUsername = securePreferences.omadaUsername.first()
            val omadaPassword = securePreferences.omadaPassword.first()
            _uiState.update { it.copy(omadaUrl = omadaUrl, omadaUsername = omadaUsername, omadaPassword = omadaPassword) }
        }
    }

    fun toggleMeraki() { _uiState.update { it.copy(expandedMeraki = !it.expandedMeraki) } }
    fun toggleUnifi() { _uiState.update { it.copy(expandedUnifi = !it.expandedUnifi) } }
    fun toggleOmada() { _uiState.update { it.copy(expandedOmada = !it.expandedOmada) } }

    fun setMerakiApiKey(v: String) { _uiState.update { it.copy(merakiApiKey = v) } }
    fun setMerakiOrgId(v: String) { _uiState.update { it.copy(merakiOrgId = v) } }
    fun setUnifiUrl(v: String) { _uiState.update { it.copy(unifiUrl = v) } }
    fun setUnifiUsername(v: String) { _uiState.update { it.copy(unifiUsername = v) } }
    fun setUnifiPassword(v: String) { _uiState.update { it.copy(unifiPassword = v) } }
    fun setOmadaUrl(v: String) { _uiState.update { it.copy(omadaUrl = v) } }
    fun setOmadaUsername(v: String) { _uiState.update { it.copy(omadaUsername = v) } }
    fun setOmadaPassword(v: String) { _uiState.update { it.copy(omadaPassword = v) } }

    // ── Salvataggio (crittografato) ──

    fun saveMeraki() {
        viewModelScope.launch {
            securePreferences.setMerakiCredentials(
                _uiState.value.merakiApiKey.trim(),
                _uiState.value.merakiOrgId.trim()
            )
            _uiState.update { it.copy(merakiStatus = "✅ Credenziali salvate") }
        }
    }

    fun saveUnifi() {
        viewModelScope.launch {
            securePreferences.setUnifiCredentials(
                _uiState.value.unifiUrl.trim(),
                _uiState.value.unifiUsername.trim(),
                _uiState.value.unifiPassword
            )
            _uiState.update { it.copy(unifiStatus = "✅ Credenziali salvate") }
        }
    }

    fun saveOmada() {
        viewModelScope.launch {
            securePreferences.setOmadaCredentials(
                _uiState.value.omadaUrl.trim(),
                _uiState.value.omadaUsername.trim(),
                _uiState.value.omadaPassword
            )
            _uiState.update { it.copy(omadaStatus = "✅ Credenziali salvate") }
        }
    }

    // ── Eliminazione ──

    fun deleteMeraki() {
        viewModelScope.launch {
            securePreferences.setMerakiCredentials("", "")
            _uiState.update { it.copy(
                merakiApiKey = "", merakiOrgId = "",
                merakiStatus = "🗑️ Credenziali eliminate"
            )}
        }
    }

    fun deleteUnifi() {
        viewModelScope.launch {
            securePreferences.setUnifiCredentials("", "", "")
            _uiState.update { it.copy(
                unifiUrl = "", unifiUsername = "", unifiPassword = "",
                unifiStatus = "🗑️ Credenziali eliminate"
            )}
        }
    }

    fun deleteOmada() {
        viewModelScope.launch {
            securePreferences.setOmadaCredentials("", "", "")
            _uiState.update { it.copy(
                omadaUrl = "", omadaUsername = "", omadaPassword = "",
                omadaStatus = "🗑️ Credenziali eliminate"
            )}
        }
    }

    // ── Test Connessione ──

    fun testMeraki() {
        viewModelScope.launch {
            _uiState.update { it.copy(merakiTesting = true, merakiStatus = "Test in corso…") }

            val apiKey = _uiState.value.merakiApiKey.trim()
            val orgId = _uiState.value.merakiOrgId.trim()

            if (apiKey.isBlank()) {
                _uiState.update { it.copy(merakiTesting = false, merakiStatus = "❌ Inserisci API Key") }
                return@launch
            }

            val client = MerakiApiClient(apiKey)
            val repository = MerakiRepository(client)

            val result = repository.testConnection()
            _uiState.update {
                it.copy(
                    merakiTesting = false,
                    merakiStatus = when (result) {
                        is com.seeker.app.data.meraki.MerakiResult.Success -> result.data
                        is com.seeker.app.data.meraki.MerakiResult.Error -> "❌ ${result.message}"
                    }
                )
            }

            if (result is com.seeker.app.data.meraki.MerakiResult.Success) {
                securePreferences.setMerakiCredentials(apiKey, orgId)
                loadMerakiOrganizations(apiKey)
            }
        }
    }

    fun loadMerakiOrganizations(apiKey: String = _uiState.value.merakiApiKey.trim()) {
        if (apiKey.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(merakiLoadingOrgs = true) }
            val client = MerakiApiClient(apiKey)
            when (val result = client.getOrganizations()) {
                is com.seeker.app.data.meraki.MerakiResult.Success -> {
                    val orgs = result.data.map { MerakiOrgOption(it.id, it.name) }
                    _uiState.update { it.copy(merakiOrganizations = orgs, merakiLoadingOrgs = false) }
                }
                is com.seeker.app.data.meraki.MerakiResult.Error -> {
                    _uiState.update { it.copy(merakiLoadingOrgs = false) }
                }
            }
        }
    }

    fun selectMerakiOrganization(org: MerakiOrgOption) {
        _uiState.update { it.copy(merakiOrgId = org.id, merakiStatus = "✅ Selezionata: ${org.name}") }
        viewModelScope.launch {
            securePreferences.setMerakiCredentials(_uiState.value.merakiApiKey, org.id)
        }
    }

    fun testUnifi() {
        viewModelScope.launch {
            _uiState.update { it.copy(unifiTesting = true, unifiStatus = "Test in corso…") }

            val url = _uiState.value.unifiUrl
            val username = _uiState.value.unifiUsername
            val password = _uiState.value.unifiPassword

            if (url.isBlank()) {
                _uiState.update { it.copy(unifiTesting = false, unifiStatus = "❌ Inserisci URL controller") }
                return@launch
            }

            val client = UniFiApiClient(url, username, password)
            val repository = UniFiRepository(client)

            val result = repository.testConnection()
            _uiState.update {
                it.copy(
                    unifiTesting = false,
                    unifiStatus = when (result) {
                        is com.seeker.app.data.unifi.UniFiResult.Success -> result.data
                        is com.seeker.app.data.unifi.UniFiResult.Error -> "❌ ${result.message}"
                    }
                )
            }

            if (result is com.seeker.app.data.unifi.UniFiResult.Success) {
                securePreferences.setUnifiCredentials(url, username, password)
            }
        }
    }

    fun testOmada() {
        viewModelScope.launch {
            _uiState.update { it.copy(omadaTesting = true, omadaStatus = "Test in corso…") }

            val url = _uiState.value.omadaUrl
            val username = _uiState.value.omadaUsername
            val password = _uiState.value.omadaPassword

            if (url.isBlank()) {
                _uiState.update { it.copy(omadaTesting = false, omadaStatus = "❌ Inserisci URL controller") }
                return@launch
            }

            val client = OmadaApiClient(url, username, password)
            val repository = OmadaRepository(client)

            val result = repository.testConnection()
            _uiState.update {
                it.copy(
                    omadaTesting = false,
                    omadaStatus = when (result) {
                        is com.seeker.app.data.omada.OmadaResult.Success -> result.data
                        is com.seeker.app.data.omada.OmadaResult.Error -> "❌ ${result.message}"
                    }
                )
            }

            if (result is com.seeker.app.data.omada.OmadaResult.Success) {
                securePreferences.setOmadaCredentials(url, username, password)
            }
        }
    }
}

private fun <T> MutableStateFlow<T>.update(block: (T) -> T) {
    value = block(value)
}
