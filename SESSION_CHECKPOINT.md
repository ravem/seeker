# Seeker — Checkpoint di Sessione

> **Ultimo aggiornamento:** 24 Luglio 2025 (fine sessione 4)
> **Stato:** Build riuscita ✅

---

## ✅ Completato (Sessione 4)

### Impostazioni 🎨
- Tema Sistema/Chiaro/Scuro salvato in DataStore
- Intervallo aggiornamento OUI configurabile (slider 1-30gg)
- Intervallo scansione LAN configurabile (slider 10-120s)
- Integrazioni API (Meraki, UniFi, Omada) — configurazione credenziali
- Info e Licenze (MIT, componenti open source con licenze)
- Navigazione: ingranaggio in ogni schermata → Impostazioni → About / Integrazioni

### Bug Fix 🔧
- **Xerox**: MAC zero `000000` filtrato nel lookup OUI
- **Crash ConcurrentHashMap**: non si cacheggiano valori null
- **"local" hostname**: mDNS filtra correttamente nomi servizio
- **Android 14 permission**: aggiunto `NEARBY_WIFI_DEVICES` per BSSID reali
- **BSSID redatto**: `02:00:00:00:00:00` filtrato in lookup

### SNMP Scanner 📡
- Nuovo `SnmpScanner` con SNMP4J (v2c, community "public")
- Query sysDescr, sysName, sysObjectID, sysLocation, sysContact, sysUpTime
- Vendor da enterprise OID (es. .1.3.6.1.4.1.23693 → Meraki) + pattern sysDescr
- SNMP solo su scansione esplicita (non rallenta la scansione iniziale)
- Timeout 1.5s, 0 retry, barra progresso visibile

### Port Scan 🔌
- TCP 1-1024 (well-known ports) + UDP 11 porte
- Barra di progresso con conteggio live (porta corrente / totale)
- SNMP scan dopo il port scan

### Pulizia Codice 🧹
- Rimosso OUI da LAN discovery (non funzionante su Android 14+)
- Rimosso `DeviceType` dalla scansione locale
- Emoji rimosso dal dettaglio dispositivo

---

## ✅ Completato (Sessioni Precedenti)

- Progetto: Gradle, Hilt, Compose, Material 3
- Modelli: ConnectedNetwork, AccessPoint, LanDevice, PortInfo, SnmpInfo
- Utility: WifiUtils, NetworkUtils, OuiDatabase, LatencyMonitor
- Data: WifiScanner, WifiRepository, NetworkRepository, PingScanner, PortScanner, MdnsScanner, SnmpScanner
- UI: Rete Attuale, Scanner Wi-Fi, Dispositivi (con dettaglio e port scan)
- OUI: Database IEEE completo (39.804 entry), aggiornamento periodico

---

## 🔜 Da Fare

### Priorità alta
- [ ] **API polling**: implementare chiamate effettive a Meraki/UniFi/Omada per stato dispositivi

### Backlog
- [ ] NetBIOS — testare su rete con dispositivi Windows
- [ ] Tests (unitari e UI)
- [ ] Notifiche con stato scansione
- [ ] Export report rete (JSON/CSV)
- [ ] Salvataggio cronologia dispositivi
- [ ] Scansione porte personalizzabile (range configurabile dall'utente)

---

## 📐 Decisioni Architetturali

| Decisione | Scelta |
|-----------|--------|
| Min / Target SDK | 29 / 35 |
| Architettura | MVVM + Repository Pattern |
| DI | Hilt + KSP |
| UI | Jetpack Compose + Material 3 |
| Tema | DataStore + Dynamic Colors + System/Light/Dark |
| Grafici storici | Canvas Compose personalizzato |
| MAC discovery | `ip neigh` command (best-effort su Android 14+) |
| OUI | IEEE CSV ufficiale (~40K entry) + cache JSON + aggiornamento periodico |
| Port scanning | TCP Connect (1-1024) + UDP probe (11 porte) |
| SNMP | SNMP4J, v2c, community "public", 1.5s timeout |
| API Controller | Meraki / UniFi / Omada (configurabile da UI) |
| Lingua | Solo italiano |

---

## 📝 Per Riprendere

```bash
# Apri progetto in Android Studio
open /Users/paolostefani/Documents/AndroidStudioProjects/Seeker/

# Compila e installa
cd /Users/paolostefani/Documents/AndroidStudioProjects/Seeker/
export ANDROID_HOME=$HOME/Library/Android/sdk
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Files chiave aggiunti di recente
- `data/settings/UserPreferences.kt` — DataStore preferences
- `data/network/SnmpScanner.kt` — SNMP scanner
- `data/network/NetbiosScanner.kt` — NetBIOS name resolver
- `util/ConnectionMonitor.kt` — Rilevamento Wi-Fi/Ethernet
- `ui/settings/SettingsScreen.kt` + `SettingsViewModel.kt`
- `ui/about/AboutScreen.kt` — Licenze e componenti
- `ui/integrations/IntegrationsScreen.kt` + `IntegrationsViewModel.kt`
- `di/SettingsModule.kt`
