package com.example
/**
 * Feature: Root Compose Application Layer
 * Description: The main composition root that delegates to different screens based on app state.
 * Use Cases: Determines whether to show the Loading screen, No Internet screen, or Main Layout screen.
 */


import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Browser
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.io.File
import java.text.Normalizer
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.webkit.JavascriptInterface
import android.webkit.CookieManager
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun ColnectUrlApp(
    modifier: Modifier = Modifier,
    viewModel: ColnectViewModel = viewModel()
) {
    val context = LocalContext.current
    val appState by viewModel.appState.collectAsStateWithLifecycle()
    val showNoInternet by viewModel.showNoInternetDialog.collectAsStateWithLifecycle()
    val showUpdatePrompt by viewModel.showUpdatePromptDialog.collectAsStateWithLifecycle()
    val updateInfo by viewModel.updateInfo.collectAsStateWithLifecycle()

    // Trigger loading once on initial layout
    LaunchedEffect(Unit) {
        viewModel.initData(context)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4F9)) // Vibrant slate gray base background
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
                                // Ignore
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
                                            // Ignore
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

        // Startup update dialog (La app en ese caso deberá preguntar al iniciar si se desea actualizar o solo continuar)
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

        // Warnings / Cartel de atención dialog
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

// --------------------------------------------------------------------
// No Internet First Launch Screen (Banner indicando falta de conexión en primer inicio)
// --------------------------------------------------------------------
