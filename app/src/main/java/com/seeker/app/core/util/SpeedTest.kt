package com.seeker.app.core.util

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Risultato di uno speed test.
 */
data class SpeedTestResult(
    val downloadMbps: Double? = null,
    val uploadMbps: Double? = null,
    val latencyMs: Long? = null,
    val server: String = "",
    val error: String? = null
)

/**
 * Esegue speed test di download/upload scaricando file da CDN noti.
 *
 * Strategia:
 * - Download: scarica un file di dimensioni note e misura il throughput
 * - Upload: POST di dati casuali e misura throughput
 *
 * I server usati sono CDN pubblici affidabili (Cloudflare, OVH, Google).
 */
@Singleton
class SpeedTest @Inject constructor() {

    private val TAG = "SeekerSpeedTest"

    // File di test su CDN affidabili (fallback se uno non funziona)
    private val DOWNLOAD_URLS = listOf(
        "https://speed.cloudflare.com/__down?bytes=%d",
        "https://proof.ovh.net/files/%d.dat",
        "https://ftp.lysator.liu.se/pub/opensuse/tumbleweed/iso/openSUSE-Tumbleweed-DVD-x86_64-Current.iso"
    )

    // Dimensione file di test (10 MB = 10_000_000 bytes)
    private val TEST_FILE_SIZE = 10_000_000L
    // Dimensione più piccola per upload (1 MB)
    private val UPLOAD_SIZE = 1_000_000L

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    /**
     * Esegue speed test completo (download + upload).
     */
    suspend fun runFullTest(): SpeedTestResult = withContext(Dispatchers.IO) {
        val downloadResult = testDownload()
        if (downloadResult.error != null) {
            return@withContext SpeedTestResult(error = downloadResult.error)
        }

        val uploadResult = testUpload()

        SpeedTestResult(
            downloadMbps = downloadResult.downloadMbps,
            uploadMbps = uploadResult.uploadMbps,
            latencyMs = downloadResult.latencyMs,
            server = downloadResult.server
        )
    }

    /**
     * Solo test di download.
     */
    suspend fun testDownload(): SpeedTestResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        var lastSuccess: SpeedTestResult? = null

        for (urlTemplate in DOWNLOAD_URLS) {
            val url = if (urlTemplate.contains("%d")) {
                urlTemplate.format(TEST_FILE_SIZE)
            } else {
                urlTemplate
            }

            try {
                Log.d(TAG, "Download test: $url")

                val request = Request.Builder()
                    .url(url)
                    .header("Accept", "*/*")
                    .header("User-Agent", "Seeker/1.0")
                    .build()

                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    Log.w(TAG, "Server $url risponde ${response.code}, provo altro...")
                    continue
                }

                val body = response.body ?: continue
                val contentLength = body.contentLength()
                Log.d(TAG, "Content-Length: $contentLength")

                // Legge tutto il body misurando il tempo
                val downloadStart = System.nanoTime()
                val bytesRead = body.byteStream().use { stream ->
                    val buffer = ByteArray(8192)
                    var total = 0L
                    var read: Int
                    while (stream.read(buffer).also { read = it } != -1) {
                        total += read
                    }
                    total
                }
                val elapsedNs = System.nanoTime() - downloadStart

                if (bytesRead < 100_000) {
                    Log.w(TAG, "File troppo piccolo: $bytesRead bytes, provo altro...")
                    continue
                }

                // Calcola velocità in Mbps
                val elapsedSec = elapsedNs / 1_000_000_000.0
                val bits = bytesRead * 8L
                val mbps = if (elapsedSec > 0) bits / 1_000_000.0 / elapsedSec else 0.0
                val latency = System.currentTimeMillis() - startTime

                Log.d(TAG, "Download: ${bytesRead / 1_000_000}MB in ${"%.1f".format(elapsedSec)}s = ${"%.1f".format(mbps)} Mbps")

                // Estrai nome server dall'URL
                val server = try {
                    java.net.URI(url).host ?: url
                } catch (_: Exception) { url }

                lastSuccess = SpeedTestResult(
                    downloadMbps = mbps,
                    latencyMs = latency,
                    server = server
                )
                break // successo, esci dal loop

            } catch (e: Exception) {
                Log.w(TAG, "Fallimento con $url: ${e.message}")
                // Prova prossimo server
            }
        }

        lastSuccess ?: SpeedTestResult(error = "Nessun server raggiungibile per lo speed test")
    }

    /**
     * Test di upload: genera dati casuali e li POST a un endpoint.
     */
    suspend fun testUpload(): SpeedTestResult = withContext(Dispatchers.IO) {
        try {
            // Genera dati casuali per l'upload
            val data = ByteArray(UPLOAD_SIZE.toInt())
            java.security.SecureRandom().nextBytes(data)

            val requestBody = okhttp3.RequestBody.create(
                "application/octet-stream".toMediaTypeOrNull(),
                data
            )

            val request = Request.Builder()
                .url("https://speed.cloudflare.com/__up")
                .header("User-Agent", "Seeker/1.0")
                .post(requestBody)
                .build()

            val uploadStart = System.nanoTime()
            val response = client.newCall(request).execute()
            val elapsedNs = System.nanoTime() - uploadStart

            if (!response.isSuccessful) {
                Log.w(TAG, "Upload fallito: ${response.code}")
                return@withContext SpeedTestResult(error = null) // upload non critico
            }

            val elapsedSec = elapsedNs / 1_000_000_000.0
            val bits = UPLOAD_SIZE * 8L
            val mbps = if (elapsedSec > 0) bits / 1_000_000.0 / elapsedSec else 0.0

            Log.d(TAG, "Upload: ${UPLOAD_SIZE / 1_000_000}MB in ${"%.1f".format(elapsedSec)}s = ${"%.1f".format(mbps)} Mbps")

            SpeedTestResult(uploadMbps = mbps)
        } catch (e: Exception) {
            Log.w(TAG, "Upload test fallito: ${e.message}")
            SpeedTestResult(error = null) // upload non critico, non blocca
        }
    }
}
