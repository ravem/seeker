package com.seeker.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.seeker.app.data.settings.UserPreferences
import com.seeker.app.ui.components.PermissionExplanationDialog
import com.seeker.app.ui.components.PermissionSettingsDialog
import com.seeker.app.ui.navigation.AppNavigation
import com.seeker.app.ui.theme.SeekerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var userPreferences: UserPreferences

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        permissionState = if (allGranted) {
            PermissionState.GRANTED
        } else {
            val showRationale = permissions.any { (perm, _) ->
                shouldShowRequestPermissionRationale(perm)
            }
            if (!showRationale && permissions.any { (_, granted) -> !granted }) {
                PermissionState.DENIED_PERMANENTLY
            } else {
                PermissionState.DENIED
            }
        }
    }

    private var permissionState by mutableStateOf(PermissionState.INITIAL)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        permissionState = if (hasRequiredPermissions()) {
            PermissionState.GRANTED
        } else {
            PermissionState.REQUIRED
        }

        setContent {
            SeekerTheme(userPreferences = userPreferences) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (permissionState) {
                        PermissionState.INITIAL,
                        PermissionState.GRANTED -> {
                            AppNavigation()
                        }

                        PermissionState.REQUIRED -> {
                            PermissionExplanationDialog(
                                title = "Permessi necessari",
                                explanation = "Per monitorare le reti Wi-Fi e scoprire i dispositivi sulla tua rete, " +
                                        "Seeker ha bisogno del permesso di accesso alla posizione.\n\n" +
                                        "Android richiede questo permesso per motivi di privacy. " +
                                        "Seeker non traccia né condivide la tua posizione in alcun modo.\n\n" +
                                        "Su Android 13+ è richiesto anche il permesso per le notifiche, " +
                                        "usato solo per mostrare lo stato delle scansioni.",
                                onGrant = { requestPermissions() },
                                onDeny = { permissionState = PermissionState.DENIED }
                            )
                        }

                        PermissionState.DENIED -> {
                            AppNavigation()
                        }

                        PermissionState.DENIED_PERMANENTLY -> {
                            PermissionSettingsDialog(
                                title = "Permesso negato permanentemente",
                                explanation = "Hai negato il permesso di posizione e selezionato 'Non chiedere più'. " +
                                        "Per utilizzare Seeker, concedi il permesso dalle Impostazioni.",
                                onOpenSettings = { openAppSettings() },
                                onDismiss = { permissionState = PermissionState.DENIED }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun hasRequiredPermissions(): Boolean {
        val locationFine = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val locationCoarse = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val nearbyWifi = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.NEARBY_WIFI_DEVICES
            ) == PackageManager.PERMISSION_GRANTED
        } else true
        val phoneState = if (Build.VERSION.SDK_INT >= 29) {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
        } else true
        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true
        return locationFine && locationCoarse && nearbyWifi && phoneState && notification
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 14+: NEARBY_WIFI_DEVICES sostituisce la location per BSSID
            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }
        if (Build.VERSION.SDK_INT >= 29) {
            permissions.add(Manifest.permission.READ_PHONE_STATE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        locationPermissionLauncher.launch(permissions.toTypedArray())
    }

    private fun openAppSettings() {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also {
            it.data = Uri.fromParts("package", packageName, null)
            startActivity(it)
        }
    }
}

enum class PermissionState {
    INITIAL, REQUIRED, GRANTED, DENIED, DENIED_PERMANENTLY
}
