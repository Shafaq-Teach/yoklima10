package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.supabase.SupabaseConnectionStatus
import com.example.data.supabase.SupabaseSyncResult
import com.example.data.supabase.SupabaseSyncService
import com.example.ui.viewmodel.AttendanceViewModel

@Composable
fun SupabaseSyncManagementDialog(
    viewModel: AttendanceViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val s = viewModel.strings

    val connectionStatus by viewModel.supabaseStatus.collectAsState()
    val isSyncing by viewModel.isSupabaseSyncing.collectAsState()
    val lastResult by viewModel.lastSyncResult.collectAsState()
    val isAutoSync by viewModel.isAutoSyncEnabled.collectAsState()

    var showSqlDialog by remember { mutableStateOf(false) }
    var showLogs by remember { mutableStateOf(false) }

    // Automatically check connection when opening dialog if not checked yet
    LaunchedEffect(Unit) {
        if (connectionStatus == null) {
            viewModel.testSupabaseConnection()
        }
    }

    Dialog(
        onDismissRequest = { if (!isSyncing) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .testTag("supabase_sync_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3ECF8E).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = Color(0xFF2E9A68),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Supabase بۇلۇت بازىسى",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "پۈتۈن سىستېما مەلۇماتلىرىنى قوش يۆنىلىشلىك ماسقەدەملەش",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        enabled = !isSyncing,
                        modifier = Modifier.testTag("close_supabase_dialog_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = s.cancel)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Connection Status Card
                    ElevatedCard(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val statusColor = when {
                                        isSyncing -> MaterialTheme.colorScheme.primary
                                        connectionStatus?.isConnected == true -> Color(0xFF2E9A68)
                                        connectionStatus != null -> MaterialTheme.colorScheme.error
                                        else -> MaterialTheme.colorScheme.outline
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(statusColor)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = when {
                                            isSyncing -> "تەكشۈرۈلۈۋاتىدۇ / ماسقەدەملىنىۋاتىدۇ..."
                                            connectionStatus?.isConnected == true -> "Supabase نورمال ئۇلاندى"
                                            connectionStatus != null -> "ئۇلىنىش مەغلۇپ بولدى"
                                            else -> "تەكشۈرۈلمىدى"
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                if (connectionStatus?.latencyMs != null && connectionStatus!!.isConnected) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF2E9A68).copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "سۈرئىتى: ${connectionStatus!!.latencyMs} ms",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E6C47),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Project Info Details
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Project ID:",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = SupabaseSyncService.getProjectId(),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "URL:",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = SupabaseSyncService.getProjectUrl(),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontFamily = FontFamily.Monospace,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    if (connectionStatus?.details?.isNotBlank() == true) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = connectionStatus!!.details,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (connectionStatus!!.isConnected) Color(0xFF2E9A68) else MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Test Connection Button
                            OutlinedButton(
                                onClick = { viewModel.testSupabaseConnection() },
                                enabled = !isSyncing,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("test_supabase_connection_btn")
                            ) {
                                if (isSyncing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("سىناۋاتىدۇ...")
                                } else {
                                    Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("ئۇلىنىشنى تەكشۈرۈش ۋە سىناش", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // 2. Main Cloud Actions
                    Text(
                        text = "بۇلۇت مەشغۇلاتلىرى",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Upload Button Card
                    ElevatedCard(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudUpload,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Supabase كە تولۇق يۈكلەش (Upload)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "بارلىق 6 گۇرۇپپا، ئەزالار، يوقلىما ۋە ئۇقتۇرۇشلارنى بۇلۇتقا يۈكلەيدۇ",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    viewModel.uploadAllToSupabase(context)
                                },
                                enabled = !isSyncing,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("upload_to_supabase_btn")
                            ) {
                                if (isSyncing) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("بۇلۇتقا يۈكلىنىۋاتىدۇ...", fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("بارلىق مەلۇماتلارنى Supabase كە يۈكلەش", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Pull & Restore Button Card
                    ElevatedCard(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.secondaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDownload,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Supabase تىن چۈشۈرۈش / ئەسلىگە كەلتۈرۈش",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "بۇلۇتتىكى ئەڭ يېڭى سانلىق مەلۇماتلارنى ئەپكە كۆچۈرۈپ ئەسلىگە كەلتۈرىدۇ",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedButton(
                                onClick = {
                                    viewModel.pullAllFromSupabase(context)
                                },
                                enabled = !isSyncing,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("pull_from_supabase_btn")
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Supabase تىن چۈشۈرۈپ ئەسلىگە كەلتۈرۈش", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // 3. SQL DDL Script & Advanced Tools
                    ElevatedCard(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Code,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Supabase SQL جەدۋەل جۈملىلىرى",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                TextButton(
                                    onClick = { showSqlDialog = true },
                                    modifier = Modifier.testTag("view_sql_schema_btn")
                                ) {
                                    Text("كۆرۈش / كۆچۈرۈش", fontWeight = FontWeight.Bold)
                                }
                            }

                            Text(
                                text = "ئەگەر Supabase بازىسىدا جەدۋەللەر قۇرۇلمىغان بولسا، SQL جۈملىسىنى Supabase SQL Editor غا چاپلاپ قۇرسىڭىز بولىدۇ.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 4. Auto Sync Option
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ئاپتوماتىك قەرەللىك ساقلاش",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "مەلۇماتلار قوشۇلغاندا ياكى ئۆزگەرگەندە بۇلۇتقا تەڭ قەدەمدە ساقلاش",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isAutoSync,
                                onCheckedChange = { viewModel.toggleAutoSync(it) },
                                modifier = Modifier.testTag("auto_sync_switch")
                            )
                        }
                    }

                    // 5. Upload Result Logs (if any)
                    if (lastResult != null) {
                        val res = lastResult!!
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (res.isSuccess) Color(0xFF2E9A68).copy(alpha = 0.1f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (res.isSuccess) Color(0xFF2E9A68).copy(alpha = 0.3f) else MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (res.isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                            contentDescription = null,
                                            tint = if (res.isSuccess) Color(0xFF2E9A68) else MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (res.isSuccess) "ئەڭ يېڭى يۈكلەش دوكلاتى" else "يۈكلەش ئاگاھلاندۇرۇشى",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    IconButton(
                                        onClick = { showLogs = !showLogs },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (showLogs) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = res.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (res.isSuccess) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "• گۇرۇپپىلار: ${res.groupsUploaded} | ئەزالار: ${res.membersUploaded} | يوقلىما: ${res.attendanceUploaded} | قوراللار: ${res.equipmentUploaded} | ئۇقتۇرۇشلار: ${res.updatesUploaded}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF1E6C47)
                                    )
                                }

                                AnimatedVisibility(visible = showLogs && res.logDetails.isNotEmpty()) {
                                    Column(
                                        modifier = Modifier
                                            .padding(top = 10.dp)
                                            .fillMaxWidth()
                                            .background(
                                                MaterialTheme.colorScheme.surface,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(10.dp)
                                    ) {
                                        res.logDetails.forEach { log ->
                                            Text(
                                                text = log,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontFamily = FontFamily.Monospace,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Close Button
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dismiss_supabase_dialog_btn")
                ) {
                    Text("تامام / تاقاش", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // SQL Schema Viewer & Copy Dialog
    if (showSqlDialog) {
        val sqlScript = remember { viewModel.getSupabaseSqlSchema() }
        AlertDialog(
            onDismissRequest = { showSqlDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Supabase SQL قۇرۇلما جۈملىلىرى",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { showSqlDialog = false }) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                ) {
                    Text(
                        text = "بۇ جۈملىنى Supabase Dashboard > SQL Editor غا چاپلاپ «Run» نى باسسىڭىز، بارلىق جەدۋەللەر تولۇق ئاپتوماتىك تەييار بولىدۇ:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Text(
                            text = sqlScript,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .padding(12.dp)
                                .verticalScroll(rememberScrollState())
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Supabase SQL", sqlScript)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "SQL جۈملىسى چاپلاش تاختىسىغا كۆچۈرۈلدى!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("copy_sql_script_btn")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SQL نى كۆچۈرۈش", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSqlDialog = false }) {
                    Text(s.cancel)
                }
            }
        )
    }
}
