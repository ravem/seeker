package com.seeker.app.core.extension

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

val Context.wifiManager: WifiManager
    get() = getSystemService(Context.WIFI_SERVICE) as WifiManager

val Context.connectivityManager: ConnectivityManager
    get() = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

val Context.packageNameWithDebugSuffix: String
    get() {
        val base = packageName
        return try {
            val info = packageManager.getPackageInfo(base, 0)
            if (info.applicationInfo?.flags?.let { it and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0 } == true) {
                "$base.debug"
            } else base
        } catch (_: Exception) {
            base
        }
    }

fun Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

fun Context.hasNotificationPermission(): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else true

fun Context.isLocationEnabled(): Boolean {
    return try {
        val locationMode = Settings.Secure.getInt(
            contentResolver,
            Settings.Secure.LOCATION_MODE
        )
        locationMode != Settings.Secure.LOCATION_MODE_OFF
    } catch (_: Exception) {
        true
    }
}

fun Context.isWifiEnabled(): Boolean =
    wifiManager.isWifiEnabled

fun Context.openWifiSettings() {
    Intent(Settings.ACTION_WIFI_SETTINGS).also {
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(it)
    }
}

fun Context.openLocationSettings() {
    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).also {
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(it)
    }
}

fun Context.openAppSettings() {
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also {
        it.data = android.net.Uri.fromParts("package", packageName, null)
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(it)
    }
}
