package com.example.ui.components

import java.util.Locale
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecordEntity
import com.example.data.model.AttendanceStatus
import com.example.data.model.GroupEntity
import com.example.data.model.MemberEntity
import com.example.data.model.SanjaqLeaderEntity
import com.example.i18n.LocalizedStrings
import com.example.ui.viewmodel.AttendanceViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SanjaqManagementCard(
    group: GroupEntity,
    viewModel: AttendanceViewModel,
    s: LocalizedStrings,
    modifier: Modifier = Modifier
) {
    val allSanjaqs by viewModel.allSanjaqLeaders.collectAsState()
    val allMembers by viewModel.allMembers.collectAsState()
    val allAttendance by viewModel.allAttendance.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedSanjaqNumbers by viewModel.selectedSanjaqNumbers.collectAsState()

    androidx.compose.runtime.LaunchedEffect(group.id) {
        viewModel.loadSelectedSanjaqsForGroup(group.id)
    }

    // Ensure we have at least 4 sanjaqs (1..4)
    val groupSanjaqs = allSanjaqs.filter { it.groupId == group.id }.sortedBy { it.sanjaqNumber }
    val displaySanjaqs = if (groupSanjaqs.isEmpty()) {
        (1..4).map { num ->
            SanjaqLeaderEntity(
                groupId = group.id,
                sanjaqNumber = num,
                sanjaqCustomName = "$num-سانجاق"
            )
        }
    } else {
        groupSanjaqs
    }

    val groupMembers = allMembers.filter { it.groupId == group.id }
    val groupAttendance = allAttendance.filter { it.groupId == group.id && it.date == selectedDate }

    // Dynamic Consolidated Attendance Calculation for Selected Sanjaqs
    val filteredMembers = groupMembers.filter { it.subGroup in selectedSanjaqNumbers }
    val filteredMemberIds = filteredMembers.map { it.id }.toSet()
    val filteredAttendance = groupAttendance.filter { it.memberId in filteredMemberIds }

    val totalMembersCount = filteredMembers.size
    val presentCount = filteredAttendance.count { it.status == AttendanceStatus.PRESENT }
    val absentCount = filteredAttendance.count { it.status == AttendanceStatus.ABSENT }
    val excusedCount = filteredAttendance.count { it.status == AttendanceStatus.EXCUSED }
    val lateCount = filteredAttendance.count { it.status == AttendanceStatus.LATE }

    val baseTotal = (presentCount + absentCount + excusedCount + lateCount).coerceAtLeast(totalMembersCount)
    val attendanceRate = if (baseTotal > 0) (presentCount.toFloat() / baseTotal) * 100f else 0f

    var expandedDetails by remember { mutableStateOf(false) }
    var editingSanjaq by remember { mutableStateOf<SanjaqLeaderEntity?>(null) }
    var showAddSanjaqDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_sanjaqs"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header with title and expand/collapse
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "سانجاقلار ۋە ئورتاق يوقلىما ھېساباتى",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "سانجاقلارنى ئايرىم ياكى بىرلەشتۈرۈپ تاللاپ نىسبەت چىقىرىش",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showAddSanjaqDialog = true }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Sanjaq",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { expandedDetails = !expandedDetails }) {
                        Icon(
                            imageVector = if (expandedDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Multi-Select Filter Chips for all Sanjaqs (Requirement 4 & 5)
            Text(
                text = "يوقلىما قىلىش ياكى كۆرۈش ئۈچۈن سانجاق تاللاڭ (كۆپ تاللاشقا بولىدۇ):",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // All Sanjaqs Chip
                val allSanjaqNums = displaySanjaqs.map { it.sanjaqNumber }.toSet()
                val isAllSelected = selectedSanjaqNumbers.containsAll(allSanjaqNums)

                FilterChip(
                    selected = isAllSelected,
                    onClick = {
                        if (isAllSelected) {
                            viewModel.selectAllSanjaqs(setOf(displaySanjaqs.firstOrNull()?.sanjaqNumber ?: 1))
                        } else {
                            viewModel.selectAllSanjaqs(allSanjaqNums)
                        }
                    },
                    label = { Text("ھەممە سانجاق (${groupMembers.size} ئەزا)", fontWeight = FontWeight.Bold) },
                    leadingIcon = if (isAllSelected) { { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) } } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )

                // Individual Sanjaqs Chips with Member Counts
                displaySanjaqs.forEach { sanjaq ->
                    val isSelected = selectedSanjaqNumbers.contains(sanjaq.sanjaqNumber)
                    val sMemberCount = groupMembers.count { it.subGroup == sanjaq.sanjaqNumber }
                    val sLabel = sanjaq.sanjaqCustomName.ifBlank { "${sanjaq.sanjaqNumber}-سانجاق" }

                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.toggleSanjaqSelection(sanjaq.sanjaqNumber) },
                        label = {
                            Text(
                                text = "$sLabel ($sMemberCount)",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = if (isSelected) { { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) } } else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dynamic Consolidated Attendance KPI Board (Requirement 5)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val sTotal = (presentCount + absentCount + excusedCount + lateCount).coerceAtLeast(totalMembersCount).coerceAtLeast(1)
                    val sPresentPct = (presentCount.toFloat() / sTotal) * 100f
                    val sAbsentPct = (absentCount.toFloat() / sTotal) * 100f
                    val sExcusedPct = (excusedCount.toFloat() / sTotal) * 100f

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تاللانغان سانجاقلار يىغىنچاق ھالىتى",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "يوقلىما: %.0f%%".format(attendanceRate),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFED6C02)
                            ) {
                                Text(
                                    text = "رۇخسەت: %.0f%%".format(sExcusedPct),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SanjaqStatItem(title = "جەمئىي ئەزا", countText = "$totalMembersCount", color = MaterialTheme.colorScheme.onPrimaryContainer)
                        SanjaqStatItem(title = "بار", countText = "$presentCount (${String.format(Locale.US, "%.0f%%", sPresentPct)})", color = Color(0xFF2E7D32))
                        SanjaqStatItem(title = "يوق", countText = "$absentCount (${String.format(Locale.US, "%.0f%%", sAbsentPct)})", color = Color(0xFFD32F2F))
                        SanjaqStatItem(title = "رۇخسەت", countText = "$excusedCount (${String.format(Locale.US, "%.0f%%", sExcusedPct)})", color = Color(0xFFED6C02))
                        if (lateCount > 0) {
                            SanjaqStatItem(title = "كېچىككەن", countText = "$lateCount", color = Color(0xFF9C27B0))
                        }
                    }
                }
            }

            // Expandable Sanjaq Leaders & Deputies Directory (Requirement 4)
            AnimatedVisibility(visible = expandedDetails) {
                Column(
                    modifier = Modifier.padding(top = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "سانجاق مەسئۇللىرى ۋە نائىب سانجاقلار تىزىملىكى:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    displaySanjaqs.forEach { sanjaq ->
                        SanjaqDetailItem(
                            sanjaq = sanjaq,
                            memberCount = groupMembers.count { it.subGroup == sanjaq.sanjaqNumber },
                            onEdit = { editingSanjaq = sanjaq }
                        )
                    }
                }
            }
        }
    }

    // Edit Sanjaq Leader & Deputy Dialog
    if (editingSanjaq != null) {
        val target = editingSanjaq!!
        var sName by remember { mutableStateOf(target.sanjaqCustomName.ifBlank { "${target.sanjaqNumber}-سانجاق" }) }
        var leaderName by remember { mutableStateOf(target.leaderName) }
        var leaderPhone by remember { mutableStateOf(target.leaderPhone) }
        var leaderTelegram by remember { mutableStateOf(target.leaderTelegram) }
        var leaderWhatsapp by remember { mutableStateOf(target.leaderWhatsapp) }
        var deputyName by remember { mutableStateOf(target.deputyName) }
        var deputyPhone by remember { mutableStateOf(target.deputyPhone) }
        var deputyTelegram by remember { mutableStateOf(target.deputyTelegram) }
        var deputyWhatsapp by remember { mutableStateOf(target.deputyWhatsapp) }

        AlertDialog(
            onDismissRequest = { editingSanjaq = null },
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "${target.sanjaqNumber}-سانجاق مەسئۇللىرىنى تەھرىرلەش",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = sName,
                        onValueChange = { sName = it },
                        label = { Text("سانجاق نامى (مەسىلەن: 1-سانجاق)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "سانجاق مەسئۇلى:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedTextField(
                        value = leaderName,
                        onValueChange = { leaderName = it },
                        label = { Text("سانجاق مەسئۇلى ئىسمى") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = leaderPhone,
                        onValueChange = { leaderPhone = it },
                        label = { Text("مەسئۇل تېلېفون نومۇرى") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = leaderTelegram,
                        onValueChange = { leaderTelegram = it },
                        label = { Text("مەسئۇل تېلېگرام (@username)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = leaderWhatsapp,
                        onValueChange = { leaderWhatsapp = it },
                        label = { Text("مەسئۇل ۋاتسئاپ نومۇرى") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "نائىب سانجاق:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    OutlinedTextField(
                        value = deputyName,
                        onValueChange = { deputyName = it },
                        label = { Text("نائىب سانجاق ئىسمى") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = deputyPhone,
                        onValueChange = { deputyPhone = it },
                        label = { Text("نائىب تېلېفون نومۇرى") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = deputyTelegram,
                        onValueChange = { deputyTelegram = it },
                        label = { Text("نائىب تېلېگرام (@username)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = deputyWhatsapp,
                        onValueChange = { deputyWhatsapp = it },
                        label = { Text("نائىب ۋاتسئاپ نومۇرى") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveSanjaqLeader(
                            target.copy(
                                sanjaqCustomName = sName.trim(),
                                leaderName = leaderName.trim(),
                                leaderPhone = leaderPhone.trim(),
                                leaderTelegram = leaderTelegram.trim(),
                                leaderWhatsapp = leaderWhatsapp.trim(),
                                deputyName = deputyName.trim(),
                                deputyPhone = deputyPhone.trim(),
                                deputyTelegram = deputyTelegram.trim(),
                                deputyWhatsapp = deputyWhatsapp.trim()
                            )
                        )
                        editingSanjaq = null
                    }
                ) {
                    Text(s.save)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingSanjaq = null }) {
                    Text(s.cancel)
                }
            }
        )
    }

    // Add New Sanjaq Dialog
    if (showAddSanjaqDialog) {
        val nextNumber = if (displaySanjaqs.isEmpty()) 1 else (displaySanjaqs.maxOf { it.sanjaqNumber } + 1)
        var newName by remember { mutableStateOf("$nextNumber-سانجاق") }
        var newLeader by remember { mutableStateOf("") }
        var newLeaderPhone by remember { mutableStateOf("") }
        var newLeaderTelegram by remember { mutableStateOf("") }
        var newLeaderWhatsapp by remember { mutableStateOf("") }
        var newDeputy by remember { mutableStateOf("") }
        var newDeputyPhone by remember { mutableStateOf("") }
        var newDeputyTelegram by remember { mutableStateOf("") }
        var newDeputyWhatsapp by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddSanjaqDialog = false },
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "يېڭى سانجاق قوشۇش",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("سانجاق نامى (مەسىلەن: $nextNumber-سانجاق)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "سانجاق مەسئۇلى (تاللاشچان):",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedTextField(
                        value = newLeader,
                        onValueChange = { newLeader = it },
                        label = { Text("مەسئۇل ئىسمى") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = newLeaderPhone,
                        onValueChange = { newLeaderPhone = it },
                        label = { Text("مەسئۇل تېلېفون نومۇرى") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = newLeaderTelegram,
                        onValueChange = { newLeaderTelegram = it },
                        label = { Text("مەسئۇل تېلېگرام (@username)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = newLeaderWhatsapp,
                        onValueChange = { newLeaderWhatsapp = it },
                        label = { Text("مەسئۇل ۋاتسئاپ نومۇرى") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "نائىب سانجاق (تاللاشچان):",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    OutlinedTextField(
                        value = newDeputy,
                        onValueChange = { newDeputy = it },
                        label = { Text("نائىب ئىسمى") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = newDeputyPhone,
                        onValueChange = { newDeputyPhone = it },
                        label = { Text("نائىب تېلېفون نومۇرى") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = newDeputyTelegram,
                        onValueChange = { newDeputyTelegram = it },
                        label = { Text("نائىب تېلېگرام (@username)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = newDeputyWhatsapp,
                        onValueChange = { newDeputyWhatsapp = it },
                        label = { Text("نائىب ۋاتسئاپ نومۇرى") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addNewSanjaqWithDetails(
                            groupId = group.id,
                            customName = newName.trim(),
                            leaderName = newLeader.trim(),
                            leaderPhone = newLeaderPhone.trim(),
                            leaderTelegram = newLeaderTelegram.trim(),
                            leaderWhatsapp = newLeaderWhatsapp.trim(),
                            deputyName = newDeputy.trim(),
                            deputyPhone = newDeputyPhone.trim(),
                            deputyTelegram = newDeputyTelegram.trim(),
                            deputyWhatsapp = newDeputyWhatsapp.trim()
                        )
                        showAddSanjaqDialog = false
                    }
                ) {
                    Text("قوشۇش", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSanjaqDialog = false }) {
                    Text(s.cancel)
                }
            }
        )
    }
}

@Composable
private fun SanjaqStatItem(title: String, countText: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = countText,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = color
        )
        Text(
            text = title,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SanjaqDetailItem(
    sanjaq: SanjaqLeaderEntity,
    memberCount: Int,
    onEdit: () -> Unit
) {
    val context = LocalContext.current
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Header Row: Title, Member Badge, Edit Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = sanjaq.sanjaqCustomName.ifBlank { "${sanjaq.sanjaqNumber}-سانجاق" },
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "$memberCount ئەزا",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Sanjaq",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 1. Sanjaq Leader (سانجاق مەسئۇلى)
            val hasLeaderPhone = sanjaq.leaderPhone.isNotBlank()
            val hasLeaderTg = sanjaq.leaderTelegram.isNotBlank()
            val hasLeaderWa = sanjaq.leaderWhatsapp.isNotBlank()
            val hasAnyLeaderContact = hasLeaderPhone || hasLeaderTg || hasLeaderWa

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "مەسئۇل: ${sanjaq.leaderName.ifBlank { "—" }}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(end = 6.dp)
                )
            }

            if (hasAnyLeaderContact) {
                Spacer(modifier = Modifier.height(3.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (hasLeaderPhone) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF2E7D32).copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E7D32).copy(alpha = 0.4f)),
                            modifier = Modifier.clickable {
                                com.example.util.ContactUtils.openPhoneCall(context, sanjaq.leaderPhone)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Call, contentDescription = "تېلېفون", tint = Color(0xFF2E7D32), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(sanjaq.leaderPhone, fontSize = 10.sp, color = Color(0xFF1B5E20), fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (hasLeaderTg) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF0088CC).copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0088CC).copy(alpha = 0.4f)),
                            modifier = Modifier.clickable {
                                com.example.util.ContactUtils.openTelegram(context, sanjaq.leaderTelegram)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Telegram", tint = Color(0xFF0088CC), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (sanjaq.leaderTelegram.startsWith("@")) sanjaq.leaderTelegram else "TG: ${sanjaq.leaderTelegram}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF01579B),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (hasLeaderWa) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF1EBE5D).copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1EBE5D).copy(alpha = 0.4f)),
                            modifier = Modifier.clickable {
                                com.example.util.ContactUtils.openWhatsApp(context, sanjaq.leaderWhatsapp)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = Color(0xFF1EBE5D), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("WA: ${sanjaq.leaderWhatsapp}", fontSize = 10.sp, color = Color(0xFF0F6E35), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 2. Sanjaq Deputy (نائىب سانجاق)
            val hasDeputyPhone = sanjaq.deputyPhone.isNotBlank()
            val hasDeputyTg = sanjaq.deputyTelegram.isNotBlank()
            val hasDeputyWa = sanjaq.deputyWhatsapp.isNotBlank()
            val hasAnyDeputyContact = hasDeputyPhone || hasDeputyTg || hasDeputyWa

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "نائىب: ${sanjaq.deputyName.ifBlank { "—" }}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 6.dp)
                )
            }

            if (hasAnyDeputyContact) {
                Spacer(modifier = Modifier.height(3.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (hasDeputyPhone) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF2E7D32).copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E7D32).copy(alpha = 0.4f)),
                            modifier = Modifier.clickable {
                                com.example.util.ContactUtils.openPhoneCall(context, sanjaq.deputyPhone)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Call, contentDescription = "تېلېفون", tint = Color(0xFF2E7D32), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(sanjaq.deputyPhone, fontSize = 10.sp, color = Color(0xFF1B5E20), fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (hasDeputyTg) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF0088CC).copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0088CC).copy(alpha = 0.4f)),
                            modifier = Modifier.clickable {
                                com.example.util.ContactUtils.openTelegram(context, sanjaq.deputyTelegram)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Telegram", tint = Color(0xFF0088CC), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (sanjaq.deputyTelegram.startsWith("@")) sanjaq.deputyTelegram else "TG: ${sanjaq.deputyTelegram}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF01579B),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (hasDeputyWa) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF1EBE5D).copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1EBE5D).copy(alpha = 0.4f)),
                            modifier = Modifier.clickable {
                                com.example.util.ContactUtils.openWhatsApp(context, sanjaq.deputyWhatsapp)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = Color(0xFF1EBE5D), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("WA: ${sanjaq.deputyWhatsapp}", fontSize = 10.sp, color = Color(0xFF0F6E35), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
