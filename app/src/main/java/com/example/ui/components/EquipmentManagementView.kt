package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EquipmentEntity
import com.example.i18n.AppStrings
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.AbsentRedContainer
import com.example.ui.theme.PresentGreen
import com.example.ui.theme.PresentGreenContainer
import com.example.ui.viewmodel.AttendanceViewModel
import java.util.Locale

@Composable
fun EquipmentManagementView(
    groupId: Long,
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier,
    canEdit: Boolean = true
) {
    val language by viewModel.currentLanguage.collectAsState()
    val s = AppStrings.get(language)
    val allEquipment by viewModel.allEquipment.collectAsState()
    val groups by viewModel.groups.collectAsState()

    var selectedFilterGroupId by remember { mutableStateOf<Long?>(if (groupId != 0L) groupId else null) }

    val effectiveGroupId = if (groupId != 0L) groupId else (selectedFilterGroupId ?: 0L)

    val equipmentList = remember(allEquipment, effectiveGroupId) {
        if (effectiveGroupId == 0L) allEquipment else allEquipment.filter { it.groupId == effectiveGroupId }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<EquipmentEntity?>(null) }
    var itemToDelete by remember { mutableStateOf<EquipmentEntity?>(null) }

    val totalCount = equipmentList.sumOf { it.totalCount }
    val readyCount = equipmentList.sumOf { it.readyCount }
    val notReadyCount = equipmentList.sumOf { it.notReadyCount }
    val readinessRate = if (totalCount > 0) (readyCount.toFloat() / totalCount) * 100f else 0f

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Group Filter Row for Admin (Requirement 7)
            if (groupId == 0L) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = s.filterByGroupTitle,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedFilterGroupId == null || selectedFilterGroupId == 0L,
                                onClick = { selectedFilterGroupId = null },
                                label = { Text(s.all, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                        items(groups, key = { it.id }) { grp ->
                            FilterChip(
                                selected = selectedFilterGroupId == grp.id,
                                onClick = { selectedFilterGroupId = grp.id },
                                label = { Text(grp.name, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Summary Statistics Card
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("equipment_summary_card"),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
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
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Inventory,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = s.equipmentInventory,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${s.equipmentReadinessRate}: ${String.format(Locale.US, "%.1f", readinessRate)}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Progress Bar
                        LinearProgressIndicator(
                            progress = { (readinessRate / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = PresentGreen,
                            trackColor = AbsentRed.copy(alpha = 0.3f)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 3 Stat Badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Total
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = s.totalCount,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "$totalCount",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Ready
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = PresentGreenContainer
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = s.readyCount,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PresentGreen,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "$readyCount",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = PresentGreen
                                    )
                                }
                            }

                            // Not Ready
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = AbsentRedContainer
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = s.notReadyCount,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AbsentRed,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "$notReadyCount",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AbsentRed
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (equipmentList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = s.emptyEquipment,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(equipmentList, key = { it.id }) { item ->
                    EquipmentItemCard(
                        item = item,
                        canEdit = canEdit,
                        onEdit = { editingItem = item },
                        onDelete = { itemToDelete = item },
                        s = s
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // FAB to add equipment
        if (canEdit && groupId > 0L) {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("add_equipment_fab"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = s.addEquipment)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = s.addEquipment, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Add Equipment Dialog
    if (showAddDialog) {
        EquipmentEditDialog(
            title = s.addEquipment,
            initialName = "",
            initialTotal = 1,
            initialReady = 1,
            initialNotReady = 0,
            initialNotes = "",
            s = s,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, total, ready, notReady, notes ->
                viewModel.addEquipment(
                    groupId = groupId,
                    name = name,
                    totalCount = total,
                    readyCount = ready,
                    notReadyCount = notReady,
                    notes = notes
                )
                showAddDialog = false
            }
        )
    }

    // Edit Equipment Dialog
    editingItem?.let { item ->
        EquipmentEditDialog(
            title = s.editEquipment,
            initialName = item.name,
            initialTotal = item.totalCount,
            initialReady = item.readyCount,
            initialNotReady = item.notReadyCount,
            initialNotes = item.notes,
            s = s,
            onDismiss = { editingItem = null },
            onConfirm = { name, total, ready, notReady, notes ->
                viewModel.updateEquipment(
                    item.copy(
                        name = name,
                        totalCount = total,
                        readyCount = ready,
                        notReadyCount = notReady,
                        notes = notes
                    )
                )
                editingItem = null
            }
        )
    }

    // Delete Confirmation Dialog
    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text(s.deleteEquipment) },
            text = { Text(s.deleteEquipmentConfirm) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteEquipment(item)
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(s.deleteEquipment)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text(s.cancel)
                }
            }
        )
    }
}

@Composable
fun EquipmentItemCard(
    item: EquipmentEntity,
    canEdit: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    s: com.example.i18n.LocalizedStrings
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("equipment_item_${item.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (item.notReadyCount == 0 && item.totalCount > 0)
                                    PresentGreen.copy(alpha = 0.15f)
                                else
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (item.notReadyCount == 0 && item.totalCount > 0)
                                Icons.Default.CheckCircle
                            else
                                Icons.Default.Build,
                            contentDescription = null,
                            tint = if (item.notReadyCount == 0 && item.totalCount > 0) PresentGreen else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (item.notes.isNotBlank()) {
                            Text(
                                text = item.notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (canEdit) {
                    Row {
                        IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = s.editEquipment,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = s.deleteEquipment,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quantities Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Total
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${s.totalCount}: ${item.totalCount}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Ready
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PresentGreenContainer)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓ ${s.readyBadge}: ${item.readyCount}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = PresentGreen
                    )
                }

                // Not Ready
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (item.notReadyCount > 0) AbsentRedContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✗ ${s.notReadyBadge}: ${item.notReadyCount}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (item.notReadyCount > 0) AbsentRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun EquipmentEditDialog(
    title: String,
    initialName: String,
    initialTotal: Int,
    initialReady: Int,
    initialNotReady: Int,
    initialNotes: String,
    s: com.example.i18n.LocalizedStrings,
    onDismiss: () -> Unit,
    onConfirm: (name: String, total: Int, ready: Int, notReady: Int, notes: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var totalText by remember { mutableStateOf(if (initialTotal > 0) initialTotal.toString() else "1") }
    var readyText by remember { mutableStateOf(initialReady.toString()) }
    var notReadyText by remember { mutableStateOf(initialNotReady.toString()) }
    var notes by remember { mutableStateOf(initialNotes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(s.equipmentName) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("equipment_name_input")
                )

                // Total Count
                OutlinedTextField(
                    value = totalText,
                    onValueChange = { input ->
                        totalText = input.filter { it.isDigit() }
                        val t = totalText.toIntOrNull() ?: 0
                        val r = readyText.toIntOrNull() ?: 0
                        if (r > t) {
                            readyText = t.toString()
                            notReadyText = "0"
                        } else {
                            notReadyText = (t - r).coerceAtLeast(0).toString()
                        }
                    },
                    label = { Text(s.totalCount) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("equipment_total_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Ready Count
                    OutlinedTextField(
                        value = readyText,
                        onValueChange = { input ->
                            readyText = input.filter { it.isDigit() }
                            val r = readyText.toIntOrNull() ?: 0
                            val t = totalText.toIntOrNull() ?: 0
                            if (t >= r) {
                                notReadyText = (t - r).toString()
                            }
                        },
                        label = { Text(s.readyCount) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("equipment_ready_input")
                    )

                    // Not Ready Count
                    OutlinedTextField(
                        value = notReadyText,
                        onValueChange = { input ->
                            notReadyText = input.filter { it.isDigit() }
                            val nr = notReadyText.toIntOrNull() ?: 0
                            val t = totalText.toIntOrNull() ?: 0
                            if (t >= nr) {
                                readyText = (t - nr).toString()
                            }
                        },
                        label = { Text(s.notReadyCount) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("equipment_not_ready_input")
                    )
                }

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(s.equipmentNotes) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalTotal = totalText.toIntOrNull() ?: 0
                    val finalReady = readyText.toIntOrNull() ?: 0
                    val finalNotReady = notReadyText.toIntOrNull() ?: (finalTotal - finalReady).coerceAtLeast(0)
                    if (name.isNotBlank() && finalTotal >= 0) {
                        onConfirm(name.trim(), finalTotal, finalReady, finalNotReady, notes.trim())
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
