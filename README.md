# Seeker

Applicazione Android per il monitoraggio e l'analisi di reti Wi-Fi e dispositivi di rete. Supporta il controllo di rete locale (LAN discovery), la scansione Wi-Fi, e l'integrazione con controller AP professionali (Meraki, UniFi, Omada).

---

## Funzionalita

### Rete Attuale
- Visualizzazione in tempo reale della connessione Wi-Fi/Ethernet
- Dettaglio: SSID, BSSID, banda, canale, potenza segnale
- Latenza verso gateway e internet
- Speed Test integrato (via Cloudflare CDN)
- Informazioni rete mobile (operatore, tipo, segnale, cella)

### Scanner Wi-Fi
- Scansione reti Wi-Fi nelle vicinanze
- Raggruppamento per SSID
- Dettaglio: canale, banda, sicurezza, vendor (da OUI)
- Ordinamento per segnale, canale o banda

### Dispositivi (LAN Discovery)
- Scansione della rete locale (ping sweep)
- Rilevamento MAC address (da tabella ARP)
- Risoluzione nomi: mDNS, reverse DNS, NetBIOS
- Scansione porte TCP (1-1024) + UDP
- Scansione SNMP (vendor, sysDescr, sysName)
- Progress bar incrementale per ogni fase

### Controller AP
- Integrazione con Meraki Dashboard API
- Integrazione con Ubiquiti UniFi Controller
- Integrazione con TP-Link Omada (locale e cloud)
- Dashboard unificata con stato dispositivi (online/offline)
- Dettaglio: MAC, seriale, firmware, IP, SSID, client connessi
- Latenza ping verso ogni dispositivo
- Polling automatico ogni 10 secondi
- Credenziali crittografate (AES-256 GCM via Android KeyStore)

### Heatmap Wi-Fi *(in sviluppo)*
- Rilevamento segnale su griglia personalizzata
- Interpolazione Inverse Distance Weighting
- Visualizzazione colorata (verde, giallo, rosso)
- Salvataggio sessioni di rilevamento

---

## Installazione

### Prerequisiti
- Android 10 (API 29) o superiore
- Permessi: Posizione (per scansione Wi-Fi), Notifiche, Telefono

### Download
Scarica l'APK dalla sezione Releases o compila da sorgente:

```bash
git clone https://github.com/ravem/seeker.git
cd seeker
export ANDROID_HOME=$HOME/Library/Android/sdk
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## Configurazione Controller

### Meraki
1. Ottieni una API key dal Meraki Dashboard -> Organization -> Settings -> Dashboard API access
2. Inserisci API Key e Organization ID in Seeker -> tab Controller -> ingranaggio
3. Fai Test per verificare la connessione
4. Seleziona l'organizzazione da monitorare

### UniFi
1. Inserisci l'URL del controller (es. `https://192.168.1.10:8443`)
2. Inserisci username e password
3. Fai Test per verificare

### Omada
- Locale: URL diretto al controller (es. `https://192.168.1.10`)
- Cloud: URL del cloud Omada (es. `https://eu.omada.tplinkcloud.com`)
- Inserisci email/username e password
- Fai Test per verificare

---

## Sicurezza

- Le credenziali dei controller (API key, password) sono salvate crittografate con AES-256 GCM
- La chiave di cifratura e protetta dall'Android KeyStore (hardware-backed dove disponibile)
- Nessun dato sensibile viene loggato o trasmesso a server terzi
- I dati di scansione e rilevamento rimangono solo sul dispositivo

---

## Componenti e Licenze

Seeker e rilasciato con licenza **MIT**. Utilizza i seguenti componenti open source:

| Componente | Licenza | Utilizzo |
|-----------|---------|----------|
| Android Jetpack | Apache 2.0 | Architettura app (Navigation, Compose, Lifecycle, DataStore) |
| Jetpack Compose | Apache 2.0 | UI dichiarativa |
| Material 3 | Apache 2.0 | Design system |
| Hilt | Apache 2.0 | Dependency injection |
| KSP | Apache 2.0 | Symbol processing (Hilt) |
| Kotlinx Serialization | Apache 2.0 | JSON parsing |
| Kotlinx Coroutines | Apache 2.0 | Async programming |
| OkHttp | Apache 2.0 | HTTP client |
| SNMP4J | Apache 2.0 | SNMP scanner |
| AndroidX Security | Apache 2.0 | Crittografia credenziali |
| JmDNS | Apache 2.0 | mDNS discovery |
| Material Icons Extended | Apache 2.0 | Icone UI |

---

## Licenza (MIT)

Copyright (c) 2025 Seeker

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

---


- Cisco Meraki per l'API Dashboard
- Ubiquiti per l'API UniFi Controller
- TP-Link per l'API Omada SDN
- Cloudflare per gli endpoint speed test
- Tutti i contributor e i progetti open source utilizzati

---

## Ringraziamenti e Crediti

### Ispirazione e codice

- **Cisco Meraki** — Documentazione API Dashboard v1
- **Ubiquiti UniFi** — Documentazione API Controller
- **TP-Link Omada** — Documentazione API SDN Controller (locale e cloud)
- **Cloudflare** — Endpoint speed test (speed.cloudflare.com/__down, __up)
- **Google Android Architecture Samples** — Pattern MVVM, Repository, Flow
- **Android Open Source Project** — Linee guida e best practice per app Android
- **IEEE** — Database OUI (Organizationally Unique Identifier) per vendor MAC
- **JetBrains** — Kotlin, Kotlinx Serialization, Coroutines
- **Square** — OkHttp (HTTP client utilizzato per tutte le API)

### Librerie open source utilizzate

| Componente | Licenza | Progetto originale |
|-----------|---------|-------------------|
| OkHttp | Apache 2.0 | https://github.com/square/okhttp |
| SNMP4J | Apache 2.0 | https://www.snmp4j.org/ |
| JmDNS | Apache 2.0 | https://github.com/jmdns/jmdns |
| Hilt | Apache 2.0 | https://dagger.dev/hilt/ |
| Kotlinx Serialization | Apache 2.0 | https://github.com/Kotlin/kotlinx.serialization |
| Kotlinx Coroutines | Apache 2.0 | https://github.com/Kotlin/kotlinx.coroutines |
| AndroidX Security | Apache 2.0 | https://developer.android.com/jetpack/androidx/releases/security |
| Jetpack Compose | Apache 2.0 | https://developer.android.com/jetpack/compose |
| Material 3 | Apache 2.0 | https://m3.material.io/ |

### Codice adattato

- **Speed test**: implementazione basata sul protocollo di speed.cloudflare.com e ispirata a progetti open source come LibreSpeed (https://librespeed.org/)
- **Heatmap interpolation**: algoritmo Inverse Distance Weighting, standard in ambito geostatistico, implementato seguendo la letteratura scientifica
- **Wi-Fi scanning patterns**: tratti dalle Android documentation samples e adattati per l'uso con Jetpack Compose
- **SNMP scanner**: basato su SNMP4J con query standard MIB-II (sysDescr, sysName, sysObjectID, sysLocation, sysContact, sysUpTime)

### Strumenti

- **Pi coding agent harness** — Ambiente di sviluppo assistito
- **Android Studio** — IDE di sviluppo
- **Gradle** — Sistema di build
