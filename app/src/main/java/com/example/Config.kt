package com.example

/**
 * Feature: Centralized Configuration
 * Description: Stores all configurable constants such as API URLs and defaults.
 * Use Cases: Making changes to data sources without hunting through code files.
 */
object Config {
    const val COLNECT_BASE_URL = "https://colnect.com"
    const val DATA_REPO_BASE_URL = "https://raw.githubusercontent.com/gabrielschvartz/colnect-data/main"
    const val GITHUB_REPO_URL = "https://github.com/GabrielSchvartz/colnect-url-builder"
}
