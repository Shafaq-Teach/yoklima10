package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.rotate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CircularProgressIndicator
import com.example.data.model.AttendanceRecordEntity
import com.example.data.model.AttendanceStatus
import com.example.data.model.GroupEntity
import com.example.data.model.MemberEntity
import com.example.data.model.UserEntity
import com.example.i18n.AppStrings
import com.example.i18n.Language
import com.example.ui.components.ActivePortalsManagementView
import com.example.ui.components.DailyUpdatesView
import com.example.ui.components.DateSelectorStrip
import com.example.ui.components.EquipmentManagementView
import com.example.ui.components.ExecutiveContactsView
import com.example.ui.components.GroupExportSection
import com.example.ui.components.LanguageToggleHeader
import com.example.ui.components.MultiDateAnalyticsDialog
import com.example.ui.components.SupabaseSyncManagementDialog
import com.example.ui.components.AdminSystemSettingsDialog
import androidx.compose.material.icons.filled.Settings
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.AbsentRedContainer
import com.example.ui.theme.ExcusedBlue
import com.example.ui.theme.ExcusedBlueContainer
import com.example.ui.theme.PresentGreen
import com.example.ui.theme.PresentGreenContainer
import com.example.ui.viewmodel.AttendanceViewModel
import java.util.Locale

