package com.example
/**
 * Feature: Reusable UI Components
 * Description: Individual UI elements like inputs, logos, and custom UI components.
 * Use Cases: Composing complex screens by reusing smaller, tested visual building blocks.
 */


import android.content.Context
import android.net.Uri
import android.webkit.WebView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun DiagnosticItemRow(label: String, current: Int, target: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1E293B)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$current",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (current >= target) Color(0xFF059669) else Color(0xFFD97706)
                )
                Text(
                    text = "/",
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8)
                )
                Text(
                    text = "$target",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B)
                )
            }
            
            Icon(
                imageVector = if (current >= target) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (current >= target) Color(0xFF10B981) else Color(0xFFF59E0B),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun ValidatedInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    validationState: ValidationState,
    placeholder: String,
    suggestions: List<ColnectItem>,
    onSuggestionClicked: (ColnectItem) -> Unit,
    keyboardOptions: KeyboardOptions,
    testTag: String,
    isExpanded: Boolean = false,
    expandedHeight: androidx.compose.ui.unit.Dp = 53.dp, // Compact default height (increased 20%)
    isDarkMode: Boolean = false,
    onFocusChanged: (Boolean) -> Unit = {}
) {
    var isFocused by remember { mutableStateOf(false) }
    var fieldWidthDp by remember { mutableStateOf(0.dp) }

    // Animate height with a custom specific spring physics per field (damping and stiffness variation for a "randomized" playful dynamic feel)
    val currentHeight by animateDpAsState(
        targetValue = if (isExpanded) expandedHeight else 41.dp, // Ultra-compact non-expanded height (previously 34.dp, increased by 20% to 41.dp)
        animationSpec = spring(
            dampingRatio = when (testTag) {
                "country_input" -> 0.5f
                "face_value_input" -> 0.65f
                "year_input" -> 0.55f
                else -> 0.6f
            },
            stiffness = when (testTag) {
                "country_input" -> 110f
                "face_value_input" -> 190f
                "year_input" -> 140f
                else -> 170f
            }
        ),
        label = "heightAnimation"
    )

    val borderColor = when (validationState) {
        ValidationState.Neutral -> {
            if (isFocused) {
                if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF4F46E5)
            } else {
                if (isDarkMode) Color(0xFF3F3F46) else Color(0xFFCBD5E1)
            }
        }
        ValidationState.Valid -> Color(0xFF10B981)
        ValidationState.Invalid -> Color(0xFFEF4444)
    }

    val internalBg = when (validationState) {
        ValidationState.Neutral -> if (isDarkMode) Color(0xFF27272A) else Color(0xFFF8FAFC)
        ValidationState.Valid -> if (isDarkMode) Color(0xFF064E3B) else Color(0xFFECFDF5)
        ValidationState.Invalid -> if (isDarkMode) Color(0xFF7F1D1D) else Color(0xFFFEF2F2)
    }

    val textColor = if (isDarkMode) Color(0xFFF8FAFC) else Color.Black
    val placeholderColor = if (isDarkMode) Color(0xFFA1A1AA) else Color(0xFF64748B)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .focusable(false)
    ) {
        Text(
            text = label.uppercase(),
            modifier = Modifier
                .padding(start = 4.dp, bottom = 1.dp), // Tiny bottom padding for space efficiency
            fontWeight = FontWeight.Bold,
            color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF94A3B8),
            fontSize = 9.5.sp, // Slightly more compact label
            letterSpacing = 0.8.sp
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            val density = LocalDensity.current
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(currentHeight) // Animated height
                    .shadow(
                        elevation = if (isDarkMode) 2.dp else 5.dp,
                        shape = RoundedCornerShape(10.dp),
                        clip = false,
                        ambientColor = if (isDarkMode) Color.Black else Color(0xFF1E293B).copy(alpha = 0.12f),
                        spotColor = if (isDarkMode) Color.Black else Color(0xFF1E293B).copy(alpha = 0.16f)
                    )
                    .background(internalBg, RoundedCornerShape(10.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                    .onGloballyPositioned { coordinates ->
                        fieldWidthDp = with(density) { coordinates.size.width.toDp() }
                    },
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                color = placeholderColor,
                                fontSize = 11.sp, // Slightly compact to guarantee it never overflows in dual layout
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        androidx.compose.foundation.text.BasicTextField(
                            value = value,
                            onValueChange = onValueChange,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            ),
                            singleLine = true,
                            keyboardOptions = keyboardOptions,
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { state ->
                                    isFocused = state.isFocused
                                    onFocusChanged(state.isFocused)
                                }
                                .testTag(testTag)
                        )
                    }
                    if (value.isNotEmpty()) {
                        IconButton(
                            onClick = { onValueChange("") },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Borrar campo",
                                tint = if (isDarkMode) Color(0xFFA1A1AA) else Color(0xFF475569),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }

            val maxSuggestions = suggestions.take(4)
            if (isFocused && value.isNotEmpty() && maxSuggestions.isNotEmpty()) {
                val keyboardPadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
                val isKeyboardOpen = keyboardPadding > 0.dp
                
                // Custom alignment rules: 
                // 1) País list (testTag == "country_input") ALWAYS deploys downwards.
                // 2) Valor Facial list (testTag == "face_value_input") deploys downwards normally, but deploys upwards when keyboard is open to avoid overlapping.
                val forceDownward = testTag == "country_input"
                val deployDownward = forceDownward || !isKeyboardOpen
                
                val positionProvider = remember(deployDownward, density) {
                    object : androidx.compose.ui.window.PopupPositionProvider {
                        override fun calculatePosition(
                            anchorBounds: androidx.compose.ui.unit.IntRect,
                            windowSize: androidx.compose.ui.unit.IntSize,
                            layoutDirection: androidx.compose.ui.unit.LayoutDirection,
                            popupContentSize: androidx.compose.ui.unit.IntSize
                        ): androidx.compose.ui.unit.IntOffset {
                            val x = anchorBounds.left
                            val y = if (deployDownward) {
                                anchorBounds.bottom + with(density) { 2.dp.roundToPx() }
                            } else {
                                anchorBounds.top - popupContentSize.height - with(density) { 3.dp.roundToPx() }
                            }
                            return androidx.compose.ui.unit.IntOffset(x, y)
                        }
                    }
                }

                Popup(
                    popupPositionProvider = positionProvider,
                    properties = PopupProperties(
                        focusable = false,
                        dismissOnClickOutside = true,
                        dismissOnBackPress = true
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .width(fieldWidthDp)
                            .shadow(
                                3.dp,
                                RoundedCornerShape(12.dp),
                                ambientColor = Color.Black.copy(alpha = 0.15f),
                                spotColor = Color.Black.copy(alpha = 0.2f)
                            )
                            .background(Color(0xFFFFFDF5), RoundedCornerShape(12.dp))
                            .padding(3.dp),
                        verticalArrangement = Arrangement.spacedBy(1.5.dp) // Spaced by 1 to 2 pixels vertically
                    ) {
                        maxSuggestions.forEach { item ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFFFDF5), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFFB45309).copy(alpha = 0.22f), RoundedCornerShape(8.dp)) // softer option dividing border
                                    .clickable { onSuggestionClicked(item) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp) // perfect mobile/tablet touch area
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Sugerencia",
                                        tint = Color(0xFFB45309), // Lighter warm amber tint
                                        modifier = Modifier.size(11.dp) // Proportional smaller icon
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = item.name,
                                        color = Color(0xFF78350F), // Rich dark contrast color
                                        fontSize = 11.sp, // Slightly compact and neat size
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 13.sp // Proportional linegap
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --------------------------------------------------------------------
// 4. Custom Pulsing Footer Logo of Colnect (With precise "COLLECT CONNECT" blue styling)
// --------------------------------------------------------------------
private enum class LogoAnimType {
    SLIDE,
    SPACING,
    LENS_ZOOM
}

@Composable
fun ColnectLogo(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "logoAnimation")
    
    // Randomized cycle time (duration) remembered across recompositions
    val animDuration = remember { (3000..4150).random() }
    
    // Smooth angle from 0 to 2*PI driving unified circular movement
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(animDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "lupaAngle"
    )

    // Dynamic radar search wave animation progress for the magnifying glass
    val searchWaveProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2080, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "searchWaveProgress"
    )

    // Determine orbit direction randomly on startup (1f for clockwise, -1f for counter-clockwise)
    val orbitDirection = remember { if ((0..1).random() == 1) 1f else -1f }

    // Interactively Toggle-able Animation mode, randomized on startup!
    // 0 = Slide (words shift horizontally)
    // 1 = Spacing Expansion (letter spacing expands and contracts)
    // 2 = Lens Hover Zoom (suggested: words scale up dynamically as magnifier approaches them)
    // 3 = Letter-by-letter Zoom Wave (individual sequential letters wave scale + interactive touch bouncy feedback)
    // 4 = Rainbow Hue Cycle + Breathing Scale
    // 5 = Vertical Hop (Bouncy Jump sequential letters)
    // 6 = 3D Y-Axis Flip (letter-by-letter spin)
    var animTypeIndex by remember { mutableStateOf((0..6).random()) }

    // Coordinates for the orbiting magnifying glass (unified circular motion)
    val hoverX = orbitDirection * kotlin.math.sin(angle.toDouble()).toFloat() * 4.5f
    val hoverY = -kotlin.math.cos(angle.toDouble()).toFloat() * 4.5f

    // Compute animation-specific variables dynamically
    val liveLetterSpacing: androidx.compose.ui.unit.TextUnit
    val collectShiftX: Float
    val connectShiftX: Float
    val collectScaleX: Float
    val collectScaleY: Float
    val connectScaleX: Float
    val connectScaleY: Float
    val collectShadow: Shadow
    val connectShadow: Shadow

    val shadowColor = Color(0x3B000000)

    when (animTypeIndex) {
        0 -> { // 1. SLIDE MODE
            liveLetterSpacing = 1.1.sp
            val textShift = -kotlin.math.cos(angle.toDouble()).toFloat() * 4.5f
            collectShiftX = textShift
            connectShiftX = -textShift
            collectScaleX = 1.0f
            collectScaleY = 1.35f
            connectScaleX = 1.0f
            connectScaleY = 1.35f

            collectShadow = Shadow(
                color = shadowColor,
                offset = Offset(
                    x = -textShift * 0.4f,
                    y = 2.0f + kotlin.math.cos(angle.toDouble()).toFloat() * 1.0f
                ),
                blurRadius = 3.5f
            )
            connectShadow = Shadow(
                color = shadowColor,
                offset = Offset(
                    x = textShift * 0.4f,
                    y = 2.0f + kotlin.math.cos(angle.toDouble()).toFloat() * 1.0f
                ),
                blurRadius = 3.5f
            )
        }
        1 -> { // 2. SPACING MODE
            val letterExpansionFactor = kotlin.math.cos(angle.toDouble()).toFloat()
            liveLetterSpacing = (1.4f + letterExpansionFactor * 1.3f).sp
            collectShiftX = 0f
            connectShiftX = 0f
            collectScaleX = 1.0f
            collectScaleY = 1.35f
            connectScaleX = 1.0f
            connectScaleY = 1.35f

            collectShadow = Shadow(
                color = shadowColor,
                offset = Offset(
                    x = -letterExpansionFactor * 1.5f,
                    y = 2.0f + letterExpansionFactor * 1.0f
                ),
                blurRadius = 3.5f
            )
            connectShadow = Shadow(
                color = shadowColor,
                offset = Offset(
                    x = letterExpansionFactor * 1.5f,
                    y = 2.0f + letterExpansionFactor * 1.0f
                ),
                blurRadius = 3.5f
            )
        }
        2 -> { // 3. LENS HOVER ZOOM MODE (Creative alternative!)
            liveLetterSpacing = 1.1.sp
            collectShiftX = 0f
            connectShiftX = 0f

            // Calculate dynamic glass zoom based on horizontal proximity of hoverX
            val collectsNear = hoverX < 0f
            val connectsNear = hoverX > 0f
            val collectZoomFactor = if (collectsNear) 1.0f + (-hoverX / 4.5f) * 0.16f else 1.0f
            val connectZoomFactor = if (connectsNear) 1.0f + (hoverX / 4.5f) * 0.16f else 1.0f

            collectScaleX = collectZoomFactor
            collectScaleY = 1.35f * collectZoomFactor
            connectScaleX = connectZoomFactor
            connectScaleY = 1.35f * connectZoomFactor

            collectShadow = Shadow(
                color = shadowColor,
                offset = Offset(
                    x = -1.0f - (collectZoomFactor - 1.0f) * 2.5f,
                    y = 2.0f + (collectZoomFactor - 1.0f) * 3.0f
                ),
                blurRadius = 3.5f + (collectZoomFactor - 1.0f) * 4.0f
            )
            connectShadow = Shadow(
                color = shadowColor,
                offset = Offset(
                    x = 1.0f + (connectZoomFactor - 1.0f) * 2.5f,
                    y = 2.0f + (connectZoomFactor - 1.0f) * 3.0f
                ),
                blurRadius = 3.5f + (connectZoomFactor - 1.0f) * 4.0f
            )
        }
        else -> { // 4. LETTER-BY-LETTER ZOOM WAVE MODE
            liveLetterSpacing = 1.1.sp
            collectShiftX = 0f
            connectShiftX = 0f
            collectScaleX = 1.0f
            collectScaleY = 1.35f
            connectScaleX = 1.0f
            connectScaleY = 1.35f
            collectShadow = Shadow(color = shadowColor, offset = Offset(0f, 2f), blurRadius = 3.5f)
            connectShadow = Shadow(color = shadowColor, offset = Offset(0f, 2f), blurRadius = 3.5f)
        }
    }

    val spaceLeftDp = if (animTypeIndex == 2) {
        (2.0f - (hoverX * 0.5f) + 17f * (collectScaleX - 1.0f)).coerceAtLeast(2.0f).dp
    } else {
        6.25.dp
    }

    val spaceRightDp = if (animTypeIndex == 2) {
        (2.0f + (hoverX * 0.5f) + 17f * (connectScaleX - 1.0f)).coerceAtLeast(2.0f).dp
    } else {
        6.25.dp
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Choose randomly from any of the OTHER available animations!
                val available = (0..6).filter { it != animTypeIndex }
                animTypeIndex = available.random()
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // First part: 'COLLECT' - styled blue (or rainbow!)
            if (animTypeIndex >= 3) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    "COLLECT".forEachIndexed { index, char ->
                        var clickedExtraScale by remember { mutableStateOf(1f) }
                        val animatedClickedExtraScale by animateFloatAsState(
                            targetValue = clickedExtraScale,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            finishedListener = { clickedExtraScale = 1f },
                            label = "charClickedScale"
                        )

                        // 1. Mode 3: Wave Scale
                        val activePointer = (angle / (2.0 * java.lang.Math.PI)) * 14.0
                        val dist = kotlin.math.min(
                            kotlin.math.abs(activePointer - index),
                            kotlin.math.min(
                                kotlin.math.abs(activePointer - index - 14),
                                kotlin.math.abs(activePointer - index + 14)
                            )
                        )
                        val waveScale = if (dist < 1.8) {
                            1.0f + 0.38f * (1.0f - (dist.toFloat() / 1.8f))
                        } else {
                            1.0f
                        }

                        // 2. Mode 4: Colnect Blues Gradient Shift + Breathing Scale
                        val colnectBlues = listOf(
                            Color(0xFF001E3D),
                            Color(0xFF003366),
                            Color(0xFF1E4D82),
                            Color(0xFF2B5C8F),
                            Color(0xFF4A90E2),
                            Color(0xFF5D9CEC),
                            Color(0xFF8EBEF4)
                        )
                        val colorIndexDouble = (((kotlin.math.sin(angle.toDouble() + index * 0.45) + 1.0) / 2.0) * (colnectBlues.size - 1))
                        val idx1 = colorIndexDouble.toInt().coerceIn(0, colnectBlues.size - 1)
                        val idx2 = (idx1 + 1).coerceIn(0, colnectBlues.size - 1)
                        val fraction = (colorIndexDouble - idx1).toFloat()
                        val animColnectColor = Color(
                            red = colnectBlues[idx1].red + (colnectBlues[idx2].red - colnectBlues[idx1].red) * fraction,
                            green = colnectBlues[idx1].green + (colnectBlues[idx2].green - colnectBlues[idx1].green) * fraction,
                            blue = colnectBlues[idx1].blue + (colnectBlues[idx2].blue - colnectBlues[idx1].blue) * fraction,
                            alpha = 1.0f
                        )
                        val breathingFactor = 1.0f + 0.15f * kotlin.math.sin(angle.toDouble() + (index * 0.4)).toFloat()

                        // 3. Mode 5: Vertical Hop (Bouncy jump)
                        val verticalOffset = if (animTypeIndex == 5) {
                            val bounce = kotlin.math.sin(angle.toDouble() * 1.5 + (index * 0.6)).toFloat()
                            (bounce * -5.5f).coerceAtMost(0f)
                        } else {
                            0f
                        }

                        // 4. Mode 6: 3D Flip (Y-axis rotation)
                        val rotationYAngle = if (animTypeIndex == 6) {
                            (kotlin.math.sin(angle.toDouble() + (index * 0.5)) * 180f).toFloat()
                        } else {
                            0f
                        }

                        val finalLetterScale = when (animTypeIndex) {
                            3 -> waveScale * animatedClickedExtraScale
                            4 -> breathingFactor * animatedClickedExtraScale
                            else -> 1.0f * animatedClickedExtraScale
                        }

                        val finalColor = if (animTypeIndex == 4) animColnectColor else Color(0xFF2B5C8F)

                        Text(
                            text = char.toString(),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.SansSerif,
                            color = finalColor,
                            style = TextStyle(shadow = collectShadow),
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = finalLetterScale
                                    scaleY = finalLetterScale * 1.35f
                                    translationY = verticalOffset.dp.toPx()
                                    rotationY = rotationYAngle
                                }
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    clickedExtraScale = 1.6f
                                }
                        )
                    }
                }
            } else {
                Text(
                    text = "COLLECT",
                    fontSize = 17.sp, // slightly larger font size
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    color = Color(0xFF2B5C8F), // Colnect blue
                    letterSpacing = liveLetterSpacing,
                    style = TextStyle(
                        shadow = collectShadow
                    ),
                    modifier = Modifier
                        .offset(x = collectShiftX.dp)
                        .graphicsLayer(scaleX = collectScaleX, scaleY = collectScaleY)
                )
            }

            // Dynamic gap that shrinks mathematically with circular limits, centered around 4.dp or 12.5.dp spacers
            Spacer(modifier = Modifier.width(spaceLeftDp))

            // Blue Magnifying glass - 18dp size, perfectly proportioned to match the height of the letters (17.sp with scaleY=1.35f)
            // Loops continuously in a perfect circle that coordinates with the text speeds, never clipped!
            // Clickable tactile target area expanded to 48.dp to comply with AAA standards, keeping 18.dp visual scale!
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .offset(x = hoverX.dp, y = hoverY.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        val available = (0..6).filter { it != animTypeIndex }
                        animTypeIndex = available.random()
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(18.dp)) {
                    val strokeW = 1.5f.dp.toPx() // perfectly proportioned for 18.dp size
                    val colorBlue = Color(0xFF2B5C8F)
                    val colorLightBlue = Color(0xFFF0F7FF)
                    
                    // Center of lens
                    val circleCenter = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.45f)
                    val circleRadius = size.width * 0.28f
                    
                    // Drawing the radar search-waves emitting from the magnifier lens center
                    val waveRadius = circleRadius + (searchWaveProgress * size.width * 0.28f)
                    val waveAlpha = (1f - searchWaveProgress) * 0.6f
                    if (waveAlpha > 0f) {
                        drawCircle(
                            color = colorBlue.copy(alpha = waveAlpha),
                            radius = waveRadius,
                            center = circleCenter,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2f.dp.toPx())
                        )
                    }

                    // Second smaller search wave offset for smoother searching motion
                    val wave2Progress = (searchWaveProgress + 0.5f) % 1.0f
                    val wave2Radius = circleRadius + (wave2Progress * size.width * 0.28f)
                    val wave2Alpha = (1f - wave2Progress) * 0.6f
                    if (wave2Alpha > 0f) {
                        drawCircle(
                            color = colorBlue.copy(alpha = wave2Alpha),
                            radius = wave2Radius,
                            center = circleCenter,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2f.dp.toPx())
                        )
                    }

                    // Lens glass fill
                    drawCircle(
                        color = colorLightBlue,
                        radius = circleRadius,
                        center = circleCenter
                    )
                    
                    // Glass circular outer metal rim
                    drawCircle(
                        color = colorBlue,
                        radius = circleRadius,
                        center = circleCenter,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW)
                    )
                    
                    // Glass handle pointing bottom-left
                    drawLine(
                        color = colorBlue,
                        start = androidx.compose.ui.geometry.Offset(size.width * 0.32f, size.height * 0.63f),
                        end = androidx.compose.ui.geometry.Offset(size.width * 0.1f, size.height * 0.85f),
                        strokeWidth = strokeW * 1.2f,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                    
                    // Inner glare flare highlight
                    drawCircle(
                        color = Color.White,
                        radius = size.width * 0.05f,
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.65f, size.height * 0.32f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(spaceRightDp))

            // Remainder letters 'CONNECT' - styled blue (or rainbow!)
            if (animTypeIndex >= 3) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    "CONNECT".forEachIndexed { index, char ->
                        val globalIndex = index + 7
                        var clickedExtraScale by remember { mutableStateOf(1f) }
                        val animatedClickedExtraScale by animateFloatAsState(
                            targetValue = clickedExtraScale,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            finishedListener = { clickedExtraScale = 1f },
                            label = "charClickedScaleConnect"
                        )

                        // 1. Mode 3: Wave Scale
                        val activePointer = (angle / (2.0 * java.lang.Math.PI)) * 14.0
                        val dist = kotlin.math.min(
                            kotlin.math.abs(activePointer - globalIndex),
                            kotlin.math.min(
                                kotlin.math.abs(activePointer - globalIndex - 14),
                                kotlin.math.abs(activePointer - globalIndex + 14)
                            )
                        )
                        val waveScale = if (dist < 1.8) {
                            1.0f + 0.38f * (1.0f - (dist.toFloat() / 1.8f))
                        } else {
                            1.0f
                        }

                        // 2. Mode 4: Colnect Blues Gradient Shift + Breathing Scale
                        val colnectBlues = listOf(
                            Color(0xFF001E3D),
                            Color(0xFF003366),
                            Color(0xFF1E4D82),
                            Color(0xFF2B5C8F),
                            Color(0xFF4A90E2),
                            Color(0xFF5D9CEC),
                            Color(0xFF8EBEF4)
                        )
                        val colorIndexDouble = (((kotlin.math.sin(angle.toDouble() + globalIndex * 0.45) + 1.0) / 2.0) * (colnectBlues.size - 1))
                        val idx1 = colorIndexDouble.toInt().coerceIn(0, colnectBlues.size - 1)
                        val idx2 = (idx1 + 1).coerceIn(0, colnectBlues.size - 1)
                        val fraction = (colorIndexDouble - idx1).toFloat()
                        val animColnectColor = Color(
                            red = colnectBlues[idx1].red + (colnectBlues[idx2].red - colnectBlues[idx1].red) * fraction,
                            green = colnectBlues[idx1].green + (colnectBlues[idx2].green - colnectBlues[idx1].green) * fraction,
                            blue = colnectBlues[idx1].blue + (colnectBlues[idx2].blue - colnectBlues[idx1].blue) * fraction,
                            alpha = 1.0f
                        )
                        val breathingFactor = 1.0f + 0.15f * kotlin.math.sin(angle.toDouble() + (globalIndex * 0.4)).toFloat()

                        // 3. Mode 5: Vertical Hop (Bouncy jump)
                        val verticalOffset = if (animTypeIndex == 5) {
                            val bounce = kotlin.math.sin(angle.toDouble() * 1.5 + (globalIndex * 0.6)).toFloat()
                            (bounce * -5.5f).coerceAtMost(0f)
                        } else {
                            0f
                        }

                        // 4. Mode 6: 3D Flip (Y-axis rotation)
                        val rotationYAngle = if (animTypeIndex == 6) {
                            (kotlin.math.sin(angle.toDouble() + (globalIndex * 0.5)) * 180f).toFloat()
                        } else {
                            0f
                        }

                        val finalLetterScale = when (animTypeIndex) {
                            3 -> waveScale * animatedClickedExtraScale
                            4 -> breathingFactor * animatedClickedExtraScale
                            else -> 1.0f * animatedClickedExtraScale
                        }

                        val finalColor = if (animTypeIndex == 4) animColnectColor else Color(0xFF2B5C8F)

                        Text(
                            text = char.toString(),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.SansSerif,
                            color = finalColor,
                            style = TextStyle(shadow = connectShadow),
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = finalLetterScale
                                    scaleY = finalLetterScale * 1.35f
                                    translationY = verticalOffset.dp.toPx()
                                    rotationY = rotationYAngle
                                }
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    clickedExtraScale = 1.6f
                                }
                        )
                    }
                }
            } else {
                Text(
                    text = "CONNECT",
                    fontSize = 17.sp, // slightly larger font size
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    color = Color(0xFF2B5C8F), // Colnect blue
                    letterSpacing = liveLetterSpacing,
                    style = TextStyle(
                        shadow = connectShadow
                    ),
                    modifier = Modifier
                        .offset(x = connectShiftX.dp)
                        .graphicsLayer(scaleX = connectScaleX, scaleY = connectScaleY)
                )
            }
        }
    }
}

