package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyUpdateEntity
import com.example.data.model.UserRole
import com.example.i18n.AppStrings
import com.example.i18n.Language
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.AbsentRedContainer
import com.example.ui.theme.ExcusedBlue
import com.example.ui.theme.ExcusedBlueContainer
import com.example.ui.theme.LateAmber
import com.example.ui.theme.LateAmberContainer
import com.example.ui.theme.PresentGreen
import com.example.ui.theme.PresentGreenContainer
import com.example.ui.viewmodel.AttendanceViewModel

@Composable
fun DailyUpdatesView(
    viewModel: AttendanceViewModel,
    targetGroupId: Long? = null, // null = all for admin, or specific group
    modifier: Modifier = Modifier
) {
    val language by viewModel.currentLanguage.collectAsState()
    val s = AppStrings.get(language)
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val allUpdates by viewModel.allDailyUpdates.collectAsState()
    val allReceipts by viewModel.allNoticeReceipts.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val dutySummary by viewModel.dutyGroupAttendanceSummary.collectAsState()
    val isSyncing by viewModel.isSupabaseSyncing.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var updateToEdit by remember { mutableStateOf<DailyUpdateEntity?>(null) }
    var updateToDelete by remember { mutableStateOf<DailyUpdateEntity?>(null) }
    var selectedDetailUpdate by remember { mutableStateOf<DailyUpdateEntity?>(null) }
    var selectedPriorityFilter by remember { mutableStateOf<String?>(null) } // null = all, or NORMAL, IMPORTANT, URGENT

    val isAdmin = currentUser?.role == UserRole.ADMIN
    val currentUserGroupId = currentUser?.groupId

    // Auto pull latest data when viewing screen
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.pullAllFromSupabase(silent = true)
    }

    // Auto mark notices delivered for logged-in group lead and read locally on this device
    androidx.compose.runtime.LaunchedEffect(allUpdates, currentUserGroupId) {
        if (currentUserGroupId != null && currentUserGroupId > 0L) {
            allUpdates.forEach { u ->
                if (u.groupId == 0L || u.groupId == currentUserGroupId) {
                    viewModel.markNoticeDelivered(u.id, currentUserGroupId)
                }
            }
        }
        allUpdates.forEach { u ->
            com.example.util.LocalReadNoticeTracker.markNoticeAsRead(context, u.id)
        }
        com.example.util.AppNotificationManager.clearAllBadgeNotifications(context)
    }

    // Filter updates
    val filteredUpdates = allUpdates.filter { update ->
        val groupMatch = if (isAdmin && targetGroupId == null) {
            true
        } else {
            val gid = targetGroupId ?: currentUser?.groupId ?: 0L
            update.groupId == 0L || update.groupId == gid
        }
        val priorityMatch = selectedPriorityFilter == null || update.priority == selectedPriorityFilter
        groupMatch && priorityMatch
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Banner
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Campaign,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = s.dailyUpdatesTitle,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "${filteredUpdates.size} ${s.notes}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Cloud Refresh / Pull Button
                                Surface(
                                    onClick = { viewModel.pullAllFromSupabase(context) },
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shadowElevation = 2.dp,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("refresh_daily_updates_btn")
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        val spinTransition = rememberInfiniteTransition(label = "spin")
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
                                            contentDescription = "بۇلۇتتىن يېڭىلاش",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier
                                                .size(20.dp)
                                                .then(if (isSyncing) Modifier.rotate(rotation) else Modifier)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Surface(
                                    onClick = { showAddDialog = true },
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    shadowElevation = 3.dp,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("add_daily_update_btn")
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = s.addUpdate,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Priority Filters
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedPriorityFilter == null,
                                onClick = { selectedPriorityFilter = null },
                                label = { Text(s.allGroups) }
                            )
                            FilterChip(
                                selected = selectedPriorityFilter == "URGENT",
                                onClick = {
                                    selectedPriorityFilter = if (selectedPriorityFilter == "URGENT") null else "URGENT"
                                },
                                label = { Text(s.updatePriorityUrgent) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AbsentRedContainer,
                                    selectedLabelColor = AbsentRed
                                )
                            )
                            FilterChip(
                                selected = selectedPriorityFilter == "IMPORTANT",
                                onClick = {
                                    selectedPriorityFilter = if (selectedPriorityFilter == "IMPORTANT") null else "IMPORTANT"
                                },
                                label = { Text(s.updatePriorityImportant) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = LateAmberContainer,
                                    selectedLabelColor = LateAmber
                                )
                            )
                        }
                    }
                }
            }

            // Duty Group Live Attendance Status Card (Requirement 8)
            if (dutySummary.dutyGroup != null) {
                item {
                    ElevatedCard(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationImportant,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${s.dutyGroupTitle}: ${dutySummary.dutyGroup?.name}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }

                                if (dutySummary.lastSubmittedTime != null) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Text(
                                            text = "🕒 ${s.submittedAtLabel}: ${dutySummary.lastSubmittedTime}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    color = PresentGreenContainer
                                ) {
                                    Text(
                                        text = "${s.statusPresent}: ${dutySummary.presentCount}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = PresentGreen,
                                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 6.dp)
                                    )
                                }
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    color = AbsentRedContainer
                                ) {
                                    Text(
                                        text = "${s.statusAbsent}: ${dutySummary.absentCount}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = AbsentRed,
                                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 6.dp)
                                    )
                                }
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    color = ExcusedBlueContainer
                                ) {
                                    Text(
                                        text = "${s.statusExcused}: ${dutySummary.excusedCount}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ExcusedBlue,
                                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (filteredUpdates.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = s.noUpdatesYet,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            } else {
                items(filteredUpdates, key = { it.id }) { update ->
                    val receiptsForThis = allReceipts.filter { it.noticeId == update.id }
                    DailyUpdateCard(
                        update = update,
                        groups = groups,
                        receipts = receiptsForThis,
                        isAdmin = isAdmin,
                        currentUserId = currentUser?.id ?: 0L,
                        currentUserGroupId = currentUserGroupId,
                        language = language,
                        onOpenDetail = { selectedDetailUpdate = update },
                        onAcknowledge = {
                            if (currentUserGroupId != null) {
                                viewModel.acknowledgeNotice(update.id, currentUserGroupId)
                            }
                        },
                        onToggleCancel = { viewModel.toggleCancelDailyUpdate(update) },
                        onEdit = { updateToEdit = update },
                        onDelete = { updateToDelete = update }
                    )
                }
            }
        }
    }

    // Detail & Management Dialog when clicking any notice card
    if (selectedDetailUpdate != null) {
        val currentSelected = allUpdates.find { it.id == selectedDetailUpdate!!.id } ?: selectedDetailUpdate!!
        val receiptsForThis = allReceipts.filter { it.noticeId == currentSelected.id }
        NoticeDetailDialog(
            update = currentSelected,
            groups = groups,
            receipts = receiptsForThis,
            isAdmin = isAdmin,
            currentUserId = currentUser?.id ?: 0L,
            currentUserGroupId = currentUserGroupId,
            language = language,
            onAcknowledge = {
                if (currentUserGroupId != null) {
                    viewModel.acknowledgeNotice(currentSelected.id, currentUserGroupId)
                }
            },
            onToggleCancel = {
                viewModel.toggleCancelDailyUpdate(currentSelected)
            },
            onEdit = {
                val target = currentSelected
                selectedDetailUpdate = null
                updateToEdit = target
            },
            onDelete = {
                val target = currentSelected
                selectedDetailUpdate = null
                updateToDelete = target
            },
            onDismiss = { selectedDetailUpdate = null }
        )
    }

    // Add Update Dialog
    if (showAddDialog) {
        DailyUpdateEditDialog(
            initialUpdate = null,
            targetGroupId = targetGroupId ?: currentUser?.groupId ?: 0L,
            viewModel = viewModel,
            language = language,
            onDismiss = { showAddDialog = false }
        )
    }

    // Edit Update Dialog
    if (updateToEdit != null) {
        DailyUpdateEditDialog(
            initialUpdate = updateToEdit,
            targetGroupId = updateToEdit?.groupId ?: 0L,
            viewModel = viewModel,
            language = language,
            onDismiss = { updateToEdit = null }
        )
    }

    // Delete Confirmation
    if (updateToDelete != null) {
        AlertDialog(
            onDismissRequest = { updateToDelete = null },
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = s.deleteUpdate,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(s.deleteUpdateConfirm)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        updateToDelete?.let { viewModel.deleteDailyUpdate(it) }
                        updateToDelete = null
                    }
                ) {
                    Text(s.delete, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { updateToDelete = null }) {
                    Text(s.cancel)
                }
            }
        )
    }
}

