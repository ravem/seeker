package com.seeker.app.core.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestisce la verifica e la richiesta dei permessi necessari a Seeker.
 */
@Singleton
class PermissionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Permessi necessari per il funzionamento base di Seeker.
     */
    val requiredPermissions: List<String>
        get() {
            val perms = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                perms.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            return perms
        }

    /**
     * Tutti i permessi (inclusi quelli automatici).
     */
    val allWifiPermissions: List<String>
        get() = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.INTERNET
        ) + if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.POST_NOTIFICATIONS)
        } else emptyList()

    /**
     * Verifica se tutti i permessi richiesti sono concessi.
     */
    fun hasRequiredPermissions(): Boolean =
        requiredPermissions.all { perm ->
            ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
        }

    /**
     * Verifica se un permesso specifico è concesso.
     */
    fun hasPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    /**
     * Verifica se la posizione è abilitata (necessaria per scansione Wi-Fi).
     */
    fun isLocationEnabled(): Boolean {
        return try {
            val locationMode = android.provider.Settings.Secure.getInt(
                context.contentResolver,
                android.provider.Settings.Secure.LOCATION_MODE
            )
            locationMode != android.provider.Settings.Secure.LOCATION_MODE_OFF
        } catch (_: Exception) {
            true
        }
    }

    /**
     * Verifica se il Wi-Fi è abilitato.
     */
    fun isWifiEnabled(): Boolean {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as? android.net.wifi.WifiManager
        return wifiManager?.isWifiEnabled == true
    }
}
