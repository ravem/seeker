package com.seeker.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seeker.app.data.settings.ThemeMode
import com.seeker.app.data.settings.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val currentTheme: ThemeMode = ThemeMode.SYSTEM,
    val ouiUpdateIntervalDays: Int = 7,
    val scanIntervalSeconds: Int = 30
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferences.themeMode.collect { mode ->
                _uiState.update { it.copy(currentTheme = mode) }
            }
        }
        viewModelScope.launch {
            userPreferences.ouiUpdateIntervalDays.collect { days ->
                _uiState.update { it.copy(ouiUpdateIntervalDays = days) }
            }
        }
        viewModelScope.launch {
            userPreferences.scanIntervalSeconds.collect { sec ->
                _uiState.update { it.copy(scanIntervalSeconds = sec) }
            }
        }
    }

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch { userPreferences.setThemeMode(mode) }
    }

    fun setOuiUpdateInterval(days: Int) {
        viewModelScope.launch { userPreferences.setOuiUpdateIntervalDays(days) }
    }

    fun setScanInterval(seconds: Int) {
        viewModelScope.launch { userPreferences.setScanIntervalSeconds(seconds) }
    }
}

private fun <T> MutableStateFlow<T>.update(block: (T) -> T) {
    value = block(value)
}
