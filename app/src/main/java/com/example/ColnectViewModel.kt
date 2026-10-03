package com.example
/**
 * Feature: Main ViewModel (State Management)
 * Description: Handles the business logic, URL construction, state persistence, and web view state.
 * Use Cases: Provides state variables to UI components, triggers background worker checks, manages scraped data.
 */


import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.text.Normalizer

class ColnectViewModel : ViewModel() {

    fun cleanCompositionName(name: String): String {
        var clean = name
        val parenIndex = clean.indexOf("(")
        if (parenIndex != -1) {
            clean = clean.substring(0, parenIndex)
        }
        val index = clean.indexOf(" -")
        if (index != -1) {
            clean = clean.substring(0, index)
        }
        val index2 = clean.indexOf("- ")
        if (index2 != -1) {
            clean = clean.substring(0, index2)
        }
        val index3 = clean.indexOf("-")
        if (index3 != -1 && index3 > 0 && index3 < clean.length - 1) {
            val charAfter = clean[index3 + 1]
            if (!charAfter.isLetterOrDigit() && charAfter != ' ' && charAfter != '(') {
                clean = clean.substring(0, index3)
            }
        }
        return clean.trim()
    }

    fun cleanCompositionsList(list: List<ColnectItem>): List<ColnectItem> {
        return list.map { it.copy(name = cleanCompositionName(it.name)) }
            .filter { item ->
                item.name.isNotEmpty() && item.fragment.isNotEmpty() &&
                item.name.length < 50 &&
                !item.name.lowercase().contains("composición") &&
                !item.name.lowercase().contains("detalles")
            }
            .distinctBy { it.name.lowercase().trim() }
            .distinctBy { it.fragment.lowercase().trim() }
            .sortedBy { it.name.lowercase() }
    }

    fun cleanCountriesList(list: List<ColnectItem>): List<ColnectItem> {
        return list.filter { it.name.isNotEmpty() && it.fragment.isNotEmpty() }
            .distinctBy { it.name.lowercase().trim() }
            .distinctBy { it.fragment.lowercase().trim() }
            .sortedBy { it.name.lowercase() }
    }

    fun cleanFaceValuesList(list: List<ColnectItem>): List<ColnectItem> {
        return list.filter { it.name.isNotEmpty() && it.fragment.isNotEmpty() }
            .distinctBy { it.name.lowercase().trim() }
            .distinctBy { it.fragment.lowercase().trim() }
            .sortedBy { it.name.lowercase() }
    }

    private val _appState = MutableStateFlow<AppState>(AppState.Loading)
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    private val _loadingStatus = MutableStateFlow("Iniciando la aplicación...")
    val loadingStatus: StateFlow<String> = _loadingStatus.asStateFlow()

    private val _countries = MutableStateFlow<List<ColnectItem>>(emptyList())
    val countries: StateFlow<List<ColnectItem>> = _countries.asStateFlow()

    private val _faceValues = MutableStateFlow<List<ColnectItem>>(emptyList())
    val faceValues: StateFlow<List<ColnectItem>> = _faceValues.asStateFlow()

    private val _compositions = MutableStateFlow<List<ColnectItem>>(emptyList())
    val compositions: StateFlow<List<ColnectItem>> = _compositions.asStateFlow()

    private val _currencies = MutableStateFlow<List<ColnectItem>>(emptyList())
    val currencies: StateFlow<List<ColnectItem>> = _currencies.asStateFlow()

    private val _updateInfo = MutableStateFlow(UpdateInfo())
    val updateInfo: StateFlow<UpdateInfo> = _updateInfo.asStateFlow()

    private val _displayVersion = MutableStateFlow("1.0.0")
    val displayVersion: StateFlow<String> = _displayVersion.asStateFlow()

    private val _colnectCookies = MutableStateFlow("")
    val colnectCookies: StateFlow<String> = _colnectCookies.asStateFlow()

    private val _lastCookieSyncTime = MutableStateFlow(0L)
    val lastCookieSyncTime: StateFlow<Long> = _lastCookieSyncTime.asStateFlow()

    private val _backgroundSessionStatus = MutableStateFlow("Desconectado (Inicia sesión en WebView)")
    val backgroundSessionStatus: StateFlow<String> = _backgroundSessionStatus.asStateFlow()

    private val _scrapedUserName = MutableStateFlow<String?>(null)
    val scrapedUserName: StateFlow<String?> = _scrapedUserName.asStateFlow()

    private val _isInternetConnected = MutableStateFlow(true)
    val isInternetConnected: StateFlow<Boolean> = _isInternetConnected.asStateFlow()

    // Dynamic Theme Mode state (Light vs Dark OLED)
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // URL History list state
    private val _urlHistory = MutableStateFlow<List<UrlHistoryItem>>(emptyList())
    val urlHistory: StateFlow<List<UrlHistoryItem>> = _urlHistory.asStateFlow()

