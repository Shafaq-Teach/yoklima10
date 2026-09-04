package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Refresh
import com.example.ui.components.GroupLeadSettingsDialog
import com.example.ui.components.MultiDateAnalyticsDialog
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecordEntity
import com.example.data.model.AttendanceStatus
import com.example.data.model.MemberEntity
import com.example.data.model.UserEntity
import com.example.i18n.AppStrings
import com.example.i18n.Language
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Warning
import com.example.ui.components.AttendanceItemCard
import com.example.ui.components.BulkMemberImportDialog
import com.example.ui.components.DailyUpdatesView
import com.example.ui.components.ExecutiveContactsView
import com.example.ui.components.EquipmentManagementView
import com.example.ui.components.DateSelectorStrip
import com.example.ui.components.LanguageToggleHeader
import com.example.ui.components.SupabaseSyncManagementDialog
import com.example.ui.components.BayraqLeadersCard
import com.example.ui.components.SanjaqManagementCard
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.CloudSync
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.AbsentRedContainer
import com.example.ui.theme.ExcusedBlue
import com.example.ui.theme.ExcusedBlueContainer
import com.example.ui.theme.PresentGreen
import com.example.ui.theme.PresentGreenContainer
import com.example.ui.viewmodel.AttendanceViewModel
import java.util.Locale

