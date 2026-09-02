package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.data.model.AttendanceRecordEntity
import com.example.data.model.AttendanceStatus
import com.example.data.model.MemberEntity
import com.example.data.model.MemberStatus
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

@Composable
fun AttendanceItemCard(
    member: MemberEntity,
    record: AttendanceRecordEntity?,
    language: Language,
    onStatusSelected: (AttendanceStatus) -> Unit,
    onSaveNote: (String) -> Unit,
    modifier: Modifier = Modifier,
    onClearStatus: (() -> Unit)? = null,
    isLocked: Boolean = false
) {
    val s = AppStrings.get(language)
    val currentStatus = record?.status
    var showNoteDialog by remember { mutableStateOf(false) }
    var noteText by remember(record?.note) { mutableStateOf(record?.note ?: "") }

    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("attendance_card_${member.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Avatar, Name, Status Badge, Note Icon / Lock Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar box with geometric rounded shape
                val initialLetter = member.name.firstOrNull()?.toString() ?: "A"
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initialLetter,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = member.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isLocked) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = s.editLockedAfter12Hours,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        if (member.status == MemberStatus.INACTIVE) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.errorContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = s.statusInactive,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                    if (member.contactAddress.isNotBlank() || member.telegramContact.isNotBlank() || member.whatsappContact.isNotBlank() || member.otherContact.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val ctx = androidx.compose.ui.platform.LocalContext.current
                            if (member.contactAddress.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                    modifier = Modifier.clickable {
                                        com.example.util.ContactUtils.openPhoneCall(ctx, member.contactAddress)
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "📞 ${member.contactAddress}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
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
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            if (member.otherContact.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                                    modifier = Modifier.clickable {
                                        val clipboard = ctx.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                        val clip = android.content.ClipData.newPlainText("Other Contact", member.otherContact)
                                        clipboard.setPrimaryClip(clip)
                                        android.widget.Toast.makeText(ctx, "${s.otherContact}: ${member.otherContact}", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Text(
                                        text = "📻 ${s.otherContact}: ${member.otherContact}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (currentStatus != null) {
                    StatusBadge(status = currentStatus, language = language)
                }

                IconButton(
                    onClick = { showNoteDialog = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (!record?.note.isNullOrBlank()) Icons.Default.ChatBubbleOutline else Icons.Default.Edit,
                        contentDescription = s.notes,
                        tint = if (!record?.note.isNullOrBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Note preview if present
            if (!record?.note.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = record?.note.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3 Status Buttons Row (Present, Absent, Excused)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Present (بار)
                AttendanceActionButton(
                    text = s.statusPresent,
                    isSelected = currentStatus == AttendanceStatus.PRESENT,
                    activeBg = PresentGreen,
                    activeText = Color.White,
                    inactiveBg = PresentGreenContainer.copy(alpha = 0.5f),
                    inactiveText = PresentGreen,
                    onClick = {
                        if (currentStatus == AttendanceStatus.PRESENT && onClearStatus != null) {
                            onClearStatus()
                        } else {
                            onStatusSelected(AttendanceStatus.PRESENT)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )

                // Absent (يوق)
                AttendanceActionButton(
                    text = s.statusAbsent,
                    isSelected = currentStatus == AttendanceStatus.ABSENT,
                    activeBg = AbsentRed,
                    activeText = Color.White,
                    inactiveBg = AbsentRedContainer.copy(alpha = 0.5f),
                    inactiveText = AbsentRed,
                    onClick = {
                        if (currentStatus == AttendanceStatus.ABSENT && onClearStatus != null) {
                            onClearStatus()
                        } else {
                            onStatusSelected(AttendanceStatus.ABSENT)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )

                // Excused / Leave (رۇخسەت)
                AttendanceActionButton(
                    text = s.statusExcused,
                    isSelected = currentStatus == AttendanceStatus.EXCUSED,
                    activeBg = ExcusedBlue,
                    activeText = Color.White,
                    inactiveBg = ExcusedBlueContainer.copy(alpha = 0.5f),
                    inactiveText = ExcusedBlue,
                    onClick = {
                        if (currentStatus == AttendanceStatus.EXCUSED && onClearStatus != null) {
                            onClearStatus()
                        } else {
                            onStatusSelected(AttendanceStatus.EXCUSED)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    if (showNoteDialog) {
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = {
                Text(
                    text = "${s.notes} - ${member.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text(s.notes) },
                        placeholder = { Text(s.addNote) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("note_input_field"),
                        singleLine = false,
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSaveNote(noteText)
                        showNoteDialog = false
                    }
                ) {
                    Text(s.save, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialog = false }) {
                    Text(s.cancel)
                }
            }
        )
    }
}

@Composable
private fun AttendanceActionButton(
    text: String,
    isSelected: Boolean,
    activeBg: Color,
    activeText: Color,
    inactiveBg: Color,
    inactiveText: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(if (isSelected) activeBg else inactiveBg, label = "bgColor")
    val textColor by animateColorAsState(if (isSelected) activeText else inactiveText, label = "textColor")

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 2.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
