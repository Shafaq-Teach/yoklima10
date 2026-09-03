package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.UserRole
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.GroupLeadScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AttendanceViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        com.example.util.AppNotificationManager.initNotificationChannel(this)
        com.example.util.BootAndNetworkReceiver.schedulePeriodicJob(this)
        com.example.util.BootAndNetworkReceiver.scheduleSyncAlarm(this)
        com.example.util.LiveSyncForegroundService.startService(this)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }
        setContent {
            val viewModel: AttendanceViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val themePreset by viewModel.themePreset.collectAsState()
            val currentLanguage by viewModel.currentLanguage.collectAsState()
            val layoutDirection = if (currentLanguage == com.example.i18n.Language.ENGLISH) {
                LayoutDirection.Ltr
            } else {
                LayoutDirection.Rtl
            }

            MyApplicationTheme(
                darkTheme = isDarkMode,
                themePreset = themePreset
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AttendanceApp(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceApp(viewModel: AttendanceViewModel = viewModel()) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isBlocked by viewModel.isCurrentDeviceBlocked.collectAsState()

    if (isBlocked) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF180505))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF2B0E0E),
                border = BorderStroke(1.5.dp, Color(0xFFD32F2F)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD32F2F).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "⚠️ سىستېما زىيارىتى توختىتىلدى",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF5252),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "بۇ تېلېفون ياكى ئۈسكۈنە باش باشقۇرغۇچى تەرىپىدىن چەكلەندى ۋە خىزمەتتىن توختىتىلدى. قايتا قوزغىتىش ئۈچۈن باش باشقۇرغۇچى بىلەن ئالاقىلىشىڭ.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Black.copy(alpha = 0.4f)
                    ) {
                        Text(
                            text = "ئۈسكۈنە كودى: ${viewModel.currentDeviceId.take(12)}...",
                            fontSize = 12.sp,
                            color = Color.LightGray,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.checkAndRegisterCurrentDevice() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD32F2F)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ھالەتنى يېڭىلاپ تەكشۈرۈش", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    when (val user = currentUser) {
        null -> {
            LoginScreen(viewModel = viewModel)
        }
        else -> {
            if (user.role == UserRole.ADMIN) {
                AdminDashboardScreen(user = user, viewModel = viewModel)
            } else {
                GroupLeadScreen(user = user, viewModel = viewModel)
            }
        }
    }
}
