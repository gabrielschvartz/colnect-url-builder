package com.example

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class UpdateWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
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

            if (versionTxt.isEmpty()) return@withContext Result.retry()

            val lines = versionTxt.lines().map { it.trim() }.filter { it.isNotEmpty() }
            if (lines.isEmpty()) return@withContext Result.retry()

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
                if (downloadAndSaveFile(client, "${Config.DATA_REPO_BASE_URL}/paises.csv", "countries.csv", timestamp, expectedPaisCount)) {
                    prefs.edit().putString("local_version_pais", remotePaisLine).apply()
                }
            }

            // Denominaciones
            val expectedDenoCount = extractExpectedCount(remoteDenominacionLine)
            if (remoteDenominacionLine.isNotEmpty() && remoteDenominacionLine != localDenomVersion) {
                if (downloadAndSaveFile(client, "${Config.DATA_REPO_BASE_URL}/denominaciones.csv", "currencies.csv", timestamp, expectedDenoCount)) {
                    prefs.edit().putString("local_version_denominacion", remoteDenominacionLine).apply()
                }
            }

            // Material
            val expectedMatCount = extractExpectedCount(remoteMaterialLine)
            if (remoteMaterialLine.isNotEmpty() && remoteMaterialLine != localMaterialVersion) {
                if (downloadAndSaveFile(client, "${Config.DATA_REPO_BASE_URL}/materiales.csv", "compositions.csv", timestamp, expectedMatCount)) {
                    prefs.edit().putString("local_version_material", remoteMaterialLine).apply()
                }
            }

            // Valor Facial
            val expectedValCount = extractExpectedCount(remoteValorFacialLine)
            if (remoteValorFacialLine.isNotEmpty() && remoteValorFacialLine != localValomVersion) {
                if (downloadAndSaveFile(client, "${Config.DATA_REPO_BASE_URL}/valores_faciales.csv", "face_values.csv", timestamp, expectedValCount)) {
                    prefs.edit().putString("local_version_valor_facial", remoteValorFacialLine).apply()
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun downloadAndSaveFile(client: OkHttpClient, url: String, filename: String, timestamp: Long, expectedCount: Int?): Boolean {
        var success = false
        val req = Request.Builder().url("$url?t=$timestamp").build()
        client.newCall(req).execute().use { resp ->
            if (resp.isSuccessful) {
                val bodyStr = resp.body?.string() ?: ""
                if (bodyStr.isNotEmpty()) {
                    val parsedList = parseCsvContentLocal(bodyStr)
                    if (expectedCount != null && parsedList.size != expectedCount) {
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
            fragment = fragment.replace("\"", "").replace("'", "")
            
            fragment = fragment.substringAfterLast("/")

            if (name.isNotEmpty() && fragment.isNotEmpty()) {
                items.add(ColnectItem(name, fragment))
            }
        }
        
        return items.filter { it.name.isNotEmpty() && it.fragment.isNotEmpty() }
            .distinctBy { it.fragment }
    }

    
    private fun extractExpectedCount(line: String): Int? {
        return line.split("=").lastOrNull()?.trim()?.toIntOrNull()
    }

    private fun cleanVersionString(version: String): String {
        var clean = version
            .replace("VERSION:", "", ignoreCase = true)
            .replace("VERSION", "", ignoreCase = true)
            .replace("versión:", "", ignoreCase = true)
            .replace("versión", "", ignoreCase = true)
            .replace("version:", "", ignoreCase = true)
            .replace("version", "", ignoreCase = true)
            .replace("=", "")
            .trim()
        return clean
    }
}
