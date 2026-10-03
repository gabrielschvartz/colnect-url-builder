package com.example

import android.util.Log

/**
 * =========================================================================================
 * FEATURE: Centralized App Configuration
 * =========================================================================================
 * This object serves as the single source of truth for all configurable constants across
 * the application, including remote API endpoints, GitHub repositories, and timeout limits.
 *
 * USE CASES:
 * 1. Modifying target backend URLs or raw data repository mirrors without altering business logic.
 * 2. Adjusting default polling intervals or timeout thresholds in production environments.
 * =========================================================================================
 */
object Config {
    /**
     * Base URL for the main Colnect website.
     */
    const val COLNECT_BASE_URL = "https://colnect.com"

    /**
     * Base URL for downloading remote CSV datasets (countries, currencies, compositions, face values).
     */
    const val DATA_REPO_BASE_URL = "https://raw.githubusercontent.com/gabrielschvartz/colnect-data/main"

    /**
     * Public GitHub repository link for the Colnect URL Builder project.
     */
    const val GITHUB_REPO_URL = "https://github.com/GabrielSchvartz/colnect-url-builder"

    /**
     * Log tag used for system-wide info and diagnostic logs.
     */
    const val LOG_TAG = "ColnectApp"

    /**
     * Helper method to log function execution details across the app.
     *
     * @param className Name of the invoking class or component.
     * @param methodName Name of the function being executed.
     * @param params Key-value pairs or string description of input parameters.
     */
    fun logFunctionCall(className: String, methodName: String, params: String = "") {
        Log.i(LOG_TAG, "[$className::$methodName] Called with params: $params")
    }
}
