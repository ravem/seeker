package com.seeker.app.data.oui

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.InputStream

/**
 * Parser per il database OUI in formato JSON di Wireshark.
 *
 * Formato atteso (da https://www.wireshark.org/download/oui.json):
 * ```json
 * {
 *   "001A2B": {
 *     "vendor": "Cisco Systems, Inc",
 *     "address": "170 West Tasman Drive\nSan Jose CA 95134-1706\nUS"
 *   },
 *   ...
 * }
 * ```
 */
object OuiParser {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        allowSpecialFloatingPointValues = true
    }

    /**
     * Parsa l'input stream JSON e restituisce la lista delle entry OUI.
     */
    fun parse(input: InputStream): List<OuiEntry> {
        val jsonString = input.bufferedReader().use { it.readText() }
        return parse(jsonString)
    }

    /**
     * Parsa una stringa JSON e restituisce la lista delle entry OUI.
     */
    fun parse(jsonString: String): List<OuiEntry> {
        return try {
            val root: JsonObject = json.parseToJsonElement(jsonString).jsonObject
            root.entries.mapNotNull { (key, value) ->
                try {
                    val obj = value.jsonObject
                    val vendor = obj["vendor"]?.jsonPrimitive?.content
                        ?: obj["org"]?.jsonPrimitive?.content
                        ?: return@mapNotNull null

                    val address = obj["address"]?.jsonPrimitive?.content

                    // Pulisci il prefisso: rimuovi separatori, maiuscolo
                    val cleanKey = key
                        .replace(":", "")
                        .replace("-", "")
                        .replace(".", "")
                        .replace(" ", "")
                        .uppercase()
                        .take(6)

                    if (cleanKey.length < 6) return@mapNotNull null

                    OuiEntry(
                        macPrefix = cleanKey,
                        vendor = vendor.trim(),
                        address = address?.trim()
                    )
                } catch (_: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
