package com.example

/**
 * =========================================================================================
 * FEATURE: Core Data Models and Application State Definitions
 * =========================================================================================
 * This file encapsulates all fundamental data transfer objects, application runtime states,
 * and background update status models used throughout the Colnect URL Builder app.
 *
 * USE CASES:
 * 1. [ColnectItem]: Represents a single catalog dropdown entry (e.g. Country "Argentina", fragment "argentina").
 * 2. [AppState]: Drives top-level navigation states (Loading, Ready, NoInternetFirstLaunch).
 * 3. [UpdateInfo]: Tracks available app and CSV dataset updates fetched from GitHub.
 * 4. [UrlHistoryItem]: Encapsulates a generated Colnect URL, field summary, and creation timestamp for history management.
 * =========================================================================================
 */

/**
 * Represents a standard Colnect filter item with display name and URL fragment.
 *
 * @property name Human-readable display name (e.g., "Argentina").
 * @property fragment URL parameter fragment used in Colnect path construction (e.g., "country/13-Argentina").
 */
data class ColnectItem(
    val name: String,
    val fragment: String
)

/**
 * Encapsulates a historically generated Colnect URL for user re-use and history logging.
 *
 * @property id Unique identifier for the history item.
 * @property url Full constructed Colnect website URL.
 * @property summary Human-readable string summary of applied filters (e.g., "México | 1982 | Plata").
 * @property timestamp Epoch timestamp when the URL was generated.
 */
data class UrlHistoryItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val url: String,
    val summary: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Sealed class representing the global UI runtime state of the application.
 */
sealed class AppState {
    /** State indicating initial data loading or background initialization in progress. */
    object Loading : AppState()

    /** Normal operational state with UI fully rendered and interactive. */
    object Ready : AppState()

    /** Offline state when app is launched for the first time without cached local data. */
    data class NoInternetFirstLaunch(val secondsRemaining: Int) : AppState()
}

/**
 * Data model encapsulating version comparison results for both application APK and CSV datasets.
 *
 * @property hasUpdate Indicates if a newer dataset version or APK build is available online.
 * @property appVersionOnGithub Remote application version string parsed from GitHub repository.
 * @property csvVersionOnGithub Remote CSV dataset version string parsed from GitHub repository.
 * @property githubUrl Destination repository URL for downloading or viewing updates.
 */
data class UpdateInfo(
    val hasUpdate: Boolean = false,
    val appVersionOnGithub: String = "",
    val csvVersionOnGithub: String = "",
    val githubUrl: String = Config.GITHUB_REPO_URL
)
