package com.seeker.app.data.network

import android.util.Log
import com.seeker.app.core.model.SnmpInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.snmp4j.CommunityTarget
import org.snmp4j.PDU
import org.snmp4j.Snmp
import org.snmp4j.event.ResponseEvent
import org.snmp4j.smi.*
import org.snmp4j.transport.DefaultUdpTransportMapping
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "SeekerSNMP"

/**
 * Scanner SNMP per dispositivi di rete.
 *
 * Tenta connessione SNMP v2c con community "public" e recupera
 * informazioni di sistema (sysDescr, sysName, sysObjectID, ecc.)
 * usando OID numerici RFC 1213 — nessun MIB file necessario.
 */
@Singleton
class SnmpScanner @Inject constructor() {

    companion object {
        private const val SNMP_PORT = 161
        private const val DEFAULT_COMMUNITY = "public"
        private const val SNMP_VERSION = org.snmp4j.mp.SnmpConstants.version2c
        private const val TIMEOUT_MS = 1500L
        private const val RETRIES = 0

        /** OID di sistema RFC 1213 */
        private val SYSTEM_OIDS = linkedMapOf(
            "1.3.6.1.2.1.1.1.0" to "sysDescr",
            "1.3.6.1.2.1.1.2.0" to "sysObjectID",
            "1.3.6.1.2.1.1.3.0" to "sysUpTime",
            "1.3.6.1.2.1.1.4.0" to "sysContact",
            "1.3.6.1.2.1.1.5.0" to "sysName",
            "1.3.6.1.2.1.1.6.0" to "sysLocation"
        )
    }

    /**
     * Esegue scansione SNMP su un indirizzo IP.
     * @param ipAddress IP del dispositivo
     * @param community Community SNMP (default "public")
     * @return SnmpInfo o null se SNMP non disponibile
     */
    suspend fun scan(ipAddress: String, community: String = DEFAULT_COMMUNITY): SnmpInfo? =
        withContext(Dispatchers.IO) {
            Log.d(TAG, "scan: probing $ipAddress ...")

            try {
                // Target SNMP v2c
                val target = CommunityTarget<UdpAddress>()
                target.address = UdpAddress("$ipAddress/$SNMP_PORT")
                target.community = OctetString(community)
                target.version = SNMP_VERSION
                target.timeout = TIMEOUT_MS
                target.retries = RETRIES

                // Transport e sessione
                val transport = DefaultUdpTransportMapping()
                val snmp = Snmp(transport)
                try {
                    transport.listen()

                    // PDU con tutti gli OID richiesti
                    val pdu = PDU().apply {
                        type = PDU.GET
                        SYSTEM_OIDS.keys.forEach { oid ->
                            add(VariableBinding(OID(oid)))
                        }
                    }

                    val response: ResponseEvent<UdpAddress> = snmp.send(pdu, target)
                    parseResponse(response)
                } finally {
                    try { snmp.close() } catch (_: Exception) { }
                    try { transport.close() } catch (_: Exception) { }
                }
            } catch (e: Exception) {
                Log.d(TAG, "scan: $ipAddress -> SNMP non disponibile (${e.message})")
                null
            }
        }

    private fun parseResponse(response: ResponseEvent<UdpAddress>): SnmpInfo? {
        val pdu = response.response ?: return null
        if (pdu.type == PDU.REPORT) {
            Log.w(TAG, "parseResponse: REPORT (errore autenticazione?)")
            return null
        }

        val bindings = pdu.variableBindings
        if (bindings.isEmpty()) return null

        var sysDescr: String? = null
        var sysObjectId: String? = null
        var sysName: String? = null
        var sysLocation: String? = null
        var sysContact: String? = null
        var sysUpTime: Long? = null

        val iter = bindings.iterator()
        while (iter.hasNext()) {
            val vb = iter.next()
            val oid = vb.oid?.toString() ?: continue
            val variable = vb.variable ?: continue

            when (oid) {
                "1.3.6.1.2.1.1.1.0" -> sysDescr = variable.toString()
                "1.3.6.1.2.1.1.2.0" -> sysObjectId = variable.toString()
                "1.3.6.1.2.1.1.3.0" -> {
                    sysUpTime = if (variable is TimeTicks) variable.toLong()
                    else variable.toString().toLongOrNull()
                }
                "1.3.6.1.2.1.1.4.0" -> sysContact = variable.toString()
                "1.3.6.1.2.1.1.5.0" -> sysName = variable.toString()
                "1.3.6.1.2.1.1.6.0" -> sysLocation = variable.toString()
            }
        }

        if (sysDescr == null && sysName == null && sysObjectId == null) return null

        return SnmpInfo(
            systemDescription = sysDescr?.trim(),
            systemName = sysName?.trim(),
            systemLocation = sysLocation?.trim(),
            systemContact = sysContact?.trim(),
            systemObjectId = sysObjectId?.trim(),
            uptime = sysUpTime
        )
    }
}
