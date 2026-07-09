package com.example
/**
 * Feature: Major UI Screens
 * Description: Contains the full-screen composables (Loading, Main Layout, Web View).
 * Use Cases: Displaying the core interfaces for user interaction.
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
fun NoInternetFirstLaunchScreen(secondsRemaining: Int) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B)) // Slate 900 to Slate 800 dark theme
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .shadow(16.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E293B).copy(alpha = 0.95f),
                contentColor = Color.White
            ),
            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Warning badge/icon
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color(0xFFEF4444).copy(alpha = 0.15f), CircleShape)
                        .border(1.dp, Color(0xFFEF4444), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Sin conexión a internet",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = "Sin conexión a internet",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    ),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Es la primera vez que abres la aplicación y se requiere una conexión activa a internet para poder configurar los datos iniciales de Colnect. No es posible iniciar la aplicación sin conexión.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF94A3B8),
                        lineHeight = 20.sp
                    ),
                    textAlign = TextAlign.Center
                )

                HorizontalDivider(color = Color(0xFF334155), thickness = 1.dp)

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "La aplicación se cerrará en:",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                    )

                    // Countdown bubble
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color(0xFF334155), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = secondsRemaining.toString(),
                            style = TextStyle(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFF59E0B)
                            )
                        )
                    }
                }
            }
        }
    }
}

// --------------------------------------------------------------------
// 1. Loading Screen (Pantalla animada de carga)
// --------------------------------------------------------------------
@Composable
fun LoadingScreen(viewModel: ColnectViewModel) {
    val statusText by viewModel.loadingStatus.collectAsStateWithLifecycle()

    val dotAnimationTick = rememberInfiniteTransition(label = "dotsAnimation")
    val dotCount by dotAnimationTick.animateValue(
        initialValue = 0,
        targetValue = 4,
        typeConverter = Int.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dotCount"
    )
    val dots = when (dotCount) {
        1 -> "."
        2 -> ".."
        3 -> "..."
        else -> ""
    }

    // Spinning coin animations representing coin collector focus
    val infiniteTransition = rememberInfiniteTransition(label = "coinSpinTransition")
    val coinSpinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "coinAngle"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F7FF)) // Ultra-clean light-blue tinted background for Colnect theme
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Rotating Blue & Celeste Collector's Coin View
        Box(
            modifier = Modifier
                .size(135.dp)
                .graphicsLayer {
                    rotationY = coinSpinAngle
                    cameraDistance = 12f * density
                }
                .shadow(16.dp, RoundedCornerShape(67.5.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF4FC3F7), Color(0xFF0F3B6C))
                    ),
                    shape = RoundedCornerShape(67.5.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Coin interior border accent (Beautiful Celeste Border)
            Box(
                modifier = Modifier
                    .fillMaxSize(0.85f)
                    .border(3.1.dp, Color(0xFFE1F5FE), RoundedCornerShape(60.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Drawing dynamic magnification glass symbol in sleek Celeste styling on coin front
                Canvas(modifier = Modifier.size(45.dp)) {
                    val strokeW = 4.dp.toPx()
                    drawCircle(
                        color = Color(0xFFE1F5FE),
                        radius = size.width * 0.32f,
                        center = center - Offset(size.width * 0.08f, size.height * 0.08f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW)
                    )
                    drawLine(
                        color = Color(0xFFE1F5FE),
                        start = center + Offset(size.width * 0.12f, size.height * 0.12f),
                        end = center + Offset(size.width * 0.38f, size.height * 0.38f),
                        strokeWidth = strokeW * 1.3f
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "Colnect URL Builder",
            fontSize = 25.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF0F3B6C), // Classy Colnect Blue
            textAlign = TextAlign.Center,
            letterSpacing = 0.5.sp
        )
    }
}

// --------------------------------------------------------------------
// 2. Main Layout Screen
// --------------------------------------------------------------------
@Composable
fun MainLayoutScreen(
    viewModel: ColnectViewModel,
    onNavigateToWebView: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    val showDiagnosticCard by viewModel.showDiagnosticCard.collectAsStateWithLifecycle()
    val activeMatchAlert by viewModel.activeMatchAlert.collectAsStateWithLifecycle()

    if (activeMatchAlert != null) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { viewModel.dismissMatchAlert() }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "¡Coincidencia!",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF14532D)
                            )
                        }
                        IconButton(
                            onClick = { viewModel.dismissMatchAlert() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color(0xFF14532D),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Text(
                        text = activeMatchAlert ?: "",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF166534)
                    )

                    Button(
                        onClick = { viewModel.dismissMatchAlert() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cerrar", color = Color.White)
                    }
                }
            }
        }
    }

    if (showDiagnosticCard) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { viewModel.hideDiagnosticCard() }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF0F3B6C),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Control de Diagnóstico",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F3B6C)
                            )
                        }
                        IconButton(
                            onClick = { viewModel.hideDiagnosticCard() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Text(
                        text = "Datos cargados vs. total publicado en Colnect (para comprobar si coinciden):",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )

                    val countriesList by viewModel.countries.collectAsStateWithLifecycle()
                    val faceValuesList by viewModel.faceValues.collectAsStateWithLifecycle()
                    val compositionsList by viewModel.compositions.collectAsStateWithLifecycle()
                    val currenciesList by viewModel.currencies.collectAsStateWithLifecycle()

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        DiagnosticItemRow("Países", countriesList.size, 247)
                        DiagnosticItemRow("Valores Faciales", faceValuesList.size, 1250)
                        DiagnosticItemRow("Composiciones", compositionsList.size, 132)
                        DiagnosticItemRow("Monedas Locales / Divisas", currenciesList.size, 2065)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sincronización de Sesión en Segundo Plano:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F3B6C)
                    )

                    val colnectCookies by viewModel.colnectCookies.collectAsStateWithLifecycle()
                    val lastSyncTime by viewModel.lastCookieSyncTime.collectAsStateWithLifecycle()
                    val sessionStatus by viewModel.backgroundSessionStatus.collectAsStateWithLifecycle()

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Estado:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
                            Text(
                                text = sessionStatus,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (sessionStatus.startsWith("Sesión Activa")) Color(0xFF16A34A) else if (sessionStatus == "Verificando...") Color(0xFFD97706) else Color(0xFFEF4444)
                            )
                        }
                        
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Última extracción:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
                            val syncTimeText = if (lastSyncTime > 0) {
                                val sdf = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                                sdf.format(java.util.Date(lastSyncTime))
                            } else "Ninguna"
                            Text(syncTimeText, fontSize = 12.sp, color = Color(0xFF475569))
                        }

                        if (colnectCookies.isNotEmpty()) {
                            Button(
                                onClick = { viewModel.verifyBackgroundSession(context) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3B6C)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(32.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Verificar sesión ahora", fontSize = 11.sp, color = Color.White)
                            }
                        } else {
                            Text(
                                "Inicia sesión en Colnect desde el WebView para extraer las cookies automáticamente.",
                                fontSize = 10.sp,
                                color = Color(0xFF64748B),
                                style = TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                            )
                        }
                    }

                    Text(
                        text = "Este cartel es temporal y se ocultará al tocar el botón.",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        style = TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    )

                    Button(
                        onClick = { viewModel.hideDiagnosticCard() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3B6C)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Entendido / Cerrar", color = Color.White)
                    }
                }
            }
        }
    }

    val countrySelected by viewModel.countryInput.collectAsStateWithLifecycle()
    val faceValueSelected by viewModel.faceValueInput.collectAsStateWithLifecycle()
    val yearSelected by viewModel.yearInput.collectAsStateWithLifecycle()
    val diameterSelected by viewModel.diameterInput.collectAsStateWithLifecycle()
    val materialSelected by viewModel.materialInput.collectAsStateWithLifecycle()
    val currencySelected by viewModel.currencyInput.collectAsStateWithLifecycle()

    val countriesList by viewModel.countries.collectAsStateWithLifecycle()
    val faceValuesList by viewModel.faceValues.collectAsStateWithLifecycle()
    val compositionsList by viewModel.compositions.collectAsStateWithLifecycle()
    val currenciesList by viewModel.currencies.collectAsStateWithLifecycle()
    val updateInfo by viewModel.updateInfo.collectAsStateWithLifecycle()
    val isConnected by viewModel.isInternetConnected.collectAsStateWithLifecycle()
    val displayVersion by viewModel.displayVersion.collectAsStateWithLifecycle()

    var isCountryFocused by remember { mutableStateOf(false) }
    var isFaceValueFocused by remember { mutableStateOf(false) }
    var isYearFocused by remember { mutableStateOf(false) }
    var isDiameterFocused by remember { mutableStateOf(false) }
    var isMaterialFocused by remember { mutableStateOf(false) }
    var isCurrencyFocused by remember { mutableStateOf(false) }

    val hasAnyInput = countrySelected.trim().isNotEmpty() ||
                      faceValueSelected.trim().isNotEmpty() ||
                      materialSelected.trim().isNotEmpty() ||
                      yearSelected.trim().isNotEmpty() ||
                      diameterSelected.trim().isNotEmpty() ||
                      currencySelected.trim().isNotEmpty()

    val isAnyFieldFocused = isCountryFocused || isFaceValueFocused || isYearFocused || isDiameterFocused || isMaterialFocused || isCurrencyFocused
    val showPreview = true

    // Dynamic randomized placeholder examples
    val randomFaceValuePlaceholder = remember(hasAnyInput) {
        if (hasAnyInput) "Ej. 10" 
        else {
            val examples = listOf("1", "2", "5", "10", "20", "25", "50", "100", "500", "1000")
            "Ej. ${examples.random()}"
        }
    }
    val randomDiameterPlaceholder = remember(hasAnyInput) {
        if (hasAnyInput) "Ej. 16"
        else {
            val examples = listOf("15", "16", "17", "18", "19", "20", "22", "24", "26", "28", "30", "35", "40")
            "Ej. ${examples.random()}"
        }
    }
    val randomCountryPlaceholder = remember(hasAnyInput) {
        if (hasAnyInput) "Ej. Reino Unido"
        else {
            val examples = listOf("Estados Unidos", "Reino Unido", "Nueva Zelanda", "Costa Rica", "Países Bajos", "Puerto Rico", "Corea del Sur", "Arabia Saudita", "República Checa", "Islas Caimán", "Sudáfrica")
            "Ej. ${examples.random()}"
        }
    }
    val randomYearPlaceholder = remember(hasAnyInput) {
        if (hasAnyInput) "Ej. 1925"
        else {
            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            val randomY = (1400..currentYear).random()
            "Ej. $randomY"
        }
    }
    val randomCurrencyPlaceholder = remember(hasAnyInput) {
        if (hasAnyInput) "Ej. Peso"
        else {
            val examples = listOf("Peseta", "Dólar", "Euro", "Peso", "Ruble", "Franco", "Corona", "Escudo", "Bolívar", "Lira", "Libra")
            "Ej. ${examples.random()}"
        }
    }

    // Filter autocomplete lists dynamically
    val filteredCountries = remember(countrySelected, countriesList) {
        if (countrySelected.isEmpty()) emptyList()
        else {
            val query = countrySelected.normalizeForSearch()
            countriesList.filter { it.name.normalizeForSearch().contains(query) }
        }
    }

    val filteredFaceValues = remember(faceValueSelected, faceValuesList) {
        if (faceValueSelected.isEmpty()) emptyList()
        else {
            val query = faceValueSelected.normalizeForSearch()
            val filtered = faceValuesList.filter { it.name.normalizeForSearch().contains(query) }
            
            // Prioritize integer numbers without decimals first (sorted by length so closer match is first),
            // then others. This ensures e.g., "2" comes before "2.5" or "2.0".
            filtered.sortedWith(compareBy<ColnectItem> { item ->
                val nameRaw = item.name.trim()
                val isPureInt = nameRaw.isNotEmpty() && nameRaw.all { it.isDigit() }
                if (isPureInt) 0 else 1
            }.thenBy { item ->
                item.name.length
            }.thenBy { item ->
                item.name
            })
        }
    }

    val filteredCompositions = remember(materialSelected, compositionsList) {
        if (materialSelected.isEmpty()) emptyList()
        else {
            val query = materialSelected.normalizeForSearch()
            compositionsList.filter { it.name.normalizeForSearch().contains(query) }
        }
    }

    val filteredCurrencies = remember(currencySelected, currenciesList) {
        if (currencySelected.isEmpty()) emptyList()
        else {
            val query = currencySelected.normalizeForSearch()
            currenciesList.filter { it.name.normalizeForSearch().contains(query) }
        }
    }

    // Determine current validations based on text and suggestions availability
    val countryValidation = when {
        countrySelected.isEmpty() -> ValidationState.Neutral
        filteredCountries.isNotEmpty() -> ValidationState.Valid
        else -> ValidationState.Invalid
    }

    val faceValueValidation = when {
        faceValueSelected.isEmpty() -> ValidationState.Neutral
        filteredFaceValues.isNotEmpty() -> ValidationState.Valid
        else -> ValidationState.Invalid
    }

    val materialValidation = when {
        materialSelected.isEmpty() -> ValidationState.Neutral
        filteredCompositions.isNotEmpty() -> ValidationState.Valid
        else -> ValidationState.Invalid
    }

    val currencyValidation = when {
        currencySelected.isEmpty() -> ValidationState.Neutral
        filteredCurrencies.isNotEmpty() -> ValidationState.Valid
        else -> ValidationState.Invalid
    }

    val yearValidation = remember(yearSelected) {
        if (yearSelected.isEmpty()) {
            ValidationState.Neutral
        } else {
            val yr = yearSelected.toIntOrNull()
            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            if (yr != null && yr > 0 && yr <= currentYear) {
                ValidationState.Valid
            } else {
                ValidationState.Invalid
            }
        }
    }

    val isSendEnabled = hasAnyInput &&
                        countryValidation != ValidationState.Invalid && 
                        faceValueValidation != ValidationState.Invalid && 
                        materialValidation != ValidationState.Invalid &&
                        currencyValidation != ValidationState.Invalid &&
                        yearValidation != ValidationState.Invalid

    // Build the dynamic URL and ensure completely clean, non-redundant Colnect paths
    val colnectUrl = remember(countrySelected, faceValueSelected, materialSelected, yearSelected, diameterSelected, currencySelected, countriesList, faceValuesList, compositionsList, currenciesList) {
        if (!hasAnyInput) {
            "https://colnect.com"
        } else {
            val pathParams = java.util.LinkedHashMap<String, String>()

            fun parseFragmentAndPut(fragment: String, defaultKey: String) {
                val clean = fragment.trim().removePrefix("/").removeSuffix("/")
                if (clean.isEmpty()) return
                if (clean.contains("/")) {
                    val parts = clean.split("/")
                    var i = 0
                    while (i < parts.size - 1) {
                        val key = parts[i].trim().lowercase()
                        val value = parts[i + 1].trim()
                        if (key.isNotEmpty() && value.isNotEmpty()) {
                            pathParams[key] = value
                        }
                        i += 2
                    }
                } else {
                    pathParams[defaultKey.lowercase()] = clean
                }
            }

            if (countrySelected.isNotEmpty()) {
                val exactOrBest = countriesList.find { it.name.equals(countrySelected, ignoreCase = true) }
                    ?: countriesList.find { it.name.contains(countrySelected, ignoreCase = true) }
                if (exactOrBest != null) {
                    parseFragmentAndPut(exactOrBest.fragment, "country")
                }
            }

            if (materialSelected.isNotEmpty()) {
                val exactOrBest = compositionsList.find { it.name.equals(materialSelected, ignoreCase = true) }
                    ?: compositionsList.find { it.name.contains(materialSelected, ignoreCase = true) }
                if (exactOrBest != null) {
                    parseFragmentAndPut(exactOrBest.fragment, "composition")
                }
            }

            if (currencySelected.isNotEmpty()) {
                val exactOrBest = currenciesList.find { it.name.equals(currencySelected, ignoreCase = true) }
                    ?: currenciesList.find { it.name.contains(currencySelected, ignoreCase = true) }
                if (exactOrBest != null) {
                    parseFragmentAndPut(exactOrBest.fragment, "currency")
                }
            }

            if (faceValueSelected.isNotEmpty()) {
                val exactOrBest = faceValuesList.find { it.name.equals(faceValueSelected, ignoreCase = true) }
                    ?: faceValuesList.find { it.name.contains(faceValueSelected, ignoreCase = true) }
                if (exactOrBest != null) {
                    parseFragmentAndPut(exactOrBest.fragment, "face_value")
                }
            }

            if (yearSelected.trim().isNotEmpty() && yearValidation == ValidationState.Valid) {
                pathParams["mint_year"] = yearSelected.trim()
            }

            if (diameterSelected.trim().isNotEmpty()) {
                pathParams["width"] = diameterSelected.trim()
            }

            val hasListIndicator = pathParams.containsKey("currency") || 
                                   pathParams.containsKey("face_value") || 
                                   pathParams.containsKey("mint_year") || 
                                   pathParams.containsKey("width")
            
            val particle = if (hasListIndicator) "list" else "series"
            val allowedSequence = listOf("country", "series", "composition", "currency", "face_value", "mint_year", "width")
            val pathBuilder = StringBuilder("https://colnect.com/es/coins/$particle")
            
            for (key in allowedSequence) {
                val value = pathParams[key]
                if (!value.isNullOrEmpty()) {
                    pathBuilder.append("/$key/$value")
                }
            }

            pathBuilder.toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4F9))
    ) {
        // 1. Top Status Bar Header (Material 3 style)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.8f))
                .padding(horizontal = 12.dp, vertical = 4.dp), // NO statusBarsPadding because Scaffold handles it; reduced padding
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.clickable { viewModel.triggerDiagnosticCard() }
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = if (isConnected) Color(0xFF10B981) else Color(0xFFEF4444),
                            shape = RoundedCornerShape(8.dp)
                        )
                )
                Text(
                    text = if (isConnected) "CONECTADO" else "SIN CONEXIÓN",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    letterSpacing = 1.sp
                )
            }

            Box(
                modifier = Modifier
                    .background(
                        color = if (updateInfo.hasUpdate) Color(0xFFFEE2E2) else Color(0xFFEEF2F6),
                        shape = RoundedCornerShape(100.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 0.5.dp)
            ) {
                Text(
                    text = if (updateInfo.hasUpdate) "$displayVersion ⚠" else "$displayVersion ✓",
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (updateInfo.hasUpdate) Color(0xFFEF4444) else Color(0xFF0F766E)
                )
            }
        }

        // Thin Border Line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFE2E8F0).copy(alpha = 0.5f))
        )

        // 2. Middle Content Area (Taller & Centered when there is no preview)
        val middleModifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { focusManager.clearFocus() }
            .padding(horizontal = 12.dp, vertical = 1.dp)

        Column(
            modifier = middleModifier,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            // Dynamic top spacer which shrinks when any field is focused to shift all elements up,
            // or when the preview is being shown to optimize screen real estate.
            val topSpacerHeight = if (showPreview) 1.dp else 6.dp
            Spacer(modifier = Modifier.height(topSpacerHeight))

            // Page Title Header block with dynamic layout scaling to always stay in 1 line inside margins
            var titleFontSize by remember(showPreview) { mutableStateOf(if (showPreview) 15.sp else 24.sp) }
            Text(
                text = "Creador de Enlaces Colnect",
                fontSize = titleFontSize,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                letterSpacing = (-0.3).sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                onTextLayout = { textLayoutResult ->
                    if (textLayoutResult.hasVisualOverflow && titleFontSize.value > 10f) {
                        titleFontSize = (titleFontSize.value * 0.9f).sp
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 0.dp,
                        bottom = if (showPreview) 0.dp else 2.dp
                    )
            )

            // Upper dynamic version warning (GitHub) inside form scroll area if available
            if (updateInfo.hasUpdate) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            try {
                                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.githubUrl))
                                context.startActivity(webIntent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "No se pudo abrir el enlace", Toast.LENGTH_SHORT).show()
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFEF0)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFFEF3C7))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Actualización",
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "¡Hay una nueva versión disponible!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFF92400E)
                            )
                            Text(
                                "Aplicación: v${updateInfo.appVersionOnGithub} | CSV: v${updateInfo.csvVersionOnGithub}. Toque aquí para visitar el repositorio.",
                                fontSize = 9.sp,
                                color = Color(0xFFB45309)
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Ir al enlace",
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            if (showPreview) {
                Spacer(modifier = Modifier.height(2.dp))
            }

            // White Form Card Block (rounded-[24px] instead of 32px for compact shape)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp), // Compact rounded corners
                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(6.dp), // Extremely compact padding
                    verticalArrangement = Arrangement.spacedBy(4.dp) // Tight vertical spacing
                ) {
                    // Country input
                    ValidatedInputField(
                        label = "País",
                        value = countrySelected,
                        onValueChange = { viewModel.countryInput.value = it },
                        validationState = countryValidation,
                        placeholder = randomCountryPlaceholder,
                        suggestions = filteredCountries,
                        onSuggestionClicked = { item ->
                            viewModel.countryInput.value = item.name
                            focusManager.clearFocus()
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        testTag = "country_input",
                        isExpanded = !showPreview,
                        expandedHeight = 58.dp, // Comfortably compact when URL is not visible (increased 20%)
                        onFocusChanged = { isCountryFocused = it }
                    )

                    // Row showing Year and Material side-by-side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Year input - width adjusted to display "Ej. 1892" perfectly and no more
                        Column(modifier = Modifier.width(100.dp)) {
                            ValidatedInputField(
                                label = "Año",
                                value = yearSelected,
                                onValueChange = { newVal ->
                                    val checked = newVal.filter { it.isDigit() }
                                    if (checked.length <= 4) {
                                        viewModel.yearInput.value = checked
                                    }
                                },
                                validationState = yearValidation,
                                placeholder = randomYearPlaceholder,
                                suggestions = emptyList(),
                                onSuggestionClicked = {},
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                testTag = "year_input",
                                isExpanded = !showPreview,
                                expandedHeight = 53.dp, // Comfortably compact when URL is not visible (increased 20%)
                                onFocusChanged = { isYearFocused = it }
                            )
                        }

                        // Material input - fills remaining width
                        Column(modifier = Modifier.weight(1f)) {
                            ValidatedInputField(
                                label = "Material",
                                value = materialSelected,
                                onValueChange = { viewModel.materialInput.value = it },
                                validationState = materialValidation,
                                placeholder = "Ejemplo: Plata",
                                suggestions = filteredCompositions,
                                onSuggestionClicked = { item ->
                                    viewModel.materialInput.value = item.name
                                    focusManager.clearFocus()
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                ),
                                testTag = "material_input",
                                isExpanded = !showPreview,
                                expandedHeight = 53.dp, // Comfortably compact when URL is not visible (increased 20%)
                                onFocusChanged = { isMaterialFocused = it }
                            )
                        }
                    }

                    // Row showing Face Value and Diameter side-by-side (giving Face Value the same width and height as Diameter)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Face value input
                        Column(modifier = Modifier.weight(1f)) {
                            ValidatedInputField(
                                label = "Valor Facial",
                                value = faceValueSelected,
                                onValueChange = { viewModel.faceValueInput.value = it },
                                validationState = faceValueValidation,
                                placeholder = randomFaceValuePlaceholder,
                                suggestions = filteredFaceValues,
                                onSuggestionClicked = { item ->
                                    viewModel.faceValueInput.value = item.name
                                    focusManager.clearFocus()
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                ),
                                testTag = "face_value_input",
                                isExpanded = !showPreview,
                                expandedHeight = 53.dp, // Comfortably compact when URL is not visible (increased 20%)
                                onFocusChanged = { isFaceValueFocused = it }
                            )
                        }

                        // Diameter
                        Column(modifier = Modifier.weight(1f)) {
                            ValidatedInputField(
                                label = "Diámetro",
                                value = diameterSelected,
                                onValueChange = { newVal ->
                                    val checked = newVal.filter { it.isDigit() }
                                    if (checked.length <= 3) {
                                        viewModel.diameterInput.value = checked
                                    }
                                },
                                validationState = if (diameterSelected.isNotEmpty()) ValidationState.Valid else ValidationState.Neutral,
                                placeholder = randomDiameterPlaceholder,
                                suggestions = emptyList(),
                                onSuggestionClicked = {},
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                testTag = "diameter_input",
                                isExpanded = !showPreview,
                                expandedHeight = 53.dp, // Comfortably compact when URL is not visible (increased 20%)
                                onFocusChanged = { isDiameterFocused = it }
                            )
                        }
                    }

                    // Denominations (Currencies) input field
                    ValidatedInputField(
                        label = "Denominación",
                        value = currencySelected,
                        onValueChange = { viewModel.currencyInput.value = it },
                        validationState = currencyValidation,
                        placeholder = randomCurrencyPlaceholder,
                        suggestions = filteredCurrencies,
                        onSuggestionClicked = { item ->
                            viewModel.currencyInput.value = item.name
                            focusManager.clearFocus()
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        testTag = "currency_input",
                        isExpanded = !showPreview,
                        expandedHeight = 53.dp,
                        onFocusChanged = { isCurrencyFocused = it }
                    )

                    // Compact, low-priority clear fields button - shifted lower and icon height matched to text
                    // Disabled and styled in gray color with no action when hasAnyInput is false, as requested
                    OutlinedButton(
                        onClick = {
                            viewModel.clearForm()
                            focusManager.clearFocus()
                        },
                        enabled = hasAnyInput,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (hasAnyInput) Color(0xFFB91C1C) else Color(0xFFF1F5F9),
                            contentColor = if (hasAnyInput) Color.White else Color(0xFF94A3B8),
                            disabledContainerColor = Color(0xFFF1F5F9),
                            disabledContentColor = Color(0xFF94A3B8)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = if (hasAnyInput) 2.5.dp else 0.dp,
                            pressedElevation = if (hasAnyInput) 4.dp else 0.dp,
                            disabledElevation = 0.dp
                        ),
                        border = BorderStroke(1.2.dp, if (hasAnyInput) Color(0xFF991B1B) else Color(0xFFCBD5E1)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp), // Zero vertical padding to let Row center elements perfectly
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp) // Shifted down a bit more as requested
                            .height(34.dp) // Compact, low-priority height
                            .shadow(if (hasAnyInput) 4.dp else 0.dp, RoundedCornerShape(12.dp), ambientColor = Color.Black.copy(alpha = 0.18f), spotColor = Color.Black.copy(alpha = 0.22f))
                            .testTag("clear_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxHeight() // Fill full height for precise mechanical vertical centering
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Limpiar campos",
                                tint = if (hasAnyInput) Color.White else Color(0xFF94A3B8),
                                modifier = Modifier.size(15.dp) // Perfectly proportional to 12.sp text size, centered
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Limpiar todos los campos",
                                fontSize = 12.sp, // Enhanced font size for optimal alignment and legibility
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            if (showPreview) {
                Spacer(modifier = Modifier.height(3.dp)) // Tight spacing above the URL block to directly reduce empty space

                // Indigo preview & CTA card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(16.dp), ambientColor = Color(0xFF4F46E5).copy(alpha = 0.2f)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4F46E5)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp), // Increased vertical padding
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) { // Slightly increased layout spacing
                            Text(
                                text = "VISTA PREVIA DEL ENLACE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White.copy(alpha = 0.6f),
                                letterSpacing = 1.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(bottom = 0.dp)
                            )
                            Text(
                                text = colnectUrl,
                                fontSize = 10.5.sp, // Compact readable link text
                                color = Color.White.copy(alpha = 0.95f),
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 13.sp,
                                modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                            )
                        }

                        // Shifted down: Dedicated Spacer to leave more space between the url and the button itself.
                        Spacer(modifier = Modifier.height(10.dp))

                        // Open in Colnect Button (Primary, single full width) - highlighted and prominent with vivid brand gold accents
                        // Greyed out and disabled completely when no internet connection is active
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                if (viewModel.checkNetwork(context)) {
                                    val activeUrl = viewModel.activeWebViewUrl.value
                                    if (onNavigateToWebView != null && activeUrl != null && activeUrl == colnectUrl) {
                                        onNavigateToWebView()
                                    } else {
                                        val currentCount = viewModel.queryCount
                                        viewModel.queryCount = currentCount + 1
                                        viewModel.openInWebView(colnectUrl)
                                    }
                                }
                            },
                            enabled = isSendEnabled && isConnected,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFBBF24), // Vivid gold-amber, extremely destacados!
                                contentColor = Color(0xFF0F172A), // High contrast dark slate for clear readability
                                disabledContainerColor = Color(0xFF94A3B8).copy(alpha = 0.8f), // Prominent grey container when offline
                                disabledContentColor = Color(0xFFF1F5F9).copy(alpha = 0.7f) // Ultra clear disabled slate contrast
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 3.dp,
                                pressedElevation = 1.5.dp,
                                disabledElevation = 0.dp
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(0.dp), // Zero padding tells Compose to center completely
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp) // Compact size
                                .testTag("submit_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxSize() // Full width and height sizing to guarantee horizontal and vertical centering
                            ) {
                                Text(
                                    text = if (isConnected) "ABRIR EN COLNECT" else "SIN CONEXIÓN",
                                    fontWeight = FontWeight.ExtraBold, // Highlighted text style
                                    fontSize = 12.5.sp,
                                    letterSpacing = 0.6.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = if (isConnected) Icons.Default.Share else Icons.Default.Warning,
                                    contentDescription = "Abrir enlace",
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (!showPreview && !isAnyFieldFocused) {
                Spacer(modifier = Modifier.height(28.dp)) // Nudge entire group upwards when there is no preview shown and keyboard is hidden
            }
        }

        // 3. Footer Logo area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFE2E8F0).copy(alpha = 0.5f))
        )
        PulsingColnectLogo()
    }
}

// --------------------------------------------------------------------
// Validation states
// --------------------------------------------------------------------
enum class ValidationState {
    Neutral,
    Valid,
    Invalid
}

// --------------------------------------------------------------------
// 3. Custom Form Input Fields with pastel border shading on validations
// --------------------------------------------------------------------
@Composable
fun ColnectWebViewScreen(
    url: String,
    onClose: () -> Unit,
    viewModel: ColnectViewModel
) {
    val context = LocalContext.current
    val loggedInStatus by viewModel.isUserLoggedInInWebView.collectAsStateWithLifecycle()
    val scrapedCountries by viewModel.scrapedCountries.collectAsStateWithLifecycle()
    val scrapedFaceValues by viewModel.scrapedFaceValues.collectAsStateWithLifecycle()
    val scrapedCompositions by viewModel.scrapedCompositions.collectAsStateWithLifecycle()
    val scrapedCurrencies by viewModel.scrapedCurrencies.collectAsStateWithLifecycle()
    val pendingUrl by viewModel.pendingConformedUrl.collectAsStateWithLifecycle()

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    BackHandler(enabled = true) {
        webViewInstance?.let { wb ->
            if (canWebViewGoBackSafely(wb)) {
                wb.goBack()
            } else {
                onClose()
            }
        } ?: onClose()
    }

    LaunchedEffect(url) {
        webViewInstance?.let { wb ->
            if (wb.url != url) {
                wb.loadUrl(url)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.clearScrapedData()
    }

    LaunchedEffect(loggedInStatus) {
        if (loggedInStatus == true) {
            pendingUrl?.let { target ->
                viewModel.clearPendingAndLoad(target)
                webViewInstance?.loadUrl(target)
            }
        }
    }

    LaunchedEffect(scrapedCountries) {
        if (scrapedCountries.isNotEmpty()) {
            viewModel.saveScrapedCountriesToStorage(context, scrapedCountries)
        }
    }

    LaunchedEffect(scrapedFaceValues) {
        if (scrapedFaceValues.isNotEmpty()) {
            viewModel.saveScrapedFaceValuesToStorage(context, scrapedFaceValues)
        }
    }

    LaunchedEffect(scrapedCompositions) {
        if (scrapedCompositions.isNotEmpty()) {
            viewModel.saveScrapedCompositionsToStorage(context, scrapedCompositions)
        }
    }

    LaunchedEffect(scrapedCurrencies) {
        if (scrapedCurrencies.isNotEmpty()) {
            viewModel.saveScrapedCurrenciesToStorage(context, scrapedCurrencies)
        }
    }
    val initialLaunchUrl = url
    var webTitle by remember { mutableStateOf("Cargando Colnect...") }
    var webUrl by remember { mutableStateOf(url) }
    var loadProgress by remember { mutableStateOf(0) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var isAtTop by remember { mutableStateOf(true) }
    var isAtBottom by remember { mutableStateOf(false) }

    // This helper runs Javascript to verify login status manually/on-demand
    val runLoginCheck = {
        webViewInstance?.let { webView ->
            val loginCheckJs = """
                (function() {
                    try {
                        try { document.querySelectorAll('a[target="_blank"]').forEach(function(el) { el.removeAttribute('target'); }); } catch(e) {}
                        var hasLogout = document.querySelector("a[href*='/account/logout']") !== null || 
                                        document.querySelector("a[href*='logout']") !== null;
                        var isEsLogoutText = document.body.innerText.indexOf("Cerrar sesión") !== -1 || 
                                             document.body.innerText.indexOf("Cierre de sesión") !== -1 || 
                                             document.body.innerText.indexOf("Salir") !== -1;
                        var isEnLogoutText = document.body.innerText.indexOf("Logout") !== -1 || 
                                             document.body.innerText.indexOf("Log out") !== -1 || 
                                             document.body.innerText.indexOf("Sign out") !== -1;
                        var memberMenu = document.querySelector(".member_menu") !== null || 
                                         document.querySelector(".my-account") !== null || 
                                         document.querySelector(".member-menu") !== null ||
                                         document.querySelector("a[href*='/es/account']") !== null;
                        var isLoginForm = document.querySelector("form[action*='/account/login']") !== null;

                        var isLoggedIn = hasLogout || isEsLogoutText || isEnLogoutText || memberMenu;
                        if (isLoginForm) {
                            isLoggedIn = false;
                        }
                        ColnectAppInterface.reportLoginStatus(isLoggedIn);
                    } catch(e) {
                        ColnectAppInterface.reportLoginStatus(false);
                    }
                })()
            """.trimIndent()
            webView.post {
                webView.evaluateJavascript(loginCheckJs, null)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Clean white backdrop to eliminate back screen flashes during loading
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header TopBar / Visor Control Panel styled matching app theme
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .background(Color(0xFF1E293B)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Close Cell
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onClose() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar visor web",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(Color.White.copy(alpha = 0.3f))
                )

                // 2. Back Cell
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(enabled = canGoBack) {
                            webViewInstance?.let { wb ->
                                if (canWebViewGoBackSafely(wb)) {
                                    wb.goBack()
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retroceder página",
                        tint = if (canGoBack) Color.White else Color(0xFF475569),
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(Color.White.copy(alpha = 0.3f))
                )

                // 3. Forward Cell
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(enabled = canGoForward) {
                            webViewInstance?.let { if (it.canGoForward()) it.goForward() }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Avanzar página",
                        tint = if (canGoForward) Color.White else Color(0xFF475569),
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(Color.White.copy(alpha = 0.3f))
                )

                // 3a. Home Cell (Ir al Home: https://colnect.com/es/coins)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            webViewInstance?.loadUrl("https://colnect.com/es/coins")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Ir al Inicio (Coins)",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(Color.White.copy(alpha = 0.3f))
                )

                // 3b. Scroll to Top Cell (Ir al inicio)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(enabled = !isAtTop) {
                            webViewInstance?.evaluateJavascript("window.scrollTo(0, 0);", null)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Ir al inicio",
                        tint = if (!isAtTop) Color.White else Color(0xFF475569),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(Color.White.copy(alpha = 0.3f))
                )

                // 3c. Scroll to Bottom Cell (Ir al final)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(enabled = !isAtBottom) {
                            webViewInstance?.evaluateJavascript("window.scrollTo(0, document.body.scrollHeight);", null)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Ir al final",
                        tint = if (!isAtBottom) Color.White else Color(0xFF475569),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(Color.White.copy(alpha = 0.3f))
                )

                // 4. Refresh Cell
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { webViewInstance?.reload() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Recargar página",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(Color.White.copy(alpha = 0.3f))
                )

                // 5. Login Indicator Cell
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { webViewInstance?.loadUrl("https://colnect.com/es/account/login") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (loggedInStatus) {
                            true -> Icons.Default.CheckCircle
                            false -> Icons.Default.Lock
                            else -> Icons.Default.Refresh
                        },
                        contentDescription = "Estado de inicio de sesión",
                        tint = when (loggedInStatus) {
                            true -> Color(0xFF10B981) // Emerald / Green OK
                            false -> Color(0xFFEF4444) // Red for Logged out / Locked
                            else -> Color(0xFF94A3B8) // Slate Gray for Loading/Verifying
                        },
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(Color.White.copy(alpha = 0.3f))
                )

                // 6. Share Cell
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "No se pudo abrir", Toast.LENGTH_SHORT).show()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Abrir en navegador externo",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Real-time page loads progress indicator
            if (loadProgress < 100) {
                LinearProgressIndicator(
                    progress = { loadProgress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = Color(0xFFFBBF24), // Vivid gold accent contrast
                    trackColor = Color(0xFF334155),
                )
            } else {
                Spacer(modifier = Modifier.height(3.dp))
            }

            // Scraped banners removed in favor of background auto-sync

            // The embedded web rendering viewport engine
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            allowFileAccess = true
                            allowContentAccess = true
                            // Support wide viewport scaling and overview mode to let Colnect's responsive engine layout elements, dropdowns, and account menus beautifully
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            // Support safe zooming in and out
                            builtInZoomControls = true
                            displayZoomControls = false
                            setSupportZoom(true)
                            javaScriptCanOpenWindowsAutomatically = true
                            // Support mixed content modes to ensure all resource content and dynamic scripts render and load correctly
                            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            // Modern mobile browser User-Agent to ensure Colnect serves its beautiful responsive mobile theme
                            userAgentString = "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36"
                        }

                        // Robust Cookie management ensuring stay-logged-in sessions preserve over restarts
                        val cookieManager = CookieManager.getInstance()
                        cookieManager.setAcceptCookie(true)
                        cookieManager.setAcceptThirdPartyCookies(this, true)

                        setOnScrollChangeListener { _, _, _, _, _ ->
                            isAtTop = !canScrollVertically(-1)
                            isAtBottom = !canScrollVertically(1)
                        }

                        var startTouchX = 0f
                        var startTouchY = 0f
                        var isGestureDirectionDecided = false

                        setOnTouchListener { v, event ->
                            when (event.action) {
                                android.view.MotionEvent.ACTION_DOWN -> {
                                    startTouchX = event.x
                                    startTouchY = event.y
                                    isGestureDirectionDecided = false
                                    v.parent?.requestDisallowInterceptTouchEvent(false)
                                }
                                android.view.MotionEvent.ACTION_MOVE -> {
                                    if (!isGestureDirectionDecided) {
                                        val dx = event.x - startTouchX
                                        val dy = event.y - startTouchY
                                        val absX = kotlin.math.abs(dx)
                                        val absY = kotlin.math.abs(dy)
                                        
                                        if (absX > 15 || absY > 15) {
                                            isGestureDirectionDecided = true
                                            if (absY > absX * 1.5f) {
                                                // Vertical scroll strictly prioritized: Block HorizontalPager from stealing it
                                                v.parent?.requestDisallowInterceptTouchEvent(true)
                                            } else if (absX > absY * 3.0f) {
                                                // Highly horizontal swipe: Let the parent (HorizontalPager) intercept and slide smoothly
                                                v.parent?.requestDisallowInterceptTouchEvent(false)
                                            } else {
                                                v.parent?.requestDisallowInterceptTouchEvent(true)
                                            }
                                        }
                                    }
                                }
                                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                                    isGestureDirectionDecided = false
                                }
                            }
                            false
                        }

                        val reportObj = object {
                            @JavascriptInterface
                            fun reportScroll(scrollY: Double, maxScroll: Double) {
                                webViewInstance?.post {
                                    isAtTop = scrollY <= 15.0
                                    isAtBottom = scrollY >= (maxScroll - 25.0)
                                }
                            }

                            @JavascriptInterface
                            fun reportLoginStatus(isLoggedIn: Boolean) {
                                viewModel.setUserLoggedInStatus(isLoggedIn)
                                webViewInstance?.post {
                                    try {
                                        val cookieManager = CookieManager.getInstance()
                                        val cookies = cookieManager.getCookie("https://colnect.com")
                                        if (cookies != null && cookies.trim().isNotEmpty()) {
                                            viewModel.syncCookies(context, cookies)
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }

                            @JavascriptInterface
                            fun onCountriesScraped(countriesRaw: String) {
                                if (countriesRaw.trim().isEmpty()) {
                                    viewModel.updateScrapedCountries(emptyList())
                                    return
                                }
                                val list = countriesRaw.split("\n")
                                    .map { it.trim() }
                                    .filter { it.isNotEmpty() }
                                    .mapNotNull { line ->
                                        val parts = line.split("\t")
                                        if (parts.size >= 2) {
                                            ColnectItem(parts[0].trim(), parts[1].trim())
                                        } else {
                                            null
                                        }
                                    }
                                viewModel.updateScrapedCountries(list)
                            }

                            @JavascriptInterface
                            fun onFaceValuesScraped(faceValuesRaw: String) {
                                if (faceValuesRaw.trim().isEmpty()) {
                                    viewModel.updateScrapedFaceValues(emptyList())
                                    return
                                }
                                val list = faceValuesRaw.split("\n")
                                    .map { it.trim() }
                                    .filter { it.isNotEmpty() }
                                    .mapNotNull { line ->
                                        val parts = line.split("\t")
                                        if (parts.size >= 2) {
                                            ColnectItem(parts[0].trim(), parts[1].trim())
                                        } else {
                                            null
                                        }
                                    }
                                viewModel.updateScrapedFaceValues(list)
                            }

                            @JavascriptInterface
                            fun onCompositionsScraped(compositionsRaw: String) {
                                if (compositionsRaw.trim().isEmpty()) {
                                    viewModel.updateScrapedCompositions(emptyList())
                                    return
                                }
                                val list = compositionsRaw.split("\n")
                                    .map { it.trim() }
                                    .filter { it.isNotEmpty() }
                                    .mapNotNull { line ->
                                        val parts = line.split("\t")
                                        if (parts.size >= 2) {
                                            ColnectItem(parts[0].trim(), parts[1].trim())
                                        } else {
                                            null
                                        }
                                    }
                                viewModel.updateScrapedCompositions(list)
                            }

                            @JavascriptInterface
                            fun onCurrenciesScraped(currenciesRaw: String) {
                                if (currenciesRaw.trim().isEmpty()) {
                                    viewModel.updateScrapedCurrencies(emptyList())
                                    return
                                }
                                val list = currenciesRaw.split("\n")
                                    .map { it.trim() }
                                    .filter { it.isNotEmpty() }
                                    .mapNotNull { line ->
                                        val parts = line.split("\t")
                                        if (parts.size >= 2) {
                                            ColnectItem(parts[0].trim(), parts[1].trim())
                                        } else {
                                            null
                                        }
                                    }
                                viewModel.updateScrapedCurrencies(list)
                            }
                        }
                        addJavascriptInterface(reportObj, "ColnectAppInterface")

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                url?.let {
                                    val u = it.trim().lowercase().removeSuffix("/")
                                    if (u == "https://colnect.com" || u == "https://colnect.com/es") {
                                        view?.loadUrl("https://colnect.com/es/coins")
                                    } else if (u.startsWith("https://colnect.com") && !u.contains("/coins") && !it.contains("/account/") && !it.contains("/user/")) {
                                        view?.loadUrl("https://colnect.com/es/coins")
                                    }
                                }
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                try {
                                    CookieManager.getInstance().flush()
                                    val cookieStr = CookieManager.getInstance().getCookie("https://colnect.com")
                                    if (cookieStr != null && cookieStr.trim().isNotEmpty()) {
                                        viewModel.syncCookies(context, cookieStr)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                url?.let { webUrl = it }
                                view?.title?.let { if (it.isNotEmpty()) webTitle = it }
                                canGoBack = canWebViewGoBackSafely(view)
                                canGoForward = view?.canGoForward() ?: false
                                // 1. Inject the user's exact JavaScript code verbatim to clean the page and improve mobile view
                                val userCleanJs = """
                                     (function() {
                                         // 1. Reseteo agresivo de CSS para eliminar espacios reservados por el header de Colnect
                                         var style = document.createElement('style');
                                         style.innerHTML = `
                                             html, body, #main, #page_wrap, .page_wrap, #main_content, .content_wrapper, .page_container, #content, #content_main, .main_content {
                                                  padding-top: 0 !important;
                                                  margin-top: 0 !important;
                                              }
                                              #main, #page_wrap, .page_wrap, #main_content, .content_wrapper, .page_container, #content {
                                                 padding-top: 0 !important;
                                                 margin-top: 0 !important;
                                                 min-height: 0 !important;
                                                 height: auto !important;
                                             }
                                             header, footer, #header, .header, #footer, .footer,
                                              #top_bar, #top-bar, .top-bar, .header_container,
                                              #top_info_bar, #breadcrumbs, .breadcrumbs,
                                              .site-header, #masthead, .top-nav, #nav-top,
                                              #user_bar, .site-header-container, #top-header,
                                              #bottom_bar, #bottom-bar, .site-footer, #social_media, 
                                              #copyright, .copyright, #lower_footer,
                                             #improve_catalog, .improve_catalog, a[href*="/improve/"], 
                                              a[href*="ebay.com/sch/"], #ebay_similar_items, .ebay_similar_items,
                                             .item_sell_box, a[href*="/sell/"], a[href*="/premium/"], .sell_articles,
                                             .plumb_box, .ad_box, .ads, [class*="ad-"], [id*="ad-"],
                                             #main_content_headers {
                                                  display: none !important;
                                              }
                                             .quick_marking, .item_co_update, .co_update, [class*="co_update"], [class*="marking"],
                                              form, .item_marking, .marking_box {
                                                 height: auto !important;
                                                 max-height: none !important;
                                                 min-height: 0 !important;
                                                 overflow: visible !important;
                                                 line-height: normal !important;
                                             }
                                             .quick_marking label, .co-update-form label, .item_marking label,
                                              .quick_marking span, .co-update-form span, .item_marking span,
                                              .quick_marking p, .co-update-form p, .item_marking p {
                                                 height: auto !important;
                                                 max-height: none !important;
                                                 line-height: 1.25 !important;
                                                 overflow: visible !important;
                                                 word-break: break-word !important;
                                             }
                                         `;
                                         // Ocultar texto innecesario
                                         try {
                                             var tagsToClean = ['p', 'span', 'a', 'h1', 'h2', 'h3', 'div', 'td', 'th', 'li']; var allElements = document.querySelectorAll(tagsToClean.join(','));
                                             allElements.forEach(function(el) {
                                                 var id = (el.id || "").toLowerCase(); var cl = (el.className && typeof el.className === "string") ? el.className.toLowerCase() : ""; if (id.indexOf("main") !== -1 || id.indexOf("content") !== -1 || id.indexOf("wrap") !== -1 || id.indexOf("container") !== -1) return; if (cl.indexOf("main") !== -1 || cl.indexOf("content") !== -1 || cl.indexOf("wrap") !== -1 || cl.indexOf("container") !== -1) return; if (el.querySelectorAll("div, table, form, section, article").length > 0) return;
                                                 var text = (el.textContent || el.innerText || "").toLowerCase().trim();
                                                 if (
                                                     text.indexOf("ocultar elementos") !== -1 ||
                                                     text.indexOf("enfoque estrecho") !== -1 ||
                                                     text.indexOf("enfoque amplio") !== -1 ||
                                                     text.indexOf("los catálogos de colnect") !== -1 ||
                                                     text.indexOf("colnect es el único") !== -1 ||
                                                     text.indexOf("compra, vende o intercambia") !== -1 ||
                                                     text.indexOf("comprar ahora:") !== -1 ||
                                                     text.indexOf("mejora nuestro catálogo") !== -1 ||
                                                     text.indexOf("encuentra ítems similares") !== -1
                                                 ) {
                                                     el.style.setProperty('display', 'none', 'important');
                                                 }
                                             });
                                         } catch(e) {}
                                         document.head.appendChild(style);
                                         
                                         // 2. Buscar exactamente el nodo que dice "Cambiar a la lista" y ocultar todo lo que está arriba
                                         var xpath = "//text()[contains(., 'Cambiar a la lista')]/parent::*";
                                         var result = document.evaluate(xpath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null);
                                         var targetElement = result.singleNodeValue;
                                         
                                         if (targetElement) {
                                             try {
                                                var curr = targetElement;
                                                while (curr && curr.parentElement && curr.parentElement !== document.body) {
                                                    var sib = curr.previousElementSibling;
                                                    while (sib) {
                                                        sib.style.setProperty('display', 'none', 'important');
                                                        sib = sib.previousElementSibling;
                                                    }
                                                    curr = curr.parentElement;
                                                }
                                                // Adjust wrappers to fill space
                                                var c = targetElement;
                                                while (c && c.parentElement && c.parentElement !== document.body) {
                                                    c.style.setProperty('padding-top', '0', 'important');
                                                    c.style.setProperty('margin-top', '0', 'important');
                                                    c.style.setProperty('height', '100%', 'important');
                                                    c = c.parentElement;
                                                }
                                             } catch(e) {}
                                         }
                                         
                                         // 4. Forzar la vista al tope exacto de la página
                                         window.scrollTo(0, 0);
                                     })();
                                 """.trimIndent()
                                view?.evaluateJavascript(userCleanJs, null)
                                // 2. Robust integration helpers for session status (login indicator) and scroll state
                                val appIntegrationJs = """
                                    (function() {
                                        try {
                                            document.querySelectorAll('a[target="_blank"]').forEach(function(el) {
                                                el.removeAttribute('target');
                                            });

                                            var hasLogout = document.querySelector("a[href*='/account/logout']") !== null || 
                                                            document.querySelector("a[href*='logout']") !== null;
                                            var isEsLogoutText = document.body.innerText.indexOf("Cerrar sesión") !== -1 || 
                                                                 document.body.innerText.indexOf("Cierre de sesión") !== -1 || 
                                                                 document.body.innerText.indexOf("Salir") !== -1;
                                            var isEnLogoutText = document.body.innerText.indexOf("Logout") !== -1 || 
                                                                 document.body.innerText.indexOf("Log out") !== -1 || 
                                                                 document.body.innerText.indexOf("Sign out") !== -1;
                                            var memberMenu = document.querySelector(".member_menu") !== null || 
                                                             document.querySelector(".my-account") !== null || 
                                                             document.querySelector(".member-menu") !== null ||
                                                             document.querySelector("a[href*='/es/account']") !== null;
                                            var isLoginForm = document.querySelector("form[action*='/account/login']") !== null;

                                            var isLoggedIn = hasLogout || isEsLogoutText || isEnLogoutText || memberMenu;
                                            if (isLoginForm) {
                                                isLoggedIn = false;
                                            }
                                            ColnectAppInterface.reportLoginStatus(isLoggedIn);

                                            var sY = window.scrollY || window.pageYOffset || 0;
                                            var mS = document.documentElement.scrollHeight - window.innerHeight;
                                            ColnectAppInterface.reportScroll(sY, mS);
                                        } catch(e) {}
                                    })()
                                """.trimIndent()

                                view?.evaluateJavascript(appIntegrationJs, null)

                                val deprecatedJs3 = """
                                    (function() {
                                        // --- 1. Robust and Aggressive CSS Style Injector ---
                                        function injectCleanStyles() {
                                            try {
                                                var css = 'header, footer, #header, .header, #footer, .footer, ' +
                                                          '#top_bar, #top-bar, .top-bar, .header_container, ' +
                                                          '#top_info_bar, #breadcrumbs, .breadcrumbs, ' +
                                                          '.site-header, #masthead, .top-nav, #nav-top, ' +
                                                          '#user_bar, .site-header-container, #top-header, ' +
                                                          '#bottom_bar, #bottom-bar, .site-footer, #social_media, ' +
                                                          '#copyright, .copyright, #lower_footer, ' +
                                                          '#improve_catalog, .improve_catalog, a[href*="/improve/"], ' +
                                                          'a[href*="ebay.com/sch/"], #ebay_similar_items, .ebay_similar_items, ' +
                                                          'a[href*="/sell/"], a[href*="/premium/"], .sell_articles, ' +
                                                          '.ad_box, .ads, [class*="ad-"], [id*="ad-"] { display: none !important; }' +
                                                          ' * { word-break: normal !important; overflow-wrap: break-word !important; word-wrap: break-word !important; hyphens: none !important; }';

                                                var parent = document.head || document.documentElement;
                                                if (parent) {
                                                    var style = document.getElementById('colnect-visor-override-style');
                                                    if (!style) {
                                                        style = document.createElement('style');
                                                        style.id = 'colnect-visor-override-style';
                                                        style.type = 'text/css';
                                                        style.innerHTML = css;
                                                        parent.appendChild(style);
                                                    } else if (style.innerHTML !== css) {
                                                        style.innerHTML = css;
                                                    }
                                                }
                                            } catch(e) {}
                                        }

                                        // --- 2. Remove Target Blank opens (Keep links internal) ---
                                        function removeTargetBlanks() {
                                            try { 
                                                document.querySelectorAll('a[target="_blank"]').forEach(function(el) { 
                                                    el.removeAttribute('target'); 
                                                }); 
                                            } catch(e) {}
                                        }

                                        // --- 3. Verify Login Status ---
                                        function checkLoginStatus() {
                                            try {
                                                var hasLogout = document.querySelector("a[href*='/account/logout']") !== null || 
                                                                document.querySelector("a[href*='logout']") !== null;
                                                var isEsLogoutText = document.body && document.body.innerText && (
                                                    document.body.innerText.indexOf("Cerrar sesión") !== -1 || 
                                                    document.body.innerText.indexOf("Cierre de sesión") !== -1 || 
                                                    document.body.innerText.indexOf("Salir") !== -1
                                                );
                                                var isEnLogoutText = document.body && document.body.innerText && (
                                                    document.body.innerText.indexOf("Logout") !== -1 || 
                                                    document.body.innerText.indexOf("Log out") !== -1 || 
                                                    document.body.innerText.indexOf("Sign out") !== -1
                                                );
                                                var memberMenu = document.querySelector(".member_menu") !== null || 
                                                                 document.querySelector(".my-account") !== null || 
                                                                 document.querySelector(".member-menu") !== null ||
                                                                 document.querySelector("a[href*='/es/account']") !== null;
                                                var isLoginForm = document.querySelector("form[action*='/account/login']") !== null;

                                                var isLoggedIn = hasLogout || isEsLogoutText || isEnLogoutText || memberMenu;
                                                if (isLoginForm) {
                                                    isLoggedIn = false;
                                                }
                                                ColnectAppInterface.reportLoginStatus(isLoggedIn);
                                            } catch(e) {}
                                        }

                                        // --- 4. Report Scroll offset updates ---
                                        function checkScrollOffset() {
                                            try {
                                                var sY = window.scrollY || window.pageYOffset || 0;
                                                var mS = document.documentElement.scrollHeight - window.innerHeight;
                                                ColnectAppInterface.reportScroll(sY, mS);
                                            } catch(e) {}
                                        }

                                        // --- 5. Background Scrapers (Run once per location change safely) ---
                                        function runScrapers() {
                                            try {
                                                if (window.lastScrapedUrl === location.href) return;
                                                window.lastScrapedUrl = location.href;

                                                // Country scraper
                                                try {
                                                    var resC = [];
                                                    document.querySelectorAll('a').forEach(function(a) {
                                                        var url = a.href;
                                                        if (url && url.indexOf('/country/') !== -1 && url.indexOf('/page/') === -1) {
                                                            var raw = a.innerText.trim().replace(/\n/g, ' ');
                                                            var clean = raw.replace(/\s*\([^)]*\)$/, '').trim();
                                                            var parts = url.split('/country/');
                                                            if (parts.length > 1) {
                                                                var frag = parts[1].split(/[?#]/)[0];
                                                                if (frag && clean !== "" && frag.indexOf('/') === -1) {
                                                                    resC.push(clean + "\t" + frag);
                                                                }
                                                            }
                                                        }
                                                    });
                                                    ColnectAppInterface.onCountriesScraped(resC.length > 0 ? [...new Set(resC)].join('\n') : "");
                                                } catch(ec) {}

                                                // Face values scraper
                                                try {
                                                    var resFV = [];
                                                    document.querySelectorAll('a').forEach(function(a) {
                                                        var url = a.href;
                                                        if (url && url.indexOf('/face_value/') !== -1 && url.indexOf('/page/') === -1) {
                                                            var raw = a.innerText.trim().replace(/\n/g, ' ');
                                                            var clean = raw.replace(/\s*\([^)]*\)$/, '').trim();
                                                            var parts = url.split('/face_value/');
                                                            if (parts.length > 1) {
                                                                var frag = parts[1].split(/[?#]/)[0];
                                                                if (frag && clean !== "" && frag.indexOf('/') === -1) {
                                                                    resFV.push(clean + "\t" + frag);
                                                                }
                                                            }
                                                        }
                                                    });
                                                    ColnectAppInterface.onFaceValuesScraped(resFV.length > 0 ? [...new Set(resFV)].join('\n') : "");
                                                } catch(efv) {}

                                                // Composition scraper
                                                try {
                                                    var compMap = new Map();
                                                    document.querySelectorAll('a[href*="/composition/"]').forEach(function(a) {
                                                        try {
                                                            var clone = a.cloneNode(true);
                                                            clone.querySelectorAll('span').forEach(function(s) { s.remove(); });
                                                            var text = clone.textContent.trim().split('-')[0].trim().replace(/\s*\([^)]*\)$/, '').trim();
                                                            var urlPart = a.href.split('/').pop().split(/[?#]/)[0];
                                                            if (text && urlPart && urlPart.indexOf('/') === -1) compMap.set(text, urlPart);
                                                        } catch(e) {}
                                                    });
                                                    var list = Array.from(compMap, function(p) { return p[0] + "\t" + p[1]; });
                                                    ColnectAppInterface.onCompositionsScraped(list.length > 0 ? [...new Set(list)].join('\n') : "");
                                                } catch(ecomp) {}

                                                // Currency scraper
                                                try {
                                                    var currMap = new Map();
                                                    document.querySelectorAll('a[href*="/currency/"]').forEach(function(a) {
                                                        try {
                                                            var clone = a.cloneNode(true);
                                                            clone.querySelectorAll('span').forEach(function(s) { s.remove(); });
                                                            var text = clone.textContent.trim().replace(/\s*\([^)]*\)$/, '').trim();
                                                            var lastHyphen = text.lastIndexOf('-');
                                                            if (lastHyphen !== -1) text = text.substring(0, lastHyphen).trim();
                                                            var urlPart = a.href.split('/').pop().split(/[?#]/)[0];
                                                            if (text && urlPart && urlPart.indexOf('/') === -1) currMap.set(text, urlPart);
                                                        } catch(e) {}
                                                    });
                                                    var list = Array.from(currMap, function(p) { return p[0] + "\t" + p[1]; });
                                                    ColnectAppInterface.onCurrenciesScraped(list.length > 0 ? [...new Set(list)].join('\n') : "");
                                                } catch(ecurr) {}
                                            } catch(e) {}
                                        }

                                        // Main execution loop
                                        function loop() {
                                            injectCleanStyles();
                                            removeTargetBlanks();
                                            checkLoginStatus();
                                            checkScrollOffset();
                                            runScrapers();
                                        }

                                        // Direct startup call
                                        loop();

                                        // Set periodic interval checks to handle AJAX or dynamically loaded tags
                                        setInterval(loop, 1000);

                                        // Scroll helper listener binding
                                        try {
                                            if (!window.scrollListenerBound) {
                                                window.scrollListenerBound = true;
                                                window.addEventListener('scroll', checkScrollOffset);
                                            }
                                        } catch(e) {}
                                    })();
                                """.trimIndent()

                                val selfLoopingJs2 = """
                                    (function check() {
                                        try {
                                             if (!document || !document.body) {
                                                 setTimeout(check, 1000);
                                                 return;
                                             }
                                             // --- 1. Quick Login Verification (Runs fast every loop) ---
                                             var hasLogout = document.querySelector("a[href*='/account/logout']") !== null || document.querySelector("a[href*='logout']") !== null;
                                             var isEsLogoutText = document.body.innerText.indexOf("Cerrar sesión") !== -1 || document.body.innerText.indexOf("Cierre de sesión") !== -1 || document.body.innerText.indexOf("Salir") !== -1;
                                             var isEnLogoutText = document.body.innerText.indexOf("Logout") !== -1 || document.body.innerText.indexOf("Log out") !== -1 || document.body.innerText.indexOf("Sign out") !== -1;
                                             var memberMenu = document.querySelector(".member_menu") !== null || document.querySelector(".my-account") !== null || document.querySelector(".member-menu") !== null || document.querySelector("a[href*='/es/account']") !== null;
                                             var isLoginForm = document.querySelector("form[action*='/account/login'] ") !== null;
                                             var isLoggedIn = hasLogout || isEsLogoutText || isEnLogoutText || memberMenu;
                                             if (isLoginForm) { isLoggedIn = false; }
                                             
                                            // --- Crop / Hide elements above "Marcado rápido" and below "mostrando" ---
                                             try {
                                                 // --- Polished Custom Extractor/Cleaner injected directly ---
                                                  (function() {
                                                      try {
                                                          // 1. Hide unwanted clutter selectors
                                                          var selectors = [
                                                              '#header', '#top_bar', '#top-bar', '#user_menu', '#top_menu', '#menu', 
                                                              '.header_container', '.user_menu', '#top_info_bar', '#breadcrumbs', 
                                                              '.breadcrumbs', '.site-header', '#masthead', '.top-nav', '#nav-top',
                                                              '#identity', '#user_bar', '.site-header-container', '#top-header',
                                                              '#footer', '.footer', '#bottom_bar', '#bottom-bar', '.site-footer',
                                                              '#social_media', '#copyright', '.copyright', '#lower_footer',
                                                              'form[action*="/account/login"]',
                                                              '.member_menu', '.my-account', '.member-menu', 'a[href*="/es/account"]',
                                                              '.items_co_update', '#items_co_update', '.co-update-form', '#co-update-form',
                                                              '#improve_catalog', '.improve_catalog', 'a[href*="/improve/"]',
                                                              'a[href*="ebay.com/sch/"]', '#ebay_similar_items', '.ebay_similar_items',
                                                              '.quick_marking', '#quick_marking', '#quick_select', '.quick_select',
                                                              'select[name="per_page"]', '#per_page', '.per_page',
                                                              'a[href*="/sell/"]', 'a[href*="/premium/"]', '.sell_articles',
                                                              '#filter_box', '.filter_box', '#search_box', '.search_box',
                                                              '#view_filter_header', '.view_filter_header', '#items_list_header', '.items_list_header'
                                                          ];
                                                          selectors.forEach(function(sel) {
                                                              try {
                                                                  document.querySelectorAll(sel).forEach(function(el) {
                                                                      if (el.style.display !== 'none') {
                                                                          el.style.setProperty('display', 'none', 'important');
                                                                      }
                                                                  });
                                                              } catch(e) {}
                                                          });

                                                          // 2. Hide unwanted static/leaf words/texts from non-item cells
                                                          var allEls = document.body.getElementsByTagName('*');
                                                          for (var i = 0; i < allEls.length; i++) {
                                                              var el = allEls[i];
                                                              try {
                                                                  if (el.children.length === 0 || (el.children.length <= 2 && (el.tagName === 'A' || el.tagName === 'BUTTON' || el.tagName === 'LABEL' || el.tagName === 'SPAN'))) {
                                                                      var text = (el.textContent || el.innerText || "").toLowerCase().trim();
                                                                      var shouldHide = false;
                                                                      
                                                                      if (text.indexOf("código de catálogo") !== -1 || text.indexOf("catalog code") !== -1 ||
                                                                          text.indexOf("búsqueda de imágenes") !== -1 || text.indexOf("image search") !== -1 ||
                                                                          text.indexOf("búsqueda avanzada") !== -1 || text.indexOf("advanced search") !== -1 ||
                                                                          text.indexOf("en venta") !== -1 || text.indexOf("for sale") !== -1 ||
                                                                          text.indexOf("vender estos artículos") !== -1 || text.indexOf("sell these items") !== -1 ||
                                                                          text.indexOf("vender este artículo") !== -1 || text.indexOf("sell this item") !== -1 ||
                                                                          text.indexOf("exportación") !== -1 || text.indexOf("export") !== -1 ||
                                                                          text.indexOf("marcado rápido") !== -1 || text.indexOf("quick marking") !== -1 ||
                                                                          text.indexOf("marcado selección") !== -1 || text.indexOf("quick select") !== -1 ||
                                                                          text.indexOf("mejora nuestro catálogo") !== -1 || text.indexOf("improve our catalog") !== -1 ||
                                                                          text.indexOf("encuentra ítems similares") !== -1 || text.indexOf("find similar items") !== -1 ||
                                                                          text.indexOf("colección") !== -1 || text.indexOf("intercambio") !== -1 || text.indexOf("deseo") !== -1) {
                                                                          
                                                                          var isInsideListItem = false;
                                                                          var parent = el.parentElement;
                                                                          while (parent && parent !== document.body) {
                                                                              var pCl = (parent.className && typeof parent.className === 'string') ? parent.className : "";
                                                                              var pId = parent.id || "";
                                                                              if (pCl.indexOf("item") !== -1 || pId.indexOf("item") !== -1) {
                                                                                  isInsideListItem = true;
                                                                                  break;
                                                                              }
                                                                              parent = parent.parentElement;
                                                                          }
                                                                          if (!isInsideListItem) { shouldHide = true; }
                                                                      }
                                                                      
                                                                      if (shouldHide) {
                                                                          el.style.setProperty('display', 'none', 'important');
                                                                          if (el.parentElement && el.parentElement.children.length === 1 && el.parentElement.tagName === 'DIV') {
                                                                              el.parentElement.style.setProperty('display', 'none', 'important');
                                                                          }
                                                                      }
                                                                  }
                                                              } catch(e) {}
                                                          }

                                                          // 3. Transform all navigation/paging grids to transparent, hiding showing info texts
                                                          var navs = document.querySelectorAll('.navigation, #navigation, .paging, #paging, .pagination, .pager');
                                                          navs.forEach(function(nv) {
                                                              try {
                                                                  nv.style.setProperty('background', 'transparent', 'important');
                                                                  nv.style.setProperty('background-color', 'transparent', 'important');
                                                                  nv.style.setProperty('background-image', 'none', 'important');
                                                                  nv.style.setProperty('border', 'none', 'important');
                                                                  nv.style.setProperty('box-shadow', 'none', 'important');
                                                                  
                                                                  var childNodes = nv.childNodes;
                                                                  for (var n = 0; n < childNodes.length; n++) {
                                                                      var node = childNodes[n];
                                                                      if (node.nodeType === Node.TEXT_NODE) {
                                                                          var val = node.nodeValue.toLowerCase().trim();
                                                                          if (val.indexOf("mostrando") !== -1 || val.indexOf("showing") !== -1 || val.indexOf("de ") !== -1 || val.indexOf("of ") !== -1) {
                                                                              node.nodeValue = ""; 
                                                                          }
                                                                      }
                                                                  }

                                                                  var kids = nv.getElementsByTagName('*');
                                                                  for (var k = 0; k < kids.length; k++) {
                                                                      var kid = kids[k];
                                                                      if (kid && kid.style) {
                                                                          var txt = (kid.textContent || kid.innerText || "").toLowerCase().trim();
                                                                          var isLink = kid.tagName === 'A' || kid.tagName === 'BUTTON' || kid.classList.contains('active') || kid.classList.contains('current');
                                                                          var isArrowOrNum = /^[0-9«»<>\s\-]+$/.test(txt) || txt === "siguiente" || txt === "anterior" || txt === "next" || txt === "prev";
                                                                          
                                                                          if (txt.indexOf("mostrando") !== -1 || txt.indexOf("showing") !== -1) {
                                                                              if (!isLink || !isArrowOrNum) {
                                                                                  kid.style.setProperty('display', 'none', 'important');
                                                                              }
                                                                          } else {
                                                                              kid.style.setProperty('background-color', 'transparent', 'important');
                                                                              kid.style.setProperty('background', 'transparent', 'important');
                                                                              kid.style.setProperty('border', 'none', 'important');
                                                                              kid.style.setProperty('box-shadow', 'none', 'important');
                                                                          }
                                                                      }
                                                                  }
                                                              } catch(e) {}
                                                          });
                                                      } catch(err) {}
                                                  })();
                                                  var staticToHide = [
                                                     '#header', '#top_bar', '#top-bar', '#user_menu', '#top_menu', '#menu', 
                                                     '.header_container', '.user_menu', '#top_info_bar', '#breadcrumbs', 
                                                     '.breadcrumbs', '.site-header', '#masthead', '.top-nav', '#nav-top',
                                                     '#identity', '#user_bar', '.site-header-container', '#top-header',
                                                     '#footer', '.footer', '#bottom_bar', '#bottom-bar', '.site-footer',
                                                     '#social_media', '#copyright', '.copyright', '#lower_footer'
                                                 ];
                                                 staticToHide.forEach(function(sel) {
                                                     try {
                                                         document.querySelectorAll(sel).forEach(function(el) {
                                                             el.style.display = 'none';
                                                         });
                                                     } catch(e) {}
                                                 });

                                                 // Dynamic transparent styling for all navigation/pagination areas (eliminates gray bars)
                                                 var navContainers = document.querySelectorAll('.navigation, #navigation, .paging, #paging, .pagination, .pager');
                                                 navContainers.forEach(function(nv) {
                                                     try {
                                                         nv.style.setProperty('background', 'transparent', 'important');
                                                         nv.style.setProperty('background-color', 'transparent', 'important');
                                                         nv.style.setProperty('background-image', 'none', 'important');
                                                         nv.style.setProperty('border', 'none', 'important');
                                                         nv.style.setProperty('box-shadow', 'none', 'important');
                                                         
                                                         var kids = nv.getElementsByTagName('*');
                                                         for (var k = 0; k < kids.length; k++) {
                                                             var kid = kids[k];
                                                             if (kid && kid.style) {
                                                                 kid.style.setProperty('background-color', 'transparent', 'important');
                                                                 kid.style.setProperty('background', 'transparent', 'important');
                                                                 kid.style.setProperty('border', 'none', 'important');
                                                                 kid.style.setProperty('box-shadow', 'none', 'important');
                                                             }
                                                         }
                                                     } catch(err) {}
                                                 });

                                                 var allElements = document.body.getElementsByTagName('*');
                                                 var mrEl = null;

                                                 // First priority: "Cambiar a la lista"
                                                 for (var i = 0; i < allElements.length; i++) {
                                                     var el = allElements[i];
                                                     if (el.children.length === 0 || el.tagName === 'A' || el.tagName === 'SPAN' || el.tagName === 'DIV') {
                                                         var text = (el.textContent || el.innerText || "").toLowerCase().trim();
                                                         if (text.indexOf("cambiar a la lista") !== -1 || 
                                                             text.indexOf("cambiar a lista") !== -1 ||
                                                             text.indexOf("switch to list") !== -1) {
                                                             mrEl = el;
                                                             break;
                                                         }
                                                     }
                                                 }

                                                 if (!mrEl) {
                                                     for (var i = 0; i < allElements.length; i++) {
                                                         var el = allElements[i];
                                                         if (el.children.length === 0) {
                                                             var text = (el.textContent || el.innerText || "").toLowerCase().trim();
                                                         if (text.indexOf("marcado rápido") !== -1 || 
                                                             text.indexOf("marcado rapido") !== -1 ||
                                                             text.indexOf("marcado selección") !== -1 ||
                                                             text.indexOf("marcado seleccion") !== -1 ||
                                                             text.indexOf("marcado clásico") !== -1 ||
                                                             text.indexOf("marcado clasico") !== -1 ||
                                                             text.indexOf("quick marking") !== -1 ||
                                                             text.indexOf("quick select") !== -1) {
                                                             mrEl = el;
                                                             break;
                                                         }
                                                     }
                                                 }

                                                 if (!mrEl) {
                                                     for (var i = 0; i < allElements.length; i++) {
                                                         var el = allElements[i];
                                                         var id = (el.id || "").toLowerCase();
                                                         var cl = (el.className && typeof el.className === 'string') ? el.className.toLowerCase() : "";
                                                         if (id.indexOf("quick_marking") !== -1 || cl.indexOf("quick_marking") !== -1 ||
                                                             id.indexOf("quick_select") !== -1 || cl.indexOf("quick_select") !== -1 ||
                                                             id.indexOf("quick-marking") !== -1 || cl.indexOf("quick-marking") !== -1) {
                                                             mrEl = el;
                                                             break;
                                                         }
                                                     }
                                                 }

                                                 if (mrEl) {
                                                     var curr = mrEl;
                                                     while (curr && curr.parentElement && curr.parentElement !== document.body) {
                                                         var sib = curr.previousElementSibling;
                                                         while (sib) {
                                                             var hasNav = sib.matches('.navigation, .paging, .pagination, .pager') || 
                                                                          sib.querySelector('.navigation, .paging, .pagination, .pager') !== null;
                                                             if (!hasNav) {
                                                                 if (sib.style.display !== 'none') {
                                                                     sib.style.setProperty('display', 'none', 'important');
                                                                 }
                                                             } else {
                                                                 // Strip background from navigation siblings too
                                                                 sib.style.setProperty('background-color', 'transparent', 'important');
                                                                 sib.style.setProperty('background', 'transparent', 'important');
                                                                 sib.style.setProperty('border', 'none', 'important');
                                                                 var subNavs = sib.querySelectorAll('.navigation, .paging, .pagination, .pager');
                                                                 subNavs.forEach(function(nv) {
                                                                     nv.style.setProperty('background-color', 'transparent', 'important');
                                                                     nv.style.setProperty('background', 'transparent', 'important');
                                                                     nv.style.setProperty('border', 'none', 'important');
                                                                 });
                                                             }
                                                             sib = sib.previousElementSibling;
                                                         }
                                                         curr = curr.parentElement;
                                                     }
                                                 }

                                                 var mostrandoEl = null;
                                                 for (var i = 0; i < allElements.length; i++) {
                                                     var el = allElements[i];
                                                     if (el.children.length === 0) {
                                                         var text = (el.textContent || el.innerText || "").toLowerCase().trim();
                                                         if (text.indexOf("mostrando") !== -1 || text.indexOf("showing") !== -1) {
                                                             mostrandoEl = el;
                                                         }
                                                     }
                                                 }

                                                 if (!mostrandoEl) {
                                                     for (var i = allElements.length - 1; i >= 0; i--) {
                                                         var el = allElements[i];
                                                         var id = (el.id || "").toLowerCase();
                                                         var cl = (el.className && typeof el.className === 'string') ? el.className.toLowerCase() : "";
                                                         if (id.indexOf("navigation") !== -1 || cl.indexOf("navigation") !== -1 ||
                                                             id.indexOf("pagination") !== -1 || cl.indexOf("pagination") !== -1 ||
                                                             id.indexOf("paging") !== -1 || cl.indexOf("paging") !== -1) {
                                                             mostrandoEl = el;
                                                             break;
                                                         }
                                                     }
                                                 }

                                                 if (mostrandoEl) {
                                                     var curr = mostrandoEl;
                                                     while (curr && curr.parentElement && curr.parentElement !== document.body) {
                                                         var sib = curr.nextElementSibling;
                                                         while (sib) {
                                                             var hasNav = sib.matches('.navigation, .paging, .pagination, .pager') || 
                                                                          sib.querySelector('.navigation, .paging, .pagination, .pager') !== null;
                                                             if (!hasNav) {
                                                                 if (sib.style.display !== 'none') {
                                                                     sib.style.setProperty('display', 'none', 'important');
                                                                 }
                                                             } else {
                                                                 // Strip background from navigation siblings too
                                                                 sib.style.setProperty('background-color', 'transparent', 'important');
                                                                 sib.style.setProperty('background', 'transparent', 'important');
                                                                 sib.style.setProperty('border', 'none', 'important');
                                                                 var subNavs = sib.querySelectorAll('.navigation, .paging, .pagination, .pager');
                                                                 subNavs.forEach(function(nv) {
                                                                     nv.style.setProperty('background-color', 'transparent', 'important');
                                                                     nv.style.setProperty('background', 'transparent', 'important');
                                                                     nv.style.setProperty('border', 'none', 'important');
                                                                 });
                                                             }
                                                             sib = sib.nextElementSibling;
                                                         }
                                                         curr = curr.parentElement;
                                                     }
                                                 }
                                             } catch(e) {}

                                             // --- Scroll position reporting logic ---
                                             try {
                                                 var sY = window.scrollY || window.pageYOffset || 0;
                                                 var mS = document.documentElement.scrollHeight - window.innerHeight;
                                                 ColnectAppInterface.reportScroll(sY, mS);

                                                 if (!window.scrollListenerBound) {
                                                     window.scrollListenerBound = true;
                                                     window.addEventListener('scroll', function() {
                                                         var sy = window.scrollY || window.pageYOffset || 0;
                                                         var ms = document.documentElement.scrollHeight - window.innerHeight;
                                                         ColnectAppInterface.reportScroll(sy, ms);
                                                     });
                                                 }
                                             } catch(e) {}
                                             
                                             // Remove blank targets so links load inside our WebView
                                             try { 
                                                 document.querySelectorAll('a[target="_blank"]').forEach(function(el) { 
                                                     el.removeAttribute('target'); 
                                                 }); 
                                             } catch(e) {}

                                             // --- 2. Dynamic Scrapers (Only run when location.href changes or on initial run) ---
                                             if (window.lastScrapedUrl !== location.href) {
                                                 window.lastScrapedUrl = location.href;
                                                 
                                                 // --- Country Scraper ---
                                                 var resultadosC = [];
                                                 document.querySelectorAll('a').forEach(function(enlace) {
                                                     var url = enlace.href;
                                                     if (url && url.indexOf('/country/') !== -1 && url.indexOf('/page/') === -1) {
                                                         var textoOriginal = enlace.innerText.trim().replace(/\n/g, ' ');
                                                         var textoLimpio = textoOriginal.replace(/\s*\([^)]*\)$/, '').trim();
                                                         var parts = url.split('/country/');
                                                         if (parts.length > 1) {
                                                             var fragmentoUrl = parts[1].split(/[?#]/)[0];
                                                             if (fragmentoUrl && textoLimpio !== "" && fragmentoUrl.indexOf('/') === -1) {
                                                                 resultadosC.push(textoLimpio + "\t" + fragmentoUrl);
                                                             }
                                                         }
                                                     }
                                                 });
                                                 if (resultadosC.length > 0) {
                                                     var uniqueC = [...new Set(resultadosC)];
                                                     ColnectAppInterface.onCountriesScraped(uniqueC.join('\n'));
                                                 } else {
                                                     ColnectAppInterface.onCountriesScraped("");
                                                 }

                                                 // --- Face Value Scraper ---
                                                 var resultadosFv = [];
                                                 document.querySelectorAll('a').forEach(function(enlace) {
                                                     var url = enlace.href;
                                                     if (url && url.indexOf('/face_value/') !== -1 && url.indexOf('/page/') === -1) {
                                                         var textoOriginal = enlace.innerText.trim().replace(/\n/g, ' ');
                                                         var textoLimpio = textoOriginal.replace(/\s*\([^)]*\)$/, '').trim();
                                                         var parts = url.split('/face_value/');
                                                         if (parts.length > 1) {
                                                             var fragmentoUrl = parts[1].split(/[?#]/)[0];
                                                             if (fragmentoUrl && textoLimpio !== "" && fragmentoUrl.indexOf('/') === -1) {
                                                                 resultadosFv.push(textoLimpio + "\t" + fragmentoUrl);
                                                             }
                                                         }
                                                     }
                                                 });
                                                 if (resultadosFv.length > 0) {
                                                     var uniqueFv = [...new Set(resultadosFv)];
                                                     ColnectAppInterface.onFaceValuesScraped(uniqueFv.join('\n'));
                                                 } else {
                                                     ColnectAppInterface.onFaceValuesScraped("");
                                                 }

                                                 // --- Currency Scraper ---
                                                 var resultadosComp = [];
                                                 try {
                                                     var composicionesMap = new Map();
                                                     var enlaces = document.querySelectorAll('a[href*="/composition/"]');
                                                     enlaces.forEach(function(a) {
                                                         try {
                                                             var clone = a.cloneNode(true);
                                                             var spans = clone.querySelectorAll('span');
                                                             spans.forEach(function(span) { span.remove(); });
                                                             var textoBruto = clone.textContent.trim();
                                                             if (textoBruto) {
                                                                 var textoLimpio = textoBruto.split('-')[0].trim();
                                                                 textoLimpio = textoLimpio.replace(/\s*\([^)]*\)$/, '').trim();
                                                                 var urlPart = a.href.split('/').pop().split(/[?#]/)[0];
                                                                 if (textoLimpio && urlPart && urlPart.indexOf('/') === -1) {
                                                                     if (!composicionesMap.has(textoLimpio)) {
                                                                         composicionesMap.set(textoLimpio, urlPart);
                                                                     }
                                                                 }
                                                             }
                                                         } catch(err) {}
                                                     });
                                                     var divisasList = Array.from(composicionesMap, function(pair) { return [pair[0], pair[1]]; });
                                                     divisasList.sort(function(a, b) { return a[0].localeCompare(b[0]); });
                                                     divisasList.forEach(function(fila) {
                                                         resultadosComp.push(fila[0] + "\t" + fila[1]);
                                                     });
                                                 } catch(err) {}
                                                 if (resultadosComp.length > 0) {
                                                     var uniqueCur = [...new Set(resultadosComp)];
                                                     ColnectAppInterface.onCompositionsScraped(uniqueCur.join('\n'));
                                                  } else {
                                                      ColnectAppInterface.onCompositionsScraped("");
                                                  }

                                                  // --- Currencies (Denominations) Scraper ---
                                                  var resultadosCurr = [];
                                                  try {
                                                      var divisasMap = new Map();
                                                      var enlacesDiv = document.querySelectorAll('a[href*="/currency/"]');
                                                      enlacesDiv.forEach(function(a) {
                                                          try {
                                                              var clone = a.cloneNode(true);
                                                              var spans = clone.querySelectorAll('span');
                                                              spans.forEach(function(span) { span.remove(); });
                                                              var textoBruto = clone.textContent.trim();
                                                              if (textoBruto) {
                                                                  textoBruto = textoBruto.replace(/\s*\([^)]*\)$/, '').trim();
                                                                  var lastHyphenIdx = textoBruto.lastIndexOf('-');
                                                                  var textoLimpio = textoBruto;
                                                                  if (lastHyphenIdx !== -1) {
                                                                      textoLimpio = textoBruto.substring(0, lastHyphenIdx).trim();
                                                                  }
                                                                  var urlPart = a.href.split('/').pop().split(/[?#]/)[0];
                                                                  if (textoLimpio && urlPart && urlPart.indexOf('/') === -1) {
                                                                      if (!divisasMap.has(textoLimpio)) {
                                                                          divisasMap.set(textoLimpio, urlPart);
                                                                      }
                                                                  }
                                                              }
                                                          } catch(err) {}
                                                      });
                                                      var dList = Array.from(divisasMap, function(pair) { return [pair[0], pair[1]]; });
                                                      dList.sort(function(a, b) { return a[0].localeCompare(b[0]); });
                                                      dList.forEach(function(fila) {
                                                          resultadosCurr.push(fila[0] + "\t" + fila[1]);
                                                      });
                                                  } catch(err) {}
                                                  if (resultadosCurr.length > 0) {
                                                      var uniqueCurr = [...new Set(resultadosCurr)];
                                                      ColnectAppInterface.onCurrenciesScraped(uniqueCurr.join('\n'));
                                                  } else {
                                                      ColnectAppInterface.onCurrenciesScraped("");
                                                  }
                                              }
                                        } catch(e) {}
                                        setTimeout(check, 2500);
                                    })()
                                """.trimIndent()

                                // Infinite interval check injected immediately which guarantees instant login updates instantly 
                                val selfLoopingJs = "(function check() {\n" +
                                        "    try {\n" +
                                        "        try { document.querySelectorAll('a[target=\"_blank\"]').forEach(function(el) { el.removeAttribute('target'); }); } catch(e) {}\n" +
                                        "        var hasLogout = document.querySelector(\"a[href*='/account/logout']\") !== null || document.querySelector(\"a[href*='logout']\") !== null;\n" +
                                        "        var isEsLogoutText = document.body.innerText.indexOf(\"Cerrar sesión\") !== -1 || document.body.innerText.indexOf(\"Cierre de sesión\") !== -1 || document.body.innerText.indexOf(\"Salir\") !== -1;\n" +
                                        "        var isEnLogoutText = document.body.innerText.indexOf(\"Logout\") !== -1 || document.body.innerText.indexOf(\"Log out\") !== -1 || document.body.innerText.indexOf(\"Sign out\") !== -1;\n" +
                                        "        var memberMenu = document.querySelector(\".member_menu\") !== null || document.querySelector(\".my-account\") !== null || document.querySelector(\".member-menu\") !== null || document.querySelector(\"a[href*='/es/account']\") !== null;\n" +
                                        "        var isLoginForm = document.querySelector(\"form[action*='/account/login']\") !== null;\n" +
                                        "        var isLoggedIn = hasLogout || isEsLogoutText || isEnLogoutText || memberMenu;\n" +
                                        "        if (isLoginForm) { isLoggedIn = false; }\n" +
                                        "        ColnectAppInterface.reportLoginStatus(isLoggedIn);\n" +
                                        "\n" +
                                        "        // --- Country Scraper ---\n" +
                                        "        var resultadosC = [];\n" +
                                        "        document.querySelectorAll('a').forEach(function(enlace) {\n" +
                                        "            var url = enlace.href;\n" +
                                        "            if (url && url.indexOf('/country/') !== -1 && url.indexOf('/page/') === -1) {\n" +
                                        "                var textoOriginal = enlace.innerText.trim().replace(/\\n/g, ' ');\n" +
                                        "                var textoLimpio = textoOriginal.replace(/\\s*\\([^)]*\\)$/, '').trim();\n" +
                                        "                var parts = url.split('/country/');\n" +
                                        "                if (parts.length > 1) {\n" +
                                        "                    var fragmentoUrl = parts[1].split(/[?#]/)[0];\n" +
                                        "                    if (fragmentoUrl && textoLimpio !== \"\" && fragmentoUrl.indexOf('/') === -1) {\n" +
                                        "                        resultadosC.push(textoLimpio + \"\\t\" + fragmentoUrl);\n" +
                                        "                    }\n" +
                                        "                }\n" +
                                        "            }\n" +
                                        "        });\n" +
                                        "        if (resultadosC.length > 12) {\n" +
                                        "            var uniqueC = [...new Set(resultadosC)];\n" +
                                        "            ColnectAppInterface.onCountriesScraped(uniqueC.join('\\n'));\n" +
                                        "        } else {\n" +
                                        "            ColnectAppInterface.onCountriesScraped(\"\");\n" +
                                        "        }\n" +
                                        "\n" +
                                        "        // --- Face Value Scraper ---\n" +
                                        "        var resultadosFv = [];\n" +
                                        "        document.querySelectorAll('a').forEach(function(enlace) {\n" +
                                        "            var url = enlace.href;\n" +
                                        "            if (url && url.indexOf('/face_value/') !== -1 && url.indexOf('/page/') === -1) {\n" +
                                        "                var textoOriginal = enlace.innerText.trim().replace(/\\n/g, ' ');\n" +
                                        "                var textoLimpio = textoOriginal.replace(/\\s*\\([^)]*\\)$/, '').trim();\n" +
                                        "                var parts = url.split('/face_value/');\n" +
                                        "                if (parts.length > 1) {\n" +
                                        "                    var fragmentoUrl = parts[1].split(/[?#]/)[0];\n" +
                                        "                    if (fragmentoUrl && textoLimpio !== \"\" && fragmentoUrl.indexOf('/') === -1) {\n" +
                                        "                        resultadosFv.push(textoLimpio + \"\\t\" + fragmentoUrl);\n" +
                                        "                    }\n" +
                                        "                }\n" +
                                        "            }\n" +
                                        "        });\n" +
                                        "        if (resultadosFv.length > 12) {\n" +
                                        "            var uniqueFv = [...new Set(resultadosFv)];\n" +
                                        "            ColnectAppInterface.onFaceValuesScraped(uniqueFv.join('\\n'));\n" +
                                        "        } else {\n" +
                                        "            ColnectAppInterface.onFaceValuesScraped(\"\");\n" +
                                        "        }\n" +
                                        "\n" +
                                        "        // --- Currency Scraper (Dynamic extraction based on clean user algorithm) ---\n" +
                                        "        var resultadosComp = [];\n" +
                                        "        try {\n" +
                                        "            var composicionesMap = new Map();\n" +
                                        "            var enlaces = document.querySelectorAll('a[href*=\"/composition/\"]');\n" +
                                        "            enlaces.forEach(function(a) {\n" +
                                        "                try {\n" +
                                        "                    var clone = a.cloneNode(true);\n" +
                                        "                    var spans = clone.querySelectorAll('span');\n" +
                                        "                    spans.forEach(function(span) { span.remove(); });\n" +
                                        "                    var textoBruto = clone.textContent.trim();\n" +
                                        "                    if (textoBruto) {\n" +
                                        "                        var textoLimpio = textoBruto.split('-')[0].trim();\n" +
                                        "                        textoLimpio = textoLimpio.replace(/\\s*\\([^)]*\\)$/, '').trim();\n" +
                                        "                        var urlPart = a.href.split('/').pop().split(/[?#]/)[0];\n" +
                                        "                        if (textoLimpio && urlPart && urlPart.indexOf('/') === -1) {\n" +
                                        "                            if (!composicionesMap.has(textoLimpio)) {\n" +
                                        "                                composicionesMap.set(textoLimpio, urlPart);\n" +
                                        "                            }\n" +
                                        "                        }\n" +
                                        "                    }\n" +
                                        "                } catch(err) {}\n" +
                                        "            });\n" +
                                        "            var divisasList = Array.from(composicionesMap, function(pair) { return [pair[0], pair[1]]; });\n" +
                                        "            divisasList.sort(function(a, b) { return a[0].localeCompare(b[0]); });\n" +
                                        "            divisasList.forEach(function(fila) {\n" +
                                        "                resultadosComp.push(fila[0] + \"\\t\" + fila[1]);\n" +
                                        "                \n" +
                                        "            });\n" +
                                        "        } catch(err) {}\n" +
                                        "        if (resultadosComp.length > 0) {\n" +
                                        "            var uniqueCur = [...new Set(resultadosComp)];\n" +
                                        "            ColnectAppInterface.onCompositionsScraped(uniqueCur.join('\\n'));\n" +
                                        "        } else {\n" +
                                        "            ColnectAppInterface.onCompositionsScraped(\"\");\n" +
                                        "        }\n" +
                                        "    } catch(e) {}\n" +
                                        "    // Run once per page loaded to prevent multiple timer loops from freezing the Colnect UI\n" +
                                        "})()"
                                val discardedJsMarker = """
                                    (function check() {
                                        try {
                                            var hasLogout = document.querySelector("a[href*='/account/logout']") !== null || 
                                                            document.querySelector("a[href*='logout']") !== null;
                                            var isEsLogoutText = document.body.innerText.indexOf("Cerrar sesión") !== -1 || 
                                                                 document.body.innerText.indexOf("Cierre de sesión") !== -1 || 
                                                                 document.body.innerText.indexOf("Salir") !== -1;
                                            var isEnLogoutText = document.body.innerText.indexOf("Logout") !== -1 || 
                                                                 document.body.innerText.indexOf("Log out") !== -1 || 
                                                                 document.body.innerText.indexOf("Sign out") !== -1;
                                            var memberMenu = document.querySelector(".member_menu") !== null || 
                                                             document.querySelector(".my-account") !== null || 
                                                             document.querySelector(".member-menu") !== null ||
                                                             document.querySelector("a[href*='/es/account']") !== null;
                                            var isLoginForm = document.querySelector("form[action*='/account/login']") !== null;

                                            var isLoggedIn = hasLogout || isEsLogoutText || isEnLogoutText || memberMenu;
                                            if (isLoginForm) {
                                                isLoggedIn = false;
                                            }
                                            ColnectAppInterface.reportLoginStatus(isLoggedIn);
                                        } catch(e) {
                                            ColnectAppInterface.reportLoginStatus(false);
                                        }
                                        setTimeout(check, 3000);
                                    })()
                                """.trimIndent()

                                 view?.evaluateJavascript(deprecatedJs3, null)
                            }

                            override fun shouldOverrideUrlLoading(view: WebView?, request: android.webkit.WebResourceRequest?): Boolean {
                                val destinationUrl = request?.url?.toString() ?: ""
                                val u = destinationUrl.trim().lowercase().removeSuffix("/")
                                if (u == "https://colnect.com" || u == "https://colnect.com/es") {
                                    view?.loadUrl("https://colnect.com/es/coins")
                                    return true
                                }
                                if (u.startsWith("https://colnect.com") && !u.contains("/coins") && !destinationUrl.contains("/account/") && !destinationUrl.contains("/user/")) {
                                    view?.loadUrl("https://colnect.com/es/coins")
                                    return true
                                }
                                if (destinationUrl.startsWith("http://") || destinationUrl.startsWith("https://") || destinationUrl.startsWith("javascript:") || destinationUrl.startsWith("data:") || destinationUrl.startsWith("#")) {
                                    return false // Let WebView render inline
                                }
                                if (destinationUrl.startsWith("mailto:") || destinationUrl.startsWith("tel:") || destinationUrl.startsWith("intent:")) {
                                    try {
                                        val intent = Intent.parseUri(destinationUrl, Intent.URI_INTENT_SCHEME)
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // Ignore
                                    }
                                    return true
                                }
                                return false
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                loadProgress = newProgress
                                view?.url?.let { webUrl = it }
                                view?.title?.let { if (it.isNotEmpty()) webTitle = it }
                                canGoBack = canWebViewGoBackSafely(view)
                                canGoForward = view?.canGoForward() ?: false
                                runLoginCheck()

                                if (newProgress > 25) {
                                    view?.evaluateJavascript("""
                                        (function() {
                                            var css = `
                                                html, body, #main, #page_wrap, .page_wrap, #main_content, .content_wrapper, .page_container, #content, #content_main, .main_content {
                                                  padding-top: 0 !important;
                                                  margin-top: 0 !important;
                                              }
                                              #main, #page_wrap, .page_wrap, #main_content, .content_wrapper, .page_container, #content {
                                                    padding-top: 0 !important;
                                                    margin-top: 0 !important;
                                                    min-height: 0 !important;
                                                    height: auto !important;
                                                }
                                                header, footer, #header, .header, #footer, .footer, 
                                                #top_bar, #top-bar, .top-bar, .header_container, 
                                                #top_info_bar, #breadcrumbs, .breadcrumbs, 
                                                .site-header, #masthead, .top-nav, #nav-top, 
                                                #user_bar, .site-header-container, #top-header, 
                                                #bottom_bar, #bottom-bar, .site-footer, #social_media, 
                                                #copyright, .copyright, #lower_footer,
                                                #improve_catalog, .improve_catalog, a[href*="/improve/"], 
                                                a[href*="ebay.com/sch/"], #ebay_similar_items, .ebay_similar_items,
                                                .item_sell_box, a[href*="/sell/"], a[href*="/premium/"], .sell_articles,
                                                .plumb_box, .ad_box, .ads, [class*="ad-"], [id*="ad-"],
                                                #main_content_headers { 
                                                    display: none !important; 
                                                }
                                                .quick_marking, .item_co_update, .co_update, [class*="co_update"], [class*="marking"], 
                                                form, .item_marking, .marking_box {
                                                    height: auto !important;
                                                    max-height: none !important;
                                                    min-height: 0 !important;
                                                    overflow: visible !important;
                                                    line-height: normal !important;
                                                }
                                                .quick_marking label, .co-update-form label, .item_marking label,
                                              .quick_marking span, .co-update-form span, .item_marking span,
                                              .quick_marking p, .co-update-form p, .item_marking p {
                                                    height: auto !important;
                                                    max-height: none !important;
                                                    line-height: 1.25 !important;
                                                    overflow: visible !important;
                                                    word-break: break-word !important;
                                                }
                                            `;
                                            var parent = document.head || document.documentElement;
                                            if (parent) {
                                                var style = document.getElementById('colnect-visor-override-style');
                                                if (!style) {
                                                    style = document.createElement('style');
                                                    style.id = 'colnect-visor-override-style';
                                                    style.type = 'text/css';
                                                    style.innerHTML = css;
                                                    parent.appendChild(style);
                                                } else if (style.innerHTML !== css) {
                                                    style.innerHTML = css;
                                                }
                                            }
                                        })();
                                    """.trimIndent(), null)
                                }
                            }

                            override fun onReceivedTitle(view: WebView?, title: String?) {
                                super.onReceivedTitle(view, title)
                                title?.let { if (it.isNotEmpty()) webTitle = it }
                            }
                        }

                        webViewInstance = this
                        loadUrl(url)
                    }
                },
                update = { webView ->
                    webViewInstance = webView
                }
            )
        }

        if (loadProgress < 100) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.95f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF4F46E5),
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(42.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Conectando con Colnect...",
                        color = Color(0xFF475569),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// --------------------------------------------------------------------
// Launch / Open URL helper
// --------------------------------------------------------------------
fun openColnectUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(Browser.EXTRA_APPLICATION_ID, context.packageName)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "No se encontró ningún navegador disponible", Toast.LENGTH_SHORT).show()
    }
}
