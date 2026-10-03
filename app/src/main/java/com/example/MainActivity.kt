package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.ui.theme.MyApplicationTheme
import java.util.concurrent.TimeUnit

/**
 * =========================================================================================
 * FEATURE: Main Activity Entry Point
 * =========================================================================================
 * The primary Android Activity hosting the Jetpack Compose UI graph. Responsible for edge-to-edge
 * window configuration, system UI bar behaviors, and enqueuing the background WorkManager task.
 *
 * USE CASES:
 * 1. Application cold start and initialization.
 * 2. Enqueuing periodic background worker for dataset updates every 2 hours.
 * 3. Rendering [ColnectUrlApp] within the application theme.
 * =========================================================================================
 */
class MainActivity : ComponentActivity() {

    /**
     * Called when the activity is starting. Configures edge-to-edge layout, background work, and content view.
     *
     * @param savedInstanceState Saved state bundle.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Config.logFunctionCall("MainActivity", "onCreate")
        enableEdgeToEdge()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val updateWorkRequest = PeriodicWorkRequestBuilder<UpdateWorker>(3, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "ColnectUpdateWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            updateWorkRequest
        )

        // Configure system UI controller to hide status and navigation bars for immersive full screen
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        setContent {
            MyApplicationTheme {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    ColnectUrlApp(
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
