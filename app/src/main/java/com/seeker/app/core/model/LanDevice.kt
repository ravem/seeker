package com.seeker.app.core.model

/**
 * Rappresenta un dispositivo trovato sulla rete locale.
 */
data class LanDevice(
    val ipAddress: String,
    val macAddress: String = "",
    val vendor: String? = null,          // Compilato via SNMP (sysDescr/sysObjectID)
    val hostname: String? = null,
    val dnsName: String? = null,
    val ports: List<PortInfo> = emptyList(),
    val isReachable: Boolean = true,
    val lastSeen: Long = System.currentTimeMillis(),
    val snmpInfo: SnmpInfo? = null       // Informazioni SNMP
) {
    /** Il nome visualizzato: hostname o DNS o IP. */
    val displayName: String
        get() = hostname ?: dnsName ?: snmpInfo?.systemName ?: ipAddress
}

/**
 * Risultati di una scansione SNMP su un dispositivo.
 */
data class SnmpInfo(
    val systemDescription: String? = null,  // sysDescr.0
    val systemName: String? = null,         // sysName.0
    val systemLocation: String? = null,     // sysLocation.0
    val systemContact: String? = null,      // sysContact.0
    val systemObjectId: String? = null,     // sysObjectID.0 — usato per identificare vendor
    val uptime: Long? = null                // sysUpTime.0 (in centesimi di secondo)
) {
    /** Tenta di estrarre il vendor da sysDescr o sysObjectID. */
    val detectedVendor: String?
        get() {
            val desc = systemDescription?.lowercase() ?: ""
            val oid = systemObjectId ?: ""

            // Enterprise OID → vendor mapping (sottalberi .1.3.6.1.4.1.*)
            if (oid.startsWith(".1.3.6.1.4.1")) {
                val enterpriseNum = oid.removePrefix(".1.3.6.1.4.1.").split(".").firstOrNull()
                val vendor = KNOWN_ENTERPRISE_OIDS[enterpriseNum]
                if (vendor != null) return vendor
            }

            // Pattern matching su sysDescr
            for ((pattern, vendor) in VENDOR_PATTERNS) {
                if (desc.contains(pattern)) return vendor
            }

            return null
        }

    companion object {
        // Enterprise OID → Vendor
        private val KNOWN_ENTERPRISE_OIDS = mapOf(
            "9" to "Cisco",
            "2636" to "Juniper",
            "2011" to "Huawei",
            "2352" to "HP/Aruba",
            "11" to "Hewlett-Packard",
            "43" to "3Com",
            "45" to "IBM",
            "311" to "Microsoft",
            "3951" to "D-Link",
            "890" to "Netgear",
            "10002" to "TP-Link",
            "18747" to "Ubiquiti",
            "20485" to "Zyxel",
            "12356" to "MikroTik",
            "1337" to "Grandstream",
            "21208" to "EnGenius",
            "11863" to "Ruckus",
            "17821" to "Aruba Networks",
            "8744" to "SonicWall",
            "388" to "Fortinet",
            "12394" to "Sophos",
            "210" to "VMware",
            "42" to "Xerox",
            "1916" to "Apple",
            "2366" to "Dell",
            "2" to "IBM",
            "674" to "Synology",
            "6574" to "QNAP",
            "1680" to "AVM (Fritz!)",
            "23693" to "Meraki"
        )

        // Vendors riconoscibili da sysDescr
        private val VENDOR_PATTERNS = listOf(
            "cisco" to "Cisco",
            "meraki" to "Cisco Meraki",
            "juniper" to "Juniper",
            "huawei" to "Huawei",
            "tp-link" to "TP-Link",
            "tplink" to "TP-Link",
            "netgear" to "Netgear",
            "d-link" to "D-Link",
            "dlink" to "D-Link",
            "ubiquiti" to "Ubiquiti",
            "mikrotik" to "MikroTik",
            "zyxel" to "Zyxel",
            "asus" to "ASUS",
            "aruba" to "Aruba",
            "ruckus" to "Ruckus",
            "engenius" to "EnGenius",
            "grandstream" to "Grandstream",
            "fortinet" to "Fortinet",
            "sonicwall" to "SonicWall",
            "sophos" to "Sophos",
            "synology" to "Synology",
            "qnap" to "QNAP",
            "fritz" to "AVM (Fritz!)",
            "avm" to "AVM (Fritz!)",
            "dell" to "Dell",
            "hp " to "HP",
            "hewlett" to "HP",
            "lenovo" to "Lenovo",
            "apple" to "Apple",
            "vmware" to "VMware",
            "proxmox" to "Proxmox",
            "esxi" to "VMware",
            "xerox" to "Xerox",
            "canon" to "Canon",
            "brother" to "Brother",
            "epson" to "Epson",
            "raspberry" to "Raspberry Pi",
            "linux" to "Linux"
        )
    }
}

/**
 * Risultato della scansione di una singola porta.
 */
data class PortInfo(
    val port: Int,
    val service: String,
    val isOpen: Boolean,
    val transport: String = "TCP",
    val responseTimeMs: Long? = null
) {
    companion object {
        /**
         * Mappa delle porte comuni e del servizio associato.
         */
        val COMMON_PORTS = mapOf(
            21 to "FTP",
            22 to "SSH",
            23 to "Telnet",
            25 to "SMTP",
            53 to "DNS",
            80 to "HTTP",
            110 to "POP3",
            143 to "IMAP",
            443 to "HTTPS",
            445 to "SMB",
            465 to "SMTPS",
            587 to "SMTP Submission",
            993 to "IMAPS",
            995 to "POP3S",
            1433 to "MSSQL",
            1521 to "Oracle DB",
            1701 to "L2TP",
            1723 to "PPTP",
            1883 to "MQTT",
            3306 to "MySQL",
            3389 to "RDP",
            5432 to "PostgreSQL",
            5900 to "VNC",
            5901 to "VNC (1)",
            6379 to "Redis",
            8080 to "HTTP Proxy",
            8443 to "HTTPS Alt",
            9000 to "SonarQube",
            9090 to "Prometheus",
            27017 to "MongoDB",
            32400 to "Plex Media Server"
        )

        fun serviceName(port: Int): String = COMMON_PORTS[port] ?: "Sconosciuto"
    }
}
