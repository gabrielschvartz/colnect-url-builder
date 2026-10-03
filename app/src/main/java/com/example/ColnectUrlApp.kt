package com.example

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

/**
 * =========================================================================================
 * FEATURE: Root Compose Application Container
 * =========================================================================================
 * The main top-level Composable host. Controls screen transitions according to the current
 * [AppState] (Loading, Ready, or NoInternetFirstLaunch) and presents update/connectivity dialogs.
 *
 * USE CASES:
 * 1. Initializing data loading via [ColnectViewModel.initData] on app launch.
 * 2. Swiping between the dynamic URL Generator ([MainLayoutScreen]) and the Web View ([ColnectWebViewScreen]).
 * 3. Displaying prompt dialogs for available updates or network errors.
 * =========================================================================================
 */

/**
 * Main application composable wrapping the primary layout and dialog overlays.
 *
 * @param modifier Composable modifier for outer sizing.
 * @param viewModel Shared [ColnectViewModel] instance.
 */
@Composable
fun ColnectUrlApp(
    modifier: Modifier = Modifier,
    viewModel: ColnectViewModel = viewModel()
) {
    Config.logFunctionCall("ColnectUrlApp", "ColnectUrlApp")
    val context = LocalContext.current
    val appState by viewModel.appState.collectAsStateWithLifecycle()
    val showNoInternet by viewModel.showNoInternetDialog.collectAsStateWithLifecycle()
    val showUpdatePrompt by viewModel.showUpdatePromptDialog.collectAsStateWithLifecycle()
    val updateInfo by viewModel.updateInfo.collectAsStateWithLifecycle()

    // Trigger loading once on initial layout
    LaunchedEffect(Unit) {
        viewModel.initData(context)
    }

    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkMode) Color(0xFF000000) else Color(0xFFF3F4F9))
    ) {
        val activeWebViewUrl by viewModel.activeWebViewUrl.collectAsStateWithLifecycle()

        when (appState) {
            is AppState.Loading -> {
                LoadingScreen(viewModel = viewModel)
            }
            is AppState.Ready -> {
                if (activeWebViewUrl == null) {
                    MainLayoutScreen(viewModel = viewModel)
                } else {
                    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 2 })
                    val coroutineScope = rememberCoroutineScope()

                    LaunchedEffect(activeWebViewUrl) {
                        if (activeWebViewUrl != null) {
                            try {
                                pagerState.animateScrollToPage(1)
                            } catch (e: Exception) {
                                // Ignore animation interrupt
                            }
                        }
                    }

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = true
                    ) { page ->
                        when (page) {
                            0 -> MainLayoutScreen(
                                viewModel = viewModel,
                                onNavigateToWebView = {
                                    coroutineScope.launch {
                                        try {
                                            pagerState.animateScrollToPage(1)
                                        } catch (e: Exception) {
                                            // Ignore animation interrupt
                                        }
                                    }
                                }
                            )
                            1 -> ColnectWebViewScreen(
                                url = activeWebViewUrl!!,
                                onClose = {
                                    coroutineScope.launch {
                                        try {
                                            pagerState.animateScrollToPage(0)
                                        } catch (e: Exception) {
                                            viewModel.closeWebView()
                                        }
                                    }
                                },
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
            is AppState.NoInternetFirstLaunch -> {
                val sec = (appState as AppState.NoInternetFirstLaunch).secondsRemaining
                NoInternetFirstLaunchScreen(secondsRemaining = sec)
            }
        }

        // Startup update dialog
        if (showUpdatePrompt) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissUpdatePrompt() },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.dismissUpdatePrompt()
                            try {
                                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.githubUrl))
                                context.startActivity(webIntent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "No se pudo abrir el enlace de actualización", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                    ) {
                        Text("Actualizar", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { viewModel.dismissUpdatePrompt() }
                    ) {
                        Text("Continuar", color = Color(0xFF64748B))
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Actualización",
                            tint = Color(0xFF4F46E5),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Actualización Disponible", fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Text(
                        "Se ha detectado una nueva versión de la aplicación disponible en GitHub (v${updateInfo.appVersionOnGithub}). ¿Desea actualizar la aplicación ahora o prefiere continuar utilizando la versión instalada?",
                        fontSize = 14.sp
                    )
                },
                properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
            )
        }

        // Warning dialog for offline state
        if (showNoInternet) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissNoInternetDialog() },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.dismissNoInternetDialog()
                            viewModel.checkNetwork(context)
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF355E8D))
                    ) {
                        Text("Reintentar", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissNoInternetDialog() }) {
                        Text("Omitir", color = Color.Gray)
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Alerta",
                            tint = Color(0xFFE74C3C),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("¡Sin conexión a internet!", fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Text(
                        "No se detecta una conexión activa a internet. Por favor, compruebe su conexión. " +
                                "Puede generar el enlace de todos modos, pero recuerde que necesitará estar conectado a internet para navegar por el sitio de Colnect.",
                        fontSize = 14.sp
                    )
                },
                properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
            )
        }
    }
}
