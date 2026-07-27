package com.seeker.app.core.model

/**
 * Classificazione del tipo di dispositivo in base al vendor MAC.
 * Ispirato da Ning (csicar/Ning).
 */
enum class DeviceType(
    val label: String,
    val emoji: String
) {
    UNKNOWN("Sconosciuto", "❓"),
    PC("Computer", "💻"),
    PHONE("Smartphone", "📱"),
    TABLET("Tablet", "📟"),
    ROUTER("Router", "📡"),
    ACCESS_POINT("Access Point", "📶"),
    SPEAKER("Speaker", "🔊"),
    SMART_TV("Smart TV", "📺"),
    GAME_CONSOLE("Console", "🎮"),
    CAST("Chromecast/AirPlay", "📽️"),
    PRINTER("Stampante", "🖨️"),
    IOT("IoT", "💡"),
    SERVER("Server", "🖥️"),
    VM("Macchina Virtuale", "🖧"),
    SOC("SoC/Embedded", "🔌"),
    HOME_APPLIANCE("Elettrodomestico", "🏠"),
    ;

    companion object {
        /**
         * Classifica un dispositivo in base al vendor OUI e ad altri indizi.
         */
        fun classify(vendor: String?, macAddress: String?, hostname: String?): DeviceType {
            // 1. Indizi dall'hostname
            if (hostname != null) {
                val h = hostname.lowercase()
                if (h.contains("iphone") || h.contains("ipad") || h.contains("android")) return PHONE
                if (h.contains("macbook") || h.contains("mac-pro") || h.contains("macmini")
                    || h.contains("imac") || h.contains("windows")) return PC
                if (h.contains("apple-tv") || h.contains("roku") || h.contains("fire-tv")) return SMART_TV
                if (h.contains("chromecast") || h.contains("googlecast")) return CAST
                if (h.contains("printer") || h.contains("brother") || h.contains("hp-")
                    || h.contains("epson") || h.contains("canon")) return PRINTER
                if (h.contains("router") || h.contains("gateway") || h.contains("ap-")
                    || h.contains("wifi-")) return ROUTER
                if (h.contains("server") || h.contains("nas") || h.contains("synology")
                    || h.contains("qnap") || h.contains("proxmox")) return SERVER
                if (h.contains("esp") || h.contains("arduino") || h.contains("raspberry")
                    || h.contains("pi-")) return SOC
                if (h.contains("nest") || h.contains("hue") || h.contains("smart")
                    || h.contains("tplink-plug")) return IOT
                if (h.contains("ps4") || h.contains("ps5") || h.contains("xbox")
                    || h.contains("nintendo") || h.contains("switch")) return GAME_CONSOLE
            }

            // 2. Indizi dall'indirizzo MAC
            if (macAddress != null) {
                val mac = macAddress.uppercase()
                // VM: KVM (52:54:00), VMware (00:0C:29, 00:50:56, 00:05:69)
                if (mac.startsWith("52:54:00") ||
                    mac.startsWith("00:0C:29") ||
                    mac.startsWith("00:50:56") ||
                    mac.startsWith("00:05:69")) return VM
            }

            // 3. Classificazione per vendor OUI
            if (vendor != null) {
                val v = vendor.lowercase()

                // Router / Networking
                if (v.contains("cisco") || v.contains("meraki") ||
                    v.contains("ubiquiti") || v.contains("mikrotik") ||
                    v.contains("aruba") || v.contains("ruckus") ||
                    v.contains("zyxel") || v.contains("enGenius")) return ROUTER

                if (v.contains("tp-link") || v.contains("d-link") ||
                    v.contains("netgear") || v.contains("asus") ||
                    v.contains("huawei") || v.contains("grandstream")) return ACCESS_POINT

                // PC / Laptop
                if (v.contains("dell") || v.contains("hewlett") || v.contains("lenovo") ||
                    v.contains("micro-star") || v.contains("acer") || v.contains("apple") ||
                    v.contains("msi") || v.contains("razer") || v.contains("lg electronics") ||
                    v.contains("samsung electronics") || v.contains("toshiba")) return PC

                // Phone
                if (v.contains("xiaomi communications") || v.contains("huawei technologies") ||
                    v.contains("fairphone") || v.contains("motorola mobility") ||
                    v.contains("htc corporation") || v.contains("oneplus") ||
                    v.contains("google") || v.contains("oppo")) return PHONE

                // Speaker
                if (v.contains("sonos") || v.contains("bose") ||
                    v.contains("harman") || v.contains("jbl")) return SPEAKER

                // SoC/Embedded
                if (v.contains("espressif") || v.contains("raspberry") ||
                    v.contains("broadcom") || v.contains("qualcomm") ||
                    v.contains("mediatek") || v.contains("realtek")) return SOC

                // Printer
                if (v.contains("brother") || v.contains("seiko epson") ||
                    v.contains("canon") || v.contains("hp ") ||
                    v.contains("xerox") || v.contains("kyocera")) return PRINTER

                // Game Console
                if (v.contains("nintendo") || v.contains("sony interactive") ||
                    v.contains("microsoft")) return GAME_CONSOLE

                // Cast / Streaming
                if (v.contains("azurewave")) return CAST

                // Smart TV
                if (v.contains("samsung electronics") || v.contains("lg electronics") ||
                    v.contains("sony") || v.contains("philips") ||
                    v.contains("tcl") || v.contains("hisense")) return SMART_TV

                // IoT
                if (v.contains("philips hue") || v.contains("belkin") ||
                    v.contains("wemo") || v.contains("tuya") ||
                    v.contains("eero") || v.contains("plume")) return IOT

                // Home Appliance
                if (v.contains("xiaomi home") || v.contains("samsung") ||
                    v.contains("lg electronics")) return HOME_APPLIANCE
            }

            return UNKNOWN
        }
    }
}