@Composable
fun PulsingColnectLogo(modifier: Modifier = Modifier) {
    // Generate infinite soft breathing opacity pulse matching specs
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoOpacity"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp) // Gives sufficient unclipped space for full circular motion
            .background(Color.White)
            .alpha(alphaAnim),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ColnectLogo()
    }
}

// Helper to determine if the webview can safely go back without leaving /coins parent tree or hitting parent home
fun canWebViewGoBackSafely(webView: WebView?): Boolean {
    val wb = webView ?: return false
    val currentUrl = wb.url ?: ""
    val u = currentUrl.trim().lowercase().removeSuffix("/")
    
    // If we are at the home URL or higher (landing pages colnect.com, colnect.com/es etc.), we cannot go back.
    if (u == "https://colnect.com" || u == "https://colnect.com/es" || u == "https://colnect.com/es/coins") {
        return false
    }
    if (u.startsWith("https://colnect.com") && !u.contains("/coins")) {
        return false
    }
    
    // Check normal WebView back support
    if (!wb.canGoBack()) return false
    
    // Check if the previous item in back history would lead us out of coins section or back to landing pages
    val list = wb.copyBackForwardList()
    val prevIndex = list.currentIndex - 1
    if (prevIndex >= 0) {
        val prevUrl = list.getItemAtIndex(prevIndex)?.url ?: ""
        val pu = prevUrl.trim().lowercase().removeSuffix("/")
        if (pu == "https://colnect.com" || pu == "https://colnect.com/es" || (pu.startsWith("https://colnect.com") && !pu.contains("/coins"))) {
            return false
        }
    }
    return true
}