@Composable
fun AdminDashboardScreen(
    user: UserEntity,
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val language by viewModel.currentLanguage.collectAsState()
    val s = AppStrings.get(language)
    val selectedDate by viewModel.selectedDate.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val currentThemePreset by viewModel.themePreset.collectAsState()
    val leaderAttendanceVisible by viewModel.leaderAttendanceVisible.collectAsState()

    val groups by viewModel.groups.collectAsState()
    val users by viewModel.users.collectAsState()
    val groupStats by viewModel.groupStats.collectAsState()
    val topAbsentees by viewModel.topAbsentees.collectAsState()
    val allMembers by viewModel.allMembers.collectAsState()
    val allAttendance by viewModel.allAttendance.collectAsState()
    val currentDutyGroup by viewModel.currentDutyGroupEntity.collectAsState()
    val dutyGroupNotes by viewModel.dutyGroupNotes.collectAsState()
    val dutyAttendanceSummary by viewModel.dutyGroupAttendanceSummary.collectAsState()

    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedQuarter by viewModel.selectedQuarter.collectAsState()
    val monthlyStats by viewModel.monthlyStats.collectAsState()
    val quarterlyStats by viewModel.quarterlyStats.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Overview, 1: Analytics, 2: Group Leads & Settings, 3: Updates & Contacts, 4: Export
    var analyticsPeriodTab by remember { mutableStateOf(0) } // 0: Monthly, 1: Quarterly, 2: Absentees
    var adminManagementSubTab by remember { mutableStateOf(0) } // 0: Accounts/Security, 1: Portal & Group Live Control, 2: Equipment / Weapons
    var adminUpdatesSubTab by remember { mutableStateOf(0) } // 0: Daily Updates, 1: Executive Contacts

    var userForCredentialsEdit by remember { mutableStateOf<UserEntity?>(null) }
    var editUsernameInput by remember { mutableStateOf("") }
    var editPasswordInput by remember { mutableStateOf("") }
    var showPasswordInDialog by remember { mutableStateOf(false) }

    var selectedGroupDetails by remember { mutableStateOf<GroupEntity?>(null) }
    var selectedMemberForDetails by remember { mutableStateOf<MemberEntity?>(null) }
    var showEmergencyBroadcastDialog by remember { mutableStateOf(false) }
    var showDutyGroupDialog by remember { mutableStateOf(false) }
    var showMultiDateAnalyticsDialog by remember { mutableStateOf(false) }
    var multiDateAnalyticsGroupId by remember { mutableStateOf(0L) }
    var showSupabaseSyncDialog by remember { mutableStateOf(false) }
    var showAdminSystemSettingsDialog by remember { mutableStateOf(false) }

    var aiCustomPrompt by remember { mutableStateOf("") }
    var isAiGenerating by remember { mutableStateOf(false) }
    var aiGeneratedReport by remember { mutableStateOf<String?>(null) }

    // Back handler: if not on overview tab, go to overview tab first; if on overview tab, return to admin login input
    BackHandler(enabled = true) {
        if (selectedTab != 0) {
            selectedTab = 0
        } else {
            viewModel.logout(0L)
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Header Row: Admin Title & Logout button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = s.adminDashboardTitle,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val isSyncing by viewModel.isSupabaseSyncing.collectAsState()
                            IconButton(
                                onClick = { viewModel.pullAllFromSupabase(context) },
                                modifier = Modifier.size(38.dp).testTag("admin_sync_button")
                            ) {
                                val spinTransition = rememberInfiniteTransition(label = "spin_admin")
                                val rotation by spinTransition.animateFloat(
                                    initialValue = 0f,
                                    targetValue = 360f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1000, easing = LinearEasing)
                                    ),
                                    label = "rot"
                                )
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Supabase تىن يېڭىلاش",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .size(22.dp)
                                        .then(if (isSyncing) Modifier.rotate(rotation) else Modifier)
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            val allSessions by viewModel.allDeviceSessions.collectAsState()
                            val now = System.currentTimeMillis()
                            val uniqueActiveCount = remember(allSessions, now) {
                                val active = allSessions.filter { (now - it.lastActiveTime) <= 24 * 60 * 60 * 1000L }
                                active.map {
                                    "${it.deviceName.trim().lowercase()}_${it.osVersion.trim().lowercase()}_${it.lastLoginUser.trim()}".ifBlank { it.deviceId }
                                }.distinct().size
                            }
                            IconButton(
                                onClick = { showAdminSystemSettingsDialog = true },
                                modifier = Modifier.size(38.dp).testTag("admin_system_settings_button")
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (uniqueActiveCount > 0) {
                                            Badge(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = MaterialTheme.colorScheme.onPrimary
                                            ) {
                                                Text(
                                                    text = "$uniqueActiveCount",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "سىستېما ۋە تېلېفون تەڭشىكى",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            IconButton(
                                onClick = { viewModel.logout(0L) },
                                modifier = Modifier.size(38.dp).testTag("admin_logout_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = s.logoutButton,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_bottom_navigation")
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Dashboard,
                            contentDescription = s.overview,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    alwaysShowLabel = false,
                    modifier = Modifier.testTag("tab_overview")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = s.analytics,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    alwaysShowLabel = false,
                    modifier = Modifier.testTag("tab_analytics")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = s.adminSettingsTitle,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    alwaysShowLabel = false,
                    modifier = Modifier.testTag("tab_settings_gear")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = s.dailyUpdatesTitle,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    alwaysShowLabel = false,
                    modifier = Modifier.testTag("tab_daily_updates")
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = s.exportData,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    alwaysShowLabel = false,
                    modifier = Modifier.testTag("tab_export")
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                0 -> {
                    // TAB 0: OVERVIEW (ALL 6 GROUPS IN ONE DASHBOARD)
                    val totalMembers = allMembers.size
                    val totalPresent = groupStats.sumOf { it.presentCount }
                    val totalAbsent = groupStats.sumOf { it.absentCount }
                    val totalExcused = groupStats.sumOf { it.excusedCount }
                    val totalConsidered = (totalPresent + totalAbsent + totalExcused).coerceAtLeast(totalMembers)
                    val overallRate = if (totalConsidered > 0) {
                        (totalPresent / totalConsidered.toFloat()) * 100f
                    } else 0f

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Date selector strip
                        item {
                            DateSelectorStrip(
                                selectedDate = selectedDate,
                                onDateSelected = { viewModel.setSelectedDate(it) },
                                language = language
                            )
                        }

                        // Emergency Broadcast Action Card
                        item {
                            ElevatedCard(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showEmergencyBroadcastDialog = true }
                                    .testTag("admin_emergency_broadcast_card")
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Campaign,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = s.emergencyNoticeTitle,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                            Text(
                                                text = s.emergencyBroadcastPrompt,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                    Button(
                                        onClick = { showEmergencyBroadcastDialog = true },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error,
                                            contentColor = MaterialTheme.colorScheme.onError
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.testTag("open_emergency_broadcast_btn")
                                    ) {
                                        Text(s.sendEmergencyNotice, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Macro Attendance Banner
                        item {
                            ElevatedCard(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        multiDateAnalyticsGroupId = 0L
                                        showMultiDateAnalyticsDialog = true
                                    }
                                    .testTag("admin_macro_attendance_card")
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = s.allGroupsAttendanceRate,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Text(
                                                text = "${s.totalMembers}: $totalMembers | كۆپ كۈنلۈك / ئايلىق ئارخىپ ۋە نىسبەت 👆",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                            )
                                        }

                                        Text(
                                            text = "${String.format(Locale.US, "%.1f", overallRate)}%",
                                            style = MaterialTheme.typography.displayMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    val totalConsidered = (totalPresent + totalAbsent + totalExcused).coerceAtLeast(totalMembers)
                                    val presentPct = if (totalConsidered > 0) (totalPresent.toFloat() / totalConsidered) * 100f else 0f
                                    val absentPct = if (totalConsidered > 0) (totalAbsent.toFloat() / totalConsidered) * 100f else 0f
                                    val excusedPct = if (totalConsidered > 0) (totalExcused.toFloat() / totalConsidered) * 100f else 0f

                                    // 3 Mini Badges (Present, Absent, Excused) with Counts and Percentages
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        MiniStatBadge(s.statusPresent, "$totalPresent (${String.format(Locale.US, "%.0f%%", presentPct)})", PresentGreenContainer, PresentGreen)
                                        MiniStatBadge(s.statusAbsent, "$totalAbsent (${String.format(Locale.US, "%.0f%%", absentPct)})", AbsentRedContainer, AbsentRed)
                                        MiniStatBadge(s.statusExcused, "$totalExcused (${String.format(Locale.US, "%.0f%%", excusedPct)})", ExcusedBlueContainer, ExcusedBlue)
                                    }
                                }
                            }
                        }

                        // Duty Group (نۆۋبەتچى گۇرۇپپا) Window
                        item {
                            ElevatedCard(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                                ),
                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showDutyGroupDialog = true }
                                    .testTag("admin_duty_group_card")
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.secondary),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Shield,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSecondary,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = s.dutyGroupTitle,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                                Text(
                                                    text = if (currentDutyGroup != null) "${currentDutyGroup?.name} (${currentDutyGroup?.code})" else s.selectDutyGroup,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                            }
                                        }

                                        Button(
                                            onClick = { showDutyGroupDialog = true },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.secondary
                                            ),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.testTag("change_duty_group_btn")
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(s.changeDutyGroup, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Quick Switch Chips for all 6 Bayraqs & Sanjaqs
                                    Text(
                                        text = "نۆۋەتچى بايراق ۋە سانجاقلارنى تاللاش:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        groups.forEach { grp ->
                                            val isSelected = currentDutyGroup?.id == grp.id
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = {
                                                    viewModel.setDutyGroup(grp.id, grp.dutyNotes)
                                                },
                                                label = {
                                                    Text(
                                                        text = grp.name,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        fontSize = 12.sp
                                                    )
                                                },
                                                leadingIcon = if (isSelected) {
                                                    {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                    }
                                                } else null,
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                                                )
                                            )
                                        }
                                    }

                                    val cdg = currentDutyGroup
                                    if (cdg != null) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                // Designated Duty SubGroup Banner
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        val chosenSgName = if (cdg.dutySubGroupCustomName.isNotBlank()) {
                                                            cdg.dutySubGroupCustomName
                                                        } else if (dutyAttendanceSummary.dutySubGroupName.isNotBlank()) {
                                                            dutyAttendanceSummary.dutySubGroupName
                                                        } else {
                                                            "${cdg.dutySubGroup}-سانجاق"
                                                        }
                                                        Text(
                                                            text = "🎯 ${s.designatedDutySubGroup} (${cdg.name}):",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                                        )
                                                        Text(
                                                            text = chosenSgName,
                                                            style = MaterialTheme.typography.titleSmall,
                                                            fontWeight = FontWeight.Black,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                }

                                                if (dutyAttendanceSummary.dutySubGroupLeader.isNotBlank()) {
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = "👤 ${s.dutySubGroupLeader}: ${dutyAttendanceSummary.dutySubGroupLeader}",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        if (dutyAttendanceSummary.dutySubGroupContact.isNotBlank()) {
                                                            Text(
                                                                text = "📞 ${dutyAttendanceSummary.dutySubGroupContact}",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = MaterialTheme.colorScheme.primary
                                                            )
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(8.dp))

                                                // Attendance breakdown for designated duty subgroup
                                                Text(
                                                    text = "📊 ${dutyAttendanceSummary.dutySubGroupName} (${s.dutySubGroupMembers}: ${dutyAttendanceSummary.subGroupTotalMembers} ${s.memberCountLabel}) : ${s.statusPresent}: ${dutyAttendanceSummary.subGroupPresentCount} | ${s.statusAbsent}: ${dutyAttendanceSummary.subGroupAbsentCount} | ${s.statusExcused}: ${dutyAttendanceSummary.subGroupExcusedCount}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )

                                                Spacer(modifier = Modifier.height(6.dp))

                                                val dutyTotal = (dutyAttendanceSummary.presentCount + dutyAttendanceSummary.absentCount + dutyAttendanceSummary.excusedCount).coerceAtLeast(dutyAttendanceSummary.totalMembers)
                                                val dPresentPct = if (dutyTotal > 0) (dutyAttendanceSummary.presentCount.toFloat() / dutyTotal) * 100f else 0f
                                                val dAbsentPct = if (dutyTotal > 0) (dutyAttendanceSummary.absentCount.toFloat() / dutyTotal) * 100f else 0f
                                                val dExcusedPct = if (dutyTotal > 0) (dutyAttendanceSummary.excusedCount.toFloat() / dutyTotal) * 100f else 0f

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    MiniStatBadge(s.statusPresent, "${dutyAttendanceSummary.presentCount} (${String.format(Locale.US, "%.0f%%", dPresentPct)})", PresentGreenContainer, PresentGreen)
                                                    MiniStatBadge(s.statusAbsent, "${dutyAttendanceSummary.absentCount} (${String.format(Locale.US, "%.0f%%", dAbsentPct)})", AbsentRedContainer, AbsentRed)
                                                    MiniStatBadge(s.statusExcused, "${dutyAttendanceSummary.excusedCount} (${String.format(Locale.US, "%.0f%%", dExcusedPct)})", ExcusedBlueContainer, ExcusedBlue)
                                                }

                                                Spacer(modifier = Modifier.height(8.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = Icons.Default.DateRange,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = "${s.attendanceRate}: ${String.format(Locale.US, "%.0f", dutyAttendanceSummary.attendanceRate)}%",
                                                            style = MaterialTheme.typography.labelMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (dutyAttendanceSummary.attendanceRate >= 80f) PresentGreen else AbsentRed
                                                        )
                                                    }

                                                    Text(
                                                        text = "رۇخسەت: ${String.format(Locale.US, "%.0f", dExcusedPct)}%",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = ExcusedBlue
                                                    )
                                                }

                                                if (dutyAttendanceSummary.lastSubmittedTime != null) {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                                    ) {
                                                        Text(
                                                            text = "🕒 ${s.submittedAtLabel}: ${dutyAttendanceSummary.lastSubmittedTime}",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }

                                                if (dutyGroupNotes.isNotBlank() || currentDutyGroup?.dutyNotes?.isNotBlank() == true) {
                                                    val n = if (dutyGroupNotes.isNotBlank()) dutyGroupNotes else currentDutyGroup?.dutyNotes ?: ""
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Text(
                                                        text = "📝 ${s.dutyGroupNotes}: $n",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Section Title: 6 Groups Overview
                        item {
                            Text(
                                text = s.groupsOverview,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // 6 Individual Group Cards
                        items(groupStats, key = { it.group.id }) { stat ->
                            ElevatedCard(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedGroupDetails = stat.group }
                                    .testTag("group_card_${stat.group.id}")
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(MaterialTheme.colorScheme.primary),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = stat.group.code,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = stat.group.name,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "${s.totalMembers}: ${stat.totalMembers}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "${String.format(Locale.US, "%.0f", stat.attendanceRate)}%",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = if (stat.attendanceRate >= 80f) PresentGreen else AbsentRed
                                            )
                                            Text(
                                                text = s.attendanceRate,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Progress Bar
                                    LinearProgressIndicator(
                                        progress = { (stat.attendanceRate / 100f).coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = if (stat.attendanceRate >= 80f) PresentGreen else AbsentRed,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Sub-leaders display if present
                                    if (stat.group.subLeader1.isNotBlank() || stat.group.subLeader2.isNotBlank()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            if (stat.group.subLeader1.isNotBlank()) {
                                                Text(
                                                    text = "1: ${stat.group.subLeader1}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                            if (stat.group.subLeader2.isNotBlank()) {
                                                Text(
                                                    text = "2: ${stat.group.subLeader2}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }

                                    // Counters Row (Present, Absent, Excused)
                                    val gTotal = (stat.presentCount + stat.absentCount + stat.excusedCount + stat.lateCount).coerceAtLeast(stat.totalMembers)
                                    val gPresentPct = if (gTotal > 0) (stat.presentCount.toFloat() / gTotal) * 100f else 0f
                                    val gAbsentPct = if (gTotal > 0) (stat.absentCount.toFloat() / gTotal) * 100f else 0f
                                    val gExcusedPct = if (gTotal > 0) (stat.excusedCount.toFloat() / gTotal) * 100f else 0f

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        MiniStatBadge(s.statusPresent, "${stat.presentCount} (${String.format(Locale.US, "%.0f%%", gPresentPct)})", PresentGreenContainer, PresentGreen)
                                        MiniStatBadge(s.statusAbsent, "${stat.absentCount} (${String.format(Locale.US, "%.0f%%", gAbsentPct)})", AbsentRedContainer, AbsentRed)
                                        MiniStatBadge(s.statusExcused, "${stat.excusedCount} (${String.format(Locale.US, "%.0f%%", gExcusedPct)})", ExcusedBlueContainer, ExcusedBlue)
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: MONTHLY & QUARTERLY ANALYTICS & INSIGHTS
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Sub-navigation: Monthly / Quarterly / Absentees
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = analyticsPeriodTab == 0,
                                    onClick = { analyticsPeriodTab = 0 },
                                    label = { Text(s.monthlyStats, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = analyticsPeriodTab == 1,
                                    onClick = { analyticsPeriodTab = 1 },
                                    label = { Text(s.quarterlyStats, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = analyticsPeriodTab == 2,
                                    onClick = { analyticsPeriodTab = 2 },
                                    label = { Text(s.topAbsenteesTitle, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        when (analyticsPeriodTab) {
                            0 -> {
                                // MONTHLY VIEW
                                val currentMonthInt = selectedMonth.takeLast(2).toIntOrNull() ?: 1
                                item {
                                    // Month selector (1..12)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = s.selectMonth,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        (1..6).forEach { monthNum ->
                                            val isSel = currentMonthInt == monthNum
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clickable { viewModel.setSelectedMonth(monthNum) }
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "$monthNum",
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                        (7..12).forEach { monthNum ->
                                            val isSel = currentMonthInt == monthNum
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clickable { viewModel.setSelectedMonth(monthNum) }
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "$monthNum",
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                item {
                                    Text(
                                        text = "${s.monthlyStats} ($selectedMonth - ${s.monthUnit})",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                items(monthlyStats, key = { it.group.id }) { stat ->
                                    ElevatedCard(
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = stat.group.name,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "${String.format(Locale.US, "%.0f", stat.attendanceRate)}%",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (stat.attendanceRate >= 80f) PresentGreen else AbsentRed
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            LinearProgressIndicator(
                                                progress = { (stat.attendanceRate / 100f).coerceIn(0f, 1f) },
                                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                                color = if (stat.attendanceRate >= 80f) PresentGreen else AbsentRed
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            val mTotal = (stat.presentCount + stat.absentCount + stat.excusedCount).coerceAtLeast(stat.totalRecords).coerceAtLeast(1)
                                            val mPresentPct = (stat.presentCount.toFloat() / mTotal) * 100f
                                            val mAbsentPct = (stat.absentCount.toFloat() / mTotal) * 100f
                                            val mExcusedPct = (stat.excusedCount.toFloat() / mTotal) * 100f

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                MiniStatBadge(s.statusPresent, "${stat.presentCount} (${String.format(Locale.US, "%.0f%%", mPresentPct)})", PresentGreenContainer, PresentGreen)
                                                MiniStatBadge(s.statusAbsent, "${stat.absentCount} (${String.format(Locale.US, "%.0f%%", mAbsentPct)})", AbsentRedContainer, AbsentRed)
                                                MiniStatBadge(s.statusExcused, "${stat.excusedCount} (${String.format(Locale.US, "%.0f%%", mExcusedPct)})", ExcusedBlueContainer, ExcusedBlue)
                                            }
                                        }
                                    }
                                }
                            }

                            1 -> {
                                // QUARTERLY VIEW (Q1, Q2, Q3, Q4)
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        (1..4).forEach { qNum ->
                                            FilterChip(
                                                selected = selectedQuarter == qNum,
                                                onClick = { viewModel.setSelectedQuarter(qNum) },
                                                label = { Text("${s.quarterUnit} $qNum", fontWeight = FontWeight.Bold) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                ),
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }

                                item {
                                    Text(
                                        text = "${s.quarterlyStats} (${s.quarterUnit} $selectedQuarter)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                items(quarterlyStats, key = { it.group.id }) { stat ->
                                    ElevatedCard(
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = stat.group.name,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "${String.format(Locale.US, "%.0f", stat.attendanceRate)}%",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (stat.attendanceRate >= 80f) PresentGreen else AbsentRed
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            LinearProgressIndicator(
                                                progress = { (stat.attendanceRate / 100f).coerceIn(0f, 1f) },
                                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                                color = if (stat.attendanceRate >= 80f) PresentGreen else AbsentRed
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            val qTotal = (stat.presentCount + stat.absentCount + stat.excusedCount).coerceAtLeast(stat.totalRecords).coerceAtLeast(1)
                                            val qPresentPct = (stat.presentCount.toFloat() / qTotal) * 100f
                                            val qAbsentPct = (stat.absentCount.toFloat() / qTotal) * 100f
                                            val qExcusedPct = (stat.excusedCount.toFloat() / qTotal) * 100f

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                MiniStatBadge(s.statusPresent, "${stat.presentCount} (${String.format(Locale.US, "%.0f%%", qPresentPct)})", PresentGreenContainer, PresentGreen)
                                                MiniStatBadge(s.statusAbsent, "${stat.absentCount} (${String.format(Locale.US, "%.0f%%", qAbsentPct)})", AbsentRedContainer, AbsentRed)
                                                MiniStatBadge(s.statusExcused, "${stat.excusedCount} (${String.format(Locale.US, "%.0f%%", qExcusedPct)})", ExcusedBlueContainer, ExcusedBlue)
                                            }
                                        }
                                    }
                                }
                            }

                            2 -> {
                                // TOP ABSENTEES VIEW
                                if (topAbsentees.isEmpty()) {
                                    item {
                                        EmptyStateCard(message = s.all)
                                    }
                                } else {
                                    items(topAbsentees.take(10)) { item ->
                                        ElevatedCard(
                                            shape = RoundedCornerShape(14.dp),
                                            colors = CardDefaults.elevatedCardColors(
                                                containerColor = MaterialTheme.colorScheme.surface
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(14.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(38.dp)
                                                        .clip(CircleShape)
                                                        .background(AbsentRedContainer),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = item.absentCount.toString(),
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = AbsentRed
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = item.member.name,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = "${item.group?.name ?: ""} | ${s.statusAbsent}: ${item.absentCount} ${s.absenceTimes}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(AbsentRedContainer)
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        text = "${String.format(Locale.US, "%.0f", item.absenceRate)}% ${s.statusAbsent}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = AbsentRed
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

                2 -> {
                    // TAB 2: ADMIN SETTINGS & UNIT CONTROLS (Gear Menu)
                    Column(modifier = Modifier.fillMaxSize()) {
                        TabRow(
                            selectedTabIndex = adminManagementSubTab,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            Tab(
                                selected = adminManagementSubTab == 0,
                                onClick = { adminManagementSubTab = 0 },
                                text = { Text("1. تەڭشەكلەر", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                            )
                            Tab(
                                selected = adminManagementSubTab == 1,
                                onClick = { adminManagementSubTab = 1 },
                                text = { Text("2. بايراق-قىسىملار", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                            )
                            Tab(
                                selected = adminManagementSubTab == 2,
                                onClick = { adminManagementSubTab = 2 },
                                text = { Text("3. ھېساباتلار", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                            )
                            Tab(
                                selected = adminManagementSubTab == 3,
                                onClick = { adminManagementSubTab = 3 },
                                text = { Text("4. قورال-ياراغ", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                            )
                        }

                        when (adminManagementSubTab) {
                            0 -> {
                                // 1. SYSTEM, CLOUD, APPEARANCE & DISPLAY SETTINGS
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    // 2. Color Theme Selection (رەڭ تاللاش)
                                    item {
                                        ElevatedCard(
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(36.dp)
                                                            .clip(CircleShape)
                                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.ColorLens,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column {
                                                        Text(
                                                            text = "2. رەڭ تاللاش (Theme Color)",
                                                            style = MaterialTheme.typography.titleMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Text(
                                                            text = "پۈتۈن سىستېمىنىڭ ئاساسىي كۆرۈنۈش رەڭگىنى تاللاڭ",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(12.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    com.example.ui.theme.AppThemePreset.values().forEach { preset ->
                                                        val name = when (preset) {
                                                            com.example.ui.theme.AppThemePreset.BLUE -> s.themeBlue
                                                            com.example.ui.theme.AppThemePreset.EMERALD -> s.themeEmerald
                                                            com.example.ui.theme.AppThemePreset.PURPLE -> s.themePurple
                                                            com.example.ui.theme.AppThemePreset.AMBER -> s.themeAmber
                                                        }
                                                        val isSel = currentThemePreset == preset
                                                        FilterChip(
                                                            selected = isSel,
                                                            onClick = { viewModel.setThemePreset(preset) },
                                                            label = { Text(name, fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                                                            colors = FilterChipDefaults.filterChipColors(
                                                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                            ),
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // 3. Night & Day Mode (كېچە ۋە كۈندۈزنى ئۆزگەرتىش)
                                    item {
                                        ElevatedCard(
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(36.dp)
                                                            .clip(CircleShape)
                                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column {
                                                        Text(
                                                            text = "3. كېچە ۋە كۈندۈز ھالىتى",
                                                            style = MaterialTheme.typography.titleMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Text(
                                                            text = if (isDarkMode) "نۆۋەتتە: كېچە ھالىتى (قاراڭغۇ كۆرۈنۈش)" else "نۆۋەتتە: كۈندۈز ھالىتى (يورۇق كۆرۈنۈش)",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                                Switch(
                                                    checked = isDarkMode,
                                                    onCheckedChange = { viewModel.toggleDarkMode() },
                                                    modifier = Modifier.testTag("settings_dark_mode_switch")
                                                )
                                            }
                                        }
                                    }

                                    // 4. Language Selection (تىل تاللاش)
                                    item {
                                        ElevatedCard(
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Text(
                                                    text = "4. تىل تاللاش (Language Selection)",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.height(10.dp))
                                                LanguageToggleHeader(
                                                    currentLanguage = language,
                                                    onToggle = { viewModel.toggleLanguage() },
                                                    onLanguageSelected = { viewModel.setLanguage(it) },
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        }
                                    }

                                    // 5. Leader Attendance Display Toggle
                                    item {
                                        ElevatedCard(
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "بايراق رەھبەر يوقلىمىسىنى كۆرسىتىش",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = "بار، يوق، باشقا يەردە خىزمەتتە، ئارامدا ھالەتلىرى كۆرۈنىدۇ",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Switch(
                                                    checked = leaderAttendanceVisible,
                                                    onCheckedChange = { viewModel.toggleLeaderAttendanceVisible() },
                                                    modifier = Modifier.testTag("settings_leader_att_switch")
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            1 -> {
                                // 2. ACTIVE PORTALS & REMOTE MANAGEMENT OF ALL BAYRAQ / QISIM UNITS
                                ActivePortalsManagementView(viewModel = viewModel)
                            }

                            2 -> {
                                // 3. ACCOUNTS & SECURITY
                                val groupMap = groups.associateBy { it.id }
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    item {
                                        Text(
                                            text = s.adminSettingsTitle,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    items(users, key = { it.id }) { u ->
                                        val userGroup = groupMap[u.groupId]
                                        ElevatedCard(
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.elevatedCardColors(
                                                containerColor = MaterialTheme.colorScheme.surface
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            if (u.role == com.example.data.model.UserRole.ADMIN) MaterialTheme.colorScheme.primaryContainer
                                                            else MaterialTheme.colorScheme.secondaryContainer
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.AdminPanelSettings,
                                                        contentDescription = null,
                                                        tint = if (u.role == com.example.data.model.UserRole.ADMIN) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = u.displayName,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = "${userGroup?.name ?: if (u.role == com.example.data.model.UserRole.ADMIN) s.roleAdmin else ""} | ${s.username}: ${u.loginName}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    if (u.passwordHash.isEmpty()) {
                                                        Text(
                                                            text = "🔓 ${s.noPasswordSet}",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = MaterialTheme.colorScheme.secondary
                                                        )
                                                    } else {
                                                        Text(
                                                            text = "${s.password}: ••••••",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                        )
                                                    }
                                                }

                                                Button(
                                                    onClick = {
                                                        userForCredentialsEdit = u
                                                        editUsernameInput = u.loginName
                                                        editPasswordInput = ""
                                                        showPasswordInDialog = false
                                                    },
                                                    shape = RoundedCornerShape(10.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.primary
                                                    ),
                                                    modifier = Modifier.testTag("edit_creds_btn_${u.loginName}")
                                                ) {
                                                    Icon(Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(s.resetPassword, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            3 -> {
                                // 4. EQUIPMENT & WEAPONS INVENTORY
                                EquipmentManagementView(groupId = 0L, viewModel = viewModel, canEdit = true)
                            }
                        }
                    }
                }

                3 -> {
                    // TAB 3: DAILY UPDATES & EXECUTIVE LEADERSHIP CONTACTS
                    Column(modifier = Modifier.fillMaxSize()) {
                        TabRow(
                            selectedTabIndex = adminUpdatesSubTab,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            Tab(
                                selected = adminUpdatesSubTab == 0,
                                onClick = { adminUpdatesSubTab = 0 },
                                text = { Text(s.dailyUpdatesTitle, fontWeight = FontWeight.Bold) },
                                icon = { Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(20.dp)) }
                            )
                            Tab(
                                selected = adminUpdatesSubTab == 1,
                                onClick = { adminUpdatesSubTab = 1 },
                                text = { Text(s.executiveContactsTitle, fontWeight = FontWeight.Bold) },
                                icon = { Icon(Icons.Default.ContactPhone, contentDescription = null, modifier = Modifier.size(20.dp)) }
                            )
                        }

                        if (adminUpdatesSubTab == 0) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = { showEmergencyBroadcastDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(s.sendEmergencyNotice, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                            DailyUpdatesView(
                                viewModel = viewModel,
                                targetGroupId = null // Super admin can see/post for all groups or filter
                            )
                        } else {
                            ExecutiveContactsView(
                                viewModel = viewModel
                            )
                        }
                    }
                }

                4 -> {
                    // TAB 4: EXPORT & REPORTS
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = s.exportTitle,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // 1. Group-specific & All groups professional multi-date Excel/Word Export Section
                        item {
                            GroupExportSection(viewModel = viewModel)
                        }

                        // Gemini AI Smart Report Export Card
                        item {
                            ElevatedCard(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                ),
                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("ai_export_card")
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(MaterialTheme.colorScheme.primary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = s.aiDataExportTitle,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Gemini 2.5 ئارقىلىق ئەقلىي تەھلىل، كۈندىلىك خۇلاسە ۋە ئالاھىدە فورماتتىكى دوكلات چىقىرىش",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Quick Preset Prompt Chips
                                    Text(
                                        text = "تېز تاللاش قېلىپلىرى:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        FilterChip(
                                            selected = aiCustomPrompt == s.presetPrompt1,
                                            onClick = { aiCustomPrompt = s.presetPrompt1 },
                                            label = { Text(s.presetPrompt1, style = MaterialTheme.typography.labelSmall) }
                                        )
                                        FilterChip(
                                            selected = aiCustomPrompt == s.presetPrompt2,
                                            onClick = { aiCustomPrompt = s.presetPrompt2 },
                                            label = { Text(s.presetPrompt2, style = MaterialTheme.typography.labelSmall) }
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    FilterChip(
                                        selected = aiCustomPrompt == s.presetPrompt3,
                                        onClick = { aiCustomPrompt = s.presetPrompt3 },
                                        label = { Text(s.presetPrompt3, style = MaterialTheme.typography.labelSmall) }
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = aiCustomPrompt,
                                        onValueChange = { aiCustomPrompt = it },
                                        label = { Text(s.aiPromptLabel) },
                                        placeholder = { Text(s.aiPromptHint) },
                                        minLines = 3,
                                        maxLines = 5,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("ai_custom_prompt_input")
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            if (isAiGenerating) return@Button
                                            isAiGenerating = true
                                            viewModel.generateAiExportReport(aiCustomPrompt) { resultReport ->
                                                aiGeneratedReport = resultReport
                                                isAiGenerating = false
                                            }
                                        },
                                        enabled = !isAiGenerating,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("generate_ai_export_button")
                                    ) {
                                        if (isAiGenerating) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(18.dp),
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(s.aiExportGenerating, fontWeight = FontWeight.Bold)
                                        } else {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(s.generateAiExport, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // Display Generated AI Report
                                    if (aiGeneratedReport != null) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            tonalElevation = 2.dp,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = s.aiExportResultTitle,
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )

                                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        IconButton(
                                                            onClick = {
                                                                viewModel.copyTextToClipboard(aiGeneratedReport ?: "", "AI Report")
                                                            },
                                                            modifier = Modifier.size(32.dp)
                                                        ) {
                                                            Icon(Icons.Default.ContentCopy, contentDescription = s.copyToClipboard, modifier = Modifier.size(16.dp))
                                                        }
                                                        IconButton(
                                                            onClick = {
                                                                val sendIntent = android.content.Intent().apply {
                                                                    action = android.content.Intent.ACTION_SEND
                                                                    putExtra(android.content.Intent.EXTRA_TEXT, aiGeneratedReport ?: "")
                                                                    type = "text/plain"
                                                                }
                                                                val shareIntent = android.content.Intent.createChooser(sendIntent, s.shareReport)
                                                                context.startActivity(shareIntent)
                                                            },
                                                            modifier = Modifier.size(32.dp)
                                                        ) {
                                                            Icon(Icons.Default.Share, contentDescription = s.shareReport, modifier = Modifier.size(16.dp))
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(8.dp))

                                                Text(
                                                    text = aiGeneratedReport ?: "",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // CSV Export Card
                        item {
                            ElevatedCard(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.FileDownload,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = s.exportCSV,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Excel, Google Sheets ۋە باشقا ئانالىز پروگراممىلىرىغا ماس كېلىدۇ",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Button(
                                        onClick = { viewModel.exportToCSV(context) },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("export_csv_button")
                                    ) {
                                        Icon(Icons.Default.FileDownload, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(s.exportCSV, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Summary Text Report Card
                        item {
                            ElevatedCard(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(MaterialTheme.colorScheme.secondaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = s.exportSummaryText,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Telegram, WhatsApp ياكى WeChat ئارقىلىق ئەۋەتىشكە تەييار دوكلات",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { viewModel.copySummaryReport(context) },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("copy_report_button")
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(s.copyToClipboard, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = { viewModel.shareSummaryReport(context) },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("share_report_button")
                                        ) {
                                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(s.shareReport, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Username and Password Dialog for Admin
    if (userForCredentialsEdit != null) {
        val targetUser = userForCredentialsEdit!!
        AlertDialog(
            onDismissRequest = { userForCredentialsEdit = null },
            title = {
                Text(
                    text = "${s.editCredentials}: ${targetUser.displayName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = editUsernameInput,
                        onValueChange = { editUsernameInput = it },
                        label = { Text(s.username) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_username_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editPasswordInput,
                        onValueChange = { editPasswordInput = it },
                        label = { Text(s.newPassword) },
                        placeholder = { Text(if (targetUser.passwordHash.isEmpty()) s.noPasswordSet else "••••••") },
                        visualTransformation = if (showPasswordInDialog) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPasswordInDialog = !showPasswordInDialog }) {
                                Icon(
                                    imageVector = if (showPasswordInDialog) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_password_input")
                    )

                    val hasPassword = targetUser.passwordHash.isNotEmpty()
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            if (hasPassword) {
                                viewModel.removeUserPassword(targetUser.id) {
                                    userForCredentialsEdit = null
                                }
                            } else {
                                val pwdToSet = if (editPasswordInput.isNotBlank()) editPasswordInput.trim() else "123456"
                                viewModel.resetLeaderPassword(targetUser.id, pwdToSet) {
                                    userForCredentialsEdit = null
                                }
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (hasPassword) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("toggle_password_button")
                    ) {
                        Icon(
                            Icons.Default.LockReset,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (hasPassword) s.removePassword else s.setPassword,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newUName = if (editUsernameInput.isNotBlank()) editUsernameInput.trim() else targetUser.loginName
                        val newPwd = if (editPasswordInput.isNotBlank()) editPasswordInput.trim() else targetUser.passwordHash
                        viewModel.updateUserCredentials(targetUser.id, newUName, newPwd, targetUser.displayName) {
                            userForCredentialsEdit = null
                        }
                    },
                    modifier = Modifier.testTag("confirm_reset_pwd_button")
                ) {
                    Text(s.save, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { userForCredentialsEdit = null }) {
                    Text(s.cancel)
                }
            }
        )
    }

    // Group Details Modal Dialog
    if (selectedGroupDetails != null) {
        val grp = selectedGroupDetails!!
        val grpMembers = allMembers.filter { it.groupId == grp.id }
        val grpStat = groupStats.find { it.group.id == grp.id }
        val sub1 = grpMembers.filter { it.subGroup == 1 }
        val sub2 = grpMembers.filter { it.subGroup == 2 }

        AlertDialog(
            onDismissRequest = { selectedGroupDetails = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = grp.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.0f", grpStat?.attendanceRate ?: 0f)}%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "${s.totalMembers}: ${grpMembers.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (grp.subLeader1.isNotBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${s.subGroup1LeaderLabel}: ${grp.subLeader1}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (!grp.subLeader1Contact.isNullOrBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF2E7D32).copy(alpha = 0.15f),
                                        modifier = Modifier.clickable { com.example.util.ContactUtils.openPhoneCall(context, grp.subLeader1Contact) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Phone, contentDescription = "تېلېفون", tint = Color(0xFF2E7D32), modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("تېلېفون", fontSize = 10.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                if (!grp.subLeader1Telegram.isNullOrBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF0088CC).copy(alpha = 0.15f),
                                        modifier = Modifier.clickable { com.example.util.ContactUtils.openTelegram(context, grp.subLeader1Telegram) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Send, contentDescription = "Telegram", tint = Color(0xFF0088CC), modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("TG", fontSize = 10.sp, color = Color(0xFF0088CC), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                if (!grp.subLeader1Whatsapp.isNullOrBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF1EBE5D).copy(alpha = 0.15f),
                                        modifier = Modifier.clickable { com.example.util.ContactUtils.openWhatsApp(context, grp.subLeader1Whatsapp) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = Color(0xFF1EBE5D), modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("WA", fontSize = 10.sp, color = Color(0xFF0F6E35), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (grp.subLeader2.isNotBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${s.subGroup2LeaderLabel}: ${grp.subLeader2}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.weight(1f)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (!grp.subLeader2Contact.isNullOrBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF2E7D32).copy(alpha = 0.15f),
                                        modifier = Modifier.clickable { com.example.util.ContactUtils.openPhoneCall(context, grp.subLeader2Contact) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Phone, contentDescription = "تېلېفون", tint = Color(0xFF2E7D32), modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("تېلېفون", fontSize = 10.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                if (!grp.subLeader2Telegram.isNullOrBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF0088CC).copy(alpha = 0.15f),
                                        modifier = Modifier.clickable { com.example.util.ContactUtils.openTelegram(context, grp.subLeader2Telegram) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Send, contentDescription = "Telegram", tint = Color(0xFF0088CC), modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("TG", fontSize = 10.sp, color = Color(0xFF0088CC), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                if (!grp.subLeader2Whatsapp.isNullOrBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF1EBE5D).copy(alpha = 0.15f),
                                        modifier = Modifier.clickable { com.example.util.ContactUtils.openWhatsApp(context, grp.subLeader2Whatsapp) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = Color(0xFF1EBE5D), modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("WA", fontSize = 10.sp, color = Color(0xFF0F6E35), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (sub1.isNotEmpty()) {
                            item {
                                Text(
                                    text = "🔹 ${s.subGroup1} (${sub1.size})",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                                )
                            }
                            items(sub1, key = { "sub1_${it.id}" }) { member ->
                                MemberDetailRow(
                                    member = member,
                                    subGroupLabel = s.subGroup1,
                                    context = context,
                                    onMemberClick = { selectedMemberForDetails = member }
                                )
                            }
                        }

                        if (sub2.isNotEmpty()) {
                            item {
                                Text(
                                    text = "🔹 ${s.subGroup2} (${sub2.size})",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                                )
                            }
                            items(sub2, key = { "sub2_${it.id}" }) { member ->
                                MemberDetailRow(
                                    member = member,
                                    subGroupLabel = s.subGroup2,
                                    context = context,
                                    onMemberClick = { selectedMemberForDetails = member }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedGroupDetails = null }) {
                    Text(s.confirm)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val grpId = grp.id
                        selectedGroupDetails = null
                        multiDateAnalyticsGroupId = grpId
                        showMultiDateAnalyticsDialog = true
                    }
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("كۆپ كۈنلۈك نىسبەت ۋە تەھلىل", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Member Full Details Comprehensive Dialog
    if (selectedMemberForDetails != null) {
        val member = selectedMemberForDetails!!
        val memberGroup = groups.find { it.id == member.groupId }
        val memberRecords = allAttendance.filter { it.memberId == member.id }
        val todayRecord = memberRecords.find { it.date == selectedDate }
        val totalDays = memberRecords.size
        val presentDays = memberRecords.count { it.status == AttendanceStatus.PRESENT }
        val absentDays = memberRecords.count { it.status == AttendanceStatus.ABSENT }
        val excusedDays = memberRecords.count { it.status == AttendanceStatus.EXCUSED }
        val memberRate = if (totalDays > 0) (presentDays.toFloat() / totalDays.toFloat()) * 100f else 100f

        AlertDialog(
            onDismissRequest = { selectedMemberForDetails = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = member.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${memberGroup?.name ?: ""} | ${if (member.subGroup == 1) s.subGroup1 else s.subGroup2}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (todayRecord?.status) {
                            AttendanceStatus.PRESENT -> PresentGreenContainer
                            AttendanceStatus.ABSENT -> AbsentRedContainer
                            AttendanceStatus.EXCUSED -> ExcusedBlueContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Text(
                            text = when (todayRecord?.status) {
                                AttendanceStatus.PRESENT -> s.statusPresent
                                AttendanceStatus.ABSENT -> s.statusAbsent
                                AttendanceStatus.EXCUSED -> s.statusExcused
                                else -> s.statusPresent
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = when (todayRecord?.status) {
                                AttendanceStatus.PRESENT -> PresentGreen
                                AttendanceStatus.ABSENT -> AbsentRed
                                AttendanceStatus.EXCUSED -> ExcusedBlue
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Contact Info Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "📞 ${s.executiveContactsTitle}:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (member.contactAddress.isNotBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${s.phoneContact}: ${member.contactAddress}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    IconButton(
                                        onClick = { com.example.util.ContactUtils.openPhoneCall(context, member.contactAddress) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            if (member.telegramContact.isNotBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "✈ Telegram: ${member.telegramContact}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF0088CC),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    IconButton(
                                        onClick = { com.example.util.ContactUtils.openTelegram(context, member.telegramContact) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFF0088CC), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            if (member.whatsappContact.isNotBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "💬 WhatsApp: ${member.whatsappContact}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF1EBE5D),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    IconButton(
                                        onClick = { com.example.util.ContactUtils.openWhatsApp(context, member.whatsappContact) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFF1EBE5D), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            if (member.otherContact.isNotBlank()) {
                                Text(
                                    text = "📝 ${s.otherContact}: ${member.otherContact}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Attendance Summary Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = s.memberAttendanceSummary,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${String.format(Locale.US, "%.0f", memberRate)}%",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (memberRate >= 80f) PresentGreen else AbsentRed
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("✅ ${s.presentDays}: $presentDays", style = MaterialTheme.typography.bodySmall, color = PresentGreen, fontWeight = FontWeight.Bold)
                                Text("❌ ${s.absentDays}: $absentDays", style = MaterialTheme.typography.bodySmall, color = AbsentRed, fontWeight = FontWeight.Bold)
                                Text("ℹ️ ${s.excusedDays}: $excusedDays", style = MaterialTheme.typography.bodySmall, color = ExcusedBlue, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Recent Attendance History (Last 5 records)
                    if (memberRecords.isNotEmpty()) {
                        Text(
                            text = "🗓️ ${s.recentAttendanceHistory}:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(memberRecords.sortedByDescending { it.date }.take(7)) { rec ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp, vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = rec.date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                                    Text(
                                        text = when (rec.status) {
                                            AttendanceStatus.PRESENT -> s.statusPresent
                                            AttendanceStatus.ABSENT -> s.statusAbsent
                                            AttendanceStatus.EXCUSED -> s.statusExcused
                                            else -> s.statusPresent
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = when (rec.status) {
                                            AttendanceStatus.PRESENT -> PresentGreen
                                            AttendanceStatus.ABSENT -> AbsentRed
                                            AttendanceStatus.EXCUSED -> ExcusedBlue
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedMemberForDetails = null }) {
                    Text(s.confirm, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Duty Sanjaq Selection Dialog
    if (showDutyGroupDialog) {
        var selectedDutyGroupId by remember(currentDutyGroup?.id) { mutableStateOf(currentDutyGroup?.id ?: groups.firstOrNull()?.id ?: 1L) }
        val targetGrp = groups.find { it.id == selectedDutyGroupId }
        val initialSelectedSgs = remember(targetGrp?.dutySubGroupCustomName, targetGrp?.dutySubGroup, selectedDutyGroupId) {
            viewModel.parseDutySubGroups(targetGrp?.dutySubGroupCustomName ?: "", targetGrp?.dutySubGroup ?: 1).toSet()
        }
        var selectedDutySgs by remember(initialSelectedSgs) { mutableStateOf(initialSelectedSgs) }
        var dutyNotesInput by remember(targetGrp?.dutyNotes) { mutableStateOf(targetGrp?.dutyNotes ?: "") }

        AlertDialog(
            onDismissRequest = { showDutyGroupDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "نۆۋەتچى سانجاقلارنى تاللاش",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "نۆۋبەتچىلىك ئۆتەيدىغان بايراقنى تاللاڭ:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )

                    // Bayraq Selection Chips
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        groups.chunked(3).forEach { rowGroups ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                rowGroups.forEach { grp ->
                                    FilterChip(
                                        selected = selectedDutyGroupId == grp.id,
                                        onClick = { selectedDutyGroupId = grp.id },
                                        label = { Text(grp.name, style = MaterialTheme.typography.labelSmall) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "نۆۋەتچى سانجاقلارنى تاللاڭ (بىر ياكى بىر قانچىنى بىرلا ۋاقىتتا تاللىيالايسىز):",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        (1..4).forEach { sgNum ->
                            val isSelected = selectedDutySgs.contains(sgNum)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedDutySgs = if (isSelected) {
                                        if (selectedDutySgs.size > 1) selectedDutySgs - sgNum else selectedDutySgs
                                    } else {
                                        selectedDutySgs + sgNum
                                    }
                                },
                                label = { Text("$sgNum-سانجاق", style = MaterialTheme.typography.labelSmall) },
                                leadingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                } else null,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = dutyNotesInput,
                        onValueChange = { dutyNotesInput = it },
                        label = { Text(s.dutyGroupNotes) },
                        placeholder = { Text("مەسىلەن: 1-سانجاق ۋە 2-سانجاق كېچىلىك قاراۋۇللۇق ۋەزىپىسىنى ئۆتەيدۇ") },
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sgsList = if (selectedDutySgs.isEmpty()) listOf(1) else selectedDutySgs.toList().sorted()
                        val customName = sgsList.joinToString("، ") { "$it-سانجاق" }
                        viewModel.setDutyGroup(selectedDutyGroupId, dutyNotesInput.trim())
                        viewModel.setGroupDutySubGroups(
                            groupId = selectedDutyGroupId,
                            dutySubGroups = sgsList,
                            notes = dutyNotesInput.trim(),
                            customName = customName
                        )
                        showDutyGroupDialog = false
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text(s.save, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDutyGroupDialog = false }) {
                    Text(s.cancel)
                }
            }
        )
    }

    // Emergency Broadcast Dialog
    if (showEmergencyBroadcastDialog) {
        var noticeTitle by remember { mutableStateOf("") }
        var noticeContent by remember { mutableStateOf("") }
        var targetGroupId by remember { mutableStateOf<Long?>(null) } // null = all groups
        var hasError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showEmergencyBroadcastDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = s.emergencyNoticeTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = s.targetGroupLabel,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = targetGroupId == null,
                            onClick = { targetGroupId = null },
                            label = { Text(s.allGroups, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f)
                        )
                        groups.take(2).forEach { grp ->
                            FilterChip(
                                selected = targetGroupId == grp.id,
                                onClick = { targetGroupId = grp.id },
                                label = { Text(grp.name) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    if (groups.size > 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            groups.drop(2).take(3).forEach { grp ->
                                FilterChip(
                                    selected = targetGroupId == grp.id,
                                    onClick = { targetGroupId = grp.id },
                                    label = { Text(grp.name) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    if (groups.size > 5) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            groups.drop(5).forEach { grp ->
                                FilterChip(
                                    selected = targetGroupId == grp.id,
                                    onClick = { targetGroupId = grp.id },
                                    label = { Text(grp.name) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = noticeTitle,
                        onValueChange = {
                            noticeTitle = it
                            if (it.isNotBlank()) hasError = false
                        },
                        label = { Text(s.emergencyNoticeSubject) },
                        placeholder = { Text(s.emergencyNoticeSubjectHint) },
                        isError = hasError,
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = noticeContent,
                        onValueChange = {
                            noticeContent = it
                            if (it.isNotBlank()) hasError = false
                        },
                        label = { Text(s.emergencyNoticeBody) },
                        placeholder = { Text(s.emergencyNoticeBodyHint) },
                        isError = hasError,
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noticeTitle.isBlank() || noticeContent.isBlank()) {
                            hasError = true
                            return@Button
                        }
                        viewModel.broadcastEmergencyNotice(
                            title = noticeTitle.trim(),
                            content = noticeContent.trim(),
                            groupId = targetGroupId ?: 0L,
                            onSuccess = { showEmergencyBroadcastDialog = false }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(s.sendEmergencyNotice, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmergencyBroadcastDialog = false }) {
                    Text(s.cancel)
                }
            }
        )
    }

    // Multi-Date System & Group Analytics Dialog
    if (showMultiDateAnalyticsDialog) {
        MultiDateAnalyticsDialog(
            initialGroupId = multiDateAnalyticsGroupId,
            viewModel = viewModel,
            onDismiss = { showMultiDateAnalyticsDialog = false }
        )
    }

    // Supabase Cloud Sync & Backup Dialog
    if (showSupabaseSyncDialog) {
        SupabaseSyncManagementDialog(
            viewModel = viewModel,
            onDismiss = { showSupabaseSyncDialog = false }
        )
    }

    // Admin System & Device Remote Management Settings Dialog (⚙️)
    if (showAdminSystemSettingsDialog) {
        AdminSystemSettingsDialog(
            viewModel = viewModel,
            s = s,
            onDismiss = { showAdminSystemSettingsDialog = false }
        )
    }
}

@Composable
fun MemberDetailRow(
    member: MemberEntity,
    subGroupLabel: String,
    context: android.content.Context,
    onMemberClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onMemberClick() }
            .testTag("admin_member_row_${member.id}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (member.contactAddress.isNotBlank() || member.telegramContact.isNotBlank() || member.whatsappContact.isNotBlank()) {
                    Row(
                        modifier = Modifier.padding(top = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (member.contactAddress.isNotBlank()) {
                            Text(
                                text = "📞 ${member.contactAddress}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { com.example.util.ContactUtils.openPhoneCall(context, member.contactAddress) }
                            )
                        }
                        if (member.telegramContact.isNotBlank()) {
                            Text(
                                text = "✈ Telegram",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0088CC),
                                modifier = Modifier.clickable { com.example.util.ContactUtils.openTelegram(context, member.telegramContact) }
                            )
                        }
                        if (member.whatsappContact.isNotBlank()) {
                            Text(
                                text = "💬 WA",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1EBE5D),
                                modifier = Modifier.clickable { com.example.util.ContactUtils.openWhatsApp(context, member.whatsappContact) }
                            )
                        }
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            ) {
                Text(
                    text = subGroupLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

