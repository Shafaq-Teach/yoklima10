package com.example.ui.components

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.i18n.AppStrings
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.AbsentRedContainer
import com.example.ui.theme.ExcusedBlue
import com.example.ui.theme.ExcusedBlueContainer
import com.example.ui.theme.PresentGreen
import com.example.ui.theme.PresentGreenContainer
import com.example.ui.viewmodel.AttendanceViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun MultiDateAnalyticsDialog(
    initialGroupId: Long = 0L, // 0L: All groups, 1L..6L: Specific Group
    restrictToGroupOnly: Boolean = false,
    viewModel: AttendanceViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val language by viewModel.currentLanguage.collectAsState()
    val s = AppStrings.get(language)
    val groups by viewModel.groups.collectAsState()
    val allMembers by viewModel.allMembers.collectAsState()
    val todayDate by viewModel.selectedDate.collectAsState()

    var targetGroupId by remember { mutableStateOf(if (restrictToGroupOnly && initialGroupId > 0L) initialGroupId else initialGroupId) }

    // Date Range Config
    // Year-Month (e.g. 2026-08)
    val cal = Calendar.getInstance()
    var currentYear by remember { mutableIntStateOf(cal.get(Calendar.YEAR)) }
    var currentMonth by remember { mutableIntStateOf(cal.get(Calendar.MONTH) + 1) } // 1..12

    // Selection mode: 0: Today, 1: Full Month, 2: Last 7 Days, 3: Custom Days
    var selectionMode by remember { mutableIntStateOf(1) }
    val customSelectedDays = remember { mutableStateListOf<Int>() }

    // Helper to calculate days in selected year/month
    val daysInMonth = remember(currentYear, currentMonth) {
        val c = Calendar.getInstance()
        c.set(Calendar.YEAR, currentYear)
        c.set(Calendar.MONTH, currentMonth - 1)
        c.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    // Initialize customSelectedDays if empty
    if (customSelectedDays.isEmpty()) {
        val todayDay = cal.get(Calendar.DAY_OF_MONTH).coerceIn(1, daysInMonth)
        customSelectedDays.add(todayDay)
    }

    // Compute effective selected dates list (formatted yyyy-MM-dd)
    val monthStr = String.format(Locale.US, "%04d-%02d", currentYear, currentMonth)
    val selectedDatesList = remember(selectionMode, currentYear, currentMonth, customSelectedDays.toList(), todayDate) {
        when (selectionMode) {
            0 -> listOf(todayDate) // Today only
            1 -> (1..daysInMonth).map { String.format(Locale.US, "%s-%02d", monthStr, it) } // Full month
            2 -> {
                // Last 7 days
                val list = mutableListOf<String>()
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val c = Calendar.getInstance()
                for (i in 0 until 7) {
                    list.add(sdf.format(c.time))
                    c.add(Calendar.DAY_OF_MONTH, -1)
                }
                list.reversed()
            }
            3 -> {
                // Custom picked days
                customSelectedDays.sorted().map { String.format(Locale.US, "%s-%02d", monthStr, it) }
            }
            else -> listOf(todayDate)
        }
    }

    // Compute dynamic stats for selected dates
    val groupStatsForDates = remember(selectedDatesList, groups, allMembers) {
        viewModel.getGroupStatsForDates(selectedDatesList)
    }

    val memberStatsForDates = remember(targetGroupId, selectedDatesList, allMembers) {
        viewModel.getMemberPeriodStats(targetGroupId, selectedDatesList)
    }

    // Macro overall figures
    val totalConsideredMembers = remember(targetGroupId, allMembers, memberStatsForDates) {
        if (targetGroupId == 0L) allMembers.size else memberStatsForDates.size
    }
    val totalPresent = remember(targetGroupId, groupStatsForDates, memberStatsForDates) {
        if (targetGroupId == 0L) groupStatsForDates.sumOf { it.presentCount } else memberStatsForDates.sumOf { it.presentDays }
    }
    val totalAbsent = remember(targetGroupId, groupStatsForDates, memberStatsForDates) {
        if (targetGroupId == 0L) groupStatsForDates.sumOf { it.absentCount } else memberStatsForDates.sumOf { it.absentDays }
    }
    val totalExcused = remember(targetGroupId, groupStatsForDates, memberStatsForDates) {
        if (targetGroupId == 0L) groupStatsForDates.sumOf { it.excusedCount } else memberStatsForDates.sumOf { it.excusedDays }
    }
    val totalExpectedDays = remember(totalConsideredMembers, selectedDatesList) {
        (totalConsideredMembers * selectedDatesList.size).coerceAtLeast(totalPresent + totalAbsent + totalExcused).coerceAtLeast(1)
    }
    val overallAttendanceRate = remember(totalPresent, totalExpectedDays) {
        (totalPresent.toFloat() / totalExpectedDays.toFloat()) * 100f
    }

    val targetGroupName = if (targetGroupId == 0L) "پۈتۈن سىستېما" else (groups.find { it.id == targetGroupId }?.name ?: "گۇرۇپپا $targetGroupId")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .wrapContentHeight()
                .heightIn(max = 760.dp)
                .testTag("multi_date_analytics_dialog"),
            shape = RoundedCornerShape(22.dp),
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
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "$targetGroupName يوقلىما نىسبىتى ۋە تەھلىلى",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "كۆپ كۈنلۈك، بىر ئايلىق ياكى تاللانغان كۈنلەر بويىچە نىزامى ئارخىپ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = s.cancel,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Group Switcher Tabs or Locked Group Banner
                if (restrictToGroupOnly && targetGroupId > 0L) {
                    val thisGrp = groups.find { it.id == targetGroupId }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "بايراق: ${thisGrp?.name ?: "گۇرۇپپا $targetGroupId"}",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "بايراق كۆپ كۈنلۈك تەھلىلى",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // All groups chip (Admin only)
                        FilterChip(
                            selected = targetGroupId == 0L,
                            onClick = { targetGroupId = 0L },
                            label = { Text("پۈتۈن سىستېما", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                            leadingIcon = if (targetGroupId == 0L) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )

                        // 6 Groups chips
                        groups.forEach { grp ->
                            val isSel = targetGroupId == grp.id
                            FilterChip(
                                selected = isSel,
                                onClick = { targetGroupId = grp.id },
                                label = { Text(grp.name, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal, fontSize = 11.sp) },
                                leadingIcon = if (isSel) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Date Filter & Month Range Control Card
                ElevatedCard(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        // Month selector (1..12) & Year
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$currentYear-يىلى $currentMonth-ئاي",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Month picker dropdown or mini chips
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                (1..12).forEach { m ->
                                    val isM = currentMonth == m
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isM) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clickable { currentMonth = m }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "$m",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isM) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Selection Mode Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = selectionMode == 0,
                                onClick = { selectionMode = 0 },
                                label = { Text("بۈگۈن", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = selectionMode == 1,
                                onClick = { selectionMode = 1 },
                                label = { Text("پۈتۈن ئاي", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = selectionMode == 2,
                                onClick = { selectionMode = 2 },
                                label = { Text("ئۆتكەن 7 كۈن", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = selectionMode == 3,
                                onClick = { selectionMode = 3 },
                                label = { Text("كۈن تاللاش", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // If Custom Days Mode selected: Show interactive 1..31 day selector
                        if (selectionMode == 3) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "تەھلىل قىلماقچى بولغان كۈنلەرنى بېسىپ تاللاڭ (${customSelectedDays.size} كۈن تاللانRegistered):",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                (1..daysInMonth).forEach { dayNum ->
                                    val isSelected = customSelectedDays.contains(dayNum)
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clickable {
                                                if (isSelected) {
                                                    if (customSelectedDays.size > 1) customSelectedDays.remove(dayNum)
                                                } else {
                                                    customSelectedDays.add(dayNum)
                                                }
                                            }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "$dayNum",
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        customSelectedDays.clear()
                                        customSelectedDays.addAll(1..daysInMonth)
                                    }
                                ) {
                                    Text("ھەممىنى تاللاش", fontSize = 11.sp)
                                }
                                TextButton(
                                    onClick = {
                                        customSelectedDays.clear()
                                        customSelectedDays.add(cal.get(Calendar.DAY_OF_MONTH).coerceIn(1, daysInMonth))
                                    }
                                ) {
                                    Text("بۈگۈنگە قايتۇرۇش", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Calculated Summary Banner
                ElevatedCard(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        val presentPercentage = if (totalExpectedDays > 0) (totalPresent.toFloat() / totalExpectedDays.toFloat()) * 100f else 0f
                        val absentPercentage = if (totalExpectedDays > 0) (totalAbsent.toFloat() / totalExpectedDays.toFloat()) * 100f else 0f
                        val excusedPercentage = if (totalExpectedDays > 0) (totalExcused.toFloat() / totalExpectedDays.toFloat()) * 100f else 0f

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "$targetGroupName يوقلىما نىسبىتى",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "دەۋر: ${selectedDatesList.size} كۈن (${selectedDatesList.firstOrNull() ?: ""} ~ ${selectedDatesList.lastOrNull() ?: ""})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", overallAttendanceRate)}%",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    color = if (overallAttendanceRate >= 80f) PresentGreen else AbsentRed
                                )
                                Text(
                                    text = "رۇخسەت: ${String.format(Locale.US, "%.1f", excusedPercentage)}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ExcusedBlue
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Stats Badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            MiniStatBadge(s.statusPresent, "$totalPresent (${String.format(Locale.US, "%.0f%%", presentPercentage)})", PresentGreenContainer, PresentGreen, Modifier.weight(1f))
                            MiniStatBadge(s.statusAbsent, "$totalAbsent (${String.format(Locale.US, "%.0f%%", absentPercentage)})", AbsentRedContainer, AbsentRed, Modifier.weight(1f))
                            MiniStatBadge(s.statusExcused, "$totalExcused (${String.format(Locale.US, "%.0f%%", excusedPercentage)})", ExcusedBlueContainer, ExcusedBlue, Modifier.weight(1f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Detail Section Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (targetGroupId == 0L) "گۇرۇپپىلار ھالىتى (${groups.size} چوڭ گۇرۇپپا)" else "ئەزالار يوقلىما ئارخىپى (${memberStatsForDates.size} ئەزا)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Export quick buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedButton(
                            onClick = {
                                viewModel.exportGroupAttendanceProfessional(context, targetGroupId, selectedDatesList, isExcel = true)
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(30.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Excel", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.exportGroupAttendanceProfessional(context, targetGroupId, selectedDatesList, isExcel = false)
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(30.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Word", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Detail List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .heightIn(max = 240.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (targetGroupId == 0L) {
                        // Show all 6 Groups
                        items(groupStatsForDates) { grpStat ->
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { targetGroupId = grpStat.group.id },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = grpStat.group.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${s.statusPresent}: ${grpStat.presentCount} | ${s.statusAbsent}: ${grpStat.absentCount} | ${s.statusExcused}: ${grpStat.excusedCount} (رۇخسەت: ${String.format(Locale.US, "%.0f%%", grpStat.excusedRate)})",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "${String.format(Locale.US, "%.1f", grpStat.attendanceRate)}%",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (grpStat.attendanceRate >= 80f) PresentGreen else AbsentRed
                                            )
                                            Text(
                                                text = "رۇخسەت: ${String.format(Locale.US, "%.0f", grpStat.excusedRate)}%",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = ExcusedBlue
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.Assessment,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Show Individual Members for the chosen group
                        itemsIndexed(memberStatsForDates) { index, mStat ->
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${index + 1}",
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 10.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = mStat.member.name,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${if (mStat.member.subGroup == 2) s.subGroup2 else s.subGroup1} • ${s.statusPresent}: ${mStat.presentDays}ك | ${s.statusAbsent}: ${mStat.absentDays}ك | ${s.statusExcused}: ${mStat.excusedDays}ك",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${String.format(Locale.US, "%.0f", mStat.attendanceRate)}%",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = if (mStat.attendanceRate >= 80f) PresentGreen else AbsentRed
                                        )
                                        Text(
                                            text = "رۇخسەت: ${String.format(Locale.US, "%.0f", mStat.excusedRate)}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = ExcusedBlue
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(s.cancel, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            viewModel.copyGroupAttendanceProfessionalText(context, targetGroupId, selectedDatesList)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.4f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("دوكلاتنى كۆچۈرۈش", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniStatBadge(
    label: String,
    value: String,
    containerColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = containerColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.85f),
                fontSize = 10.sp
            )
        }
    }
}
