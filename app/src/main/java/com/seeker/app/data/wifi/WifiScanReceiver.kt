package com.seeker.app.data.wifi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager

/**
 * BroadcastReceiver per ricevere notifiche di completamento scansione Wi-Fi.
 */
class WifiScanReceiver(
    private val onScanComplete: () -> Unit
) : BroadcastReceiver() {

    val intentFilter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
            val success = intent.getBooleanExtra(
                WifiManager.EXTRA_RESULTS_UPDATED,
                false
            )
            if (success) {
                onScanComplete()
            }
        }
    }
}
