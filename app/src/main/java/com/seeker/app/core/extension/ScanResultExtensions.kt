package com.seeker.app.core.extension

import android.net.wifi.ScanResult
import com.seeker.app.core.model.WifiBand

/**
 * Le funzioni principali [toAccessPoint] e [frequencyToChannel] sono ora
 * in [com.seeker.app.core.model.AccessPoint] come extension di [ScanResult].
 * Questo file contiene solo utility minori.
 */

/**
 * Restituisce il nome della banda per visualizzazione.
 */
fun ScanResult.bandDisplayName(): String =
    WifiBand.fromFrequencyMhz(frequency).label