/**
 * Modal dialog presenting the list of generated Colnect URLs for inspection, copying, and re-opening.
 *
 * @param viewModel Shared [ColnectViewModel] instance.
 */
@Composable
fun HistoryDialog(viewModel: ColnectViewModel) {
    Config.logFunctionCall("Components.kt", "HistoryDialog")
    val context = LocalContext.current
    val historyList by viewModel.urlHistory.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    val dialogBg = if (isDarkMode) Color(0xFF18181B) else Color.White
    val textColor = if (isDarkMode) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val cardBg = if (isDarkMode) Color(0xFF27272A) else Color(0xFFF8FAFC)
    val borderColor = if (isDarkMode) Color(0xFF3F3F46) else Color(0xFFE2E8F0)

    androidx.compose.ui.window.Dialog(
        onDismissRequest = { viewModel.closeHistoryDialog() },
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .padding(12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = dialogBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Historial",
                            tint = if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF4F46E5),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Historial de URLs",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                    IconButton(onClick = { viewModel.closeHistoryDialog() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (historyList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = if (isDarkMode) Color(0xFF52525B) else Color(0xFFCBD5E1),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Aún no hay URLs en el historial.",
                                fontSize = 14.sp,
                                color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF64748B)
                            )
                            Text(
                                text = "Las URLs que generes aparecerán aquí para ser reutilizadas.",
                                fontSize = 12.sp,
                                color = if (isDarkMode) Color(0xFF71717A) else Color(0xFF94A3B8),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(historyList, key = { it.id }) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                border = BorderStroke(1.dp, borderColor)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.summary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF0F3B6C),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = { viewModel.deleteHistoryItem(context, item.id) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Eliminar",
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = item.url,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (isDarkMode) Color(0xFFE2E8F0) else Color(0xFF334155),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    val dateFormat = remember { java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()) }
                                    val dateStr = dateFormat.format(java.util.Date(item.timestamp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = dateStr,
                                            fontSize = 10.sp,
                                            color = if (isDarkMode) Color(0xFF71717A) else Color(0xFF94A3B8)
                                        )

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            // Copy button
                                            OutlinedButton(
                                                onClick = {
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                    val clip = android.content.ClipData.newPlainText("Colnect URL", item.url)
                                                    clipboard.setPrimaryClip(clip)
                                                    android.widget.Toast.makeText(context, "URL copiada al portapapeles", android.widget.Toast.LENGTH_SHORT).show()
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                modifier = Modifier.height(30.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Copiar", fontSize = 11.sp)
                                            }

                                            // Open / Reuse button
                                            Button(
                                                onClick = {
                                                    viewModel.loadUrlFromHistory(context, item)
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF4F46E5)
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                modifier = Modifier.height(30.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.OpenInNew,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Abrir", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { viewModel.clearHistory(context) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        border = BorderStroke(1.dp, Color(0xFFEF4444)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Borrar Todo el Historial", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}



// --------------------------------------------------------------------
// Custom WebView Screen / Visor de Colnect integrado
// --------------------------------------------------------------------
