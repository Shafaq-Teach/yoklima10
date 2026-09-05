package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GroupEntity
import com.example.data.model.GroupLeaderAttendanceEntity
import com.example.data.model.GroupLeaderEntity
import com.example.i18n.LocalizedStrings
import com.example.ui.viewmodel.AttendanceViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BayraqLeadersCard(
    group: GroupEntity,
    viewModel: AttendanceViewModel,
    s: LocalizedStrings,
    modifier: Modifier = Modifier
) {
    val allLeaders by viewModel.allGroupLeaders.collectAsState()
    val allAttendance by viewModel.allGroupLeaderAttendance.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    val groupLeaders = allLeaders.filter { it.groupId == group.id }
    val isQisim = group.name.contains("إدارى") || group.name.contains("ادارى") || group.name.contains("عمليات")

    val leaderRole = groupLeaders.find { it.roleType == "LEADER" } ?: GroupLeaderEntity(groupId = group.id, roleType = "LEADER")
    val erkanRole = groupLeaders.find { it.roleType == "ERKAN" } ?: GroupLeaderEntity(groupId = group.id, roleType = "ERKAN")
    val idariRole = groupLeaders.find { it.roleType == "IDARI" } ?: GroupLeaderEntity(groupId = group.id, roleType = "IDARI")

    val leaderAtt = allAttendance.find { it.groupId == group.id && it.roleType == "LEADER" && it.date == selectedDate }
    val erkanAtt = allAttendance.find { it.groupId == group.id && it.roleType == "ERKAN" && it.date == selectedDate }
    val idariAtt = allAttendance.find { it.groupId == group.id && it.roleType == "IDARI" && it.date == selectedDate }
    val leaderAttendanceVisible by viewModel.leaderAttendanceVisible.collectAsState()

    var expanded by remember { mutableStateOf(false) }
    var editingLeader by remember { mutableStateOf<GroupLeaderEntity?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_bayraq_leaders"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isQisim) "قىسىم رەھبەرلىك كۆزنىكى" else "بايراق رەھبەرلىك كۆزنىكى",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "مەسئۇل، ئەركان ۋە ئىدارى رەھبەرلىرىنىڭ ئىسىملىرى، ئالاقىسى ۋە تەپسىلاتى",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    // 1. Leader (بايراق مەسئۇلى)
                    LeaderRowItem(
                        title = if (isQisim) "قىسىم مەسئۇلى" else "بايراق مەسئۇلى",
                        leader = leaderRole,
                        attendance = leaderAtt,
                        showAttendance = leaderAttendanceVisible,
                        s = s,
                        onEdit = { editingLeader = leaderRole },
                        onStatusChange = { newStatus ->
                            viewModel.saveBayraqLeaderAttendance(group.id, "LEADER", newStatus)
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. Erkan (بايراق ئەركان)
                    LeaderRowItem(
                        title = if (isQisim) "قىسىم ئەركان" else "بايراق ئەركان",
                        leader = erkanRole,
                        attendance = erkanAtt,
                        showAttendance = leaderAttendanceVisible,
                        s = s,
                        onEdit = { editingLeader = erkanRole },
                        onStatusChange = { newStatus ->
                            viewModel.saveBayraqLeaderAttendance(group.id, "ERKAN", newStatus)
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. Idari (بايراق ئىدارى)
                    LeaderRowItem(
                        title = if (isQisim) "قىسىم ئىدارى" else "بايراق ئىدارى",
                        leader = idariRole,
                        attendance = idariAtt,
                        showAttendance = leaderAttendanceVisible,
                        s = s,
                        onEdit = { editingLeader = idariRole },
                        onStatusChange = { newStatus ->
                            viewModel.saveBayraqLeaderAttendance(group.id, "IDARI", newStatus)
                        }
                    )
                }
            }
        }
    }

    // Edit Leader Dialog
    if (editingLeader != null) {
        val target = editingLeader!!
        val targetRoleTitle = when (target.roleType) {
            "LEADER" -> if (isQisim) "قىسىم مەسئۇلى" else "بايراق مەسئۇلى"
            "ERKAN" -> if (isQisim) "قىسىم ئەركان" else "بايراق ئەركان"
            "IDARI" -> if (isQisim) "قىسىم ئىدارى" else "بايراق ئىدارى"
            else -> "مەسئۇل"
        }

        var nameVal by remember { mutableStateOf(target.name) }
        var phoneVal by remember { mutableStateOf(target.phone) }
        var telegramVal by remember { mutableStateOf(target.telegram) }
        var whatsappVal by remember { mutableStateOf(target.whatsapp) }
        var otherVal by remember { mutableStateOf(target.otherContact) }

        AlertDialog(
            onDismissRequest = { editingLeader = null },
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "$targetRoleTitle ئۇچۇرىنى تەھرىرلەش",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = nameVal,
                        onValueChange = { nameVal = it },
                        label = { Text("ئىسمى / لەقىمى") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = phoneVal,
                        onValueChange = { phoneVal = it },
                        label = { Text("تېلېفون نومۇرى") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = telegramVal,
                        onValueChange = { telegramVal = it },
                        label = { Text("تېلېگرام ئادرېسى (@username)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = whatsappVal,
                        onValueChange = { whatsappVal = it },
                        label = { Text("ۋاتسئاپ نومۇرى") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = otherVal,
                        onValueChange = { otherVal = it },
                        label = { Text("باشقا ئالاقە / سىمسىز ئالاقە") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveBayraqLeader(
                            target.copy(
                                name = nameVal.trim(),
                                phone = phoneVal.trim(),
                                telegram = telegramVal.trim(),
                                whatsapp = whatsappVal.trim(),
                                otherContact = otherVal.trim()
                            )
                        )
                        editingLeader = null
                    }
                ) {
                    Text(s.save)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingLeader = null }) {
                    Text(s.cancel)
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LeaderRowItem(
    title: String,
    leader: GroupLeaderEntity,
    attendance: GroupLeaderAttendanceEntity?,
    showAttendance: Boolean,
    s: LocalizedStrings,
    onEdit: () -> Unit,
    onStatusChange: (String) -> Unit
) {
    val context = LocalContext.current
    val currentStatus = attendance?.status ?: "PRESENT"

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = leader.name.ifBlank { "— يېزىلمىدى (چىكىپ قوشۇڭ) —" },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (leader.name.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (leader.name.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Attendance 4 Status Selector (بار, يوق, باشقا يەردە خىزمەتتە, ئارام) - shown only when showAttendance is true
            if (showAttendance) {
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StatusPill(
                        label = "بار",
                        isSelected = currentStatus == "PRESENT",
                        activeColor = Color(0xFF2E7D32),
                        onClick = { onStatusChange("PRESENT") }
                    )
                    StatusPill(
                        label = "يوق",
                        isSelected = currentStatus == "ABSENT",
                        activeColor = Color(0xFFD32F2F),
                        onClick = { onStatusChange("ABSENT") }
                    )
                    StatusPill(
                        label = "باشقا يەردە خىزمەتتە",
                        isSelected = currentStatus == "EXTERNAL_MISSION",
                        activeColor = Color(0xFF0288D1),
                        onClick = { onStatusChange("EXTERNAL_MISSION") }
                    )
                    StatusPill(
                        label = "ئارام",
                        isSelected = currentStatus == "REST",
                        activeColor = Color(0xFFED6C02),
                        onClick = { onStatusChange("REST") }
                    )
                }
            }

            // 1-Tap Direct Action Communication Badges (تېلېفون، تېلېگرام، ۋاتسئاپقا بىۋاسىتە ئۇلىنىش)
            val hasPhone = leader.phone.isNotBlank()
            val hasTelegram = leader.telegram.isNotBlank()
            val hasWhatsapp = leader.whatsapp.isNotBlank()
            val hasOther = leader.otherContact.isNotBlank()
            val hasAnyContact = hasPhone || hasTelegram || hasWhatsapp || hasOther

            Spacer(modifier = Modifier.height(8.dp))

            if (hasAnyContact) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // 1-Tap Call Button
                    if (hasPhone) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF2E7D32).copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E7D32).copy(alpha = 0.4f)),
                            modifier = Modifier.clickable {
                                com.example.util.ContactUtils.openPhoneCall(context, leader.phone)
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = "تېلېفون قىلىش", tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "تېلېفون: ${leader.phone}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B5E20)
                                )
                            }
                        }
                    }

                    // 1-Tap Telegram Button
                    if (hasTelegram) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0088CC).copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0088CC).copy(alpha = 0.4f)),
                            modifier = Modifier.clickable {
                                com.example.util.ContactUtils.openTelegram(context, leader.telegram)
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Telegram", tint = Color(0xFF0088CC), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "تېلېگرام: ${leader.telegram}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF01579B)
                                )
                            }
                        }
                    }

                    // 1-Tap WhatsApp Button
                    if (hasWhatsapp) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1EBE5D).copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1EBE5D).copy(alpha = 0.4f)),
                            modifier = Modifier.clickable {
                                com.example.util.ContactUtils.openWhatsApp(context, leader.whatsapp)
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "WhatsApp", tint = Color(0xFF1EBE5D), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ۋاتسئاپ: ${leader.whatsapp}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F6E35)
                                )
                            }
                        }
                    }

                    // Other Contact
                    if (hasOther) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable {
                                Toast.makeText(context, leader.otherContact, Toast.LENGTH_LONG).show()
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Icon(Icons.Default.PersonOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = leader.otherContact,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.clickable { onEdit() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+ تېلېفون، تېلېگرام ياكى ۋاتسئاپ قوشۇش",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusPill(
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) activeColor else activeColor.copy(alpha = 0.08f),
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, activeColor.copy(alpha = 0.35f)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
                Spacer(modifier = Modifier.width(5.dp))
            }
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else activeColor
            )
        }
    }
}