@Composable
fun DailyUpdateCard(
    update: DailyUpdateEntity,
    groups: List<com.example.data.model.GroupEntity>,
    receipts: List<com.example.data.model.NoticeReceiptEntity>,
    isAdmin: Boolean,
    currentUserId: Long,
    currentUserGroupId: Long?,
    language: Language,
    onOpenDetail: () -> Unit,
    onAcknowledge: () -> Unit,
    onToggleCancel: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val s = AppStrings.get(language)
    val isCancelled = update.priority == "CANCELLED"

    val priorityColor = when (update.priority) {
        "CANCELLED" -> AbsentRed
        "URGENT" -> AbsentRed
        "IMPORTANT" -> LateAmber
        else -> MaterialTheme.colorScheme.primary
    }

    val priorityContainerColor = when (update.priority) {
        "CANCELLED" -> AbsentRedContainer
        "URGENT" -> AbsentRedContainer
        "IMPORTANT" -> LateAmberContainer
        else -> MaterialTheme.colorScheme.primaryContainer
    }

    val priorityLabel = when (update.priority) {
        "CANCELLED" -> s.noticeCancelled
        "URGENT" -> s.updatePriorityUrgent
        "IMPORTANT" -> s.updatePriorityImportant
        else -> s.updatePriorityNormal
    }

    // Formatted sent time (HH:mm)
    val sentTimeFormatted = remember(update.timestamp) {
        if (update.timestamp > 0L) {
            java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(update.timestamp))
        } else ""
    }

    val myGroupReceipt = receipts.find { it.groupId == currentUserGroupId }
    val isMyGroupAcknowledged = myGroupReceipt?.isAcknowledged == true

    ElevatedCard(
        onClick = onOpenDetail,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isCancelled) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (isCancelled) 1.dp else 2.dp),
        modifier = modifier.fillMaxWidth().testTag("notice_card_${update.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Cancelled notice prominent warning banner (visible to all groups)
            if (isCancelled) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = AbsentRedContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = null,
                            tint = AbsentRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "⚠️ " + s.noticeCancelled,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = AbsentRed
                        )
                    }
                }
            }

            // Top row: Group & Priority badge + Date & Timestamps & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Group Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = if (update.groupId == 0L) s.allGroupsUpdate else update.groupName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Priority Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = priorityContainerColor
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            if (update.priority == "URGENT" || update.priority == "IMPORTANT" || isCancelled) {
                                Icon(
                                    imageVector = if (isCancelled) Icons.Default.Block else Icons.Default.PriorityHigh,
                                    contentDescription = null,
                                    tint = priorityColor,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = priorityLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = priorityColor
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (sentTimeFormatted.isNotBlank()) "${update.date} ($sentTimeFormatted)" else update.date,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (isAdmin || update.groupId != 0L) {
                        IconButton(
                            onClick = onToggleCancel,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (isCancelled) Icons.Default.Restore else Icons.Default.Block,
                                contentDescription = if (isCancelled) s.uncancelNotice else s.cancelNotice,
                                tint = if (isCancelled) PresentGreen else AbsentRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = s.editUpdate,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = s.deleteUpdate,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = update.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Content Body
            Text(
                text = update.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            // Acknowledge Receipt Button for Group Leads
            if (currentUserGroupId != null && (update.groupId == 0L || update.groupId == currentUserGroupId)) {
                Spacer(modifier = Modifier.height(10.dp))
                if (!isMyGroupAcknowledged) {
                    Button(
                        onClick = onAcknowledge,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PresentGreen
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("btn_acknowledge_notice_${update.id}")
                    ) {
                        Text(
                            text = "✓ " + s.acknowledgeNoticeBtn,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PresentGreenContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val ackTime = if (myGroupReceipt.acknowledgedTimestamp > 0L) {
                            java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(myGroupReceipt.acknowledgedTimestamp))
                        } else ""
                        Text(
                            text = "✓ " + s.acknowledgedNoticeBanner + (if (ackTime.isNotBlank()) " ($ackTime)" else ""),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = PresentGreen,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Author footer & Sent Time
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${s.author}: ${update.authorName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (sentTimeFormatted.isNotBlank()) {
                    Text(
                        text = "${s.sentTime}: $sentTimeFormatted",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
            }

            // Requirement 3: 6 Groups Notification Tracking panel is displayed ONLY in Admin view (باش باشقۇرغۇچى كۆزنىكىدىلا كۆرسىتىلىدۇ)
            val relevantGroups = if (update.groupId == 0L) {
                groups
            } else {
                groups.filter { it.id == update.groupId }
            }

            if (isAdmin && relevantGroups.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = s.notificationTrackingTitle + ":",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Render group status chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            relevantGroups.forEach { grp ->
                                val rec = receipts.find { it.groupId == grp.id }
                                val (statusColor, containerColor, statusText, timeStr) = when {
                                    rec != null && rec.isAcknowledged -> {
                                        val t = if (rec.acknowledgedTimestamp > 0L) {
                                            java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(rec.acknowledgedTimestamp))
                                        } else ""
                                        Quad(PresentGreen, PresentGreenContainer, s.statusAcknowledged, t)
                                    }
                                    rec != null && rec.isDelivered -> {
                                        val t = if (rec.deliveredTimestamp > 0L) {
                                            java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(rec.deliveredTimestamp))
                                        } else ""
                                        Quad(LateAmber, LateAmberContainer, s.statusReceived, t)
                                    }
                                    else -> {
                                        Quad(AbsentRed, AbsentRedContainer, s.statusNotReceived, "")
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = containerColor,
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = grp.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = statusColor,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = statusText + if (timeStr.isNotBlank()) " ($timeStr)" else "",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = statusColor,
                                            fontSize = 9.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
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
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun NoticeDetailDialog(
    update: DailyUpdateEntity,
    groups: List<com.example.data.model.GroupEntity>,
    receipts: List<com.example.data.model.NoticeReceiptEntity>,
    isAdmin: Boolean,
    currentUserId: Long,
    currentUserGroupId: Long?,
    language: Language,
    onAcknowledge: () -> Unit,
    onToggleCancel: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val s = AppStrings.get(language)
    val isCancelled = update.priority == "CANCELLED"
    val canManage = isAdmin || (currentUserGroupId != null && update.groupId == currentUserGroupId) || (isAdmin && update.groupId == 0L)

    val priorityColor = when (update.priority) {
        "CANCELLED" -> AbsentRed
        "URGENT" -> AbsentRed
        "IMPORTANT" -> LateAmber
        else -> MaterialTheme.colorScheme.primary
    }

    val priorityContainerColor = when (update.priority) {
        "CANCELLED" -> AbsentRedContainer
        "URGENT" -> AbsentRedContainer
        "IMPORTANT" -> LateAmberContainer
        else -> MaterialTheme.colorScheme.primaryContainer
    }

    val priorityLabel = when (update.priority) {
        "CANCELLED" -> s.noticeCancelled
        "URGENT" -> s.updatePriorityUrgent
        "IMPORTANT" -> s.updatePriorityImportant
        else -> s.updatePriorityNormal
    }

    val sentTimeFormatted = remember(update.timestamp) {
        if (update.timestamp > 0L) {
            java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(update.timestamp))
        } else ""
    }

    val myGroupReceipt = receipts.find { it.groupId == currentUserGroupId }
    val isMyGroupAcknowledged = myGroupReceipt?.isAcknowledged == true

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = s.dailyUpdatesTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = s.cancel, modifier = Modifier.size(20.dp))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cancelled banner if cancelled
                if (isCancelled) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AbsentRedContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = null,
                                tint = AbsentRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "⚠️ " + s.noticeCancelled,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = AbsentRed
                            )
                        }
                    }
                }

                // Badges row: Target Group, Priority, Sent Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = if (update.groupId == 0L) s.allGroupsUpdate else update.groupName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = priorityContainerColor
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            if (update.priority == "URGENT" || update.priority == "IMPORTANT" || isCancelled) {
                                Icon(
                                    imageVector = if (isCancelled) Icons.Default.Block else Icons.Default.PriorityHigh,
                                    contentDescription = null,
                                    tint = priorityColor,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = priorityLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = priorityColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = if (sentTimeFormatted.isNotBlank()) "${update.date} ($sentTimeFormatted)" else update.date,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Author
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${s.author}: ${update.authorName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Title
                Text(
                    text = update.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Content in card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = update.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }

                // Group Lead Acknowledgment Action
                if (currentUserGroupId != null && (update.groupId == 0L || update.groupId == currentUserGroupId)) {
                    if (!isMyGroupAcknowledged) {
                        Button(
                            onClick = onAcknowledge,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PresentGreen
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_detail_acknowledge_${update.id}")
                        ) {
                            Text(
                                text = "✓ " + s.acknowledgeNoticeBtn,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = PresentGreenContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val ackTime = if (myGroupReceipt != null && myGroupReceipt.acknowledgedTimestamp > 0L) {
                                java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(myGroupReceipt.acknowledgedTimestamp))
                            } else ""
                            Text(
                                text = "✓ " + s.acknowledgedNoticeBanner + (if (ackTime.isNotBlank()) " ($ackTime)" else ""),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = PresentGreen,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            )
                        }
                    }
                }

                // Admin 6-Group Delivery & Acknowledgment Status Grid
                val relevantGroups = if (update.groupId == 0L) groups else groups.filter { it.id == update.groupId }
                if (isAdmin && relevantGroups.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = s.notificationTrackingTitle + ":",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                relevantGroups.forEach { grp ->
                                    val rec = receipts.find { it.groupId == grp.id }
                                    val (statusColor, containerColor, statusText, timeStr) = when {
                                        rec != null && rec.isAcknowledged -> {
                                            val t = if (rec.acknowledgedTimestamp > 0L) {
                                                java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(rec.acknowledgedTimestamp))
                                            } else ""
                                            Quad(PresentGreen, PresentGreenContainer, s.statusAcknowledged, t)
                                        }
                                        rec != null && rec.isDelivered -> {
                                            val t = if (rec.deliveredTimestamp > 0L) {
                                                java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(rec.deliveredTimestamp))
                                            } else ""
                                            Quad(LateAmber, LateAmberContainer, s.statusReceived, t)
                                        }
                                        else -> {
                                            Quad(AbsentRed, AbsentRedContainer, s.statusNotReceived, "")
                                        }
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = containerColor,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = grp.code,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = statusColor,
                                                fontSize = 10.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = statusText + if (timeStr.isNotBlank()) " ($timeStr)" else "",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = statusColor,
                                                fontSize = 9.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Action Controls for Edit, Cancel, Delete
                if (canManage) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Text(
                        text = "ئۇقتۇرۇشنى تۈزىتىش ۋە باشقۇرۇش:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Edit / Correct Button
                        Button(
                            onClick = onEdit,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_detail_edit_${update.id}")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(s.editUpdate, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                        }

                        // Cancel / Uncancel Button
                        Button(
                            onClick = onToggleCancel,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCancelled) PresentGreenContainer else AbsentRedContainer,
                                contentColor = if (isCancelled) PresentGreen else AbsentRed
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_detail_toggle_cancel_${update.id}")
                        ) {
                            Icon(if (isCancelled) Icons.Default.Restore else Icons.Default.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isCancelled) s.uncancelNotice else s.cancelNotice, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                        }

                        // Delete Button
                        Button(
                            onClick = onDelete,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_detail_delete_${update.id}")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(s.delete, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}

@Composable
fun DailyUpdateEditDialog(
    initialUpdate: DailyUpdateEntity?,
    targetGroupId: Long,
    viewModel: AttendanceViewModel,
    language: Language,
    onDismiss: () -> Unit
) {
    val s = AppStrings.get(language)
    val groups by viewModel.groups.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isAdmin = currentUser?.role == UserRole.ADMIN

    var title by remember { mutableStateOf(initialUpdate?.title ?: "") }
    var content by remember { mutableStateOf(initialUpdate?.content ?: "") }
    var priority by remember { mutableStateOf(initialUpdate?.priority ?: "NORMAL") }
    var selectedGId by remember { mutableStateOf(initialUpdate?.groupId ?: targetGroupId) }
    var isTitleError by remember { mutableStateOf(false) }

    val selectedGroupName = if (selectedGId == 0L) {
        s.allGroupsUpdate
    } else {
        groups.find { it.id == selectedGId }?.name ?: "گۇرۇپپا $selectedGId"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(22.dp),
        title = {
            Text(
                text = if (initialUpdate == null) s.addUpdate else s.editUpdate,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Group selector if admin
                if (isAdmin) {
                    Text(
                        text = s.groups,
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
                            selected = selectedGId == 0L,
                            onClick = { selectedGId = 0L },
                            label = { Text(s.allGroupsUpdate) }
                        )
                        groups.take(3).forEach { grp ->
                            FilterChip(
                                selected = selectedGId == grp.id,
                                onClick = { selectedGId = grp.id },
                                label = { Text(grp.code) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Priority Selection
                Text(
                    text = s.updatePriority,
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
                        selected = priority == "NORMAL",
                        onClick = { priority = "NORMAL" },
                        label = { Text(s.updatePriorityNormal, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = priority == "IMPORTANT",
                        onClick = { priority = "IMPORTANT" },
                        label = { Text(s.updatePriorityImportant, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LateAmberContainer,
                            selectedLabelColor = LateAmber
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = priority == "URGENT",
                        onClick = { priority = "URGENT" },
                        label = { Text(s.updatePriorityUrgent, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AbsentRedContainer,
                            selectedLabelColor = AbsentRed
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = priority == "CANCELLED",
                        onClick = { priority = "CANCELLED" },
                        label = { Text(s.noticeCancelled, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AbsentRedContainer,
                            selectedLabelColor = AbsentRed
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) isTitleError = false
                    },
                    label = { Text(s.updateTitle) },
                    shape = RoundedCornerShape(12.dp),
                    isError = isTitleError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Content Input
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text(s.updateContent) },
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        isTitleError = true
                        return@Button
                    }
                    if (initialUpdate == null) {
                        viewModel.addDailyUpdate(
                            groupId = selectedGId,
                            groupName = selectedGroupName,
                            title = title.trim(),
                            content = content.trim(),
                            priority = priority,
                            onSuccess = onDismiss
                        )
                    } else {
                        viewModel.updateDailyUpdate(
                            initialUpdate.copy(
                                groupId = selectedGId,
                                groupName = selectedGroupName,
                                title = title.trim(),
                                content = content.trim(),
                                priority = priority
                            ),
                            onSuccess = onDismiss
                        )
                    }
                },
                shape = RoundedCornerShape(10.dp)
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
}
