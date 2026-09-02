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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import java.util.Locale

@Composable
fun GroupExportSection(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val language by viewModel.currentLanguage.collectAsState()
    val s = AppStrings.get(language)
    val groups by viewModel.groups.collectAsState()
    val allMembers by viewModel.allMembers.collectAsState()
    val todayDate by viewModel.selectedDate.collectAsState()

    var selectedGroupId by remember { mutableStateOf(0L) } // 0L: All groups, 1L..6L: specific

    val cal = Calendar.getInstance()
    var selectedYear by remember { mutableIntStateOf(cal.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableIntStateOf(cal.get(Calendar.MONTH) + 1) } // 1..12
    var selectionMode by remember { mutableIntStateOf(1) } // 0: Today, 1: Full Month, 2: Last 7 Days, 3: Custom
    val customDays = remember { mutableStateListOf<Int>() }

    val daysInMonth = remember(selectedYear, selectedMonth) {
        val c = Calendar.getInstance()
        c.set(Calendar.YEAR, selectedYear)
        c.set(Calendar.MONTH, selectedMonth - 1)
        c.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    if (customDays.isEmpty()) {
        val tDay = cal.get(Calendar.DAY_OF_MONTH).coerceIn(1, daysInMonth)
        customDays.add(tDay)
    }

    val monthPrefix = String.format(Locale.US, "%04d-%02d", selectedYear, selectedMonth)
    val selectedDatesList = remember(selectionMode, selectedYear, selectedMonth, customDays.toList(), todayDate) {
        when (selectionMode) {
            0 -> listOf(todayDate)
            1 -> (1..daysInMonth).map { String.format(Locale.US, "%s-%02d", monthPrefix, it) }
            2 -> {
                val list = mutableListOf<String>()
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val c = Calendar.getInstance()
                for (i in 0 until 7) {
                    list.add(sdf.format(c.time))
                    c.add(Calendar.DAY_OF_MONTH, -1)
                }
                list.reversed()
            }
            3 -> customDays.sorted().map { String.format(Locale.US, "%s-%02d", monthPrefix, it) }
            else -> listOf(todayDate)
        }
    }

    val memberStats = remember(selectedGroupId, selectedDatesList, allMembers) {
        viewModel.getMemberPeriodStats(selectedGroupId, selectedDatesList)
    }

    val groupStats = remember(selectedDatesList, groups, allMembers) {
        viewModel.getGroupStatsForDates(selectedDatesList)
    }

    val currentGroupName = if (selectedGroupId == 0L) "پۈتۈن سىستېما (بارلىق گۇرۇپپىلار)" else (groups.find { it.id == selectedGroupId }?.name ?: "گۇرۇپپا $selectedGroupId")
    val totalCount = if (selectedGroupId == 0L) allMembers.size else memberStats.size
    val totalPresent = if (selectedGroupId == 0L) groupStats.sumOf { it.presentCount } else memberStats.sumOf { it.presentDays }
    val totalAbsent = if (selectedGroupId == 0L) groupStats.sumOf { it.absentCount } else memberStats.sumOf { it.absentDays }
    val totalExcused = if (selectedGroupId == 0L) groupStats.sumOf { it.excusedCount } else memberStats.sumOf { it.excusedDays }
    val totalExpected = (totalCount * selectedDatesList.size).coerceAtLeast(totalPresent + totalAbsent + totalExcused).coerceAtLeast(1)
    val overallRate = (totalPresent.toFloat() / totalExpected.toFloat()) * 100f

    ElevatedCard(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("group_export_section_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "چوڭ گۇرۇپپىلار كەسپىي ئېكسپورتى (Excel / Word)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "ئومۇمىي ئادەم سانى، كەلگەن، كەلمىگەن ۋە رۇخسەت كۈنلىرى تەپسىلاتى",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Group selector chips
            Text(
                text = "1. مەلۇماتى كېرەكلىك گۇرۇپپىنى تاللاڭ:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedGroupId == 0L,
                    onClick = { selectedGroupId = 0L },
                    label = { Text("بارلىق گۇرۇپپىلار", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    leadingIcon = if (selectedGroupId == 0L) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )

                groups.forEach { grp ->
                    val isSel = selectedGroupId == grp.id
                    FilterChip(
                        selected = isSel,
                        onClick = { selectedGroupId = grp.id },
                        label = { Text(grp.name, fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
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

            Spacer(modifier = Modifier.height(12.dp))

            // Date Range & Month selector
            Text(
                text = "2. ۋاقىت دەۋرى ياكى كۈنلەرنى تاللاڭ:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    // Month selector (1..12)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$selectedYear-يىلى $selectedMonth-ئاي",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            (1..12).forEach { m ->
                                val isM = selectedMonth == m
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isM) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable { selectedMonth = m }
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

                    Spacer(modifier = Modifier.height(6.dp))

                    // Preset Mode Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
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

                    if (selectionMode == 3) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            (1..daysInMonth).forEach { dayNum ->
                                val isSelected = customDays.contains(dayNum)
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clickable {
                                            if (isSelected) {
                                                if (customDays.size > 1) customDays.remove(dayNum)
                                            } else {
                                                customDays.add(dayNum)
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
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dynamic Report Summary Card
            ElevatedCard(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "📋 $currentGroupName",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "ئومۇمىي ئەزا: $totalCount نەپەر | ۋاقىت دەۋرى: ${selectedDatesList.size} كۈن",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }

                        Text(
                            text = "${String.format(Locale.US, "%.1f", overallRate)}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = if (overallRate >= 80f) PresentGreen else AbsentRed
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MiniStatBadge(s.statusPresent, totalPresent.toString(), PresentGreenContainer, PresentGreen, Modifier.weight(1f))
                        MiniStatBadge(s.statusAbsent, totalAbsent.toString(), AbsentRedContainer, AbsentRed, Modifier.weight(1f))
                        MiniStatBadge(s.statusExcused, totalExcused.toString(), ExcusedBlueContainer, ExcusedBlue, Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Export Buttons: Excel & Word
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.exportGroupAttendanceProfessional(context, selectedGroupId, selectedDatesList, isExcel = true)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E7E34),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("export_group_excel_button")
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Excel چىقىرىش", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        viewModel.exportGroupAttendanceProfessional(context, selectedGroupId, selectedDatesList, isExcel = false)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2B579A),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("export_group_word_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Word ھەمبەھىرلەش", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    viewModel.copyGroupAttendanceProfessionalText(context, selectedGroupId, selectedDatesList)
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("copy_group_report_button")
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("دوكلات تېكىستىنى كۆچۈرۈۋېلىش", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            // Member Preview Table (if specific group or all)
            if (memberStats.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "ئەزالار يوقلىما جەدۋىلى ئالدىن كۆرۈنۈشى (${memberStats.size} ئەزا):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(memberStats) { idx, mStat ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
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
                                    Text(
                                        text = "${idx + 1}.",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = mStat.member.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${if (mStat.member.subGroup == 2) s.subGroup2 else s.subGroup1} • كەلگەن: ${mStat.presentDays} | كەلمىگەن: ${mStat.absentDays} | رۇخسەت: ${mStat.excusedDays}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Text(
                                    text = "${String.format(Locale.US, "%.0f", mStat.attendanceRate)}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (mStat.attendanceRate >= 80f) PresentGreen else AbsentRed
                                )
                            }
                        }
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
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
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