@Composable
fun GroupLeadScreen(
    user: UserEntity,
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.currentLanguage.collectAsState()
    val s = AppStrings.get(language)
    val selectedDate by viewModel.selectedDate.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val group = groups.find { it.id == user.groupId }

    val members by viewModel.currentGroupMembers.collectAsState()
    val attendanceMap by viewModel.currentAttendanceMap.collectAsState()
    val allAttendance by viewModel.allAttendance.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val currentThemePreset by viewModel.themePreset.collectAsState()
    val activeEmergencyNotices by viewModel.activeEmergencyNotices.collectAsState()
    val unacknowledgedCount by viewModel.unacknowledgedNoticeCount.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Today's Attendance, 1: Members Roster, 2: Attendance History, 3: Equipment, 4: Daily Updates & Contacts
    var updatesSubTab by remember { mutableStateOf(0) } // 0: Daily Updates, 1: Executive Contacts
    var selectedSubGroupFilter by remember { mutableStateOf(0) } // 0: All, 1: SubGroup 1, 2: SubGroup 2...
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var showBulkImportDialog by remember { mutableStateOf(false) }
    var showDuplicateSubGroupDialog by remember { mutableStateOf(false) }
    var editingMember by remember { mutableStateOf<MemberEntity?>(null) }
    var memberToDelete by remember { mutableStateOf<MemberEntity?>(null) }
    var memberSearchQuery by remember { mutableStateOf("") }
    var showSupabaseSyncDialog by remember { mutableStateOf(false) }
    var showGroupLeadSettingsDialog by remember { mutableStateOf(false) }
    var showLeadAnalyticsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(user.groupId) {
        if (user.groupId != null) {
            viewModel.recordPortalActive(user.groupId)
            viewModel.loadSelectedSanjaqsForGroup(user.groupId)
        }
    }

    // Sub-group leader name and contact editing states
    var subLeader1Text by remember(group?.subLeader1) { mutableStateOf(group?.subLeader1 ?: "") }
    var subLeader1ContactText by remember(group?.subLeader1Contact) { mutableStateOf(group?.subLeader1Contact ?: "") }
    var subLeader1TelegramText by remember(group?.subLeader1Telegram) { mutableStateOf(group?.subLeader1Telegram ?: "") }
    var subLeader1WhatsappText by remember(group?.subLeader1Whatsapp) { mutableStateOf(group?.subLeader1Whatsapp ?: "") }
    var subLeader1OtherText by remember(group?.subLeader1Other) { mutableStateOf(group?.subLeader1Other ?: "") }

    var subLeader2Text by remember(group?.subLeader2) { mutableStateOf(group?.subLeader2 ?: "") }
    var subLeader2ContactText by remember(group?.subLeader2Contact) { mutableStateOf(group?.subLeader2Contact ?: "") }
    var subLeader2TelegramText by remember(group?.subLeader2Telegram) { mutableStateOf(group?.subLeader2Telegram ?: "") }
    var subLeader2WhatsappText by remember(group?.subLeader2Whatsapp) { mutableStateOf(group?.subLeader2Whatsapp ?: "") }
    var subLeader2OtherText by remember(group?.subLeader2Other) { mutableStateOf(group?.subLeader2Other ?: "") }

    // Back button behavior: if on tab > 0, return to tab 0; if on tab 0, return to group login input
    BackHandler(enabled = true) {
        if (selectedTab != 0) {
            selectedTab = 0
        } else {
            viewModel.logout(user.groupId)
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Header Row: Group Name & Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = group?.name ?: "",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (user.displayName.isNotBlank()) {
                                Text(
                                    text = user.displayName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val context = LocalContext.current
                            val isSyncing by viewModel.isSupabaseSyncing.collectAsState()

                            // Cloud Sync Refresh Button (Silent background execution)
                            IconButton(
                                onClick = { viewModel.pullAllFromSupabase(context) },
                                modifier = Modifier.size(36.dp).testTag("lead_sync_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "بۇلۇتتىن يېڭىلاش",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Settings Button (Gear ⚙️) - Consolidates Language, AutoSync/Refresh, Supabase, Theme, DarkMode
                            IconButton(
                                onClick = { showGroupLeadSettingsDialog = true },
                                modifier = Modifier.size(36.dp).testTag("lead_settings_gear_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Logout Button
                            IconButton(
                                onClick = { viewModel.logout(user.groupId) },
                                modifier = Modifier.size(36.dp).testTag("logout_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = s.logoutButton,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
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
                    .testTag("grouplead_bottom_navigation")
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = s.todayAttendance,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    alwaysShowLabel = false,
                    modifier = Modifier.testTag("tab_today_attendance")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = s.memberRoster,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    alwaysShowLabel = false,
                    modifier = Modifier.testTag("tab_member_roster")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = s.attendanceHistory,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    alwaysShowLabel = false,
                    modifier = Modifier.testTag("tab_group_history")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = s.equipmentInventory,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    alwaysShowLabel = false,
                    modifier = Modifier.testTag("tab_equipment")
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (unacknowledgedCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    ) {
                                        Text(
                                            text = if (unacknowledgedCount > 9) "9+" else "$unacknowledgedCount",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = s.dailyUpdatesTitle,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    alwaysShowLabel = false,
                    modifier = Modifier.testTag("tab_daily_updates")
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 1) {
                FloatingActionButton(
                    onClick = { showAddMemberDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_member_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = s.addMember)
                }
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
                    // TAB 0: TODAY'S ATTENDANCE
                    val selectedSanjaqs by viewModel.selectedSanjaqNumbers.collectAsState()
                    val allSanjaqEntities by viewModel.allSanjaqLeaders.collectAsState()
                    val currentGroupSanjaqs = allSanjaqEntities.filter { it.groupId == group?.id }.sortedBy { it.sanjaqNumber }

                    // Sanjaq-filtered members based on selected chips in SanjaqManagementCard
                    val displayedMembers = if (selectedSanjaqs.isEmpty()) members else members.filter { it.subGroup in selectedSanjaqs }
                    val displayedMemberIds = displayedMembers.map { it.id }.toSet()
                    val displayedRecords = displayedMembers.mapNotNull { attendanceMap[it.id] }

                    val displayedPresentCount = displayedRecords.count { it.status == AttendanceStatus.PRESENT }
                    val displayedAbsentCount = displayedRecords.count { it.status == AttendanceStatus.ABSENT }
                    val displayedExcusedCount = displayedRecords.count { it.status == AttendanceStatus.EXCUSED }
                    val displayedTotalMembersCount = displayedMembers.size

                    val totalConsidered = (displayedPresentCount + displayedAbsentCount + displayedExcusedCount).coerceAtLeast(displayedTotalMembersCount)
                    val rate = if (totalConsidered > 0) {
                        (displayedPresentCount / totalConsidered.toFloat()) * 100f
                    } else 0f

                    val availableSanjaqNumbers = (members.map { it.subGroup } + listOf(1, 2, 3, 4) + currentGroupSanjaqs.map { it.sanjaqNumber }).distinct().filter { it > 0 }.sorted()
                    val attendanceListState = androidx.compose.foundation.lazy.rememberLazyListState()

                    LazyColumn(
                        state = attendanceListState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Date Selector Strip
                        item(key = "date_selector_strip") {
                            DateSelectorStrip(
                                selectedDate = selectedDate,
                                onDateSelected = { viewModel.setSelectedDate(it) },
                                language = language
                            )
                        }

                        // Emergency Notices from Admin
                        if (activeEmergencyNotices.isNotEmpty()) {
                            items(activeEmergencyNotices, key = { "urgent_lead_${it.id}" }) { notice ->
                                ElevatedCard(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                    ),
                                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("emergency_notice_card_${notice.id}")
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Warning,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = s.emergencyAlertBanner,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Black,
                                                    color = MaterialTheme.colorScheme.onErrorContainer
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = notice.title,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = notice.content,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${notice.date} • ${notice.authorName}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                                            )
                                            Button(
                                                onClick = {
                                                    if (user.groupId != null) {
                                                        viewModel.acknowledgeNotice(notice.id, user.groupId)
                                                    } else {
                                                        viewModel.dismissEmergencyNotice(notice.id)
                                                    }
                                                    selectedTab = 4
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.error,
                                                    contentColor = MaterialTheme.colorScheme.onError
                                                ),
                                                shape = RoundedCornerShape(10.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                modifier = Modifier.testTag("dismiss_urgent_notice_${notice.id}")
                                            ) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = s.emergencyNoticeDismiss,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Locked date warning for non-admin
                        if (viewModel.isDateLockedForNonAdmin(selectedDate)) {
                            item(key = "locked_date_warning") {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = s.editLockedPastDate,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }

                        // 3 Key Bayraq Personnel Directory & Attendance Card (مەسئۇل، ئەركان، ئىدارى)
                        if (group != null) {
                            item(key = "bayraq_leaders_card") {
                                BayraqLeadersCard(
                                    group = group,
                                    viewModel = viewModel,
                                    s = s
                                )
                            }

                            // 4 Sanjaqs Multi-Select & Dynamic Consolidated Calculation Card
                            item(key = "sanjaq_mgmt_card") {
                                SanjaqManagementCard(
                                    group = group,
                                    viewModel = viewModel,
                                    s = s
                                )
                            }
                        }

                        // Duty Sanjaq Selection & Configuration Card (نۆۋەتچى سانجاقلارنى كۆرسىتىش ۋە بېكىتىش - بىر ياكى بىر قانچىنى تاللاش)
                        if (group != null) {
                            item(key = "duty_sanjaq_card") {
                                val context = LocalContext.current
                                val currentDutySgs = viewModel.parseDutySubGroups(group.dutySubGroupCustomName, group.dutySubGroup)
                                val isDutyLocked = viewModel.isDutySubGroupLocked(group.id)
                                val dutySgName = if (group.dutySubGroupCustomName.isNotBlank()) group.dutySubGroupCustomName
                                    else currentDutySgs.joinToString("، ") { sg ->
                                        currentGroupSanjaqs.find { it.sanjaqNumber == sg }?.sanjaqCustomName?.ifBlank { null } ?: "$sg-سانجاق"
                                    }

                                ElevatedCard(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("duty_subgroup_selector_card")
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Shield,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "نۆۋەتچى سانجاقلار:",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                                if (isDutyLocked) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Icon(
                                                        imageVector = Icons.Default.Lock,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.secondary
                                            ) {
                                                Text(
                                                    text = dutySgName,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSecondary,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = if (isDutyLocked) "نۆۋەتچى سانجاقلار بېكىتىلگىنىگە 24 سائەت توشتى (پەقەت باشقۇرغۇچى ئۆزگەرتەلەيدۇ):" else "نۆۋەتچى سانجاقلارنى تاللاڭ (بىر ياكى بىر قانچىنى بىرلا ۋاقىتتا تاللىغىلى بولىدۇ):",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isDutyLocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            availableSanjaqNumbers.forEach { sg ->
                                                val isSelected = currentDutySgs.contains(sg)
                                                val sName = currentGroupSanjaqs.find { it.sanjaqNumber == sg }?.sanjaqCustomName?.ifBlank { null }
                                                    ?: "$sg-سانجاق"
                                                FilterChip(
                                                    selected = isSelected,
                                                    onClick = {
                                                        if (isDutyLocked) {
                                                            android.widget.Toast.makeText(
                                                                context,
                                                                "نۆۋەتچى سانجاقلار بېكىتىلگىنىگە 24 سائەت توشتى، ئۆزگەرتىش پەقەت باشقۇرغۇچىغا ئوچۇق",
                                                                android.widget.Toast.LENGTH_LONG
                                                            ).show()
                                                        } else {
                                                            val newSelected = if (currentDutySgs.contains(sg)) {
                                                                if (currentDutySgs.size > 1) currentDutySgs - sg else currentDutySgs
                                                            } else {
                                                                (currentDutySgs + sg).sorted()
                                                            }
                                                            val namesList = newSelected.joinToString("، ") { num ->
                                                                currentGroupSanjaqs.find { it.sanjaqNumber == num }?.sanjaqCustomName?.ifBlank { null } ?: "$num-سانجاق"
                                                            }
                                                            viewModel.setGroupDutySubGroups(
                                                                groupId = group.id,
                                                                dutySubGroups = newSelected,
                                                                notes = group.dutyNotes,
                                                                customName = namesList
                                                            )
                                                        }
                                                    },
                                                    label = {
                                                        Text(
                                                            text = sName,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                            fontSize = 12.sp
                                                        )
                                                    },
                                                    leadingIcon = if (isSelected) {
                                                        {
                                                            Icon(
                                                                imageVector = Icons.Default.Check,
                                                                contentDescription = null,
                                                                modifier = Modifier.size(14.dp)
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
                                    }
                                }
                            }
                        }

                        // Mark All Present Button for active filtered sanjaqs
                        item(key = "mark_all_present_btn") {
                            Button(
                                onClick = { viewModel.markAllPresent(displayedMembers) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("mark_all_present_button")
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = s.markAllPresent,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Members Attendance Cards List
                        if (displayedMembers.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    message = s.noMembersInGroup,
                                    actionText = s.addMember,
                                    onAction = { showAddMemberDialog = true }
                                )
                            }
                        } else {
                            items(displayedMembers, key = { it.id }) { member ->
                                val record = attendanceMap[member.id]
                                val isLocked = viewModel.isRecordLocked(record)
                                AttendanceItemCard(
                                    member = member,
                                    record = record,
                                    language = language,
                                    isLocked = isLocked,
                                    onStatusSelected = { newStatus ->
                                        user.groupId?.let { gId ->
                                            viewModel.setAttendanceStatus(
                                                memberId = member.id,
                                                groupId = gId,
                                                status = newStatus,
                                                note = record?.note.orEmpty()
                                            )
                                        }
                                    },
                                    onClearStatus = {
                                        viewModel.unmarkAttendanceStatus(member.id)
                                    },
                                    onSaveNote = { newNote ->
                                        user.groupId?.let { gId ->
                                            val status = record?.status ?: AttendanceStatus.PRESENT
                                            viewModel.setAttendanceStatus(
                                                memberId = member.id,
                                                groupId = gId,
                                                status = status,
                                                note = newNote
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: MEMBER ROSTER MANAGEMENT WITH 2 SUB-GROUPS AND LEADERS WITH CONTACTS
                    val sub1Members = members.filter { it.subGroup == 1 && (memberSearchQuery.isBlank() || it.name.contains(memberSearchQuery, ignoreCase = true)) }
                    val sub2Members = members.filter { it.subGroup == 2 && (memberSearchQuery.isBlank() || it.name.contains(memberSearchQuery, ignoreCase = true)) }
                    val sub1LeaderCount = if (group?.subLeader1?.isNotBlank() == true) 1 else 0
                    val sub2LeaderCount = if (group?.subLeader2?.isNotBlank() == true) 1 else 0

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Action Bar: Search & Bulk Import & Duplicate SubGroup
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = memberSearchQuery,
                                    onValueChange = { memberSearchQuery = it },
                                    placeholder = { Text(s.searchMemberPlaceholder) },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("member_search_input")
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { showBulkImportDialog = true },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("open_bulk_import_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudUpload,
                                            contentDescription = s.smartBatchImport,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = s.smartBatchImport,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }

                                    Button(
                                        onClick = { showDuplicateSubGroupDialog = true },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("open_duplicate_subgroup_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = s.duplicateSubGroup,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = s.duplicateSubGroup,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        // Sub-group 1 Leader input & roster
                        item {
                            ElevatedCard(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${s.subGroup1} (${sub1Members.size + sub1LeaderCount} ${s.memberCountLabel})",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Sub-group 1 Leader Name Input
                                    OutlinedTextField(
                                        value = subLeader1Text,
                                        onValueChange = { subLeader1Text = it },
                                        label = { Text(s.subGroup1Leader) },
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("sub_leader_1_input")
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Sub-group 1 Leader Phone
                                    val ctx = androidx.compose.ui.platform.LocalContext.current
                                    OutlinedTextField(
                                        value = subLeader1ContactText,
                                        onValueChange = { subLeader1ContactText = it },
                                        label = { Text("${s.subGroup1Leader} ${s.phoneContact}") },
                                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                        trailingIcon = {
                                            if (subLeader1ContactText.isNotBlank()) {
                                                IconButton(onClick = { com.example.util.ContactUtils.openPhoneCall(ctx, subLeader1ContactText) }) {
                                                    Icon(Icons.Default.Phone, contentDescription = s.callPhone, tint = MaterialTheme.colorScheme.primary)
                                                }
                                            }
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("sub_leader_1_contact_input")
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Sub-group 1 Leader Telegram
                                    OutlinedTextField(
                                        value = subLeader1TelegramText,
                                        onValueChange = { subLeader1TelegramText = it },
                                        label = { Text(s.telegramContact) },
                                        placeholder = { Text("@username") },
                                        leadingIcon = { Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFF0088CC)) },
                                        trailingIcon = {
                                            if (subLeader1TelegramText.isNotBlank()) {
                                                IconButton(onClick = { com.example.util.ContactUtils.openTelegram(ctx, subLeader1TelegramText) }) {
                                                    Icon(Icons.Default.OpenInNew, contentDescription = s.openTelegram, tint = Color(0xFF0088CC))
                                                }
                                            }
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("sub_leader_1_telegram_input")
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Sub-group 1 Leader WhatsApp + Other + Save button row
                                    OutlinedTextField(
                                        value = subLeader1WhatsappText,
                                        onValueChange = { subLeader1WhatsappText = it },
                                        label = { Text(s.whatsappContact) },
                                        placeholder = { Text("+90555...") },
                                        leadingIcon = { Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFF1EBE5D)) },
                                        trailingIcon = {
                                            if (subLeader1WhatsappText.isNotBlank()) {
                                                IconButton(onClick = { com.example.util.ContactUtils.openWhatsApp(ctx, subLeader1WhatsappText) }) {
                                                    Icon(Icons.Default.OpenInNew, contentDescription = s.openWhatsApp, tint = Color(0xFF1EBE5D))
                                                }
                                            }
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("sub_leader_1_whatsapp_input")
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = subLeader1OtherText,
                                            onValueChange = { subLeader1OtherText = it },
                                            label = { Text(s.otherContact) },
                                            placeholder = { Text(s.otherContactHint) },
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("sub_leader_1_other_input")
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = {
                                                user.groupId?.let { gId ->
                                                    viewModel.updateGroupSubLeaders(
                                                        groupId = gId,
                                                        leader1 = subLeader1Text.trim(),
                                                        contact1 = subLeader1ContactText.trim(),
                                                        tele1 = subLeader1TelegramText.trim(),
                                                        wa1 = subLeader1WhatsappText.trim(),
                                                        other1 = subLeader1OtherText.trim(),
                                                        leader2 = subLeader2Text.trim(),
                                                        contact2 = subLeader2ContactText.trim(),
                                                        tele2 = subLeader2TelegramText.trim(),
                                                        wa2 = subLeader2WhatsappText.trim(),
                                                        other2 = subLeader2OtherText.trim()
                                                    )
                                                }
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                            modifier = Modifier.testTag("save_sub_leader_1_btn")
                                        ) {
                                            Icon(Icons.Default.Save, contentDescription = s.save, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(s.save, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    if (sub1Members.isEmpty()) {
                                        Text(
                                            text = s.noMembersInGroup,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    } else {
                                        sub1Members.forEach { member ->
                                            MemberRosterRow(
                                                member = member,
                                                language = language,
                                                onEdit = { editingMember = member },
                                                onDelete = { memberToDelete = member }
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                        }
                                    }
                                }
                            }
                        }

                        // Sub-group 2 Leader input & roster
                        item {
                            ElevatedCard(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${s.subGroup2} (${sub2Members.size + sub2LeaderCount} ${s.memberCountLabel})",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    val ctx = androidx.compose.ui.platform.LocalContext.current

                                    // Sub-group 2 Leader Name Input
                                    OutlinedTextField(
                                        value = subLeader2Text,
                                        onValueChange = { subLeader2Text = it },
                                        label = { Text(s.subGroup2Leader) },
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("sub_leader_2_input")
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Sub-group 2 Leader Phone
                                    OutlinedTextField(
                                        value = subLeader2ContactText,
                                        onValueChange = { subLeader2ContactText = it },
                                        label = { Text("${s.subGroup2Leader} ${s.phoneContact}") },
                                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                        trailingIcon = {
                                            if (subLeader2ContactText.isNotBlank()) {
                                                IconButton(onClick = { com.example.util.ContactUtils.openPhoneCall(ctx, subLeader2ContactText) }) {
                                                    Icon(Icons.Default.Phone, contentDescription = s.callPhone, tint = MaterialTheme.colorScheme.secondary)
                                                }
                                            }
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("sub_leader_2_contact_input")
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Sub-group 2 Leader Telegram
                                    OutlinedTextField(
                                        value = subLeader2TelegramText,
                                        onValueChange = { subLeader2TelegramText = it },
                                        label = { Text(s.telegramContact) },
                                        placeholder = { Text("@username") },
                                        leadingIcon = { Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFF0088CC)) },
                                        trailingIcon = {
                                            if (subLeader2TelegramText.isNotBlank()) {
                                                IconButton(onClick = { com.example.util.ContactUtils.openTelegram(ctx, subLeader2TelegramText) }) {
                                                    Icon(Icons.Default.OpenInNew, contentDescription = s.openTelegram, tint = Color(0xFF0088CC))
                                                }
                                            }
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("sub_leader_2_telegram_input")
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Sub-group 2 Leader WhatsApp + Other + Save button row
                                    OutlinedTextField(
                                        value = subLeader2WhatsappText,
                                        onValueChange = { subLeader2WhatsappText = it },
                                        label = { Text(s.whatsappContact) },
                                        placeholder = { Text("+90555...") },
                                        leadingIcon = { Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFF1EBE5D)) },
                                        trailingIcon = {
                                            if (subLeader2WhatsappText.isNotBlank()) {
                                                IconButton(onClick = { com.example.util.ContactUtils.openWhatsApp(ctx, subLeader2WhatsappText) }) {
                                                    Icon(Icons.Default.OpenInNew, contentDescription = s.openWhatsApp, tint = Color(0xFF1EBE5D))
                                                }
                                            }
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("sub_leader_2_whatsapp_input")
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = subLeader2OtherText,
                                            onValueChange = { subLeader2OtherText = it },
                                            label = { Text(s.otherContact) },
                                            placeholder = { Text(s.otherContactHint) },
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("sub_leader_2_other_input")
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = {
                                                user.groupId?.let { gId ->
                                                    viewModel.updateGroupSubLeaders(
                                                        groupId = gId,
                                                        leader1 = subLeader1Text.trim(),
                                                        contact1 = subLeader1ContactText.trim(),
                                                        tele1 = subLeader1TelegramText.trim(),
                                                        wa1 = subLeader1WhatsappText.trim(),
                                                        other1 = subLeader1OtherText.trim(),
                                                        leader2 = subLeader2Text.trim(),
                                                        contact2 = subLeader2ContactText.trim(),
                                                        tele2 = subLeader2TelegramText.trim(),
                                                        wa2 = subLeader2WhatsappText.trim(),
                                                        other2 = subLeader2OtherText.trim()
                                                    )
                                                }
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                            modifier = Modifier.testTag("save_sub_leader_2_btn")
                                        ) {
                                            Icon(Icons.Default.Save, contentDescription = s.save, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(s.save, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    if (sub2Members.isEmpty()) {
                                        Text(
                                            text = s.noMembersInGroup,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    } else {
                                        sub2Members.forEach { member ->
                                            MemberRosterRow(
                                                member = member,
                                                language = language,
                                                onEdit = { editingMember = member },
                                                onDelete = { memberToDelete = member }
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: ATTENDANCE HISTORY & ANALYTICS (يوقلىما تارىخى ۋە نىسبەت تەھلىلى)
                    val groupRecords = allAttendance.filter { it.groupId == user.groupId }
                    val datesWithRecords = groupRecords.map { it.date }.distinct().sortedDescending()

                    val totalPresentAll = groupRecords.count { it.status == AttendanceStatus.PRESENT }
                    val totalAbsentAll = groupRecords.count { it.status == AttendanceStatus.ABSENT }
                    val totalExcusedAll = groupRecords.count { it.status == AttendanceStatus.EXCUSED }
                    val totalAllRecords = totalPresentAll + totalAbsentAll + totalExcusedAll
                    val overallRate = if (totalAllRecords > 0) (totalPresentAll / totalAllRecords.toFloat()) * 100f else 0f

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = s.attendanceHistory,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Button(
                                    onClick = { showLeadAnalyticsDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("تەپسىلىي كۆپ كۈنلۈك تەھلىل", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }

                        // Group Overall Rate & Analytics Summary Card
                        item {
                            ElevatedCard(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    val excusedRateAll = if (totalAllRecords > 0) (totalExcusedAll.toFloat() / totalAllRecords) * 100f else 0f
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "ئومۇمىي يوقلىما نىسبىتى",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "${String.format(Locale.US, "%.1f", overallRate)}%",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Black,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "رۇخسەت: ${String.format(Locale.US, "%.1f", excusedRateAll)}%",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = ExcusedBlue
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        MiniStatBadge(s.statusPresent, "$totalPresentAll (${if (totalAllRecords > 0) String.format(Locale.US, "%.0f%%", (totalPresentAll.toFloat()/totalAllRecords)*100f) else "0%"})", PresentGreenContainer, PresentGreen)
                                        MiniStatBadge(s.statusAbsent, "$totalAbsentAll (${if (totalAllRecords > 0) String.format(Locale.US, "%.0f%%", (totalAbsentAll.toFloat()/totalAllRecords)*100f) else "0%"})", AbsentRedContainer, AbsentRed)
                                        MiniStatBadge(s.statusExcused, "$totalExcusedAll (${if (totalAllRecords > 0) String.format(Locale.US, "%.0f%%", (totalExcusedAll.toFloat()/totalAllRecords)*100f) else "0%"})", ExcusedBlueContainer, ExcusedBlue)
                                    }
                                }
                            }
                        }

                        if (datesWithRecords.isEmpty()) {
                            item {
                                EmptyStateCard(message = s.emptyAttendanceRecord)
                            }
                        } else {
                            items(datesWithRecords) { dateStr ->
                                val dayRecords = groupRecords.filter { it.date == dateStr }
                                val pCount = dayRecords.count { it.status == AttendanceStatus.PRESENT }
                                val aCount = dayRecords.count { it.status == AttendanceStatus.ABSENT }
                                val eCount = dayRecords.count { it.status == AttendanceStatus.EXCUSED }
                                val total = (pCount + aCount + eCount).coerceAtLeast(1)
                                val dayRate = (pCount / total.toFloat()) * 100f
                                val dayExcusedRate = (eCount / total.toFloat()) * 100f

                                ElevatedCard(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.setSelectedDate(dateStr)
                                            selectedTab = 0
                                        }
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${s.date}: $dateStr",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )

                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = "يوقلىما: ${String.format(Locale.US, "%.0f", dayRate)}%",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = "رۇخسەت: ${String.format(Locale.US, "%.0f", dayExcusedRate)}%",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ExcusedBlue
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            MiniStatBadge(s.statusPresent, "$pCount (${String.format(Locale.US, "%.0f%%", (pCount.toFloat()/total)*100f)})", PresentGreenContainer, PresentGreen)
                                            MiniStatBadge(s.statusAbsent, "$aCount (${String.format(Locale.US, "%.0f%%", (aCount.toFloat()/total)*100f)})", AbsentRedContainer, AbsentRed)
                                            MiniStatBadge(s.statusExcused, "$eCount (${String.format(Locale.US, "%.0f%%", (eCount.toFloat()/total)*100f)})", ExcusedBlueContainer, ExcusedBlue)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // TAB 3: EQUIPMENT / WEAPONS MANAGEMENT
                    EquipmentManagementView(
                        groupId = user.groupId ?: 1L,
                        viewModel = viewModel,
                        canEdit = group?.isSuspended != true
                    )
                }

                4 -> {
                    // TAB 4: DAILY UPDATES & EXECUTIVE CONTACTS
                    Column(modifier = Modifier.fillMaxSize()) {
                        TabRow(
                            selectedTabIndex = updatesSubTab,
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary
                        ) {
                            Tab(
                                selected = updatesSubTab == 0,
                                onClick = { updatesSubTab = 0 },
                                text = { Text(s.dailyUpdatesTitle, fontWeight = FontWeight.Bold) },
                                icon = { Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(20.dp)) }
                            )
                            Tab(
                                selected = updatesSubTab == 1,
                                onClick = { updatesSubTab = 1 },
                                text = { Text(s.executiveContactsTitle, fontWeight = FontWeight.Bold) },
                                icon = { Icon(Icons.Default.ContactPhone, contentDescription = null, modifier = Modifier.size(20.dp)) }
                            )
                        }

                        if (updatesSubTab == 0) {
                            DailyUpdatesView(
                                viewModel = viewModel,
                                targetGroupId = user.groupId
                            )
                        } else {
                            ExecutiveContactsView(
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }

    // Duplicate Sub-Group Dialog (ئىخچام، ئۈستى ۋە ئاستىدا بوش ئاق يەر يوق)
    if (showDuplicateSubGroupDialog && user.groupId != null) {
        val existingSubGroupsList = (members.map { it.subGroup } + listOf(1, 2)).distinct().filter { it > 0 }.sorted()
        var sourceSubGroup by remember { mutableStateOf(existingSubGroupsList.firstOrNull() ?: 1) }
        var targetSubGroup by remember { mutableStateOf((existingSubGroupsList.maxOrNull() ?: 2) + 1) }

        Dialog(
            onDismissRequest = { showDuplicateSubGroupDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .wrapContentHeight()
                    .testTag("duplicate_subgroup_dialog"),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = s.duplicateSubGroup,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(
                            onClick = { showDuplicateSubGroupDialog = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = s.cancel,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "كۆپەيتىدىغان ئەسلى گۇرۇپپا:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        existingSubGroupsList.forEach { sg ->
                            FilterChip(
                                selected = sourceSubGroup == sg,
                                onClick = { sourceSubGroup = sg },
                                label = { Text(if (sg == 1) s.subGroup1 else if (sg == 2) s.subGroup2 else "$sg${s.subGroupUnit}", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "يېڭى گۇرۇپپا رەت نومۇرى:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = targetSubGroup.toString(),
                        onValueChange = { targetSubGroup = it.toIntOrNull() ?: targetSubGroup },
                        label = { Text(s.subGroupUnit) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showDuplicateSubGroupDialog = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(s.cancel)
                        }

                        Button(
                            onClick = {
                                viewModel.duplicateSubGroup(
                                    groupId = user.groupId,
                                    sourceSubGroup = sourceSubGroup,
                                    newSubGroup = targetSubGroup,
                                    onSuccess = { showDuplicateSubGroupDialog = false }
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(s.confirm, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    val allSanjaqs by viewModel.allSanjaqLeaders.collectAsState()
    val groupSanjaqs = allSanjaqs.filter { it.groupId == user.groupId }.sortedBy { it.sanjaqNumber }

    // Add Member Dialog
    if (showAddMemberDialog && user.groupId != null) {
        MemberDialog(
            groupId = user.groupId,
            language = language,
            sanjaqs = groupSanjaqs,
            onAddNewSanjaq = { sanjaqName, onAdded ->
                viewModel.addNewSanjaqWithDetails(
                    groupId = user.groupId,
                    customName = sanjaqName,
                    onCreated = onAdded
                )
            },
            onDismiss = { showAddMemberDialog = false },
            onSave = { name, subGroup, contactAddress, telegramContact, whatsappContact, otherContact ->
                viewModel.addMember(
                    name = name,
                    groupId = user.groupId,
                    subGroup = subGroup,
                    contactAddress = contactAddress,
                    telegramContact = telegramContact,
                    whatsappContact = whatsappContact,
                    otherContact = otherContact
                )
            }
        )
    }

    // Bulk Member Import Dialog
    if (showBulkImportDialog && user.groupId != null) {
        BulkMemberImportDialog(
            groupId = user.groupId,
            viewModel = viewModel,
            onDismiss = { showBulkImportDialog = false }
        )
    }

    // Edit Member Dialog
    if (editingMember != null && user.groupId != null) {
        MemberDialog(
            initialMember = editingMember,
            groupId = user.groupId,
            language = language,
            sanjaqs = groupSanjaqs,
            onAddNewSanjaq = { sanjaqName, onAdded ->
                viewModel.addNewSanjaqWithDetails(
                    groupId = user.groupId,
                    customName = sanjaqName,
                    onCreated = onAdded
                )
            },
            onDismiss = { editingMember = null },
            onSave = { name, subGroup, contactAddress, telegramContact, whatsappContact, otherContact ->
                editingMember?.let { m ->
                    viewModel.updateMember(
                        m.copy(
                            name = name,
                            subGroup = subGroup,
                            contactAddress = contactAddress,
                            telegramContact = telegramContact,
                            whatsappContact = whatsappContact,
                            otherContact = otherContact
                        )
                    )
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (memberToDelete != null) {
        AlertDialog(
            onDismissRequest = { memberToDelete = null },
            title = {
                Text(
                    text = s.deleteMemberConfirmTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("${s.deleteMemberConfirmDesc}\n(${memberToDelete?.name})")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        memberToDelete?.let { viewModel.deleteMember(it) }
                        memberToDelete = null
                    }
                ) {
                    Text(s.deleteMember, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { memberToDelete = null }) {
                    Text(s.cancel)
                }
            }
        )
    }

    // Supabase Cloud Sync & Backup Dialog
    if (showSupabaseSyncDialog) {
        SupabaseSyncManagementDialog(
            viewModel = viewModel,
            onDismiss = { showSupabaseSyncDialog = false }
        )
    }

    // Consolidated App Settings Dialog (Gear Menu ⚙️)
    if (showGroupLeadSettingsDialog) {
        GroupLeadSettingsDialog(
            viewModel = viewModel,
            s = s,
            onOpenSupabaseSync = { showSupabaseSyncDialog = true },
            onDismiss = { showGroupLeadSettingsDialog = false }
        )
    }

    // Comprehensive Attendance Analytics & Reports Dialog (Restricted to this group only)
    if (showLeadAnalyticsDialog) {
        MultiDateAnalyticsDialog(
            initialGroupId = user.groupId ?: 1L,
            restrictToGroupOnly = true,
            viewModel = viewModel,
            onDismiss = { showLeadAnalyticsDialog = false }
        )
    }
}

@Composable
private fun MemberRosterRow(
    member: MemberEntity,
    language: Language,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val s = AppStrings.get(language)
    val ctx = androidx.compose.ui.platform.LocalContext.current
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.name.firstOrNull()?.toString() ?: "M",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (member.contactAddress.isNotBlank() || member.telegramContact.isNotBlank() || member.whatsappContact.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (member.contactAddress.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                modifier = Modifier.clickable {
                                    com.example.util.ContactUtils.openPhoneCall(ctx, member.contactAddress)
                                }
                            ) {
                                Text(
                                    text = "📞 ${member.contactAddress}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (member.telegramContact.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF0088CC).copy(alpha = 0.15f),
                                modifier = Modifier.clickable {
                                    com.example.util.ContactUtils.openTelegram(ctx, member.telegramContact)
                                }
                            ) {
                                Text(
                                    text = "✈ Telegram",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0088CC),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (member.whatsappContact.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF25D366).copy(alpha = 0.15f),
                                modifier = Modifier.clickable {
                                    com.example.util.ContactUtils.openWhatsApp(ctx, member.whatsappContact)
                                }
                            ) {
                                Text(
                                    text = "💬 WhatsApp",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1EBE5D),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = s.editMember,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = s.deleteMember,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MemberDialog(
    groupId: Long,
    language: Language,
    sanjaqs: List<com.example.data.model.SanjaqLeaderEntity> = emptyList(),
    initialMember: MemberEntity? = null,
    onAddNewSanjaq: ((name: String, onAdded: (Int) -> Unit) -> Unit)? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, subGroup: Int, contactAddress: String, telegramContact: String, whatsappContact: String, otherContact: String) -> Unit
) {
    val s = AppStrings.get(language)
    val ctx = androidx.compose.ui.platform.LocalContext.current
    var name by remember { mutableStateOf(initialMember?.name ?: "") }
    var subGroup by remember { mutableStateOf(initialMember?.subGroup ?: 1) }
    var contactAddress by remember { mutableStateOf(initialMember?.contactAddress ?: "") }
    var telegramContact by remember { mutableStateOf(initialMember?.telegramContact ?: "") }
    var whatsappContact by remember { mutableStateOf(initialMember?.whatsappContact ?: "") }
    var otherContact by remember { mutableStateOf(initialMember?.otherContact ?: "") }
    var hasError by remember { mutableStateOf(false) }
    var showCreateSanjaqPrompt by remember { mutableStateOf(false) }

    val existingNums = (sanjaqs.map { it.sanjaqNumber } + listOf(1, 2, 3, 4)).distinct().sorted()
    val sanjaqMap = sanjaqs.associateBy { it.sanjaqNumber }
    val displaySanjaqs = existingNums.map { num ->
        sanjaqMap[num] ?: com.example.data.model.SanjaqLeaderEntity(
            groupId = groupId,
            sanjaqNumber = num,
            sanjaqCustomName = "$num-سانجاق"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialMember == null) s.addMemberTitle else s.editMember,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) hasError = false
                    },
                    label = { Text(s.memberName) },
                    isError = hasError,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_name_input")
                )

                // Sanjaq selector with dynamic Sanjaqs and + button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ئەزا قوشۇلىدىغان سانجاقنى تاللاڭ:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (onAddNewSanjaq != null) {
                        IconButton(
                            onClick = { showCreateSanjaqPrompt = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Sanjaq",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    displaySanjaqs.forEach { sj ->
                        val isSelected = subGroup == sj.sanjaqNumber
                        val labelText = sj.sanjaqCustomName.ifBlank { "${sj.sanjaqNumber}-سانجاق" }
                        FilterChip(
                            selected = isSelected,
                            onClick = { subGroup = sj.sanjaqNumber },
                            label = { Text(labelText, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                // Phone Contact
                OutlinedTextField(
                    value = contactAddress,
                    onValueChange = { contactAddress = it },
                    label = { Text(s.phoneContact) },
                    placeholder = { Text("+90555...") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    trailingIcon = {
                        if (contactAddress.isNotBlank()) {
                            IconButton(onClick = { com.example.util.ContactUtils.openPhoneCall(ctx, contactAddress) }) {
                                Icon(Icons.Default.Phone, contentDescription = s.callPhone, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_phone_input")
                )

                // Telegram Contact
                OutlinedTextField(
                    value = telegramContact,
                    onValueChange = { telegramContact = it },
                    label = { Text(s.telegramContact) },
                    placeholder = { Text("@username") },
                    leadingIcon = { Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFF0088CC)) },
                    trailingIcon = {
                        if (telegramContact.isNotBlank()) {
                            IconButton(onClick = { com.example.util.ContactUtils.openTelegram(ctx, telegramContact) }) {
                                Icon(Icons.Default.OpenInNew, contentDescription = s.openTelegram, tint = Color(0xFF0088CC))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_telegram_input")
                )

                // WhatsApp Contact
                OutlinedTextField(
                    value = whatsappContact,
                    onValueChange = { whatsappContact = it },
                    label = { Text(s.whatsappContact) },
                    placeholder = { Text("+90555...") },
                    leadingIcon = { Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFF1EBE5D)) },
                    trailingIcon = {
                        if (whatsappContact.isNotBlank()) {
                            IconButton(onClick = { com.example.util.ContactUtils.openWhatsApp(ctx, whatsappContact) }) {
                                Icon(Icons.Default.OpenInNew, contentDescription = s.openWhatsApp, tint = Color(0xFF1EBE5D))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_whatsapp_input")
                )

                // Other Contact (Requirement 3)
                OutlinedTextField(
                    value = otherContact,
                    onValueChange = { otherContact = it },
                    label = { Text(s.otherContact) },
                    placeholder = { Text(s.otherContactHint) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_other_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        hasError = true
                    } else {
                        onSave(name.trim(), subGroup, contactAddress.trim(), telegramContact.trim(), whatsappContact.trim(), otherContact.trim())
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("save_member_btn")
            ) {
                Text(s.save, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(s.cancel)
            }
        }
    )

    if (showCreateSanjaqPrompt && onAddNewSanjaq != null) {
        val nextNum = if (displaySanjaqs.isEmpty()) 1 else (displaySanjaqs.maxOf { it.sanjaqNumber } + 1)
        var newSanjaqNameInput by remember { mutableStateOf("$nextNum-سانجاق") }
        AlertDialog(
            onDismissRequest = { showCreateSanjaqPrompt = false },
            title = {
                Text("يېڭى سانجاق قوشۇش", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            },
            text = {
                Column {
                    Text("سانجاق نامىنى كىرگۈزۈڭ، قوشۇلغاندىن كېيىن ئاپتوماتىك تاللىنىدۇ:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newSanjaqNameInput,
                        onValueChange = { newSanjaqNameInput = it },
                        label = { Text("سانجاق نامى") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAddNewSanjaq(newSanjaqNameInput.trim()) { createdNum ->
                            subGroup = createdNum
                        }
                        showCreateSanjaqPrompt = false
                    }
                ) {
                    Text("قوشۇش")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateSanjaqPrompt = false }) {
                    Text(s.cancel)
                }
            }
        )
    }
}

@Composable
fun MiniStatBadge(
    label: String,
    count: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = count,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = textColor
            )
        }
    }
}

@Composable
fun EmptyStateCard(
    message: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (actionText != null && onAction != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onAction,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(actionText, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
