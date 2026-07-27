package com.seeker.app.data.oui

import kotlinx.serialization.Serializable

/**
 * Entry del database OUI (Organizationally Unique Identifier).
 * Mappa un prefisso MAC a 6 esadecimali al nome del produttore.
 */
@Serializable
data class OuiEntry(
    /** Prefisso MAC a 6 caratteri esadecimali (es. "001A2B"). */
    val macPrefix: String,

    /** Nome del produttore/vendor. */
    val vendor: String,

    /** Indirizzo del produttore (opzionale). */
    val address: String? = null
)
