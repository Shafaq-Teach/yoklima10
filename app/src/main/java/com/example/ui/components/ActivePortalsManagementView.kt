package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GroupEntity
import com.example.i18n.AppStrings
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.AbsentRedContainer
import com.example.ui.theme.PresentGreen
import com.example.ui.theme.PresentGreenContainer
import com.example.ui.viewmodel.AttendanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ActivePortalsManagementView(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.currentLanguage.collectAsState()
    val s = AppStrings.get(language)
    val groups by viewModel.groups.collectAsState()
    val allMembers by viewModel.allMembers.collectAsState()

    var showAddGroupDialog by remember { mutableStateOf(false) }
    var groupToDelete by remember { mutableStateOf<GroupEntity?>(null) }
    var groupToEdit by remember { mutableStateOf<GroupEntity?>(null) }

    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.US) }
    val now = System.currentTimeMillis()

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Info Card
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = s.portalControlTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${groups.size} ${s.subGroupSelection}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            items(groups, key = { it.id }) { grp ->
                val memberCount = allMembers.count { it.groupId == grp.id }
                val isRecentlyActive = (now - grp.lastActiveTime) < 15 * 60 * 1000L && grp.lastActiveTime > 0
                val activeTimeStr = if (grp.lastActiveTime > 0) {
                    timeFormatter.format(Date(grp.lastActiveTime))
                } else "-"

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("group_portal_item_${grp.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = if (grp.isSuspended)
                            AbsentRedContainer.copy(alpha = 0.35f)
                        else
                            MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (grp.isSuspended) AbsentRed.copy(alpha = 0.2f)
                                            else if (isRecentlyActive) PresentGreen.copy(alpha = 0.2f)
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (grp.isSuspended) Icons.Default.Block
                                        else if (isRecentlyActive) Icons.Default.RadioButtonChecked
                                        else Icons.Default.Group,
                                        contentDescription = null,
                                        tint = if (grp.isSuspended) AbsentRed
                                        else if (isRecentlyActive) PresentGreen
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = grp.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${grp.code} • $memberCount ${s.memberCountLabel}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Active / Suspended Status Badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (grp.isSuspended) AbsentRedContainer
                                else if (isRecentlyActive) PresentGreenContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (grp.isSuspended) AbsentRed
                                                else if (isRecentlyActive) PresentGreen
                                                else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (grp.isSuspended) s.statusSuspendedPort
                                        else if (isRecentlyActive) s.statusActivePort
                                        else s.statusInactivePort,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (grp.isSuspended) AbsentRed
                                        else if (isRecentlyActive) PresentGreen
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Controls Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Last Active Time
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${s.lastActive}: $activeTimeStr",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Suspend/Resume Action Button
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = {
                                        viewModel.setGroupSuspended(grp.id, !grp.isSuspended)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (grp.isSuspended) PresentGreen else AbsentRed
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                        horizontal = 12.dp,
                                        vertical = 6.dp
                                    ),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (grp.isSuspended) Icons.Default.PlayCircle else Icons.Default.PauseCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (grp.isSuspended) s.resumePortal else s.suspendPortal,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (groups.size > 1) {
                                    IconButton(
                                        onClick = { groupToDelete = grp },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = s.deleteGroup,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // FAB to create new group directly on phone
        FloatingActionButton(
            onClick = { showAddGroupDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("admin_add_group_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = s.addNewGroup)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = s.addNewGroup, fontWeight = FontWeight.Bold)
            }
        }
    }

    // Add Group Dialog
    if (showAddGroupDialog) {
        AdminAddGroupDialog(
            s = s,
            onDismiss = { showAddGroupDialog = false },
            onConfirm = { name, code, desc, sub1, contact1, tele1, wa1, sub2, contact2, tele2, wa2, login, pass ->
                viewModel.addGroup(
                    name = name,
                    code = code,
                    description = desc,
                    subLeader1 = sub1,
                    subLeader1Contact = contact1,
                    subLeader1Telegram = tele1,
                    subLeader1Whatsapp = wa1,
                    subLeader2 = sub2,
                    subLeader2Contact = contact2,
                    subLeader2Telegram = tele2,
                    subLeader2Whatsapp = wa2,
                    leaderLoginName = login,
                    leaderPasswordPlain = pass
                )
                showAddGroupDialog = false
            }
        )
    }

    // Delete Group Confirmation
    groupToDelete?.let { grp ->
        AlertDialog(
            onDismissRequest = { groupToDelete = null },
            title = { Text(s.deleteGroup) },
            text = { Text(s.deleteGroupConfirm) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteGroup(grp.id)
                        groupToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(s.deleteGroup)
                }
            },
            dismissButton = {
                TextButton(onClick = { groupToDelete = null }) {
                    Text(s.cancel)
                }
            }
        )
    }
}

@Composable
fun AdminAddGroupDialog(
    s: com.example.i18n.LocalizedStrings,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        code: String,
        desc: String,
        sub1: String,
        contact1: String,
        tele1: String,
        wa1: String,
        sub2: String,
        contact2: String,
        tele2: String,
        wa2: String,
        loginName: String,
        passwordPlain: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var subLeader1 by remember { mutableStateOf("") }
    var contact1 by remember { mutableStateOf("") }
    var tele1 by remember { mutableStateOf("") }
    var wa1 by remember { mutableStateOf("") }
    var subLeader2 by remember { mutableStateOf("") }
    var contact2 by remember { mutableStateOf("") }
    var tele2 by remember { mutableStateOf("") }
    var wa2 by remember { mutableStateOf("") }
    var loginName by remember { mutableStateOf("") }
    var passwordPlain by remember { mutableStateOf("123456") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = s.addNewGroup, fontWeight = FontWeight.Bold)
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    androidx.compose.material3.OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(s.groupName) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    androidx.compose.material3.OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text(s.groupCode) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    androidx.compose.material3.OutlinedTextField(
                        value = loginName,
                        onValueChange = { loginName = it },
                        label = { Text(s.leaderLoginName) },
                        placeholder = { Text("lead...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    androidx.compose.material3.OutlinedTextField(
                        value = passwordPlain,
                        onValueChange = { passwordPlain = it },
                        label = { Text(s.initialPassword) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    androidx.compose.material3.OutlinedTextField(
                        value = subLeader1,
                        onValueChange = { subLeader1 = it },
                        label = { Text(s.subGroup1Leader) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    androidx.compose.material3.OutlinedTextField(
                        value = contact1,
                        onValueChange = { contact1 = it },
                        label = { Text(s.phoneContact + " (1)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    androidx.compose.material3.OutlinedTextField(
                        value = subLeader2,
                        onValueChange = { subLeader2 = it },
                        label = { Text(s.subGroup2Leader) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    androidx.compose.material3.OutlinedTextField(
                        value = contact2,
                        onValueChange = { contact2 = it },
                        label = { Text(s.phoneContact + " (2)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val finalCode = if (code.isNotBlank()) code else "GRP"
                        onConfirm(
                            name.trim(), finalCode.trim(), "",
                            subLeader1.trim(), contact1.trim(), tele1.trim(), wa1.trim(),
                            subLeader2.trim(), contact2.trim(), tele2.trim(), wa2.trim(),
                            loginName.trim(), passwordPlain.trim()
                        )
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text(s.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(s.cancel)
            }
        }
    )
}
