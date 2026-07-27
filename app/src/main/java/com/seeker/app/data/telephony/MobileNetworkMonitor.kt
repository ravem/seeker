package com.seeker.app.data.telephony

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.telephony.*
import android.util.Log
import java.net.Inet4Address
import com.seeker.app.core.model.MobileNetworkInfo
import com.seeker.app.core.model.MobileNetworksState
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "SeekerMobile"

@Singleton
class MobileNetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun getMobileNetworks(): MobileNetworksState {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        if (tm == null) return MobileNetworksState()

        val sim = getNetworkInfo(tm)

        return MobileNetworksState(sim1 = sim)
    }

    private fun getNetworkInfo(tm: TelephonyManager): MobileNetworkInfo? {
        try {
            val carrierName = try { tm.simOperatorName?.takeIf { it.isNotBlank() } } catch (e: Exception) { Log.w(TAG,"carrierName",e); null }
            var networkType: String? = null
            try {
                networkType = getNetworkTypeString(tm.dataNetworkType)
            } catch (e: SecurityException) {
                try {
                    @Suppress("DEPRECATION")
                    networkType = getNetworkTypeString(tm.networkType)
                } catch (e2: Exception) {
                    Log.w(TAG,"networkType fallback failed", e2)
                }
            }
            if (networkType == null) {
                networkType = getNetworkTypeFromCellInfo(tm)
            }
            val signalDbm = try { getSignalDbm(tm) } catch (e: Exception) { Log.w(TAG,"signalDbm",e); null }
            val cellId = try { getCellId(tm) } catch (e: Exception) { Log.w(TAG,"cellId",e); null }
            val dataConnected = try { tm.dataState == TelephonyManager.DATA_CONNECTED } catch (e: Exception) { false }
            val mobileIp = getMobileIp()

            if (carrierName == null && networkType == null && signalDbm == null) {
                Log.d(TAG, "Nessun dato mobile disponibile")
                return null
            }

            return MobileNetworkInfo(
                simSlot = 0,
                carrierName = carrierName,
                networkType = networkType,
                signalDbm = signalDbm,
                signalLevel = null,
                cellId = cellId,
                ipAddress = mobileIp,
                isConnected = dataConnected
            )
        } catch (e: Exception) {
            Log.e(TAG, "getNetworkInfo error", e)
            return null
        }
    }

    /** Ottiene l'indirizzo IP della rete mobile (se disponibile). */
    private fun getMobileIp(): String? {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return null
            if (Build.VERSION.SDK_INT < 23) return null
            val networks = cm.allNetworks ?: return null
            for (network in networks) {
                val caps = cm.getNetworkCapabilities(network) ?: continue
                if (!caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR)) continue
                val lp = cm.getLinkProperties(network) ?: continue
                for (la in lp.linkAddresses) {
                    if (la.address is Inet4Address) {
                        val ip = la.address.hostAddress
                        if (ip != null && !ip.startsWith("0.") && !ip.startsWith("127.")) {
                            return ip
                        }
                    }
                }
            }
            null
        } catch (_: Exception) { null }
    }

    private fun getNetworkTypeString(networkType: Int): String = when (networkType) {
        TelephonyManager.NETWORK_TYPE_NR -> "5G NR"
        TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
        TelephonyManager.NETWORK_TYPE_HSPAP -> "HSPA+"
        TelephonyManager.NETWORK_TYPE_HSPA -> "HSPA"
        TelephonyManager.NETWORK_TYPE_UMTS -> "UMTS"
        TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE"
        TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS"
        else -> "Sconosciuto"
    }

    private fun getSignalDbm(tm: TelephonyManager): Int? {
        return try {
            val ss = tm.signalStrength ?: return null
            if (Build.VERSION.SDK_INT >= 23) {
                ss.getCellSignalStrengths().firstOrNull()?.dbm
            } else {
                @Suppress("DEPRECATION")
                ss.gsmSignalStrength?.let { -113 + 2 * it }
            }
        } catch (_: Exception) { null }
    }

    @Suppress("NewApi")
    private fun getCellId(tm: TelephonyManager): String? {
        return try {
            val cellInfos = tm.allCellInfo ?: return null
            val info = cellInfos.firstOrNull() ?: return null
            when (info) {
                is CellInfoLte -> {
                    val cid = info.cellIdentity
                    val mcc = if (cid.mcc in 1..999) cid.mcc.toString() else "?"
                    val mnc = if (cid.mnc in 0..999) cid.mnc.toString() else "?"
                    "LTE: $mcc-$mnc-${cid.tac}-${cid.ci}"
                }
                is CellInfoNr -> {
                    if (Build.VERSION.SDK_INT >= 29) {
                        val cellIdentity = info.cellIdentity
                        if (cellIdentity is android.telephony.CellIdentityNr) {
                            val mcc = cellIdentity.mccString?.takeIf { it.isNotBlank() && it != "0" } ?: "?"
                            val mnc = cellIdentity.mncString?.takeIf { it.isNotBlank() && it != "0" } ?: "?"
                            // tac e nci possono essere 0 o MAX_VALUE se non disponibili
                            val tac = when (cellIdentity.tac) {
                                0, Int.MAX_VALUE -> "?"
                                else -> cellIdentity.tac.toString()
                            }
                            val nci = when (cellIdentity.nci) {
                                0L, Long.MAX_VALUE -> "?"
                                else -> cellIdentity.nci.toString()
                            }
                            "5G: $mcc-$mnc-$tac-$nci"
                        } else null
                    } else null
                }
                is CellInfoWcdma -> {
                    val cid = info.cellIdentity
                    val mcc = if (cid.mcc in 1..999) cid.mcc.toString() else "?"
                    val mnc = if (cid.mnc in 0..999) cid.mnc.toString() else "?"
                    "WCDMA: $mcc-$mnc-${cid.lac}-${cid.cid}"
                }
                is CellInfoGsm -> {
                    val cid = info.cellIdentity
                    val mcc = if (cid.mcc in 1..999) cid.mcc.toString() else "?"
                    val mnc = if (cid.mnc in 0..999) cid.mnc.toString() else "?"
                    "GSM: $mcc-$mnc-${cid.lac}-${cid.cid}"
                }
                else -> null
            }
        } catch (_: Exception) { null }
    }

    private fun getNetworkTypeFromCellInfo(tm: TelephonyManager): String? {
        return try {
            val cellInfos = tm.allCellInfo ?: return null
            val info = cellInfos.firstOrNull() ?: return null
            when (info) {
                is CellInfoLte -> "LTE"
                is CellInfoNr -> "5G NR"
                is CellInfoWcdma -> "HSPA+"
                is CellInfoGsm -> "GSM"
                else -> null
            }
        } catch (_: Exception) { null }
    }
}
