package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.i18n.AppStrings
import com.example.ui.components.LanguageToggleHeader
import com.example.ui.viewmodel.AttendanceViewModel
import kotlin.math.cos
import kotlin.math.sin

data class TechSphereConfig(
    val id: Long,
    val name: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val darkColor: Color,
    val glowColor: Color,
    val angleDegrees: Double, // in degrees: -90 = top (12 o'clock)
    val defaultUsername: String
)

@Composable
fun LoginScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.currentLanguage.collectAsState()
    val s = AppStrings.get(language)
    val users by viewModel.users.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val lastOrbId by viewModel.lastActiveOrbId.collectAsState()

    val isBlocked by viewModel.isCurrentDeviceBlocked.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkAndRegisterCurrentDevice()
    }

    if (isBlocked) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = Color(0xFF1F0606),
            title = {
                Text(
                    text = "⚠️ سىستېما زىيارىتى توختىتىلدى",
                    color = Color(0xFFFF5252),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "بۇ تېلېفون ياكى ئۈسكۈنە باش باشقۇرغۇچى تەرىپىدىن چەكلەندى. باشقۇرغۇچى بىلەن ئالاقىلىشىڭ.",
                    color = Color.White,
                    fontSize = 14.sp
                )
            },
            confirmButton = {}
        )
    }

    // Dialog state for clicked sphere
    var selectedTargetSphere by remember { mutableStateOf<TechSphereConfig?>(null) }
    var isAdminSelected by remember { mutableStateOf(false) }

    var usernameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 6 Outer Spheres with م ط and عمليات swapped per user request
    val sphereConfigs = remember {
        listOf(
            TechSphereConfig(
                id = 6L,
                name = "عمليات",
                primaryColor = Color(0xFFE040FB),
                secondaryColor = Color(0xFF8E24AA),
                darkColor = Color(0xFF320042),
                glowColor = Color(0xFFEA80FC),
                angleDegrees = -90.0, // Top (12 o'clock) - عمليات (Swapped with م ط)
                defaultUsername = "lead6"
            ),
            TechSphereConfig(
                id = 2L,
                name = "قنص",
                primaryColor = Color(0xFF00B0FF),
                secondaryColor = Color(0xFF0070BA),
                darkColor = Color(0xFF001B3A),
                glowColor = Color(0xFF40C4FF),
                angleDegrees = -30.0, // Top-Right (2 o'clock) - Sky Blue
                defaultUsername = "lead2"
            ),
            TechSphereConfig(
                id = 3L,
                name = "م د",
                primaryColor = Color(0xFFFF9100),
                secondaryColor = Color(0xFFE65100),
                darkColor = Color(0xFF2E0E00),
                glowColor = Color(0xFFFFAB40),
                angleDegrees = 30.0, // Bottom-Right (4 o'clock) - Amber Gold
                defaultUsername = "lead3"
            ),
            TechSphereConfig(
                id = 4L,
                name = "اسناد",
                primaryColor = Color(0xFF2979FF),
                secondaryColor = Color(0xFF1565C0),
                darkColor = Color(0xFF0A1938),
                glowColor = Color(0xFF82B1FF),
                angleDegrees = 90.0, // Bottom (6 o'clock) - Deep Electric Blue
                defaultUsername = "lead4"
            ),
            TechSphereConfig(
                id = 5L,
                name = "ادارى",
                primaryColor = Color(0xFF00E676),
                secondaryColor = Color(0xFF00897B),
                darkColor = Color(0xFF002914),
                glowColor = Color(0xFF69F0AE),
                angleDegrees = 150.0, // Bottom-Left (8 o'clock) - Emerald Green
                defaultUsername = "lead5"
            ),
            TechSphereConfig(
                id = 1L,
                name = "م ط",
                primaryColor = Color(0xFF00E5FF),
                secondaryColor = Color(0xFF0091EA),
                darkColor = Color(0xFF001F3F),
                glowColor = Color(0xFF00E5FF),
                angleDegrees = 210.0, // Top-Left (10 o'clock) - م ط (Swapped with عمليات)
                defaultUsername = "lead1"
            )
        )
    }

    // Auto-open last active orb's login dialog on logout/back navigation
    LaunchedEffect(lastOrbId, users) {
        if (lastOrbId != null) {
            if (lastOrbId == 0L) {
                isAdminSelected = true
                selectedTargetSphere = null
                val adminUser = users.find { it.role == UserRole.ADMIN }
                usernameInput = adminUser?.loginName ?: "admin"
                passwordInput = ""
            } else {
                val target = sphereConfigs.find { it.id == lastOrbId }
                if (target != null) {
                    selectedTargetSphere = target
                    isAdminSelected = false
                    val grpUser = users.find { it.groupId == target.id }
                    usernameInput = grpUser?.loginName ?: target.defaultUsername
                    passwordInput = ""
                }
            }
            viewModel.clearLastActiveOrb()
        }
    }

    // Back handling: if dialog open -> close dialog; if on home screen -> double-tap back to exit
    val context = LocalContext.current
    var lastBackPressTime by remember { mutableStateOf(0L) }

    BackHandler(enabled = true) {
        if (selectedTargetSphere != null || isAdminSelected) {
            selectedTargetSphere = null
            isAdminSelected = false
            errorMessage = null
        } else {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastBackPressTime < 2000L) {
                (context as? Activity)?.finish()
            } else {
                lastBackPressTime = currentTime
                Toast.makeText(context, s.pressAgainToExit, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Animation Transitions
    val infiniteTransition = rememberInfiniteTransition(label = "cyber_hud")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )
    val rotationFast by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationFast"
    )
    val rotationSlow by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(45000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationSlow"
    )

    // Deep Electric Blue Sci-Fi HUD Canvas (matching Image 3)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF001133), // Deep electric navy blue at top
                        Color(0xFF001F52), // Glowing vibrant cyber blue in center
                        Color(0xFF002A6D), // Rich cyan-blue depth
                        Color(0xFF000F29)  // Dark foundation at bottom
                    )
                )
            )
    ) {
        // High-Tech Cyber HUD Radar & Circuit Background (Image 3 inspired)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Top-Center Cyber Radar Dial
            val radar1Center = Offset(width * 0.45f, height * 0.18f)
            val radar1Radius = width * 0.28f

            drawCircle(
                color = Color(0xFF00E5FF).copy(alpha = 0.25f * pulseGlow),
                radius = radar1Radius,
                center = radar1Center,
                style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), rotationFast))
            )
            drawCircle(
                color = Color(0xFF00B0FF).copy(alpha = 0.18f),
                radius = radar1Radius * 0.72f,
                center = radar1Center,
                style = Stroke(width = 1.5f)
            )
            drawCircle(
                color = Color(0xFF00E5FF).copy(alpha = 0.35f * pulseGlow),
                radius = radar1Radius * 0.4f,
                center = radar1Center,
                style = Stroke(width = 2.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), -rotationFast))
            )

            // 2. Large Bottom-Left HUD Hologram Reticle (like in Image 3)
            val radar2Center = Offset(width * 0.15f, height * 0.82f)
            val radar2Radius = width * 0.45f

            // Concentric detailed HUD rings
            drawCircle(
                color = Color(0xFF00E5FF).copy(alpha = 0.32f * pulseGlow),
                radius = radar2Radius,
                center = radar2Center,
                style = Stroke(width = 3.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 12f), rotationSlow))
            )
            drawCircle(
                color = Color(0xFF40C4FF).copy(alpha = 0.2f),
                radius = radar2Radius * 0.85f,
                center = radar2Center,
                style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f))
            )
            drawCircle(
                color = Color(0xFF00E5FF).copy(alpha = 0.28f * pulseGlow),
                radius = radar2Radius * 0.65f,
                center = radar2Center,
                style = Stroke(width = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 8f), -rotationFast))
            )
            drawCircle(
                color = Color(0xFF00E5FF).copy(alpha = 0.45f * pulseGlow),
                radius = radar2Radius * 0.3f,
                center = radar2Center,
                style = Stroke(width = 4f)
            )

            // Segmented turbine radial spokes
            for (i in 0 until 16) {
                val spokeAngle = Math.toRadians((i * 22.5 + rotationSlow).toDouble())
                val innerR = radar2Radius * 0.22f
                val outerR = radar2Radius * 0.38f
                val p1 = Offset(radar2Center.x + (innerR * cos(spokeAngle)).toFloat(), radar2Center.y + (innerR * sin(spokeAngle)).toFloat())
                val p2 = Offset(radar2Center.x + (outerR * cos(spokeAngle)).toFloat(), radar2Center.y + (outerR * sin(spokeAngle)).toFloat())
                drawLine(
                    color = Color(0xFF00E5FF).copy(alpha = 0.45f * pulseGlow),
                    start = p1,
                    end = p2,
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )
            }

            // 3. Right-Side HUD Dial
            val radar3Center = Offset(width * 0.88f, height * 0.65f)
            val radar3Radius = width * 0.32f

            drawCircle(
                color = Color(0xFF00E5FF).copy(alpha = 0.28f * pulseGlow),
                radius = radar3Radius,
                center = radar3Center,
                style = Stroke(width = 2.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 8f), rotationFast))
            )
            drawCircle(
                color = Color(0xFF00B0FF).copy(alpha = 0.18f),
                radius = radar3Radius * 0.5f,
                center = radar3Center,
                style = Stroke(width = 2f)
            )

            // 4. Glowing Tech Nodes and Data Circuit Traces
            val nodes = listOf(
                Offset(width * 0.18f, height * 0.25f),
                Offset(width * 0.82f, height * 0.22f),
                Offset(width * 0.90f, height * 0.42f),
                Offset(width * 0.12f, height * 0.52f),
                Offset(width * 0.78f, height * 0.85f),
                Offset(width * 0.52f, height * 0.92f)
            )

            nodes.forEach { node ->
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = 0.9f * pulseGlow),
                    radius = 5f,
                    center = node
                )
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = 0.25f * pulseGlow),
                    radius = 12f,
                    center = node
                )
            }

            // Tech Hexagon Grid Accents
            val hexPath = Path().apply {
                val hx = width * 0.78f
                val hy = height * 0.15f
                val r = 24f
                for (k in 0..6) {
                    val a = Math.toRadians((k * 60).toDouble())
                    val px = hx + (r * cos(a)).toFloat()
                    val py = hy + (r * sin(a)).toFloat()
                    if (k == 0) moveTo(px, py) else lineTo(px, py)
                }
            }
            drawPath(
                path = hexPath,
                color = Color(0xFF00E5FF).copy(alpha = 0.3f),
                style = Stroke(width = 1.5f)
            )
        }

        // Top Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = s.appTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 1.sp
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.cycleTheme() },
                    modifier = Modifier.size(36.dp).testTag("login_cycle_theme")
                ) {
                    Icon(
                        imageVector = Icons.Default.ColorLens,
                        contentDescription = s.switchTheme,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(22.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.toggleDarkMode() },
                    modifier = Modifier.size(36.dp).testTag("login_dark_mode_toggle")
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = if (isDarkMode) s.lightMode else s.darkMode,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                LanguageToggleHeader(
                    currentLanguage = language,
                    onToggle = { viewModel.toggleLanguage() }
                )
            }
        }

        // Central Hologram System: 7 Spheres with Precise Orbital Spacing (Matching Image 2)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 70.dp, bottom = 30.dp),
            contentAlignment = Alignment.Center
        ) {
            val density = LocalDensity.current

            // Responsive sizing to ensure perfect orbital separation without any overlapping
            val centerOrbSize = 84.dp
            val outerOrbSize = 68.dp

            // Orbit radius calculated generously in DP to guarantee clear gap between center and outer spheres
            val orbitRadiusDp = (minOf(maxWidth, maxHeight) * 0.35f).coerceIn(124.dp, 145.dp)
            val orbitRadiusPx = with(density) { orbitRadiusDp.toPx() }

            // Canvas for Laser Conduits and Inter-Sphere Web
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerOffset = Offset(size.width / 2f, size.height / 2f)

                // 1. Orbital Ring Guide
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = 0.22f * pulseGlow),
                    radius = orbitRadiusPx,
                    center = centerOffset,
                    style = Stroke(
                        width = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), rotationFast * 0.5f)
                    )
                )

                // 2. Inter-Sphere Hexagonal Connection Web
                val outerPositions = sphereConfigs.map { config ->
                    val angleRad = Math.toRadians(config.angleDegrees)
                    Offset(
                        x = centerOffset.x + (orbitRadiusPx * cos(angleRad)).toFloat(),
                        y = centerOffset.y + (orbitRadiusPx * sin(angleRad)).toFloat()
                    )
                }

                for (i in outerPositions.indices) {
                    val currentPos = outerPositions[i]
                    val nextPos = outerPositions[(i + 1) % outerPositions.size]
                    drawLine(
                        color = Color(0xFF00E5FF).copy(alpha = 0.28f * pulseGlow),
                        start = currentPos,
                        end = nextPos,
                        strokeWidth = 1.8f
                    )
                }

                // 3. High-Energy Laser Beams from Center Orb to Each Outer Orb
                sphereConfigs.forEachIndexed { idx, config ->
                    val targetPos = outerPositions[idx]

                    // Laser Beam with glowing gradient
                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF00E5FF).copy(alpha = 0.9f * pulseGlow),
                                config.glowColor.copy(alpha = 0.95f * pulseGlow)
                            ),
                            start = centerOffset,
                            end = targetPos
                        ),
                        start = centerOffset,
                        end = targetPos,
                        strokeWidth = 3.5f
                    )

                    // Glowing Node along the laser conduit
                    val midPos = Offset(
                        centerOffset.x + (targetPos.x - centerOffset.x) * 0.5f,
                        centerOffset.y + (targetPos.y - centerOffset.y) * 0.5f
                    )
                    drawCircle(
                        color = config.glowColor.copy(alpha = 0.95f * pulseGlow),
                        radius = 4.5f,
                        center = midPos
                    )
                }
            }

            // Central Core Sphere: باشقۇرغۇچى (Admin Portal)
            GlowingGlassSphere(
                title = s.roleAdmin,
                primaryColor = Color(0xFF00E5FF),
                secondaryColor = Color(0xFF0072FF),
                darkColor = Color(0xFF001E4D),
                glowColor = Color(0xFF00E5FF),
                size = centerOrbSize,
                isCenter = true,
                pulseGlow = pulseGlow,
                modifier = Modifier.testTag("orb_admin_center"),
                onClick = {
                    isAdminSelected = true
                    selectedTargetSphere = null
                    val adminUser = users.find { it.role == UserRole.ADMIN }
                    usernameInput = adminUser?.loginName ?: "admin"
                    passwordInput = ""
                    errorMessage = null
                }
            )

            // 6 Outer Glowing Glass Spheres positioned precisely around the orbit
            sphereConfigs.forEach { sphere ->
                val angleRad = Math.toRadians(sphere.angleDegrees)
                val xOffset = (orbitRadiusDp.value * cos(angleRad)).dp
                val yOffset = (orbitRadiusDp.value * sin(angleRad)).dp

                Box(
                    modifier = Modifier
                        .offset(x = xOffset, y = yOffset)
                        .testTag("orb_group_${sphere.id}")
                ) {
                    GlowingGlassSphere(
                        title = sphere.name,
                        primaryColor = sphere.primaryColor,
                        secondaryColor = sphere.secondaryColor,
                        darkColor = sphere.darkColor,
                        glowColor = sphere.glowColor,
                        size = outerOrbSize,
                        isCenter = false,
                        pulseGlow = pulseGlow,
                        onClick = {
                            isAdminSelected = false
                            selectedTargetSphere = sphere
                            val grpUser = users.find { it.groupId == sphere.id }
                            usernameInput = grpUser?.loginName ?: sphere.defaultUsername
                            passwordInput = ""
                            errorMessage = null
                        }
                    )
                }
            }
        }

        // Login Dialog (Crisp High-Contrast Glass Dialog in Electric Cyber Blue)
        if (selectedTargetSphere != null || isAdminSelected) {
            val targetName = if (isAdminSelected) s.roleAdmin else selectedTargetSphere?.name.orEmpty()
            val targetUser = if (isAdminSelected) {
                users.find { it.role == UserRole.ADMIN }
            } else {
                users.find { it.groupId == selectedTargetSphere?.id }
            }
            val accentColor = if (isAdminSelected) Color(0xFF00E5FF) else (selectedTargetSphere?.glowColor ?: Color(0xFF00E5FF))

            AlertDialog(
                onDismissRequest = {
                    selectedTargetSphere = null
                    isAdminSelected = false
                    errorMessage = null
                },
                shape = RoundedCornerShape(24.dp),
                containerColor = Color(0xFF051736), // Deep electric cyber dialog background
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(accentColor),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isAdminSelected) {
                                    Icon(
                                        imageVector = Icons.Default.AdminPanelSettings,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(26.dp)
                                    )
                                } else {
                                    Text(
                                        text = selectedTargetSphere?.name.orEmpty(),
                                        fontWeight = FontWeight.Black,
                                        color = Color.Black,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = targetName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        // High-contrast, crystal-clear Username Input Field
                        OutlinedTextField(
                            value = usernameInput,
                            onValueChange = {
                                usernameInput = it
                                errorMessage = null
                            },
                            label = { Text(s.username, color = Color(0xFF80D8FF), fontWeight = FontWeight.SemiBold) },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = accentColor)
                            },
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF09204A),
                                unfocusedContainerColor = Color(0xFF09204A),
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = Color(0xFF1E4582),
                                focusedLabelColor = accentColor,
                                unfocusedLabelColor = Color(0xFF80D8FF),
                                cursorColor = accentColor
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("username_input")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // High-contrast, crystal-clear Password Input Field with toggle
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = {
                                passwordInput = it
                                errorMessage = null
                            },
                            label = { Text(s.password, color = Color(0xFF80D8FF), fontWeight = FontWeight.SemiBold) },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = accentColor)
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = accentColor
                                    )
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF09204A),
                                unfocusedContainerColor = Color(0xFF09204A),
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = Color(0xFF1E4582),
                                focusedLabelColor = accentColor,
                                unfocusedLabelColor = Color(0xFF80D8FF),
                                cursorColor = accentColor
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("password_input")
                        )

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessage.orEmpty(),
                                color = Color(0xFFFF5252),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.login(usernameInput, passwordInput) { success ->
                                if (success) {
                                    selectedTargetSphere = null
                                    isAdminSelected = false
                                } else {
                                    errorMessage = s.loginError
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("login_button")
                    ) {
                        Text(
                            text = s.loginButton,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            selectedTargetSphere = null
                            isAdminSelected = false
                            errorMessage = null
                        }
                    ) {
                        Text(s.cancel, color = Color(0xFF80D8FF))
                    }
                }
            )
        }
    }
}