    // History dialog visibility state
    private val _showHistoryDialog = MutableStateFlow(false)
    val showHistoryDialog: StateFlow<Boolean> = _showHistoryDialog.asStateFlow()

    /**
     * Toggles between light theme and dark OLED theme, saving preference to SharedPreferences.
     *
     * @param context Application context.
     */
    fun toggleTheme(context: Context) {
        Config.logFunctionCall("ColnectViewModel", "toggleTheme", "current=${_isDarkMode.value}")
        val newMode = !_isDarkMode.value
        _isDarkMode.value = newMode
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_dark_mode", newMode).apply()
    }

    /**
     * Loads the saved theme mode preference from SharedPreferences.
     *
     * @param context Application context.
     */
    fun loadThemePreference(context: Context) {
        Config.logFunctionCall("ColnectViewModel", "loadThemePreference")
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        _isDarkMode.value = prefs.getBoolean("is_dark_mode", false)
    }

    /**
     * Displays the URL History modal dialog.
     */
    fun openHistoryDialog() {
        Config.logFunctionCall("ColnectViewModel", "openHistoryDialog")
        _showHistoryDialog.value = true
    }

    /**
     * Dismisses the URL History modal dialog.
     */
    fun closeHistoryDialog() {
        Config.logFunctionCall("ColnectViewModel", "closeHistoryDialog")
        _showHistoryDialog.value = false
    }

    /**
     * Appends a newly generated Colnect URL to history and persists the list to local storage.
     *
     * @param context Application context.
     * @param url Constructed Colnect URL string.
     * @param summary Brief description of active filters.
     */
    fun addUrlToHistory(context: Context, url: String, summary: String) {
        Config.logFunctionCall("ColnectViewModel", "addUrlToHistory", "url=$url, summary=$summary")
        if (url.isEmpty() || url == "https://colnect.com") return

        val currentList = _urlHistory.value.toMutableList()
        // Prevent duplicate consecutive entries
        if (currentList.firstOrNull()?.url == url) return

        val item = UrlHistoryItem(
            url = url,
            summary = if (summary.isNotBlank()) summary else "Búsqueda Colnect"
        )
        currentList.add(0, item)
        val trimmed = if (currentList.size > 50) currentList.take(50) else currentList
        _urlHistory.value = trimmed
        saveHistoryToPrefs(context, trimmed)
    }

    /**
     * Deletes a single history entry by ID.
     *
     * @param context Application context.
     * @param id Unique item identifier string.
     */
    fun deleteHistoryItem(context: Context, id: String) {
        Config.logFunctionCall("ColnectViewModel", "deleteHistoryItem", "id=$id")
        val updated = _urlHistory.value.filter { it.id != id }
        _urlHistory.value = updated
        saveHistoryToPrefs(context, updated)
    }

    /**
     * Clears all saved URL history items.
     *
     * @param context Application context.
     */
    fun clearHistory(context: Context) {
        Config.logFunctionCall("ColnectViewModel", "clearHistory")
        _urlHistory.value = emptyList()
        saveHistoryToPrefs(context, emptyList())
    }

    /**
     * Loads a historical URL entry into the active WebView and closes the history dialog.
     *
     * @param context Application context.
     * @param item Selected [UrlHistoryItem].
     */
    fun loadUrlFromHistory(context: Context, item: UrlHistoryItem) {
        Config.logFunctionCall("ColnectViewModel", "loadUrlFromHistory", "url=${item.url}")
        _showHistoryDialog.value = false
        openInWebView(item.url)
    }

