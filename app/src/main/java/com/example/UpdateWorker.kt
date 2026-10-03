package com.example

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * =========================================================================================
 * FEATURE: Background Synchronization Worker (WorkManager)
 * =========================================================================================
 * A [CoroutineWorker] that executes every 2 hours in the background when network connectivity
 * is available. It fetches `version.txt` with a cache-busting timestamp, compares local vs
 * remote dataset version counts, validates checksum/item counts, and downloads updated CSVs.
 *
 * USE CASES:
 * 1. Automatic background updating of Countries, Currencies, Materials, and Face Values.
 * 2. Preventing stale dataset usage while safeguarding against incomplete downloads.
 * =========================================================================================
 */
class UpdateWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    /**
     * Performs background synchronization by fetching remote version metadata and updating local CSV files.
     *
     * @return [Result.success] on completion, or [Result.retry] if an HTTP or network error occurs.
     */
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Config.logFunctionCall("UpdateWorker", "doWork", "runAttempt=${runAttemptCount}")
        try {
            val client = OkHttpClient()
            val timestamp = System.currentTimeMillis()
            val txtRequest = Request.Builder()
                .url("${Config.DATA_REPO_BASE_URL}/version.txt?t=$timestamp")
                .build()

            var versionTxt = ""
            client.newCall(txtRequest).execute().use { response ->
                if (response.isSuccessful) {
                    versionTxt = response.body?.string() ?: ""
                }
            }

            if (versionTxt.isEmpty()) {
                Config.logFunctionCall("UpdateWorker", "doWork", "versionTxt is empty -> retry")
                return@withContext Result.retry()
            }

            val lines = versionTxt.lines().map { it.trim() }.filter { it.isNotEmpty() }
            if (lines.isEmpty()) {
                return@withContext Result.retry()
            }

            val versionLine = lines.find { it.lowercase().contains("version") } ?: lines[0]
            val remoteGlobalVersion = cleanVersionString(versionLine)

            val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("global_version", remoteGlobalVersion).apply()

            var remotePaisLine = ""
            var remoteDenominacionLine = ""
            var remoteMaterialLine = ""
            var remoteValorFacialLine = ""

            for (line in lines) {
                if (line == versionLine) continue
                val lower = line.lowercase()
                if (lower.contains("pais") || lower.contains("country") || lower.contains("paises")) {
                    remotePaisLine = line
                } else if (lower.contains("denomin") || lower.contains("currency") || lower.contains("currencies")) {
                    remoteDenominacionLine = line
                } else if (lower.contains("material") || lower.contains("composition") || lower.contains("composicion")) {
                    remoteMaterialLine = line
                } else if (lower.contains("valor") || lower.contains("facial") || lower.contains("face")) {
                    remoteValorFacialLine = line
                }
            }

            val localPaisVersion = prefs.getString("local_version_pais", "") ?: ""
            val localDenomVersion = prefs.getString("local_version_denominacion", "") ?: ""
            val localMaterialVersion = prefs.getString("local_version_material", "") ?: ""
            val localValomVersion = prefs.getString("local_version_valor_facial", "") ?: ""

            // Paises
            val expectedPaisCount = extractExpectedCount(remotePaisLine)
            if (remotePaisLine.isNotEmpty() && remotePaisLine != localPaisVersion) {
                Config.logFunctionCall("UpdateWorker", "doWork", "Downloading updated paises.csv ($remotePaisLine != local $localPaisVersion)")
                if (downloadAndSaveFile(client, "${Config.DATA_REPO_BASE_URL}/paises.csv", "countries.csv", timestamp, expectedPaisCount)) {
                    prefs.edit().putString("local_version_pais", remotePaisLine).apply()
                }
            } else {
                Config.logFunctionCall("UpdateWorker", "doWork", "Skipping paises.csv (version unchanged: $localPaisVersion)")
            }

            // Denominaciones
            val expectedDenoCount = extractExpectedCount(remoteDenominacionLine)
            if (remoteDenominacionLine.isNotEmpty() && remoteDenominacionLine != localDenomVersion) {
                Config.logFunctionCall("UpdateWorker", "doWork", "Downloading updated denominaciones.csv ($remoteDenominacionLine != local $localDenomVersion)")
                if (downloadAndSaveFile(client, "${Config.DATA_REPO_BASE_URL}/denominaciones.csv", "currencies.csv", timestamp, expectedDenoCount)) {
                    prefs.edit().putString("local_version_denominacion", remoteDenominacionLine).apply()
                }
            } else {
                Config.logFunctionCall("UpdateWorker", "doWork", "Skipping denominaciones.csv (version unchanged: $localDenomVersion)")
            }

            // Material
            val expectedMatCount = extractExpectedCount(remoteMaterialLine)
            if (remoteMaterialLine.isNotEmpty() && remoteMaterialLine != localMaterialVersion) {
                Config.logFunctionCall("UpdateWorker", "doWork", "Downloading updated materiales.csv ($remoteMaterialLine != local $localMaterialVersion)")
                if (downloadAndSaveFile(client, "${Config.DATA_REPO_BASE_URL}/materiales.csv", "compositions.csv", timestamp, expectedMatCount)) {
                    prefs.edit().putString("local_version_material", remoteMaterialLine).apply()
                }
            } else {
                Config.logFunctionCall("UpdateWorker", "doWork", "Skipping materiales.csv (version unchanged: $localMaterialVersion)")
            }

            // Valor Facial
            val expectedValCount = extractExpectedCount(remoteValorFacialLine)
            if (remoteValorFacialLine.isNotEmpty() && remoteValorFacialLine != localValomVersion) {
                Config.logFunctionCall("UpdateWorker", "doWork", "Downloading updated valores_faciales.csv ($remoteValorFacialLine != local $localValomVersion)")
                if (downloadAndSaveFile(client, "${Config.DATA_REPO_BASE_URL}/valores_faciales.csv", "face_values.csv", timestamp, expectedValCount)) {
                    prefs.edit().putString("local_version_valor_facial", remoteValorFacialLine).apply()
                }
            } else {
                Config.logFunctionCall("UpdateWorker", "doWork", "Skipping valores_faciales.csv (version unchanged: $localValomVersion)")
            }

            Config.logFunctionCall("UpdateWorker", "doWork", "Success finished update work")
            Result.success()
        } catch (e: Exception) {
            Config.logFunctionCall("UpdateWorker", "doWork", "Error: ${e.message}")
            e.printStackTrace()
            Result.retry()
        }
    }

    /**
     * Downloads a CSV dataset file, verifies line count matching expected count, and saves to internal storage.
     *
     * @param client [OkHttpClient] instance.
     * @param url Remote target CSV URL.
     * @param filename Target local file name in private storage.
     * @param timestamp Cache-busting timestamp.
     * @param expectedCount Expected item count for integrity validation, or null if unconstrained.
     * @return True if downloaded, validated, and saved successfully; false otherwise.
     */
    private fun downloadAndSaveFile(
        client: OkHttpClient,
        url: String,
        filename: String,
        timestamp: Long,
        expectedCount: Int?
    ): Boolean {
        Config.logFunctionCall("UpdateWorker", "downloadAndSaveFile", "url=$url, filename=$filename, expectedCount=$expectedCount")
        var success = false
        val req = Request.Builder().url("$url?t=$timestamp").build()
        client.newCall(req).execute().use { resp ->
            if (resp.isSuccessful) {
                val bodyStr = resp.body?.string() ?: ""
                if (bodyStr.isNotEmpty()) {
                    val parsedList = parseCsvContentLocal(bodyStr)
                    if (expectedCount != null && parsedList.size != expectedCount) {
                        Config.logFunctionCall("UpdateWorker", "downloadAndSaveFile", "Validation failed: parsed ${parsedList.size} != expected $expectedCount")
                        return false // Validation failed
                    }
                    val cleanStr = parsedList.joinToString("\n") { "${it.name};${it.fragment}" }
                    context.openFileOutput(filename, Context.MODE_PRIVATE).use { os ->
                        os.write(cleanStr.toByteArray())
                    }
                    success = true
                }
            }
        }
        return success
    }

    /**
     * Parses raw CSV string data into a list of [ColnectItem].
     *
     * @param csvData Raw string containing CSV entries.
     * @return Cleaned, distinct list of items.
     */
    private fun parseCsvContentLocal(csvData: String): List<ColnectItem> {
        val items = mutableListOf<ColnectItem>()
        var foundSeparator = false
        for (line in csvData.lines()) {
            val cleanLine = line.trim()
            if (cleanLine.isEmpty() || cleanLine.startsWith("------")) continue
            if (cleanLine.contains("NAME") && cleanLine.contains("URL") && !foundSeparator) {
                foundSeparator = true
                continue
            }
            var fragment = ""
            var name = ""
            if (cleanLine.contains(";")) {
                val parts = cleanLine.split(";", limit = 2)
                fragment = parts[0].trim()
                name = parts.getOrNull(1)?.trim() ?: ""
            } else if (cleanLine.contains(",")) {
                val parts = cleanLine.split(",", limit = 2)
                fragment = parts[0].trim()
                name = parts.getOrNull(1)?.trim() ?: ""
            }
            name = name.replace("\"", "").replace("'", "")
            fragment = fragment.replace("\"", "").replace("'", "").substringAfterLast("/")

            if (name.isNotEmpty() && fragment.isNotEmpty()) {
                items.add(ColnectItem(name, fragment))
            }
        }

        return items.filter { it.name.isNotEmpty() && it.fragment.isNotEmpty() }
            .distinctBy { it.fragment }
    }

    /**
     * Extracts expected entry count integer from version string line (e.g. "PAISES = 525").
     *
     * @param line Version line string.
     * @return Extracted integer count or null.
     */
    private fun extractExpectedCount(line: String): Int? {
        return line.split("=").lastOrNull()?.trim()?.toIntOrNull()
    }

    /**
     * Cleans version string header prefixes.
     *
     * @param version Raw line string.
     * @return Clean version string.
     */
    private fun cleanVersionString(version: String): String {
        return version
            .replace("VERSION:", "", ignoreCase = true)
            .replace("VERSION", "", ignoreCase = true)
            .replace("versión:", "", ignoreCase = true)
            .replace("versión", "", ignoreCase = true)
            .replace("version:", "", ignoreCase = true)
            .replace("version", "", ignoreCase = true)
            .replace("=", "")
            .trim()
    }
}
