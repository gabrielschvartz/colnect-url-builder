# Design.md

## General Overview
This application is a specialized client for **Colnect.com**, providing an optimized mobile experience and a dynamic URL builder for catalog browsing. It includes offline data synchronization for rapid search criteria generation and an integrated web view with custom Javascript injections for improving Colnect's usability on mobile devices.

## Features

### 1. Dynamic URL Builder (URL Generator)
* **Description:** Provides a user interface for selecting Country, Composition, Currency, Face Value, Mint Year, and Diameter. It constructs precise Colnect URL paths (e.g., `https://colnect.com/es/coins/list/country/...`).
* **Use Cases:** Users who want to quickly filter the coin catalog without navigating through Colnect's standard web UI menus.

### 2. Colnect Mobile Web View (Integrated Browser)
* **Description:** An embedded `WebView` tailored for Colnect. It intercepts page loads, injects custom JavaScript to hide redundant UI elements (headers, footers, ads), and seamlessly directs the user to the correct catalog list.
* **Use Cases:** Browsing the generated URL or managing the user's Colnect collection directly within the app, with an interface modified for mobile screens.

### 3. Background Data Updater (Sync Worker)
* **Description:** A WorkManager-based background service (`UpdateWorker`) that checks for updates on a remote GitHub repository (`colnect-data`). It compares versions and downloads new CSV data for Countries, Currencies, Compositions, and Face Values.
* **Use Cases:** Ensures the app's dropdown menus always have the latest reference data from Colnect without requiring an app update.

### 4. Local Data Persistence
* **Description:** Caches the downloaded CSV datasets locally using standard internal file storage and `SharedPreferences` for version tracking.
* **Use Cases:** Allows the URL Builder to function instantly upon app launch, reading from the local cache without network delay.

## Configurable Items
* Configurable data (like the GitHub base URL for updates) can be found in a centralized configuration block or object, depending on the module.