/**
 * 3D Glowing Glass Sphere matching the visual reference image.
 */
@Composable
fun GlowingGlassSphere(
    title: String,
    primaryColor: Color,
    secondaryColor: Color,
    darkColor: Color,
    glowColor: Color,
    size: Dp,
    isCenter: Boolean,
    pulseGlow: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = if (isCenter) 18.dp else 12.dp,
                shape = CircleShape,
                ambientColor = glowColor,
                spotColor = glowColor
            )
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // 1. Deep 3D Radial Glass Sphere Body
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = this.size.minDimension / 2f
            val center = Offset(radius, radius)

            // Base 3D Radial Sphere gradient (Light source from top-left)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor,
                        secondaryColor,
                        darkColor,
                        Color(0xFF000C1F)
                    ),
                    center = Offset(radius * 0.65f, radius * 0.55f),
                    radius = radius * 1.05f
                ),
                radius = radius,
                center = center
            )

            // Inner Sci-Fi circuit / glowing ring
            drawCircle(
                color = glowColor.copy(alpha = 0.55f * pulseGlow),
                radius = radius * 0.88f,
                center = center,
                style = Stroke(width = 2.5f)
            )

            // Specular Glass Arc Highlight on upper curve
            drawOval(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.70f),
                        Color.White.copy(alpha = 0.20f),
                        Color.Transparent
                    )
                ),
                topLeft = Offset(radius * 0.28f, radius * 0.12f),
                size = Size(radius * 1.44f, radius * 0.62f)
            )

            // Bottom-right ambient shadow reflection
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.55f)
                    ),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
        }

        // Outer Neon Glow Ring Border
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = if (isCenter) 2.5.dp else 2.dp,
                    color = glowColor.copy(alpha = 0.85f * pulseGlow),
                    shape = CircleShape
                )
        )

        // Center Content (Title / Name)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            if (isCenter) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