    private fun saveHistoryToPrefs(context: Context, history: List<UrlHistoryItem>) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val serialized = history.joinToString(";;;") { "${it.id}|||${it.url}|||${it.summary}|||${it.timestamp}" }
        prefs.edit().putString("url_history_data", serialized).apply()
    }

    private fun loadHistory(context: Context) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val serialized = prefs.getString("url_history_data", "") ?: ""
        if (serialized.isNotEmpty()) {
            try {
                val items = serialized.split(";;;").mapNotNull { entry ->
                    val parts = entry.split("|||")
                    if (parts.size >= 4) {
                        UrlHistoryItem(
                            id = parts[0],
                            url = parts[1],
                            summary = parts[2],
                            timestamp = parts[3].toLongOrNull() ?: System.currentTimeMillis()
                        )
                    } else null
                }
                _urlHistory.value = items
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val _showDiagnosticCard = MutableStateFlow(false)
    val showDiagnosticCard: StateFlow<Boolean> = _showDiagnosticCard.asStateFlow()

    fun triggerDiagnosticCard() {
        _showDiagnosticCard.value = true
    }

    fun hideDiagnosticCard() {
        _showDiagnosticCard.value = false
    }

    private val _activeMatchAlert = MutableStateFlow<String?>(null)
    val activeMatchAlert: StateFlow<String?> = _activeMatchAlert.asStateFlow()

    private val alertedFields = mutableSetOf<String>()

    fun dismissMatchAlert() {
        _activeMatchAlert.value = null
    }

    fun checkCoincidences() {
        viewModelScope.launch {
            val targets = mapOf(
                "Países" to Pair(_countries.value.size, 247),
                "Valores Faciales" to Pair(_faceValues.value.size, 1250),
                "Composiciones" to Pair(_compositions.value.size, 132),
                "Monedas Locales / Divisas" to Pair(_currencies.value.size, 2065)
            )
            for ((fieldName, pair) in targets) {
                val (current, target) = pair
                if (current == target && !alertedFields.contains(fieldName)) {
                    alertedFields.add(fieldName)
                    _activeMatchAlert.value = "¡Coincidencia! El campo \"$fieldName\" ha coincidido exactamente con lo publicado en Colnect, con un total obtenido de $current registros."
                }
            }
        }
    }

    private val _showNoInternetDialog = MutableStateFlow(false)
    val showNoInternetDialog: StateFlow<Boolean> = _showNoInternetDialog.asStateFlow()

    private val _showUpdatePromptDialog = MutableStateFlow(false)
    val showUpdatePromptDialog: StateFlow<Boolean> = _showUpdatePromptDialog.asStateFlow()

    private val _activeWebViewUrl = MutableStateFlow<String?>(null)
    val activeWebViewUrl: StateFlow<String?> = _activeWebViewUrl.asStateFlow()

    private val _isUserLoggedInInWebView = MutableStateFlow<Boolean?>(null)
    val isUserLoggedInInWebView: StateFlow<Boolean?> = _isUserLoggedInInWebView.asStateFlow()

    private val _pendingConformedUrl = MutableStateFlow<String?>(null)
    val pendingConformedUrl: StateFlow<String?> = _pendingConformedUrl.asStateFlow()

    private fun isValidColnectUrl(url: String): Boolean {
        if (url == "https://colnect.com" || url == "https://colnect.com/es/coins" || url.startsWith("https://colnect.com/es/account/")) return true
        val regex = Regex("^https://colnect\\.com/[a-z]{2}/[a-z]+/(list|series|years|catalog)(/[a-z_]+/[^/]+)*$")
        return regex.matches(url)
    }

    fun openInWebView(url: String) {
        if (!isValidColnectUrl(url)) return
        if (_isUserLoggedInInWebView.value == true) {
            _activeWebViewUrl.value = url
            _pendingConformedUrl.value = null
        } else {
            _pendingConformedUrl.value = url
            _activeWebViewUrl.value = "https://colnect.com/es/account/login"
        }
    }

    fun clearPendingAndLoad(url: String) {
        _pendingConformedUrl.value = null
        _activeWebViewUrl.value = url
    }

    fun closeWebView() {
        _activeWebViewUrl.value = null
        _isUserLoggedInInWebView.value = null
        _pendingConformedUrl.value = null
    }

    fun setUserLoggedInStatus(isLoggedIn: Boolean) {
        _isUserLoggedInInWebView.value = isLoggedIn
    }

    private val _scrapedCountries = MutableStateFlow<List<ColnectItem>>(emptyList())
    val scrapedCountries: StateFlow<List<ColnectItem>> = _scrapedCountries.asStateFlow()

    private val _scrapedFaceValues = MutableStateFlow<List<ColnectItem>>(emptyList())
    val scrapedFaceValues: StateFlow<List<ColnectItem>> = _scrapedFaceValues.asStateFlow()

    private val _scrapedCompositions = MutableStateFlow<List<ColnectItem>>(emptyList())
    val scrapedCompositions: StateFlow<List<ColnectItem>> = _scrapedCompositions.asStateFlow()

    private val _scrapedCurrencies = MutableStateFlow<List<ColnectItem>>(emptyList())
    val scrapedCurrencies: StateFlow<List<ColnectItem>> = _scrapedCurrencies.asStateFlow()

    fun updateScrapedCountries(itemList: List<ColnectItem>) {
        _scrapedCountries.value = itemList
    }

    fun updateScrapedFaceValues(itemList: List<ColnectItem>) {
        _scrapedFaceValues.value = itemList
    }

    fun updateScrapedCompositions(itemList: List<ColnectItem>) {
        _scrapedCompositions.value = cleanCompositionsList(itemList)
    }

    fun updateScrapedCurrencies(itemList: List<ColnectItem>) {
        _scrapedCurrencies.value = cleanCurrenciesList(itemList)
    }

    fun clearScrapedData() {
        _scrapedCountries.value = emptyList()
        _scrapedFaceValues.value = emptyList()
        _scrapedCompositions.value = emptyList()
        _scrapedCurrencies.value = emptyList()
    }

    fun syncCookies(context: Context, cookies: String) {
        if (_colnectCookies.value == cookies) return
        _colnectCookies.value = cookies
        _lastCookieSyncTime.value = System.currentTimeMillis()
        
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("colnect_cookies", cookies)
            .putLong("colnect_cookies_time", _lastCookieSyncTime.value)
            .apply()
        
        verifyBackgroundSession(context)
    }

    fun verifyBackgroundSession(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val cookies = _colnectCookies.value
            if (cookies.isEmpty()) {
                withContext(Dispatchers.Main) {
                    _backgroundSessionStatus.value = "Desconectado (Sin cookies)"
                }
                return@launch
            }
            withContext(Dispatchers.Main) {
                _backgroundSessionStatus.value = "Verificando..."
            }
            val client = OkHttpClient.Builder().build()
            val request = Request.Builder()
                .url("https://colnect.com/es/coins")
                .header("Cookie", cookies)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36")
                .build()
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val html = response.body?.string() ?: ""
                        val isLoggedIn = html.contains("/account/logout") || html.contains("logout") || html.contains("Cerrar sesión") || html.contains("Cerrar Sesión")
                        withContext(Dispatchers.Main) {
                            if (isLoggedIn) {
                                val pattern = "/es/member/([a-zA-Z0-9_-]+)".toRegex()
                                val match = pattern.find(html)
                                val username = match?.groupValues?.get(1) ?: "Conectado"
                                _backgroundSessionStatus.value = "Sesión Activa: $username"
                                _scrapedUserName.value = username
                                val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                                prefs.edit().putString("colnect_username", username).apply()
                            } else {
                                _backgroundSessionStatus.value = "Sesión Expirada o Inválida"
                                _scrapedUserName.value = null
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            _backgroundSessionStatus.value = "Error HTTP ${response.code}"
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _backgroundSessionStatus.value = "Error de Red"
                }
            }
        }
    }

    fun saveScrapedCountriesToStorage(context: Context, list: List<ColnectItem>) {
        viewModelScope.launch {
            try {
                if (list.isNotEmpty()) {
                    val currentList = _countries.value
                    // Merge new list with current database list based on unique fragment
                    val merged = (currentList + list).distinctBy { it.fragment.lowercase() }
                    val csvStr = merged.joinToString("\n") { "${it.name};${it.fragment}" }
                    withContext(Dispatchers.IO) {
                        context.openFileOutput("countries.csv", Context.MODE_PRIVATE).use { os ->
                            os.write(csvStr.toByteArray())
                        }
                    }
                    _countries.value = merged
                    _scrapedCountries.value = emptyList()
                    checkCoincidences()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun saveScrapedFaceValuesToStorage(context: Context, list: List<ColnectItem>) {
        viewModelScope.launch {
            try {
                if (list.isNotEmpty()) {
                    val currentList = _faceValues.value
                    // Merge new list with current database list based on unique fragment
                    val merged = (currentList + list).distinctBy { it.fragment.lowercase() }
                    val csvStr = merged.joinToString("\n") { "${it.name};${it.fragment}" }
                    withContext(Dispatchers.IO) {
                        context.openFileOutput("face_values.csv", Context.MODE_PRIVATE).use { os ->
                            os.write(csvStr.toByteArray())
                        }
                    }
                    _faceValues.value = merged
                    _scrapedFaceValues.value = emptyList()
                    checkCoincidences()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun saveScrapedCompositionsToStorage(context: Context, list: List<ColnectItem>) {
        viewModelScope.launch {
            try {
                val cleanNewList = cleanCompositionsList(list)
                if (cleanNewList.isNotEmpty()) {
                    val currentList = _compositions.value
                    // Merge new list with current database list based on unique fragment
                    val merged = cleanCompositionsList((currentList + cleanNewList).distinctBy { it.fragment.lowercase() })
                    val csvStr = merged.joinToString("\n") { "${it.name};${it.fragment}" }
                    withContext(Dispatchers.IO) {
                        context.openFileOutput("compositions.csv", Context.MODE_PRIVATE).use { os ->
                            os.write(csvStr.toByteArray())
                        }
                    }
                    _compositions.value = merged
                    _scrapedCompositions.value = emptyList()
                    checkCoincidences()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun saveScrapedCurrenciesToStorage(context: Context, list: List<ColnectItem>) {
        viewModelScope.launch {
            try {
                val cleanNewList = cleanCurrenciesList(list)
                if (cleanNewList.isNotEmpty()) {
                    val currentList = _currencies.value
                    val merged = cleanCurrenciesList((currentList + cleanNewList).distinctBy { it.fragment.lowercase() })
                    val csvStr = merged.joinToString("\n") { "${it.name};${it.fragment}" }
                    withContext(Dispatchers.IO) {
                        context.openFileOutput("currencies.csv", Context.MODE_PRIVATE).use { os ->
                            os.write(csvStr.toByteArray())
                        }
                    }
                    _currencies.value = merged
                    _scrapedCurrencies.value = emptyList()
                    checkCoincidences()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun cleanCurrencyName(name: String): String {
        val cleanParen = name.replace(Regex("<[^>]*>"), "")
            .replace("\n", " ")
            .trim()
            .replace(Regex("""\s*\([^)]*\)$"""), "")
            .trim()
        val index = cleanParen.lastIndexOf('-')
        return if (index != -1) {
            cleanParen.substring(0, index).trim()
        } else {
            cleanParen.trim()
        }
    }

    fun cleanCurrenciesList(list: List<ColnectItem>): List<ColnectItem> {
        return list.map { it.copy(name = cleanCurrencyName(it.name)) }
            .filter { it.name.isNotEmpty() && it.fragment.isNotEmpty() }
            .distinctBy { it.name.lowercase().trim() }
            .distinctBy { it.fragment.lowercase().trim() }
            .sortedBy { it.name.lowercase() }
    }

    private fun getFallbackBaselineCurrencies(): List<ColnectItem> {
        return emptyList()
    }


    fun dismissUpdatePrompt() {
        _showUpdatePromptDialog.value = false
    }

    private fun getFallbackBaselineCompositions(): List<ColnectItem> {
        return emptyList()
    }

    // Form inputs
    val countryInput = MutableStateFlow("")
    val faceValueInput = MutableStateFlow("")
    val yearInput = MutableStateFlow("")
    val diameterInput = MutableStateFlow("")
    val materialInput = MutableStateFlow("")
    val currencyInput = MutableStateFlow("")
    var queryCount = 0

    val localAppVersion = "1.0"
    val localCsvVersion = "1.0"

    fun cleanVersionString(version: String): String {
        var clean = version
            .replace("VERSION:", "", ignoreCase = true)
            .replace("VERSION", "", ignoreCase = true)
            .replace("versión:", "", ignoreCase = true)
            .replace("versión", "", ignoreCase = true)
            .replace("version:", "", ignoreCase = true)
            .replace("version", "", ignoreCase = true)
            .replace("=", "")
            .trim()
        
        // Remove leading 'v' or 'v.' if present to standardize
        if (clean.lowercase().startsWith("v.") && clean.length > 2) {
            clean = clean.substring(2).trim()
        } else if (clean.lowercase().startsWith("v") && clean.length > 1) {
            clean = clean.substring(1).trim()
        }
        
        // Limit to 3 segments (e.g. 1.0.0) format only
        val parts = clean.split(".")
        if (parts.size > 3) {
            clean = parts.take(3).joinToString(".")
        } else if (parts.size == 2) {
            clean = "$clean.0"
        } else if (parts.size == 1 && clean.isNotEmpty()) {
            clean = "$clean.0.0"
        }
        return clean
    }

    fun initData(context: Context) {
        _displayVersion.value = cleanVersionString(getCachedGlobalVersion(context))
        loadThemePreference(context)
        loadHistory(context)

        // Trigger diagnostic summary card 10 seconds after app starts
        viewModelScope.launch {
            delay(10000L)
            _showDiagnosticCard.value = false
        }

        // Start a dynamic real-time network state monitoring loop
        viewModelScope.launch {
            while (true) {
                val currentConn = isInternetAvailable(context)
                if (_isInternetConnected.value != currentConn) {
                    _isInternetConnected.value = currentConn
                }
                delay(1500) // Poll every 1.5s for snappy feedback
            }
        }

        viewModelScope.launch {
            val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

            val savedCookies = prefs.getString("colnect_cookies", "") ?: ""
            _colnectCookies.value = savedCookies
            _lastCookieSyncTime.value = prefs.getLong("colnect_cookies_time", 0L)
            _scrapedUserName.value = prefs.getString("colnect_username", null)
            if (savedCookies.isNotEmpty()) {
                verifyBackgroundSession(context)
            }

            val startTime = System.currentTimeMillis()

            // Step 1: Parse locally pre-packaged asset CSV files / internal storage in parallel (our foolproof baseline fallback)
            _loadingStatus.value = "Cargando..."
            try {
                coroutineScope {
                    val countriesJob = async(Dispatchers.IO) {
                        try {
                            cleanCountriesList(loadCsvData(context, "countries.csv"))
                        } catch (e: Exception) {
                            emptyList()
                        }
                    }
                    val faceValuesJob = async(Dispatchers.IO) {
                        try {
                            cleanFaceValuesList(loadCsvData(context, "face_values.csv"))
                        } catch (e: Exception) {
                            emptyList()
                        }
                    }
                    val compositionsJob = async(Dispatchers.IO) {
                        try {
                            val cached = try { loadCsvData(context, "compositions.csv") } catch (e: Exception) { emptyList() }
                            cleanCompositionsList(if (cached.isNotEmpty()) cached else getFallbackBaselineCompositions())
                        } catch (e: Exception) {
                            cleanCompositionsList(getFallbackBaselineCompositions())
                        }
                    }
                    val currenciesJob = async(Dispatchers.IO) {
                        try {
                            val cached = try { loadCsvData(context, "currencies.csv") } catch (e: Exception) { emptyList() }
                            cleanCurrenciesList(if (cached.isNotEmpty()) cached else getFallbackBaselineCurrencies())
                        } catch (e: Exception) {
                            cleanCurrenciesList(getFallbackBaselineCurrencies())
                        }
                    }

                    _countries.value = countriesJob.await()
                    _faceValues.value = faceValuesJob.await()
                    _compositions.value = compositionsJob.await()
                    _currencies.value = currenciesJob.await()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            delay(100)

            // Show loading screen for exactly 2.5 seconds as requested
            val targetTime = 2500L
            val elapsedTime = System.currentTimeMillis() - startTime
            if (elapsedTime < targetTime) {
                delay(targetTime - elapsedTime)
            }

            prefs.edit().putBoolean("first_launch_done", true).apply()
            _loadingStatus.value = "¡Bienvenido!"
            _appState.value = AppState.Ready
            checkCoincidences()

            // Active connection verification and update check immediately, completely silently in the background
            viewModelScope.launch {
                delay(1500) // Delay checks slightly to avoid UI transition glitches
                val conn = withContext(Dispatchers.IO) { isInternetAvailable(context) }
                _isInternetConnected.value = conn
                if (conn) {
                    try {
                        performUpdateCheckAndDownloads(context, isStartup = false)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private suspend fun loadCsvData(context: Context, fileName: String): List<ColnectItem> {
        return withContext(Dispatchers.IO) {
            val fileNames = if (fileName.contains("composition", ignoreCase = true) || fileName.contains("composicion", ignoreCase = true)) {
                listOf("compositions.csv", "composiciones.csv")
            } else {
                listOf(fileName)
            }
            for (fn in fileNames) {
                val file = File(context.filesDir, fn)
                if (file.exists() && file.length() > 0) {
                    try {
                        val csvContent = file.readText()
                        val parsed = parseCsvContent(csvContent)
                        if (parsed.isNotEmpty()) return@withContext parsed
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                
                // Fallback to assets if filesDir does not contain a valid file
                try {
                    context.assets.open(fn).use { inputStream ->
                        val csvContent = inputStream.bufferedReader().use { it.readText() }
                        val parsed = parseCsvContent(csvContent)
                        if (parsed.isNotEmpty()) return@withContext parsed
                    }
                } catch (e: Exception) {
                    // Ignore asset missing exceptions
                }
            }
            emptyList()
        }
    }

    private fun cleanQuotes(s: String): String {
        var clean = s.trim()
        val quoteChars = setOf('"', '\'', '“', '”', '‘', '’', '„', '‚', '«', '»', '\\')
        while (clean.isNotEmpty() && clean[0] in quoteChars) {
            clean = clean.substring(1).trim()
        }
        while (clean.isNotEmpty() && clean[clean.length - 1] in quoteChars) {
            clean = clean.substring(0, clean.length - 1).trim()
        }
        if (clean.startsWith("\\\"")) {
            clean = clean.substring(2).trim()
        }
        if (clean.endsWith("\\\"")) {
            clean = clean.substring(0, clean.length - 2).trim()
        }
        return clean
    }

    private fun parseCsvContent(csvContent: String): List<ColnectItem> {
        return csvContent.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { line ->
                var parts = line.split(";")
                if (parts.size < 2) {
                    parts = line.split(",")
                }
                if (parts.size < 2) {
                    parts = line.split("|")
                }
                if (parts.size < 2) {
                    parts = line.split("\t")
                }
                if (parts.size >= 2) {
                    val name = cleanQuotes(parts[0])
                    val fragment = cleanQuotes(parts[1])
                    if (name.isNotEmpty() && fragment.isNotEmpty()) {
                        ColnectItem(name, fragment)
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
            .toList()
    }

    private fun parseVersionFromLine(line: String): String {
        val regex = "\\d+\\.\\d+(?:\\.\\d+)*".toRegex()
        val match = regex.find(line)
        return match?.value ?: ""
    }

    private fun parseItemCountFromLine(line: String): Int {
        val parenthesized = "\\(([^)]+)\\)".toRegex().find(line)?.groupValues?.get(1)
        if (parenthesized != null) {
            val digits = parenthesized.filter { it.isDigit() }
            if (digits.isNotEmpty()) return digits.toInt()
        }
        val matchResult = "items\\s*:\\s*(\\d+)".toRegex(RegexOption.IGNORE_CASE).find(line)
        if (matchResult != null) {
            return matchResult.groupValues[1].toInt()
        }
        val tokens = line.split("[\\s,;:]+".toRegex()).filter { it.isNotEmpty() }
        for (token in tokens.reversed()) {
            val parsed = token.toIntOrNull()
            if (parsed != null) return parsed
        }
        return -1
    }

    private fun getCachedGlobalVersion(context: Context): String {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getString("global_version", "1.0.0") ?: "1.0.0"
    }

    private fun saveCachedGlobalVersion(context: Context, version: String) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("global_version", version).apply()
    }

    private suspend fun performUpdateCheckAndDownloads(context: Context, isStartup: Boolean) {
        val client = OkHttpClient()
        val timestamp = System.currentTimeMillis()
        val txtRequest = Request.Builder()
            .url("${Config.DATA_REPO_BASE_URL}/version.txt?t=$timestamp")
            .build()
        
        var versionTxt = ""
        try {
            client.newCall(txtRequest).execute().use { response ->
                if (response.isSuccessful) {
                    versionTxt = response.body?.string() ?: ""
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (versionTxt.isEmpty()) {
            return
        }

        val lines = versionTxt.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) return

        // Look for a line containing "VERSION" (case-insensitive) as requested by user
        val versionLine = lines.find { it.lowercase().contains("version") } ?: lines[0]
        val remoteGlobalVersion = cleanVersionString(versionLine)
        saveCachedGlobalVersion(context, remoteGlobalVersion)
        withContext(Dispatchers.Main) {
            _displayVersion.value = remoteGlobalVersion
        }

        // Identify the remote lines representing file versions
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

        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        
        val localPaisVersion = prefs.getString("local_version_pais", "") ?: ""
        val localDenomVersion = prefs.getString("local_version_denominacion", "") ?: ""
        val localMaterialVersion = prefs.getString("local_version_material", "") ?: ""
        val localValomVersion = prefs.getString("local_version_valor_facial", "") ?: ""

        val hasPaisFile = File(context.filesDir, "countries.csv").exists() && File(context.filesDir, "countries.csv").length() > 0
        val hasDenomFile = File(context.filesDir, "currencies.csv").exists() && File(context.filesDir, "currencies.csv").length() > 0
        val hasMaterialFile = File(context.filesDir, "compositions.csv").exists() && File(context.filesDir, "compositions.csv").length() > 0
        val hasValomFile = File(context.filesDir, "face_values.csv").exists() && File(context.filesDir, "face_values.csv").length() > 0

        // Countries (Pais)
        val remotePaisVer = parseVersionFromLine(remotePaisLine)
        val remotePaisCount = parseItemCountFromLine(remotePaisLine)
        val localPaisVer = parseVersionFromLine(localPaisVersion)
        val localPaisCount = _countries.value.size
        val needsPaisUpdate = !hasPaisFile || 
                              remotePaisVer != localPaisVer || 
                              (remotePaisCount > 0 && remotePaisCount != localPaisCount)

        if (remotePaisLine.isNotEmpty() && needsPaisUpdate) {
            if (isStartup) {
                withContext(Dispatchers.Main) { _loadingStatus.value = "Descargando países..." }
            }
            try {
                val req = Request.Builder().url("${Config.DATA_REPO_BASE_URL}/paises.csv?t=$timestamp").build()
                client.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val bodyStr = resp.body?.string() ?: ""
                        if (bodyStr.isNotEmpty()) {
                            val parsedList = cleanCountriesList(parseCsvContent(bodyStr))
                            val cleanStr = parsedList.joinToString("\n") { "${it.name};${it.fragment}" }
                            context.openFileOutput("countries.csv", Context.MODE_PRIVATE).use { os ->
                                os.write(cleanStr.toByteArray())
                            }
                            withContext(Dispatchers.Main) {
                                _countries.value = parsedList
                            }
                            prefs.edit().putString("local_version_pais", remotePaisLine).apply()
                        }
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }

        // Denominaciones (Denominación)
        val remoteDenomVer = parseVersionFromLine(remoteDenominacionLine)
        val remoteDenomCount = parseItemCountFromLine(remoteDenominacionLine)
        val localDenomVer = parseVersionFromLine(localDenomVersion)
        val localDenomCount = _currencies.value.size
        val needsDenomUpdate = !hasDenomFile || 
                               remoteDenomVer != localDenomVer || 
                               (remoteDenomCount > 0 && remoteDenomCount != localDenomCount)

        if (remoteDenominacionLine.isNotEmpty() && needsDenomUpdate) {
            if (isStartup) {
                withContext(Dispatchers.Main) { _loadingStatus.value = "Descargando denominaciones..." }
            }
            try {
                val req = Request.Builder().url("${Config.DATA_REPO_BASE_URL}/denominaciones.csv?t=$timestamp").build()
                client.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val bodyStr = resp.body?.string() ?: ""
                        if (bodyStr.isNotEmpty()) {
                            val parsedList = cleanCurrenciesList(parseCsvContent(bodyStr))
                            val cleanStr = parsedList.joinToString("\n") { "${it.name};${it.fragment}" }
                            context.openFileOutput("currencies.csv", Context.MODE_PRIVATE).use { os ->
                                os.write(cleanStr.toByteArray())
                            }
                            withContext(Dispatchers.Main) {
                                _currencies.value = parsedList
                            }
                            prefs.edit().putString("local_version_denominacion", remoteDenominacionLine).apply()
                        }
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }

        // Material
        val remoteMaterialVer = parseVersionFromLine(remoteMaterialLine)
        val remoteMaterialCount = parseItemCountFromLine(remoteMaterialLine)
        val localMaterialVer = parseVersionFromLine(localMaterialVersion)
        val localMaterialCount = _compositions.value.size
        val needsMaterialUpdate = !hasMaterialFile || 
                                  remoteMaterialVer != localMaterialVer || 
                                  (remoteMaterialCount > 0 && remoteMaterialCount != localMaterialCount)

        if (remoteMaterialLine.isNotEmpty() && needsMaterialUpdate) {
            if (isStartup) {
                withContext(Dispatchers.Main) { _loadingStatus.value = "Descargando material..." }
            }
            try {
                val req = Request.Builder().url("${Config.DATA_REPO_BASE_URL}/material.csv?t=$timestamp").build()
                client.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val bodyStr = resp.body?.string() ?: ""
                        if (bodyStr.isNotEmpty()) {
                            val parsedList = cleanCompositionsList(parseCsvContent(bodyStr))
                            val cleanStr = parsedList.joinToString("\n") { "${it.name};${it.fragment}" }
                            context.openFileOutput("compositions.csv", Context.MODE_PRIVATE).use { os ->
                                os.write(cleanStr.toByteArray())
                            }
                            withContext(Dispatchers.Main) {
                                _compositions.value = parsedList
                            }
                            prefs.edit().putString("local_version_material", remoteMaterialLine).apply()
                        }
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }

        // Valores Faciales
        val remoteValomVer = parseVersionFromLine(remoteValorFacialLine)
        val remoteValomCount = parseItemCountFromLine(remoteValorFacialLine)
        val localValomVer = parseVersionFromLine(localValomVersion)
        val localValomCount = _faceValues.value.size
        val needsValomUpdate = !hasValomFile || 
                               remoteValomVer != localValomVer || 
                               (remoteValomCount > 0 && remoteValomCount != localValomCount)

        if (remoteValorFacialLine.isNotEmpty() && needsValomUpdate) {
            if (isStartup) {
                withContext(Dispatchers.Main) { _loadingStatus.value = "Descargando valores faciales..." }
            }
            try {
                val req = Request.Builder().url("${Config.DATA_REPO_BASE_URL}/valor_facial.csv?t=$timestamp").build()
                client.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val bodyStr = resp.body?.string() ?: ""
                        if (bodyStr.isNotEmpty()) {
                            val parsedList = cleanFaceValuesList(parseCsvContent(bodyStr))
                            val cleanStr = parsedList.joinToString("\n") { "${it.name};${it.fragment}" }
                            context.openFileOutput("face_values.csv", Context.MODE_PRIVATE).use { os ->
                                os.write(cleanStr.toByteArray())
                            }
                            withContext(Dispatchers.Main) {
                                _faceValues.value = parsedList
                            }
                            prefs.edit().putString("local_version_valor_facial", remoteValorFacialLine).apply()
                        }
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun checkNetwork(context: Context): Boolean {
        val connected = isInternetAvailable(context)
        _isInternetConnected.value = connected
        if (!connected) {
            _showNoInternetDialog.value = true
        }
        return connected
    }

    fun dismissNoInternetDialog() {
        _showNoInternetDialog.value = false
    }

    fun clearForm() {
        countryInput.value = ""
        faceValueInput.value = ""
        yearInput.value = ""
        diameterInput.value = ""
        materialInput.value = ""
        currencyInput.value = ""
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }
}

// --------------------------------------------------------------------
// String Normalization helpers for Spanish accent-insensitive search
// --------------------------------------------------------------------
fun String.normalizeForSearch(): String {
    val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
    val cleared = normalized.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
    return cleared.lowercase().trim()
}

// --------------------------------------------------------------------
// Main Activity
// --------------------------------------------------------------------
