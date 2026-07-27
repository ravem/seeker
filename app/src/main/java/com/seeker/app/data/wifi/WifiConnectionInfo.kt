package com.seeker.app.data.wifi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import com.seeker.app.core.extension.connectivityManager
import com.seeker.app.core.extension.wifiManager
import com.seeker.app.core.model.ConnectedNetwork
import com.seeker.app.core.util.WifiUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Osserva i cambiamenti della connessione Wi-Fi attuale.
 * Emette un flusso continuo di [ConnectedNetwork] o null (quando disconnesso).
 */
@Singleton
class WifiConnectionInfo @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val wifiManager: WifiManager = context.wifiManager
    private val connectivityManager: ConnectivityManager = context.connectivityManager

    /**
     * Ottiene lo stato corrente della connessione.
     */
    fun getCurrentConnection(): ConnectedNetwork? {
        return try {
            WifiUtils.getConnectedNetwork(context)
        } catch (_: SecurityException) {
            null
        }
    }

    /**
     * Flow che emette lo stato della connessione ogni volta che cambia
     * (connessione Wi-Fi stabilita, persa, cambio rete, cambio segnale).
     */
    fun observeConnection(): Flow<ConnectedNetwork?> = callbackFlow {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: android.net.Network) {
                trySend(getCurrentConnection())
            }

            override fun onLost(network: android.net.Network) {
                trySend(null)
            }

            override fun onCapabilitiesChanged(
                network: android.net.Network,
                networkCapabilities: android.net.NetworkCapabilities
            ) {
                trySend(getCurrentConnection())
            }

            override fun onLinkPropertiesChanged(
                network: android.net.Network,
                linkProperties: android.net.LinkProperties
            ) {
                trySend(getCurrentConnection())
            }
        }

        connectivityManager.registerNetworkCallback(
            android.net.NetworkRequest.Builder()
                .addTransportType(android.net.NetworkCapabilities.TRANSPORT_WIFI)
                .build(),
            networkCallback
        )

        // Emetti lo stato corrente
        trySend(getCurrentConnection())

        // Ascolta anche i cambi di RSSI tramite WifiManager
        val rssiReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == WifiManager.RSSI_CHANGED_ACTION) {
                    trySend(getCurrentConnection())
                }
            }
        }
        context.registerReceiver(
            rssiReceiver,
            IntentFilter(WifiManager.RSSI_CHANGED_ACTION),
            Context.RECEIVER_EXPORTED
        )

        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
            context.unregisterReceiver(rssiReceiver)
        }
    }.flowOn(Dispatchers.IO)
}
