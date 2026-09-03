package com.example.ui.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.repository.AttendanceRepository
import com.example.data.model.AttendanceRecordEntity
import com.example.data.model.AttendanceStatus
import com.example.data.model.GroupEntity
import com.example.data.model.MemberEntity
import com.example.data.model.MemberStatus
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.supabase.SupabaseConnectionStatus
import com.example.data.supabase.SupabaseSyncResult
import com.example.data.supabase.SupabaseSyncService
import com.example.i18n.AppStrings
import com.example.i18n.Language
import com.example.i18n.LocalizedStrings
import com.example.ui.theme.AppThemePreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class AbsenteeStat(
    val member: MemberEntity,
    val group: GroupEntity?,
    val absentCount: Int,
    val totalCheckedDays: Int,
    val absenceRate: Float
)

data class MemberPeriodStat(
    val member: MemberEntity,
    val groupName: String,
    val presentDays: Int,
    val absentDays: Int,
    val excusedDays: Int,
    val lateDays: Int,
    val totalCheckedDays: Int,
    val attendanceRate: Float
) {
    val excusedRate: Float
        get() = if (totalCheckedDays > 0) (excusedDays.toFloat() / totalCheckedDays) * 100f else 0f
}

data class GroupStat(
    val group: GroupEntity,
    val totalMembers: Int,
    val activeMembers: Int,
    val presentCount: Int,
    val absentCount: Int,
    val lateCount: Int,
    val excusedCount: Int,
    val attendanceRate: Float
) {
    val excusedRate: Float
        get() {
            val total = (presentCount + absentCount + excusedCount + lateCount).coerceAtLeast(totalMembers)
            return if (total > 0) (excusedCount.toFloat() / total) * 100f else 0f
        }
}

data class PeriodGroupStat(
    val group: GroupEntity,
    val totalRecords: Int,
    val presentCount: Int,
    val absentCount: Int,
    val excusedCount: Int,
    val attendanceRate: Float
) {
    val excusedRate: Float
        get() {
            val total = (presentCount + absentCount + excusedCount).coerceAtLeast(totalRecords)
            return if (total > 0) (excusedCount.toFloat() / total) * 100f else 0f
        }
}

data class EquipmentSummaryStats(
    val totalCount: Int,
    val readyCount: Int,
    val notReadyCount: Int,
    val readinessRate: Float
)

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {

    val repository: AttendanceRepository

    init {
        val database = AppDatabase.getInstance(application)
        repository = AttendanceRepository(database.attendanceDao())
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            repository.ensureInitialized()
            checkAndRegisterCurrentDevice()
            com.example.util.NetworkSyncManager.startListening(application, repository)
            // Pull latest cloud data immediately on app launch
            pullAllFromSupabase(silent = true)
            // Start periodic background sync every 25 seconds
            startPeriodicSync()
        }
    }

    private val appPrefs: SharedPreferences = application.getSharedPreferences("yoqlima_prefs", Context.MODE_PRIVATE)

    private val _currentLanguage = MutableStateFlow(
        try {
            Language.valueOf(appPrefs.getString("current_language", Language.UYGHUR.name) ?: Language.UYGHUR.name)
        } catch (e: Exception) {
            Language.UYGHUR
        }
    )
    val currentLanguage: StateFlow<Language> = _currentLanguage.asStateFlow()

    // Dark Mode and Themes
    private val _isDarkMode = MutableStateFlow(appPrefs.getBoolean("is_dark_mode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _themePreset = MutableStateFlow(
        try {
            AppThemePreset.valueOf(appPrefs.getString("theme_preset", AppThemePreset.BLUE.name) ?: AppThemePreset.BLUE.name)
        } catch (e: Exception) {
            AppThemePreset.BLUE
        }
    )
    val themePreset: StateFlow<AppThemePreset> = _themePreset.asStateFlow()

    val strings: LocalizedStrings
        get() = AppStrings.get(_currentLanguage.value)

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // Last selected or logged-out orb ID (0L for Admin, 1L..6L for Groups)
    private val _lastActiveOrbId = MutableStateFlow<Long?>(null)
    val lastActiveOrbId: StateFlow<Long?> = _lastActiveOrbId.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val monthFormat = SimpleDateFormat("yyyy-MM", Locale.US)

    private val _selectedDate = MutableStateFlow(dateFormat.format(Date()))
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _selectedMonth = MutableStateFlow(monthFormat.format(Date()))
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    private val _selectedQuarter = MutableStateFlow(
        (Calendar.getInstance().get(Calendar.MONTH) / 3) + 1
    )
    val selectedQuarter: StateFlow<Int> = _selectedQuarter.asStateFlow()

    private val _selectedGroupId = MutableStateFlow<Long>(1L)
    val selectedGroupId: StateFlow<Long> = _selectedGroupId.asStateFlow()

    val groups: StateFlow<List<GroupEntity>> = repository.allGroups
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val users: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMembers: StateFlow<List<MemberEntity>> = repository.allMembers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAttendance: StateFlow<List<AttendanceRecordEntity>> = repository.allAttendance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allEquipment: StateFlow<List<com.example.data.model.EquipmentEntity>> = repository.allEquipment
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDailyUpdates: StateFlow<List<com.example.data.model.DailyUpdateEntity>> = repository.allDailyUpdates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExecutiveContacts: StateFlow<List<com.example.data.model.ExecutiveContactEntity>> = repository.allExecutiveContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNoticeReceipts: StateFlow<List<com.example.data.model.NoticeReceiptEntity>> = repository.allNoticeReceipts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGroupLeaders: StateFlow<List<com.example.data.model.GroupLeaderEntity>> = repository.allGroupLeaders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGroupLeaderAttendance: StateFlow<List<com.example.data.model.GroupLeaderAttendanceEntity>> = repository.allGroupLeaderAttendance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSanjaqLeaders: StateFlow<List<com.example.data.model.SanjaqLeaderEntity>> = repository.allSanjaqLeaders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDeviceSessions: StateFlow<List<com.example.data.model.DeviceSessionEntity>> = repository.allDeviceSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Leader Attendance Visibility Toggle (Admin Settings ⚙️)
    private val _leaderAttendanceVisible = MutableStateFlow(appPrefs.getBoolean("leader_attendance_visible", true))
    val leaderAttendanceVisible: StateFlow<Boolean> = _leaderAttendanceVisible.asStateFlow()

    fun toggleLeaderAttendanceVisible() {
        val next = !_leaderAttendanceVisible.value
        _leaderAttendanceVisible.value = next
        appPrefs.edit().putBoolean("leader_attendance_visible", next).apply()
    }

    // Pull-to-Refresh State
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun triggerPullToRefresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                if (_isAutoSyncEnabled.value) {
                    pullAllFromSupabase()
                } else {
                    kotlinx.coroutines.delay(650)
                }
            } catch (e: Exception) {
                // ignore
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    // Multi-selected Sanjaqs state for dynamic consolidated calculations
    private val _selectedSanjaqNumbers = MutableStateFlow<Set<Int>>(setOf(1, 2, 3, 4))
    val selectedSanjaqNumbers: StateFlow<Set<Int>> = _selectedSanjaqNumbers.asStateFlow()

    fun loadSelectedSanjaqsForGroup(groupId: Long) {
        val key = "selected_sanjaqs_group_$groupId"
        val saved = appPrefs.getStringSet(key, null)
        if (saved != null && saved.isNotEmpty()) {
            _selectedSanjaqNumbers.value = saved.mapNotNull { it.toIntOrNull() }.toSet().ifEmpty { setOf(1, 2, 3, 4) }
        } else {
            _selectedSanjaqNumbers.value = setOf(1, 2, 3, 4)
        }
    }

    private fun persistSelectedSanjaqs(sanjaqs: Set<Int>) {
        val groupId = _currentUser.value?.groupId ?: _selectedGroupId.value
        val key = "selected_sanjaqs_group_$groupId"
        appPrefs.edit().putStringSet(key, sanjaqs.map { it.toString() }.toSet()).apply()
    }

    fun toggleSanjaqSelection(sanjaqNum: Int) {
        val current = _selectedSanjaqNumbers.value.toMutableSet()
        if (current.contains(sanjaqNum)) {
            if (current.size > 1) { // Keep at least one selected
                current.remove(sanjaqNum)
            }
        } else {
            current.add(sanjaqNum)
        }
        _selectedSanjaqNumbers.value = current
        persistSelectedSanjaqs(current)
    }

    fun selectAllSanjaqs(sanjaqNums: Set<Int>) {
        _selectedSanjaqNumbers.value = sanjaqNums
        persistSelectedSanjaqs(sanjaqNums)
    }

    // Device Management & Remote Termination
    val currentDeviceId: String by lazy {
        var devId = appPrefs.getString("app_device_uuid", "") ?: ""
        if (devId.isBlank()) {
            devId = try {
                android.provider.Settings.Secure.getString(
                    getApplication<Application>().contentResolver,
                    android.provider.Settings.Secure.ANDROID_ID
                ) ?: java.util.UUID.randomUUID().toString()
            } catch (e: Exception) {
                java.util.UUID.randomUUID().toString()
            }
            appPrefs.edit().putString("app_device_uuid", devId).apply()
        }
        devId
    }

    private val _isCurrentDeviceBlocked = MutableStateFlow(false)
    val isCurrentDeviceBlocked: StateFlow<Boolean> = _isCurrentDeviceBlocked.asStateFlow()

    fun checkAndRegisterCurrentDevice(userName: String = "") {
        viewModelScope.launch {
            val devName = "${android.os.Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${android.os.Build.MODEL}"
            val osVer = "Android ${android.os.Build.VERSION.RELEASE}"
            repository.registerOrUpdateDeviceSession(currentDeviceId, devName, osVer, userName.ifBlank { "باشقۇرغۇچى / ئەزا" })
            val blocked = repository.isDeviceBlocked(currentDeviceId)
            _isCurrentDeviceBlocked.value = blocked
        }
    }

    fun terminateDevice(deviceId: String) {
        viewModelScope.launch {
            repository.setDeviceBlockStatus(deviceId, true, "باشقۇرغۇچى تەرىپىدىن توختىتىلدى")
            if (deviceId == currentDeviceId) {
                _isCurrentDeviceBlocked.value = true
            }
            Toast.makeText(getApplication(), "تېلېفون مۇۋەپپەقىيەتلىك توختىتىلدى", Toast.LENGTH_SHORT).show()
        }
    }

    fun restoreDevice(deviceId: String) {
        viewModelScope.launch {
            repository.setDeviceBlockStatus(deviceId, false, "")
            if (deviceId == currentDeviceId) {
                _isCurrentDeviceBlocked.value = false
            }
            Toast.makeText(getApplication(), "تېلېفون ئەسلىگە كەلتۈرۈلدى", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteDevice(deviceId: String) {
        viewModelScope.launch {
            repository.deleteDeviceSession(deviceId)
            Toast.makeText(getApplication(), "ئۈسكۈنە ئۆچۈرۈلدى", Toast.LENGTH_SHORT).show()
        }
    }

    // Save Bayraq Leader Contact
    fun saveBayraqLeader(leader: com.example.data.model.GroupLeaderEntity) {
        viewModelScope.launch {
            repository.saveOrUpdateGroupLeader(leader)
            Toast.makeText(getApplication(), strings.savedSuccess, Toast.LENGTH_SHORT).show()
        }
    }

    // Save Bayraq Leader Attendance
    fun saveBayraqLeaderAttendance(groupId: Long, roleType: String, status: String, note: String = "") {
        viewModelScope.launch {
            repository.saveGroupLeaderAttendance(groupId, roleType, _selectedDate.value, status, note)
        }
    }

    // Save Sanjaq Leadership
    fun saveSanjaqLeader(sanjaq: com.example.data.model.SanjaqLeaderEntity) {
        viewModelScope.launch {
            repository.saveOrUpdateSanjaqLeader(sanjaq)
            Toast.makeText(getApplication(), strings.savedSuccess, Toast.LENGTH_SHORT).show()
        }
    }

    // Add new Sanjaq to a group
    fun addNewSanjaq(groupId: Long) {
        viewModelScope.launch {
            val existing = allSanjaqLeaders.value.filter { it.groupId == groupId }
            val nextNum = if (existing.isEmpty()) 1 else (existing.maxOf { it.sanjaqNumber } + 1)
            val newSanjaq = com.example.data.model.SanjaqLeaderEntity(
                groupId = groupId,
                sanjaqNumber = nextNum,
                sanjaqCustomName = "$nextNum-سانجاق",
                leaderName = "",
                leaderPhone = "",
                deputyName = "",
                deputyPhone = ""
            )
            repository.saveOrUpdateSanjaqLeader(newSanjaq)
            _selectedSanjaqNumbers.value = _selectedSanjaqNumbers.value + nextNum
            Toast.makeText(getApplication(), "$nextNum-سانجاق قوشۇلدى", Toast.LENGTH_SHORT).show()
        }
    }

    fun addNewSanjaqWithDetails(
        groupId: Long,
        customName: String,
        leaderName: String = "",
        leaderPhone: String = "",
        leaderTelegram: String = "",
        leaderWhatsapp: String = "",
        deputyName: String = "",
        deputyPhone: String = "",
        deputyTelegram: String = "",
        deputyWhatsapp: String = "",
        onCreated: (Int) -> Unit = {}
    ) {
        viewModelScope.launch {
            val existing = allSanjaqLeaders.value.filter { it.groupId == groupId }
            val nextNum = if (existing.isEmpty()) 1 else (existing.maxOf { it.sanjaqNumber } + 1)
            val finalName = customName.ifBlank { "$nextNum-سانجاق" }
            val newSanjaq = com.example.data.model.SanjaqLeaderEntity(
                groupId = groupId,
                sanjaqNumber = nextNum,
                sanjaqCustomName = finalName,
                leaderName = leaderName.trim(),
                leaderPhone = leaderPhone.trim(),
                leaderTelegram = leaderTelegram.trim(),
                leaderWhatsapp = leaderWhatsapp.trim(),
                deputyName = deputyName.trim(),
                deputyPhone = deputyPhone.trim(),
                deputyTelegram = deputyTelegram.trim(),
                deputyWhatsapp = deputyWhatsapp.trim()
            )
            repository.saveOrUpdateSanjaqLeader(newSanjaq)
            _selectedSanjaqNumbers.value = _selectedSanjaqNumbers.value + nextNum
            Toast.makeText(getApplication(), "$finalName مۇۋەپپەقىيەتلىك قوشۇلدى", Toast.LENGTH_SHORT).show()
            onCreated(nextNum)
        }
    }

    // Broadcast Announcement / Emergency Notice with System Notification Signal
    fun broadcastNoticeWithNotification(title: String, content: String, author: String, groupId: Long = 0L, priority: String = "URGENT") {
        viewModelScope.launch {
            val grpName = if (groupId == 0L) "بارلىق بايراق ۋە قىسىملار" else (groups.value.find { it.id == groupId }?.name ?: "")
            val newUpdate = repository.addDailyUpdate(groupId, grpName, author, title, content, _selectedDate.value, priority)
            
            // Mark as read and notified on the sender's own device so NO popup appears here
            try {
                com.example.util.LocalReadNoticeTracker.markNoticeAsRead(getApplication<android.app.Application>(), newUpdate.id)
                com.example.util.LocalReadNoticeTracker.markAsNotified(getApplication<android.app.Application>(), newUpdate.id)
            } catch (e: Exception) {
                // ignore
            }
            Toast.makeText(getApplication(), "ئۇقتۇرۇش مۇۋەپپەقىيەتلىك يوللاندى ۋە بارلىق باشقا ئۈسكۈنىلەرگە يەتكۈزۈلدى", Toast.LENGTH_SHORT).show()
        }
    }

    // Member Search Across Groups (Requirement 3)
    private val _memberSearchQuery = MutableStateFlow("")
    val memberSearchQuery: StateFlow<String> = _memberSearchQuery.asStateFlow()

    fun setMemberSearchQuery(query: String) {
        _memberSearchQuery.value = query
    }

    val searchedMembersAcrossGroups: StateFlow<List<Pair<MemberEntity, GroupEntity?>>> = combine(
        allMembers,
        groups,
        _memberSearchQuery
    ) { members, grps, query ->
        val q = query.trim().lowercase()
        if (q.isBlank()) {
            emptyList()
        } else {
            members.filter { m ->
                m.name.lowercase().contains(q) ||
                m.contactAddress.lowercase().contains(q) ||
                m.telegramContact.lowercase().contains(q) ||
                m.whatsappContact.lowercase().contains(q) ||
                m.notes.lowercase().contains(q)
            }.map { m ->
                m to grps.find { it.id == m.groupId }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Equipment filter by group for Admin view (Requirement 7)
    private val _equipmentFilterGroupId = MutableStateFlow<Long?>(null) // null = all
    val equipmentFilterGroupId: StateFlow<Long?> = _equipmentFilterGroupId.asStateFlow()

    fun setEquipmentFilterGroupId(groupId: Long?) {
        _equipmentFilterGroupId.value = groupId
    }

    val adminFilteredEquipment: StateFlow<List<com.example.data.model.EquipmentEntity>> = combine(
        allEquipment,
        _equipmentFilterGroupId
    ) { items, filterId ->
        if (filterId == null || filterId == 0L) {
            items
        } else {
            items.filter { it.groupId == filterId }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Duty Group (نۆۋبەتچى گۇرۇپپا) State
    private val _dutyGroupId = MutableStateFlow(appPrefs.getLong("duty_group_id", 1L))
    val dutyGroupId: StateFlow<Long> = _dutyGroupId.asStateFlow()

    private val _dutyGroupNotes = MutableStateFlow(appPrefs.getString("duty_group_notes", "بۈگۈنكى پۈتۈن كۈنلۈك نۆۋەتچىلىك ۋە قاراۋۇللۇق ۋەزىپىسى") ?: "")
    val dutyGroupNotes: StateFlow<String> = _dutyGroupNotes.asStateFlow()

    fun setDutyGroup(groupId: Long, notes: String = "") {
        _dutyGroupId.value = groupId
        _dutyGroupNotes.value = notes
        appPrefs.edit().putLong("duty_group_id", groupId).putString("duty_group_notes", notes).apply()
        Toast.makeText(getApplication(), strings.dutyGroupStatusLabel + ": " + (groups.value.find { it.id == groupId }?.name ?: ""), Toast.LENGTH_SHORT).show()
    }

    fun parseDutySubGroups(customName: String, fallbackSingle: Int): List<Int> {
        val extracted = customName.split(",", "،", ";", " ")
            .mapNotNull { it.trim().filter { ch -> ch.isDigit() }.toIntOrNull() }
            .filter { it in 1..20 }
            .distinct()
        if (extracted.isNotEmpty()) return extracted.sorted()
        val single = if (fallbackSingle > 0) fallbackSingle else 1
        return listOf(single)
    }

    fun isDutySubGroupLocked(groupId: Long): Boolean {
        val user = _currentUser.value
        if (user?.role == UserRole.ADMIN) return false
        val todayDate = _selectedDate.value
        val key = "duty_first_set_${groupId}_${todayDate}"
        val firstSetTime = appPrefs.getLong(key, 0L)
        if (firstSetTime == 0L) return false
        val twentyFourHoursMillis = 24 * 60 * 60 * 1000L
        return (System.currentTimeMillis() - firstSetTime) > twentyFourHoursMillis
    }

    fun setGroupDutySubGroups(groupId: Long, dutySubGroups: List<Int>, notes: String = "", customName: String = "") {
        val user = _currentUser.value
        if (user?.role != UserRole.ADMIN && isDutySubGroupLocked(groupId)) {
            Toast.makeText(getApplication(), "نۆۋەتچى سانجاقلار بېكىتىلگىنىگە 24 سائەت توشتى، ئۆزگەرتىش پەقەت باشقۇرغۇچىغا ئوچۇق", Toast.LENGTH_LONG).show()
            return
        }
        viewModelScope.launch {
            try {
                val grp = groups.value.find { it.id == groupId }
                if (grp != null) {
                    val sgs = if (dutySubGroups.isEmpty()) listOf(1) else dutySubGroups.sorted()
                    val finalCustomName = if (customName.isNotBlank()) customName else sgs.joinToString("، ") { "$it-سانجاق" }
                    val updated = grp.copy(
                        dutySubGroup = sgs.first(),
                        dutyNotes = notes,
                        dutySubGroupCustomName = finalCustomName
                    )
                    repository.updateGroup(updated)
                    val todayDate = _selectedDate.value
                    val key = "duty_first_set_${groupId}_${todayDate}"
                    if (appPrefs.getLong(key, 0L) == 0L) {
                        appPrefs.edit().putLong(key, System.currentTimeMillis()).apply()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(getApplication(), "نۆۋەتچى سانجاقلارنى ساقلاش مەغلۇپ بولدى: ${e.localizedMessage ?: "خاتالىق"}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun setGroupDutySubGroup(groupId: Long, dutySubGroup: Int, notes: String = "", customName: String = "") {
        setGroupDutySubGroups(groupId, listOf(dutySubGroup), notes, customName)
    }

    // Active duty group object combined with group details
    val currentDutyGroupEntity: StateFlow<GroupEntity?> = combine(groups, _dutyGroupId) { grps, dId ->
        grps.find { it.id == dId } ?: grps.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Duty Group Attendance Details & Time calculation (Requirement 8)
    val dutyGroupAttendanceSummary: StateFlow<DutyGroupAttendanceSummary> = combine(
        combine(currentDutyGroupEntity, allMembers, allAttendance) { dutyGrp, members, records ->
            Triple(dutyGrp, members, records)
        },
        _selectedDate,
        allSanjaqLeaders
    ) { (dutyGrp, members, records), date, sanjaqs ->
        if (dutyGrp == null) {
            DutyGroupAttendanceSummary()
        } else {
            val grpMembers = members.filter { it.groupId == dutyGrp.id }
            val grpRecords = records.filter { it.groupId == dutyGrp.id && it.date == date }
            val presentCount = grpRecords.count { it.status == AttendanceStatus.PRESENT }
            val absentCount = grpRecords.count { it.status == AttendanceStatus.ABSENT }
            val excusedCount = grpRecords.count { it.status == AttendanceStatus.EXCUSED }
            val totalConsidered = (presentCount + absentCount + excusedCount).coerceAtLeast(grpMembers.size)
            val rate = if (totalConsidered > 0) (presentCount.toFloat() / totalConsidered) * 100f else 0f
            val isSubmitted = grpRecords.isNotEmpty()
            val latestTimestamp = grpRecords.maxOfOrNull { it.timestamp } ?: 0L
            val formattedTime = if (latestTimestamp > 0L) {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(latestTimestamp))
            } else null

            // Subgroups details (supports multiple selected duty sanjaqs)
            val sgNums = parseDutySubGroups(dutyGrp.dutySubGroupCustomName, dutyGrp.dutySubGroup)
            val subName = if (dutyGrp.dutySubGroupCustomName.isNotBlank()) dutyGrp.dutySubGroupCustomName
                else sgNums.joinToString("، ") { "$it-سانجاق" }

            val allSgEntities = sanjaqs.filter { it.groupId == dutyGrp.id && it.sanjaqNumber in sgNums }
            val sgLeaders = allSgEntities.mapNotNull { it.leaderName.ifBlank { null } }
            val sgContacts = allSgEntities.mapNotNull { it.leaderPhone.ifBlank { null } }
            val sgTelegrams = allSgEntities.mapNotNull { it.leaderTelegram.ifBlank { null } }
            val sgWhatsapps = allSgEntities.mapNotNull { it.leaderWhatsapp.ifBlank { null } }

            val sgLeader = if (sgLeaders.isNotEmpty()) sgLeaders.joinToString(", ")
                else if (sgNums.contains(1) && dutyGrp.subLeader1.isNotBlank()) dutyGrp.subLeader1
                else if (sgNums.contains(2) && dutyGrp.subLeader2.isNotBlank()) dutyGrp.subLeader2 else ""

            val sgContact = if (sgContacts.isNotEmpty()) sgContacts.joinToString(", ")
                else if (sgNums.contains(1) && dutyGrp.subLeader1Contact.isNotBlank()) dutyGrp.subLeader1Contact
                else if (sgNums.contains(2) && dutyGrp.subLeader2Contact.isNotBlank()) dutyGrp.subLeader2Contact else ""

            val sgTelegram = if (sgTelegrams.isNotEmpty()) sgTelegrams.joinToString(", ")
                else if (sgNums.contains(1) && dutyGrp.subLeader1Telegram.isNotBlank()) dutyGrp.subLeader1Telegram
                else if (sgNums.contains(2) && dutyGrp.subLeader2Telegram.isNotBlank()) dutyGrp.subLeader2Telegram else ""

            val sgWhatsapp = if (sgWhatsapps.isNotEmpty()) sgWhatsapps.joinToString(", ")
                else if (sgNums.contains(1) && dutyGrp.subLeader1Whatsapp.isNotBlank()) dutyGrp.subLeader1Whatsapp
                else if (sgNums.contains(2) && dutyGrp.subLeader2Whatsapp.isNotBlank()) dutyGrp.subLeader2Whatsapp else ""

            val sgMembers = grpMembers.filter { it.subGroup in sgNums }
            val sgMemberIds = sgMembers.map { it.id }.toSet()
            val sgRecords = grpRecords.filter { it.memberId in sgMemberIds }
            val sgPresent = sgRecords.count { it.status == AttendanceStatus.PRESENT }
            val sgAbsent = sgRecords.count { it.status == AttendanceStatus.ABSENT }
            val sgExcused = sgRecords.count { it.status == AttendanceStatus.EXCUSED }
            val sgTotalConsidered = (sgPresent + sgAbsent + sgExcused).coerceAtLeast(sgMembers.size)
            val sgRate = if (sgTotalConsidered > 0) (sgPresent.toFloat() / sgTotalConsidered) * 100f else 0f
            val sgExcusedRate = if (sgTotalConsidered > 0) (sgExcused.toFloat() / sgTotalConsidered) * 100f else 0f

            DutyGroupAttendanceSummary(
                dutyGroup = dutyGrp,
                dutySubGroup = sgNums.firstOrNull() ?: 1,
                dutySubGroupList = sgNums,
                dutySubGroupName = subName,
                dutySubGroupLeader = sgLeader,
                dutySubGroupContact = sgContact,
                dutySubGroupTelegram = sgTelegram,
                dutySubGroupWhatsapp = sgWhatsapp,
                subGroupTotalMembers = sgMembers.size,
                subGroupPresentCount = sgPresent,
                subGroupAbsentCount = sgAbsent,
                subGroupExcusedCount = sgExcused,
                subGroupAttendanceRate = sgRate,
                subGroupExcusedRate = sgExcusedRate,
                totalMembers = grpMembers.size,
                isSubmitted = isSubmitted,
                presentCount = presentCount,
                absentCount = absentCount,
                excusedCount = excusedCount,
                attendanceRate = rate,
                lastSubmittedTime = formattedTime,
                dutyNotes = dutyGrp.dutyNotes
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DutyGroupAttendanceSummary())

    // Emergency Notices state & dismissal tracking
    private val _dismissedEmergencyNoticeIds = MutableStateFlow<Set<Long>>(emptySet())
    val dismissedEmergencyNoticeIds: StateFlow<Set<Long>> = _dismissedEmergencyNoticeIds.asStateFlow()

    fun dismissEmergencyNotice(noticeId: Long) {
        _dismissedEmergencyNoticeIds.value = _dismissedEmergencyNoticeIds.value + noticeId
    }

    // Unacknowledged notice count for current group lead
    val unacknowledgedNoticeCount: StateFlow<Int> = combine(
        allDailyUpdates,
        allNoticeReceipts,
        _currentUser
    ) { updates, receipts, user ->
        if (user?.role == UserRole.ADMIN) {
            0
        } else {
            val userGId = user?.groupId ?: 0L
            if (userGId == 0L) 0
            else {
                val applicableUpdates = updates.filter { it.groupId == 0L || it.groupId == userGId }
                applicableUpdates.count { update ->
                    val receipt = receipts.find { it.noticeId == update.id && it.groupId == userGId }
                    receipt == null || !receipt.isAcknowledged
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Active Emergency Notices (for group leads and all portals)
    val activeEmergencyNotices: StateFlow<List<com.example.data.model.DailyUpdateEntity>> = combine(
        allDailyUpdates,
        allNoticeReceipts,
        _currentUser,
        _dismissedEmergencyNoticeIds
    ) { updates, receipts, user, dismissedIds ->
        val urgentUpdates = updates.filter { it.priority == "URGENT" }
        if (user?.role == UserRole.ADMIN) {
            urgentUpdates
        } else {
            val userGId = user?.groupId ?: 0L
            urgentUpdates.filter { update ->
                val isTarget = (update.groupId == 0L || update.groupId == userGId)
                val receipt = receipts.find { it.noticeId == update.id && it.groupId == userGId }
                val isAcked = receipt?.isAcknowledged == true || dismissedIds.contains(update.id)
                isTarget && !isAcked
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered daily updates for current view (All updates if Admin, or Global + Group updates if Lead)
    val currentGroupDailyUpdates: StateFlow<List<com.example.data.model.DailyUpdateEntity>> = combine(
        allDailyUpdates,
        _selectedGroupId,
        _currentUser
    ) { updates, selectedGId, user ->
        if (user?.role == UserRole.ADMIN) {
            updates
        } else {
            val targetGroupId = user?.groupId ?: selectedGId
            updates.filter { it.groupId == 0L || it.groupId == targetGroupId }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Equipment list for the currently active/selected group
    val currentGroupEquipment: StateFlow<List<com.example.data.model.EquipmentEntity>> = combine(
        allEquipment,
        _selectedGroupId,
        _currentUser
    ) { items, selectedGId, user ->
        val targetGroupId = if (user?.role == UserRole.GROUP_LEAD && user.groupId != null) {
            user.groupId
        } else {
            selectedGId
        }
        items.filter { it.groupId == targetGroupId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Equipment summary stats for currently selected group
    val equipmentStats: StateFlow<EquipmentSummaryStats> = currentGroupEquipment.combine(_selectedGroupId) { items, _ ->
        val total = items.sumOf { it.totalCount }
        val ready = items.sumOf { it.readyCount }
        val notReady = items.sumOf { it.notReadyCount }
        val rate = if (total > 0) (ready.toFloat() / total) * 100f else 0f
        EquipmentSummaryStats(total, ready, notReady, rate)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EquipmentSummaryStats(0, 0, 0, 0f))

    // Filtered members for currently active group
    val currentGroupMembers: StateFlow<List<MemberEntity>> = combine(allMembers, _selectedGroupId, _currentUser) { members, selectedGId, user ->
        val targetGroupId = if (user?.role == UserRole.GROUP_LEAD && user.groupId != null) {
            user.groupId
        } else {
            selectedGId
        }
        members.filter { it.groupId == targetGroupId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Today's attendance records for current group and date
    val currentAttendanceMap: StateFlow<Map<Long, AttendanceRecordEntity>> = combine(
        allAttendance,
        _selectedDate,
        _selectedGroupId,
        _currentUser
    ) { records, date, selectedGId, user ->
        val targetGroupId = if (user?.role == UserRole.GROUP_LEAD && user.groupId != null) {
            user.groupId
        } else {
            selectedGId
        }
        records.filter { it.groupId == targetGroupId && it.date == date }
            .associateBy { it.memberId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Admin Group Statistics for selected date
    val groupStats: StateFlow<List<GroupStat>> = combine(
        groups,
        allMembers,
        allAttendance,
        _selectedDate
    ) { grps, members, records, date ->
        grps.map { grp ->
            val grpMembers = members.filter { it.groupId == grp.id }
            val leaderCount = (if (grp.subLeader1.isNotBlank()) 1 else 0) + (if (grp.subLeader2.isNotBlank()) 1 else 0)
            val totalGroupMembersCount = grpMembers.size + leaderCount
            val activeMembers = grpMembers.filter { it.status == MemberStatus.ACTIVE }
            val grpRecords = records.filter { it.groupId == grp.id && it.date == date }
            val presentCount = grpRecords.count { it.status == AttendanceStatus.PRESENT }
            val absentCount = grpRecords.count { it.status == AttendanceStatus.ABSENT }
            val lateCount = grpRecords.count { it.status == AttendanceStatus.LATE }
            val excusedCount = grpRecords.count { it.status == AttendanceStatus.EXCUSED }

            val totalConsidered = (presentCount + absentCount + excusedCount).coerceAtLeast(activeMembers.size)
            val rate = if (totalConsidered > 0) {
                (presentCount / totalConsidered.toFloat()) * 100f
            } else 0f

            GroupStat(
                group = grp,
                totalMembers = totalGroupMembersCount,
                activeMembers = activeMembers.size + leaderCount,
                presentCount = presentCount,
                absentCount = absentCount,
                lateCount = lateCount,
                excusedCount = excusedCount,
                attendanceRate = rate
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Monthly Analytics for all groups
    val monthlyStats: StateFlow<List<PeriodGroupStat>> = combine(
        groups,
        allAttendance,
        _selectedMonth
    ) { grps, records, monthStr ->
        grps.map { grp ->
            val grpRecords = records.filter { it.groupId == grp.id && it.date.startsWith(monthStr) }
            val presentCount = grpRecords.count { it.status == AttendanceStatus.PRESENT }
            val absentCount = grpRecords.count { it.status == AttendanceStatus.ABSENT }
            val excusedCount = grpRecords.count { it.status == AttendanceStatus.EXCUSED }
            val total = presentCount + absentCount + excusedCount

            val rate = if (total > 0) {
                (presentCount.toFloat() / total) * 100f
            } else 0f

            PeriodGroupStat(
                group = grp,
                totalRecords = total,
                presentCount = presentCount,
                absentCount = absentCount,
                excusedCount = excusedCount,
                attendanceRate = rate
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlyGroupStats: StateFlow<List<PeriodGroupStat>> = monthlyStats

    // Quarterly Analytics for all groups
    val quarterlyStats: StateFlow<List<PeriodGroupStat>> = combine(
        groups,
        allAttendance,
        _selectedQuarter
    ) { grps, records, quarter ->
        val monthPrefixes = when (quarter) {
            1 -> listOf("-01-", "-02-", "-03-")
            2 -> listOf("-04-", "-05-", "-06-")
            3 -> listOf("-07-", "-08-", "-09-")
            else -> listOf("-10-", "-11-", "-12-")
        }

        grps.map { grp ->
            val grpRecords = records.filter { rec ->
                rec.groupId == grp.id && monthPrefixes.any { prefix -> rec.date.contains(prefix) }
            }
            val presentCount = grpRecords.count { it.status == AttendanceStatus.PRESENT }
            val absentCount = grpRecords.count { it.status == AttendanceStatus.ABSENT }
            val excusedCount = grpRecords.count { it.status == AttendanceStatus.EXCUSED }
            val total = presentCount + absentCount + excusedCount

            val rate = if (total > 0) {
                (presentCount.toFloat() / total) * 100f
            } else 0f

            PeriodGroupStat(
                group = grp,
                totalRecords = total,
                presentCount = presentCount,
                absentCount = absentCount,
                excusedCount = excusedCount,
                attendanceRate = rate
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quarterlyGroupStats: StateFlow<List<PeriodGroupStat>> = quarterlyStats

    // Top Absentees list (Chronic absence analytics)
    val topAbsentees: StateFlow<List<AbsenteeStat>> = combine(
        allMembers,
        allAttendance,
        groups
    ) { members, records, grps ->
        val groupMap = grps.associateBy { it.id }
        members.filter { it.status == MemberStatus.ACTIVE }.mapNotNull { member ->
            val memberRecords = records.filter { it.memberId == member.id }
            val absentCount = memberRecords.count { it.status == AttendanceStatus.ABSENT }
            if (absentCount > 0) {
                val totalDays = memberRecords.size.coerceAtLeast(1)
                AbsenteeStat(
                    member = member,
                    group = groupMap[member.groupId],
                    absentCount = absentCount,
                    totalCheckedDays = totalDays,
                    absenceRate = (absentCount.toFloat() / totalDays) * 100f
                )
            } else null
        }.sortedByDescending { it.absentCount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Theme & Mode methods
    fun toggleDarkMode() {
        val newMode = !_isDarkMode.value
        _isDarkMode.value = newMode
        appPrefs.edit().putBoolean("is_dark_mode", newMode).apply()
    }

    fun setThemePreset(preset: AppThemePreset) {
        _themePreset.value = preset
        appPrefs.edit().putString("theme_preset", preset.name).apply()
    }

    fun cycleTheme() {
        val current = _themePreset.value
        val presets = AppThemePreset.values()
        val nextIdx = (presets.indexOf(current) + 1) % presets.size
        val nextPreset = presets[nextIdx]
        _themePreset.value = nextPreset
        appPrefs.edit().putString("theme_preset", nextPreset.name).apply()
    }

    fun toggleLanguage() {
        val nextLang = when (_currentLanguage.value) {
            Language.UYGHUR -> Language.ARABIC
            Language.ARABIC -> Language.ENGLISH
            Language.ENGLISH -> Language.UYGHUR
        }
        _currentLanguage.value = nextLang
        appPrefs.edit().putString("current_language", nextLang.name).apply()
    }

    fun setLanguage(lang: Language) {
        _currentLanguage.value = lang
        appPrefs.edit().putString("current_language", lang.name).apply()
    }

    private fun saveLoggedInUserSession(user: UserEntity?) {
        val prefs = getApplication<android.app.Application>().getSharedPreferences("user_session_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            if (user != null) {
                putString("role", user.role.name)
                putLong("groupId", user.groupId ?: 0L)
                putString("displayName", user.displayName)
            } else {
                remove("role")
                remove("groupId")
                remove("displayName")
            }
        }.apply()
    }

    fun login(username: String, passwordAttempt: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val user = repository.authenticate(username, passwordAttempt)
            if (user != null) {
                _currentUser.value = user
                saveLoggedInUserSession(user)
                if (user.role == UserRole.GROUP_LEAD && user.groupId != null) {
                    _selectedGroupId.value = user.groupId
                }
                checkAndRegisterCurrentDevice(user.displayName)
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    fun quickLogin(user: UserEntity) {
        _currentUser.value = user
        saveLoggedInUserSession(user)
        if (user.role == UserRole.GROUP_LEAD && user.groupId != null) {
            _selectedGroupId.value = user.groupId
        }
        checkAndRegisterCurrentDevice(user.displayName)
    }

    fun logout(orbId: Long? = null) {
        val previousOrb = orbId ?: when (_currentUser.value?.role) {
            UserRole.ADMIN -> 0L
            UserRole.GROUP_LEAD -> _currentUser.value?.groupId
            else -> null
        }
        _lastActiveOrbId.value = previousOrb
        _currentUser.value = null
        saveLoggedInUserSession(null)
    }

    fun clearLastActiveOrb() {
        _lastActiveOrbId.value = null
    }

    fun isRecordLocked(record: AttendanceRecordEntity?): Boolean {
        val user = _currentUser.value
        if (user?.role == UserRole.ADMIN) return false
        val twentyFourHoursMillis = 24 * 60 * 60 * 1000L
        val now = System.currentTimeMillis()
        if (record != null && record.timestamp > 0L) {
            return (now - record.timestamp) > twentyFourHoursMillis
        }
        val todayDate = dateFormat.format(Date())
        if (_selectedDate.value < todayDate) return true
        if (record != null && record.date < todayDate) return true
        return false
    }

    fun isDateLockedForNonAdmin(dateStr: String): Boolean {
        val user = _currentUser.value
        if (user?.role == UserRole.ADMIN) return false
        val todayDate = dateFormat.format(Date())
        return dateStr < todayDate
    }

    fun setSelectedDate(dateStr: String) {
        _selectedDate.value = dateStr
    }

    fun setSelectedMonth(monthStr: String) {
        _selectedMonth.value = monthStr
    }

    fun setSelectedMonth(monthNumber: Int) {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        val formattedMonth = String.format(Locale.US, "%04d-%02d", year, monthNumber)
        _selectedMonth.value = formattedMonth
    }

    fun setSelectedQuarter(quarter: Int) {
        _selectedQuarter.value = quarter
    }

    fun setSelectedGroup(groupId: Long) {
        _selectedGroupId.value = groupId
    }

    fun setAttendanceStatus(memberId: Long, groupId: Long, status: AttendanceStatus, note: String = "") {
        val existingRecord = currentAttendanceMap.value[memberId]
        if (isRecordLocked(existingRecord)) {
            Toast.makeText(getApplication(), "تۇنجى قېتىم ئۆزگەرتىلگىنىگە 24 سائەت توشقانلىقتىن، پەقەت باش باشقۇرغۇچىلا ئۆزگەرتەلەيدۇ", Toast.LENGTH_LONG).show()
            return
        }
        viewModelScope.launch {
            try {
                repository.saveAttendanceRecord(
                    memberId = memberId,
                    groupId = groupId,
                    date = _selectedDate.value,
                    status = status,
                    note = note
                )
            } catch (e: Exception) {
                Toast.makeText(getApplication(), "يوقلىمىنى ساقلاش مەغلۇپ بولدى: ${e.localizedMessage ?: "خاتالىق"}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun unmarkAttendanceStatus(memberId: Long) {
        val existingRecord = currentAttendanceMap.value[memberId]
        if (isRecordLocked(existingRecord)) {
            Toast.makeText(getApplication(), "تۇنجى قېتىم ئۆزگەرتىلگىنىگە 24 سائەت توشقانلىقتىن، پەقەت باش باشقۇرغۇچىلا ئۆزگەرتەلەيدۇ", Toast.LENGTH_LONG).show()
            return
        }
        val date = _selectedDate.value
        viewModelScope.launch {
            try {
                repository.deleteAttendanceRecord(memberId, date)
                viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    SupabaseSyncService.deleteAttendanceRecord(memberId, date)
                }
            } catch (e: Exception) {
                Toast.makeText(getApplication(), "يوقلىمىنى ئۆچۈرۈش مەغلۇپ بولدى: ${e.localizedMessage ?: "خاتالىق"}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Notice Tracking & Receipts (Requirement 1 & 2)
    fun markNoticeDelivered(noticeId: Long, groupId: Long) {
        viewModelScope.launch {
            val receipt = repository.markNoticeDelivered(noticeId, groupId)
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                SupabaseSyncService.upsertNoticeReceipt(receipt)
            }
        }
    }

    fun acknowledgeNotice(noticeId: Long, groupId: Long) {
        viewModelScope.launch {
            val receipt = repository.acknowledgeNotice(noticeId, groupId)
            _dismissedEmergencyNoticeIds.value = _dismissedEmergencyNoticeIds.value + noticeId
            Toast.makeText(getApplication(), strings.savedSuccess, Toast.LENGTH_SHORT).show()
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                SupabaseSyncService.upsertNoticeReceipt(receipt)
            }
        }
    }

    // AI Multimodal & Custom Task Execution (Requirement 6)
    fun executeAiMemberTask(
        userPrompt: String,
        textInput: String,
        imageBase64: String? = null,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val apiKey = com.example.BuildConfig.GEMINI_API_KEY
            val result = com.example.util.GeminiAiHelper.analyzeMemberAndExecuteTask(
                apiKey = apiKey,
                userPrompt = userPrompt,
                textInput = textInput,
                imageBase64 = imageBase64
            )
            result.onSuccess { res ->
                onSuccess(res)
            }.onFailure { ex ->
                onError(ex.localizedMessage ?: "AI Task Error")
            }
        }
    }

    fun markAllPresent(targetMembers: List<MemberEntity>? = null) {
        viewModelScope.launch {
            val targetGroupId = _currentUser.value?.groupId ?: _selectedGroupId.value
            val members = targetMembers ?: currentGroupMembers.value
            val user = _currentUser.value
            // If non-admin and date is past, notify
            if (user?.role != UserRole.ADMIN && isDateLockedForNonAdmin(_selectedDate.value)) {
                Toast.makeText(getApplication(), strings.editLockedPastDate, Toast.LENGTH_LONG).show()
                return@launch
            }
            try {
                repository.markAllPresent(
                    groupId = targetGroupId,
                    date = _selectedDate.value,
                    members = members
                )
            } catch (e: Exception) {
                Toast.makeText(getApplication(), "ھەممىنى بار قىلىپ ساقلاش مەغلۇپ بولدى: ${e.localizedMessage ?: "خاتالىق"}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Portal Status & Suspension (Requirement 2)
    fun setGroupSuspended(groupId: Long, isSuspended: Boolean) {
        viewModelScope.launch {
            repository.setGroupSuspended(groupId, isSuspended)
            val msg = if (isSuspended) strings.statusSuspendedPort else strings.statusActivePort
            Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
        }
    }

    fun recordPortalActive(groupId: Long) {
        viewModelScope.launch {
            repository.recordGroupActive(groupId)
        }
    }

    // Admin Group Creation & Deletion (Requirement 3)
    fun addGroup(
        name: String,
        code: String,
        description: String = "",
        subLeader1: String = "",
        subLeader1Contact: String = "",
        subLeader1Telegram: String = "",
        subLeader1Whatsapp: String = "",
        subLeader2: String = "",
        subLeader2Contact: String = "",
        subLeader2Telegram: String = "",
        subLeader2Whatsapp: String = "",
        leaderLoginName: String = "",
        leaderPasswordPlain: String = "123456"
    ) {
        viewModelScope.launch {
            repository.addGroup(
                name = name,
                code = code,
                description = description,
                subLeader1 = subLeader1,
                subLeader1Contact = subLeader1Contact,
                subLeader1Telegram = subLeader1Telegram,
                subLeader1Whatsapp = subLeader1Whatsapp,
                subLeader2 = subLeader2,
                subLeader2Contact = subLeader2Contact,
                subLeader2Telegram = subLeader2Telegram,
                subLeader2Whatsapp = subLeader2Whatsapp,
                leaderLoginName = leaderLoginName,
                leaderPasswordPlain = leaderPasswordPlain
            )
            Toast.makeText(getApplication(), strings.groupCreatedSuccess, Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteGroup(groupId: Long) {
        viewModelScope.launch {
            repository.deleteGroup(groupId)
        }
    }

    // Bulk Member Parsing & Import (Requirement 4 & Requirement 3)
    fun parseBulkMemberText(
        text: String,
        defaultGroupId: Long,
        defaultSubGroup: Int = 1
    ): List<MemberEntity> {
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
        val result = mutableListOf<MemberEntity>()
        val phoneRegex = Regex("""(\+?\d[\d\s\-]{6,}\d)""")
        val telegramRegex = Regex("""(@[A-Za-z0-9_]{3,})""")

        for ((index, rawLine) in lines.withIndex()) {
            var line = rawLine.replace(Regex("""^\d+[\.\-\s\)\/]+"""), "").trim()
            if (line.isBlank()) continue

            var phone = ""
            val phoneMatch = phoneRegex.find(line)
            if (phoneMatch != null) {
                phone = phoneMatch.value.replace(Regex("""[\s\-]"""), "")
                line = line.replace(phoneMatch.value, "").trim()
            }

            var telegram = ""
            val teleMatch = telegramRegex.find(line)
            if (teleMatch != null) {
                telegram = teleMatch.value
                line = line.replace(teleMatch.value, "").trim()
            }

            val cleanName = line.replace(Regex("""[,\-|/:]+"""), " ").trim()
            val finalName = if (cleanName.isNotBlank()) cleanName else "ئەزا ${index + 1}"

            result.add(
                MemberEntity(
                    groupId = defaultGroupId,
                    subGroup = defaultSubGroup,
                    name = finalName,
                    contactAddress = phone,
                    telegramContact = telegram,
                    whatsappContact = phone,
                    otherContact = "",
                    joinDate = dateFormat.format(Date()),
                    status = MemberStatus.ACTIVE
                )
            )
        }
        return result
    }

    fun importBatchMembers(members: List<MemberEntity>, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.addMembersBatch(members)
            Toast.makeText(getApplication(), strings.batchImportSuccess, Toast.LENGTH_SHORT).show()
            onComplete()
        }
    }

    // Equipment / Weapon Management (Requirement 5)
    fun addEquipment(
        groupId: Long,
        name: String,
        totalCount: Int,
        readyCount: Int,
        notReadyCount: Int,
        notes: String = "",
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val eq = repository.addEquipment(
                groupId = groupId,
                name = name,
                totalCount = totalCount,
                readyCount = readyCount,
                notReadyCount = notReadyCount,
                notes = notes
            )
            onSuccess()
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                SupabaseSyncService.upsertEquipment(eq)
            }
        }
    }

    fun updateEquipment(equipment: com.example.data.model.EquipmentEntity, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateEquipment(equipment)
            onSuccess()
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                SupabaseSyncService.upsertEquipment(equipment)
            }
        }
    }

    fun deleteEquipment(equipment: com.example.data.model.EquipmentEntity) {
        viewModelScope.launch {
            repository.deleteEquipment(equipment)
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                SupabaseSyncService.deleteEquipment(equipment.id)
            }
        }
    }

    fun deleteEquipmentById(id: Long) {
        viewModelScope.launch {
            repository.deleteEquipmentById(id)
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                SupabaseSyncService.deleteEquipment(id)
            }
        }
    }

    // Daily Updates / Announcements (Requirement 1)
    fun broadcastEmergencyNotice(
        title: String,
        content: String,
        groupId: Long = 0L,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val author = _currentUser.value?.displayName ?: "باش باشقۇرغۇچى"
            val todayStr = dateFormat.format(Date())
            val entity = repository.addDailyUpdate(
                groupId = groupId,
                groupName = if (groupId == 0L) "بارلىق گۇرۇپپىلار" else (groups.value.find { it.id == groupId }?.name ?: ""),
                authorName = author,
                title = title,
                content = content,
                date = todayStr,
                priority = "URGENT"
            )
            // Mark as read and notified on the sender device so NO popup appears here
            try {
                com.example.util.LocalReadNoticeTracker.markNoticeAsRead(getApplication<android.app.Application>(), entity.id)
                com.example.util.LocalReadNoticeTracker.markAsNotified(getApplication<android.app.Application>(), entity.id)
            } catch (e: Exception) {
                // ignore
            }
            Toast.makeText(getApplication(), "جىددىي ئۇقتۇرۇش مۇۋەپپەقىيەتلىك تارقىتىلدى", Toast.LENGTH_SHORT).show()
            onSuccess()
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                SupabaseSyncService.upsertDailyUpdate(entity)
            }
        }
    }

    fun duplicateSubGroup(
        groupId: Long,
        sourceSubGroup: Int,
        newSubGroup: Int,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val currentMembers = currentGroupMembers.value.filter { it.groupId == groupId && it.subGroup == sourceSubGroup }
            for (m in currentMembers) {
                val newMem = repository.addMember(
                    name = "${m.name} (كۆپەيتمە)",
                    groupId = groupId,
                    subGroup = newSubGroup,
                    contactAddress = m.contactAddress,
                    telegramContact = m.telegramContact,
                    whatsappContact = m.whatsappContact,
                    otherContact = m.otherContact
                )
                viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    SupabaseSyncService.upsertMember(newMem)
                }
            }
            onSuccess()
        }
    }

    fun addDailyUpdate(
        groupId: Long,
        groupName: String,
        title: String,
        content: String,
        priority: String = "NORMAL",
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val author = _currentUser.value?.displayName ?: "باشقۇرغۇچى"
            val todayStr = dateFormat.format(Date())
            val entity = repository.addDailyUpdate(
                groupId = groupId,
                groupName = groupName,
                authorName = author,
                title = title,
                content = content,
                date = todayStr,
                priority = priority
            )
            // Mark as read and notified on the sender device so NO popup appears here
            try {
                com.example.util.LocalReadNoticeTracker.markNoticeAsRead(getApplication<android.app.Application>(), entity.id)
                com.example.util.LocalReadNoticeTracker.markAsNotified(getApplication<android.app.Application>(), entity.id)
            } catch (e: Exception) {
                // ignore
            }
            Toast.makeText(getApplication(), strings.postUpdateSuccess, Toast.LENGTH_SHORT).show()
            onSuccess()
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                SupabaseSyncService.upsertDailyUpdate(entity)
            }
        }
    }

    fun updateDailyUpdate(update: com.example.data.model.DailyUpdateEntity, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateDailyUpdate(update)
            onSuccess()
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                SupabaseSyncService.upsertDailyUpdate(update)
            }
        }
    }

    fun toggleCancelDailyUpdate(update: com.example.data.model.DailyUpdateEntity, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val newPriority = if (update.priority == "CANCELLED") "NORMAL" else "CANCELLED"
            val updated = update.copy(priority = newPriority)
            repository.updateDailyUpdate(updated)
            onSuccess()
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                SupabaseSyncService.upsertDailyUpdate(updated)
            }
        }
    }

    fun deleteDailyUpdate(update: com.example.data.model.DailyUpdateEntity) {
        viewModelScope.launch {
            repository.deleteDailyUpdate(update)
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                SupabaseSyncService.deleteDailyUpdate(update.id)
            }
        }
    }

    fun deleteDailyUpdateById(id: Long) {
        viewModelScope.launch {
            repository.deleteDailyUpdateById(id)
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                SupabaseSyncService.deleteDailyUpdate(id)
            }
        }
    }

    // Executive Leadership Contacts (Requirement 2)
    fun addExecutiveContact(
        name: String,
        title: String,
        phone: String = "",
        telegram: String = "",
        whatsapp: String = "",
        radioComms: String = "",
        other: String = "",
        notes: String = "",
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val entity = repository.addExecutiveContact(
                name = name,
                title = title,
                phone = phone,
                telegram = telegram,
                whatsapp = whatsapp,
                radioComms = radioComms,
                other = other,
                notes = notes
            )
            onSuccess()
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                SupabaseSyncService.upsertExecutiveContact(entity)
            }
        }
    }

    fun updateExecutiveContact(contact: com.example.data.model.ExecutiveContactEntity, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateExecutiveContact(contact)
            onSuccess()
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                SupabaseSyncService.upsertExecutiveContact(contact)
            }
        }
    }

    fun deleteExecutiveContact(contact: com.example.data.model.ExecutiveContactEntity) {
        viewModelScope.launch {
            repository.deleteExecutiveContact(contact)
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                SupabaseSyncService.deleteExecutiveContact(contact.id)
            }
        }
    }

    fun deleteExecutiveContactById(id: Long) {
        viewModelScope.launch {
            repository.deleteExecutiveContactById(id)
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                SupabaseSyncService.deleteExecutiveContact(id)
            }
        }
    }

    // Member Management (with otherContact support)
    fun addMember(
        name: String,
        groupId: Long,
        subGroup: Int = 1,
        contactAddress: String = "",
        telegramContact: String = "",
        whatsappContact: String = "",
        otherContact: String = "",
        status: MemberStatus = MemberStatus.ACTIVE
    ) {
        viewModelScope.launch {
            val todayStr = dateFormat.format(Date())
            val member = repository.addMember(
                name = name,
                groupId = groupId,
                subGroup = subGroup,
                contactAddress = contactAddress,
                telegramContact = telegramContact,
                whatsappContact = whatsappContact,
                otherContact = otherContact,
                joinDate = todayStr,
                status = status
            )
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                SupabaseSyncService.upsertMember(member)
            }
        }
    }

    fun updateMember(member: MemberEntity) {
        viewModelScope.launch {
            repository.updateMember(member)
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                SupabaseSyncService.upsertMember(member)
            }
        }
    }

    fun deleteMember(member: MemberEntity) {
        viewModelScope.launch {
            repository.deleteMember(member)
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                SupabaseSyncService.deleteMember(member.id)
            }
        }
    }

    fun updateSubLeaders(
        groupId: Long,
        leader1: String,
        contact1: String = "",
        tele1: String = "",
        wa1: String = "",
        other1: String = "",
        leader2: String,
        contact2: String = "",
        tele2: String = "",
        wa2: String = "",
        other2: String = ""
    ) {
        viewModelScope.launch {
            repository.updateGroupSubLeaders(
                groupId = groupId,
                leader1 = leader1,
                contact1 = contact1,
                tele1 = tele1,
                wa1 = wa1,
                other1 = other1,
                leader2 = leader2,
                contact2 = contact2,
                tele2 = tele2,
                wa2 = wa2,
                other2 = other2
            )
        }
    }

    fun updateGroupSubLeaders(
        groupId: Long,
        leader1: String,
        contact1: String = "",
        tele1: String = "",
        wa1: String = "",
        other1: String = "",
        leader2: String,
        contact2: String = "",
        tele2: String = "",
        wa2: String = "",
        other2: String = ""
    ) {
        updateSubLeaders(
            groupId = groupId,
            leader1 = leader1,
            contact1 = contact1,
            tele1 = tele1,
            wa1 = wa1,
            other1 = other1,
            leader2 = leader2,
            contact2 = contact2,
            tele2 = tele2,
            wa2 = wa2,
            other2 = other2
        )
    }

    fun copyTextToClipboard(text: String, label: String = "Text") {
        if (text.isBlank()) return
        val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(getApplication(), "$label: $text", Toast.LENGTH_SHORT).show()
    }

    fun resetLeaderPassword(userId: Long, newPassword: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.resetUserPassword(userId, newPassword)
            onSuccess()
            Toast.makeText(getApplication(), strings.resetPasswordSuccess, Toast.LENGTH_SHORT).show()
        }
    }

    fun updateUserCredentials(
        userId: Long,
        loginName: String,
        newPassword: String,
        displayName: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            repository.updateUserCredentials(userId, loginName, newPassword, displayName)
            onSuccess()
            Toast.makeText(getApplication(), strings.editCredentialsSuccess, Toast.LENGTH_SHORT).show()
        }
    }

    fun exportToCSV(context: Context) {
        val s = strings
        val grpMap = groups.value.associateBy { it.id }
        val memberMap = allMembers.value.associateBy { it.id }
        val records = allAttendance.value

        val csvBuilder = StringBuilder()
        csvBuilder.append("ID,Date,Group,SubGroup,Member Name,Contact,Status,Note\n")

        for (record in records) {
            val grpName = grpMap[record.groupId]?.name ?: "Group ${record.groupId}"
            val member = memberMap[record.memberId]
            val memberName = member?.name ?: "Member ${record.memberId}"
            val subGroupStr = if (member?.subGroup == 2) s.subGroup2 else s.subGroup1
            val contactStr = member?.contactAddress.orEmpty().replace(",", " ")
            val statusStr = when (record.status) {
                AttendanceStatus.PRESENT -> s.statusPresent
                AttendanceStatus.ABSENT -> s.statusAbsent
                AttendanceStatus.LATE -> s.statusLate
                AttendanceStatus.EXCUSED -> s.statusExcused
            }
            val noteClean = record.note.replace(",", " ")
            csvBuilder.append("${record.id},${record.date},\"${grpName}\",\"${subGroupStr}\",\"${memberName}\",\"${contactStr}\",\"${statusStr}\",\"${noteClean}\"\n")
        }

        val csvText = csvBuilder.toString()
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, csvText)
            type = "text/csv"
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val shareIntent = Intent.createChooser(sendIntent, s.exportCSV).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(shareIntent)
    }

    fun copySummaryReport(context: Context) {
        val s = strings
        val curDate = _selectedDate.value
        val stats = groupStats.value

        val report = StringBuilder()
        report.append("📊 ${s.appTitle} - ${s.overview}\n")
        report.append("📅 ${s.date}: $curDate\n\n")

        stats.forEach { stat ->
            report.append("🔹 ${stat.group.name} (${stat.group.code})\n")
            report.append("  • ${s.totalMembers}: ${stat.totalMembers}\n")
            report.append("  • ${s.presentCount}: ${stat.presentCount} | ${s.absentCount}: ${stat.absentCount} | ${s.excusedCount}: ${stat.excusedCount}\n")
            report.append("  • ${s.attendanceRate}: ${String.format(Locale.US, "%.1f", stat.attendanceRate)}%\n\n")
        }

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Yoqlima Report", report.toString())
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, s.copiedToClipboard, Toast.LENGTH_SHORT).show()
    }

    fun shareSummaryReport(context: Context) {
        val s = strings
        val curDate = _selectedDate.value
        val stats = groupStats.value

        val report = StringBuilder()
        report.append("📊 ${s.appTitle} - ${s.overview}\n")
        report.append("📅 ${s.date}: $curDate\n\n")

        stats.forEach { stat ->
            report.append("🔹 ${stat.group.name} (${stat.group.code})\n")
            report.append("  • ${s.totalMembers}: ${stat.totalMembers}\n")
            report.append("  • ${s.presentCount}: ${stat.presentCount} | ${s.absentCount}: ${stat.absentCount} | ${s.excusedCount}: ${stat.excusedCount}\n")
            report.append("  • ${s.attendanceRate}: ${String.format(Locale.US, "%.1f", stat.attendanceRate)}%\n\n")
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, report.toString())
            type = "text/plain"
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val shareIntent = Intent.createChooser(sendIntent, s.shareReport).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(shareIntent)
    }

    fun getGroupStatsForDates(dates: List<String>): List<GroupStat> {
        val allGrps = groups.value
        val allMembs = allMembers.value
        val allRecs = allAttendance.value
        val dateSet = dates.toSet()

        return allGrps.map { grp ->
            val grpMembers = allMembs.filter { it.groupId == grp.id }
            val leaderCount = (if (grp.subLeader1.isNotBlank()) 1 else 0) + (if (grp.subLeader2.isNotBlank()) 1 else 0)
            val totalGroupMembersCount = grpMembers.size + leaderCount
            val activeMembers = grpMembers.filter { it.status == MemberStatus.ACTIVE }

            val grpRecords = if (dateSet.isEmpty()) {
                allRecs.filter { it.groupId == grp.id }
            } else {
                allRecs.filter { it.groupId == grp.id && it.date in dateSet }
            }

            val presentCount = grpRecords.count { it.status == AttendanceStatus.PRESENT }
            val absentCount = grpRecords.count { it.status == AttendanceStatus.ABSENT }
            val lateCount = grpRecords.count { it.status == AttendanceStatus.LATE }
            val excusedCount = grpRecords.count { it.status == AttendanceStatus.EXCUSED }

            val totalExpected = (activeMembers.size * (if (dateSet.isEmpty()) 1 else dateSet.size)).coerceAtLeast(presentCount + absentCount + excusedCount)
            val rate = if (totalExpected > 0) {
                (presentCount.toFloat() / totalExpected.toFloat()) * 100f
            } else 0f

            GroupStat(
                group = grp,
                totalMembers = totalGroupMembersCount,
                activeMembers = activeMembers.size + leaderCount,
                presentCount = presentCount,
                absentCount = absentCount,
                lateCount = lateCount,
                excusedCount = excusedCount,
                attendanceRate = rate
            )
        }
    }

    fun getMemberPeriodStats(groupId: Long, dates: List<String>): List<MemberPeriodStat> {
        val allGrps = groups.value
        val grp = allGrps.find { it.id == groupId }
        val grpName = grp?.name ?: "گۇرۇپپا $groupId"
        val membs = if (groupId == 0L) allMembers.value else allMembers.value.filter { it.groupId == groupId }
        val allRecs = allAttendance.value
        val dateSet = dates.toSet()

        return membs.map { member ->
            val mRecords = if (dateSet.isEmpty()) {
                allRecs.filter { it.memberId == member.id }
            } else {
                allRecs.filter { it.memberId == member.id && it.date in dateSet }
            }

            val pCount = mRecords.count { it.status == AttendanceStatus.PRESENT }
            val aCount = mRecords.count { it.status == AttendanceStatus.ABSENT }
            val eCount = mRecords.count { it.status == AttendanceStatus.EXCUSED }
            val lCount = mRecords.count { it.status == AttendanceStatus.LATE }
            val totalDays = if (dateSet.isNotEmpty()) dateSet.size else (pCount + aCount + eCount + lCount).coerceAtLeast(1)
            val rate = if (totalDays > 0) (pCount.toFloat() / totalDays.toFloat()) * 100f else 0f

            val memberGroupName = allGrps.find { it.id == member.groupId }?.name ?: grpName

            MemberPeriodStat(
                member = member,
                groupName = memberGroupName,
                presentDays = pCount,
                absentDays = aCount,
                excusedDays = eCount,
                lateDays = lCount,
                totalCheckedDays = totalDays,
                attendanceRate = rate
            )
        }
    }

    fun exportGroupAttendanceProfessional(
        context: Context,
        groupId: Long,
        dates: List<String>,
        isExcel: Boolean = true
    ) {
        val s = strings
        val grp = groups.value.find { it.id == groupId }
        val groupName = if (groupId == 0L) "بارلىق گۇرۇپپىلار" else (grp?.name ?: "گۇرۇپپا $groupId")
        val datePeriodText = if (dates.isEmpty()) "پۈتۈن ۋاقىت" else if (dates.size == 1) dates.first() else "${dates.minOrNull()} دىن ${dates.maxOrNull()} غىچە (${dates.size} كۈن)"
        val memberStats = getMemberPeriodStats(groupId, dates)
        val totalMembers = memberStats.size
        val totalPresent = memberStats.sumOf { it.presentDays }
        val totalAbsent = memberStats.sumOf { it.absentDays }
        val totalExcused = memberStats.sumOf { it.excusedDays }
        val totalExpected = memberStats.sumOf { it.totalCheckedDays }.coerceAtLeast(1)
        val overallRate = (totalPresent.toFloat() / totalExpected.toFloat()) * 100f

        val exportFile: java.io.File
        val mimeType: String

        if (isExcel) {
            // High-standard CSV with UTF-8 BOM (\uFEFF) for 100% Excel / WPS / Google Sheets compatibility
            val csv = buildString {
                append("\uFEFF") // UTF-8 Byte Order Mark
                append("=== ${s.appTitle} - گۇرۇپپا يوقلىما دوكلات جەدۋىلى ===\n")
                append("گۇرۇپپا:,\"$groupName\"\n")
                append("ۋاقىت ئارىلىقى:,\"$datePeriodText\"\n")
                append("ئومۇمىي ئەزا سانى:,$totalMembers\n")
                append("ئومۇمىي كەلگەن:,$totalPresent,ئومۇمىي كەلمىگەن:,$totalAbsent,ئومۇمىي رۇخسەت:,$totalExcused\n")
                append("ئومۇمىي يوقلىما نىسبىتى:,\"${String.format(Locale.US, "%.1f", overallRate)}%\"\n\n")

                append("تەرتىپى,ئەزا ئىسمى,گۇرۇپپا,كىچىك گۇرۇپپا,ئالاقە نومۇرى,تېلېگرام,كەلگەن كۈن,كەلمىگەن كۈن,رۇخسەت كۈن,يوقلىما نىسبىتى,ھالىتى\n")

                memberStats.forEachIndexed { index, stat ->
                    val subStr = if (stat.member.subGroup == 2) s.subGroup2 else s.subGroup1
                    val contact = stat.member.contactAddress.replace(",", " ")
                    val tg = stat.member.telegramContact.replace(",", " ")
                    val statusText = if (stat.member.status == MemberStatus.ACTIVE) "ئاكتىپ" else "ئارامدا"
                    append("${index + 1},\"${stat.member.name}\",\"${stat.groupName}\",\"$subStr\",\"$contact\",\"$tg\",${stat.presentDays},${stat.absentDays},${stat.excusedDays},\"${String.format(Locale.US, "%.1f", stat.attendanceRate)}%\",\"$statusText\"\n")
                }

                append("\n,خۇلاسە (جەمئىي),,,,$totalMembers ئەزا,$totalPresent كۈن,$totalAbsent كۈن,$totalExcused كۈن,\"${String.format(Locale.US, "%.1f", overallRate)}%\",\n")
            }

            val fileName = "Yoqlima_${groupName.replace(" ", "_")}_${System.currentTimeMillis()}.csv"
            exportFile = java.io.File(context.cacheDir, fileName)
            exportFile.writeText(csv, Charsets.UTF_8)
            mimeType = "text/csv"
        } else {
            // Professional formatted Word/Text Document (.doc / .txt)
            val doc = buildString {
                append("═══════════════════════════════════════════════════════════\n")
                append("              📋 ${s.appTitle} - كەسپىي يوقلىما دوكلاتى              \n")
                append("═══════════════════════════════════════════════════════════\n\n")
                append("🏢 تەكشۈرۈلگەن گۇرۇپپا: $groupName\n")
                append("📅 دوكلات دەۋرى: $datePeriodText\n")
                append("👥 ئومۇمىي خادىم / ئەزا سانى: $totalMembers ئادەم\n")
                append("📊 ئومۇمىي يوقلىما كۆرسەتكۈچى: ${String.format(Locale.US, "%.1f", overallRate)}%\n")
                append("───────────────────────────────────────────────────────────\n")
                append("  • جەمئىي كەلگەن (ھازىر): $totalPresent كۈن/قېتىم\n")
                append("  • جەمئىي كەلمىگەن (يوق): $totalAbsent كۈن/قېتىم\n")
                append("  • جەمئىي رۇخسەت سورىغان: $totalExcused كۈن/قېتىم\n")
                append("═══════════════════════════════════════════════════════════\n\n")
                append("【 خادىملارنىڭ ئايرىم-ئايرىم يوقلىما ئارخىپى 】\n\n")

                memberStats.forEachIndexed { index, stat ->
                    val subStr = if (stat.member.subGroup == 2) s.subGroup2 else s.subGroup1
                    append("${index + 1}. ${stat.member.name} [$subStr] (${stat.groupName})\n")
                    if (stat.member.contactAddress.isNotBlank()) append("   📞 تېلېفون: ${stat.member.contactAddress}\n")
                    if (stat.member.telegramContact.isNotBlank()) append("   ✈️ تېلېگرام: ${stat.member.telegramContact}\n")
                    append("   📈 ھازىر: ${stat.presentDays} كۈن | يوق: ${stat.absentDays} كۈن | رۇخسەت: ${stat.excusedDays} كۈن\n")
                    append("   🎯 قاتنىشىش نىسبىتى: ${String.format(Locale.US, "%.1f", stat.attendanceRate)}%\n")
                    append("-----------------------------------------------------------\n")
                }

                append("\nدوكلات چىقىرىلغان ۋاقىت: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}\n")
                append("تەستىقلىغۇچى: سىستېما باشقۇرغۇچىسى\n")
            }

            val fileName = "Yoqlima_${groupName.replace(" ", "_")}_${System.currentTimeMillis()}.doc"
            exportFile = java.io.File(context.cacheDir, fileName)
            exportFile.writeText(doc, Charsets.UTF_8)
            mimeType = "application/msword"
        }

        try {
            val contentUri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                exportFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "$groupName - يوقلىما دوكلاتى")
                putExtra(Intent.EXTRA_TEXT, "$groupName ئۈچۈن ئىشلەنگەن كەسپىي يوقلىما دوكلاتى ($datePeriodText)")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "$groupName - دوكلاتنى ھەمبەھىرلەش").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            Toast.makeText(context, "دوكلات مۇۋەپپەقىيەتلىك ئىشلەندى ۋە ھەمبەھىرلەندى", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // Fallback plain text share if file sharing hits permission issue
            val fileContent = exportFile.readText()
            val textIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, fileContent)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(textIntent, "$groupName - دوكلات"))
        }
    }

    fun copyGroupAttendanceProfessionalText(context: Context, groupId: Long, dates: List<String>) {
        val s = strings
        val grp = groups.value.find { it.id == groupId }
        val groupName = if (groupId == 0L) "بارلىق گۇرۇپپىلار" else (grp?.name ?: "گۇرۇپپا $groupId")
        val datePeriodText = if (dates.isEmpty()) "پۈتۈن ۋاقىت" else if (dates.size == 1) dates.first() else "${dates.minOrNull()} دىن ${dates.maxOrNull()} غىچە (${dates.size} كۈن)"
        val memberStats = getMemberPeriodStats(groupId, dates)
        val totalMembers = memberStats.size
        val totalPresent = memberStats.sumOf { it.presentDays }
        val totalAbsent = memberStats.sumOf { it.absentDays }
        val totalExcused = memberStats.sumOf { it.excusedDays }
        val totalExpected = memberStats.sumOf { it.totalCheckedDays }.coerceAtLeast(1)
        val overallRate = (totalPresent.toFloat() / totalExpected.toFloat()) * 100f

        val doc = buildString {
            append("📋 ${s.appTitle} - $groupName يوقلىما دوكلاتى\n")
            append("📅 دەۋر: $datePeriodText\n")
            append("👥 ئومۇمىي ئادەم: $totalMembers | 📈 يوقلىما نىسبىتى: ${String.format(Locale.US, "%.1f", overallRate)}%\n")
            append("✅ ھازىر: $totalPresent | ❌ يوق: $totalAbsent | 📝 رۇخسەت: $totalExcused\n\n")
            append("【 ئەزالار تەپسىلاتى 】\n")
            memberStats.forEachIndexed { i, stat ->
                append("${i + 1}. ${stat.member.name} (${if (stat.member.subGroup == 2) "2-گۇرۇپچا" else "1-گۇرۇپچا"}): ")
                append("كەلگىنى: ${stat.presentDays}، كەلمىگىنى: ${stat.absentDays}، رۇخسەت: ${stat.excusedDays} (نىسبىتى: ${String.format(Locale.US, "%.0f", stat.attendanceRate)}%)\n")
            }
        }

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("$groupName Report", doc)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, s.copiedToClipboard, Toast.LENGTH_SHORT).show()
    }

    fun removeUserPassword(userId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.removeUserPassword(userId)
            onSuccess()
            Toast.makeText(getApplication(), strings.passwordRemovedSuccess, Toast.LENGTH_SHORT).show()
        }
    }

    fun generateAiExportReport(
        customPrompt: String,
        apiKey: String = com.example.BuildConfig.GEMINI_API_KEY,
        onResult: (String) -> Unit
    ) {
        viewModelScope.launch {
            val grps = groups.value
            val members = allMembers.value
            val attRecords = allAttendance.value.filter { it.date == _selectedDate.value }
            val equips = allEquipment.value
            val dutyGrp = currentDutyGroupEntity.value
            val dutyNotesStr = _dutyGroupNotes.value
            val updates = allDailyUpdates.value
            val contacts = allExecutiveContacts.value

            val systemContext = buildString {
                append("چېسلا: ${_selectedDate.value}\n")
                if (dutyGrp != null) {
                    append("نۆۋەتتىكى نۆۋبەتچى گۇرۇپپا: ${dutyGrp.name} (${dutyGrp.code})\n")
                    append("1-تارماق مەسئۇلى: ${dutyGrp.subLeader1} (${dutyGrp.subLeader1Contact})\n")
                    append("2-تارماق مەسئۇلى: ${dutyGrp.subLeader2} (${dutyGrp.subLeader2Contact})\n")
                    append("نۆۋەتچىلىك ئىزاھاتى: $dutyNotesStr\n\n")
                }

                append("گۇرۇپپىلار ۋە بۈگۈنكى يوقلىما ئەھۋالى:\n")
                grps.forEach { g ->
                    val gMembers = members.filter { it.groupId == g.id }
                    val gAtt = attRecords.filter { it.groupId == g.id }
                    val present = gAtt.count { it.status == AttendanceStatus.PRESENT }
                    val absent = gAtt.filter { it.status == AttendanceStatus.ABSENT }
                    val excused = gAtt.count { it.status == AttendanceStatus.EXCUSED }
                    val total = gMembers.size

                    append("• ${g.name}: جەمئىي $total ئادەم, ھازىر $present, كەلمىگەن ${absent.size}, رۇخسەت $excused\n")
                    if (absent.isNotEmpty()) {
                        val absentNames = absent.mapNotNull { rec ->
                            val m = gMembers.find { it.id == rec.memberId }
                            m?.let { "${it.name} (${it.contactAddress.ifBlank { "ئالاقىسىز" }})" }
                        }.joinToString(", ")
                        append("  - كەلمىگەنلەر: $absentNames\n")
                    }
                }

                append("\nقورال-ياراغ ۋە جابدۇقلار ئەھۋالى:\n")
                equips.forEach { eq ->
                    val gName = grps.find { it.id == eq.groupId }?.name ?: "گۇرۇپپا"
                    append("• $gName - ${eq.name}: جەمئىي ${eq.totalCount}, تەييار ${eq.readyCount}, تەييار ئەمەس ${eq.notReadyCount} (${eq.notes})\n")
                }

                if (updates.isNotEmpty()) {
                    append("\nيېڭىلىقلار ۋە ئۇقتۇرۇشلار:\n")
                    updates.take(5).forEach { up ->
                        append("• [${up.priority}] ${up.title} - ${up.content} (${up.authorName})\n")
                    }
                }

                if (contacts.isNotEmpty()) {
                    append("\nباش مەسئۇللار ئالاقە ئادرېسلىرى:\n")
                    contacts.forEach { c ->
                        append("• ${c.name} (${c.title}): تېلېفون: ${c.phone}, تېلېگرام: ${c.telegram}, مۇخابىرات: ${c.radioComms}\n")
                    }
                }
            }

            val result = com.example.util.GeminiAiHelper.generateCustomReport(
                apiKey = apiKey,
                userPrompt = customPrompt,
                systemContext = systemContext
            )

            result.onSuccess { report ->
                onResult(report)
            }.onFailure { ex ->
                // Fallback report generation if network/API fails
                val fallback = buildString {
                    append("📋 ئەقلىي يوقلىما ۋە ئەھۋال دوكلاتى\n")
                    append("━━━━━━━━━━━━━━━━━━━━━\n")
                    append("📅 چېسلا: ${_selectedDate.value}\n")
                    if (dutyGrp != null) {
                        append("🛡️ نۆۋبەتچى گۇرۇپپا: ${dutyGrp.name}\n")
                        append("👤 نۆۋبەتچى مەسئۇللار: ${dutyGrp.subLeader1} / ${dutyGrp.subLeader2}\n")
                        append("📝 نۆۋەتچىلىك ئىزاھاتى: $dutyNotesStr\n")
                    }
                    append("━━━━━━━━━━━━━━━━━━━━━\n")
                    append("📊 گۇرۇپپىلار يوقلىما ئەھۋالى:\n")
                    grps.forEach { g ->
                        val gMembers = members.filter { it.groupId == g.id }
                        val gAtt = attRecords.filter { it.groupId == g.id }
                        val present = gAtt.count { it.status == AttendanceStatus.PRESENT }
                        val absent = gAtt.count { it.status == AttendanceStatus.ABSENT }
                        val excused = gAtt.count { it.status == AttendanceStatus.EXCUSED }
                        append("🔹 ${g.name}: تەييار $present | كەلمىگەن $absent | رۇخسەت $excused (جەمئىي ${gMembers.size})\n")
                    }
                    append("━━━━━━━━━━━━━━━━━━━━━\n")
                    append("💡 ئەسكەرتىش: AI ئۇلىنىش خاتالىقى: ${ex.localizedMessage ?: "قايتا سىناڭ"}")
                }
                onResult(fallback)
            }
        }
    }

    // Supabase Cloud Sync & Backup State
    private val _supabaseStatus = MutableStateFlow<SupabaseConnectionStatus?>(null)
    val supabaseStatus: StateFlow<SupabaseConnectionStatus?> = _supabaseStatus.asStateFlow()

    private val _isSupabaseSyncing = MutableStateFlow(false)
    val isSupabaseSyncing: StateFlow<Boolean> = _isSupabaseSyncing.asStateFlow()

    private val _lastSyncResult = MutableStateFlow<SupabaseSyncResult?>(null)
    val lastSyncResult: StateFlow<SupabaseSyncResult?> = _lastSyncResult.asStateFlow()

    private val _isAutoSyncEnabled = MutableStateFlow(appPrefs.getBoolean("supabase_auto_sync", false))
    val isAutoSyncEnabled: StateFlow<Boolean> = _isAutoSyncEnabled.asStateFlow()

    fun toggleAutoSync(enabled: Boolean = !_isAutoSyncEnabled.value) {
        _isAutoSyncEnabled.value = enabled
        appPrefs.edit().putBoolean("supabase_auto_sync", enabled).apply()
    }

    fun testSupabaseConnection(onResult: (SupabaseConnectionStatus) -> Unit = {}) {
        viewModelScope.launch {
            _isSupabaseSyncing.value = true
            val status = SupabaseSyncService.testConnection()
            _supabaseStatus.value = status
            _isSupabaseSyncing.value = false
            onResult(status)
        }
    }

    fun uploadAllToSupabase(
        context: Context? = null,
        onComplete: (SupabaseSyncResult) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSupabaseSyncing.value = true
            val res = SupabaseSyncService.uploadAllData(
                groups = groups.value,
                users = users.value,
                members = allMembers.value,
                attendance = allAttendance.value,
                equipment = allEquipment.value,
                updates = allDailyUpdates.value,
                contacts = allExecutiveContacts.value,
                receipts = allNoticeReceipts.value,
                deviceSessions = allDeviceSessions.value
            )
            _lastSyncResult.value = res
            _isSupabaseSyncing.value = false
            if (context != null) {
                val toastMsg = if (res.isSuccess) {
                    "بارلىق مەلۇماتلار Supabase كە مۇۋەپپەقىيەتلىك يۈكلەندى!"
                } else {
                    res.message
                }
                Toast.makeText(context, toastMsg, Toast.LENGTH_LONG).show()
            }
            onComplete(res)
        }
    }

    private suspend fun processRestoredSupabaseData(data: com.example.data.supabase.SupabasePullData) {
        repository.restoreFromSupabaseData(data)

        // 1. Session Invalidation: Check if current logged-in user credentials were changed by Super Admin
        val current = _currentUser.value
        if (current != null) {
            val updatedSelf = data.users.find { it.id == current.id }
            if (updatedSelf != null && (updatedSelf.loginName != current.loginName || updatedSelf.passwordHash != current.passwordHash)) {
                _currentUser.value = null
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    Toast.makeText(getApplication(), "ھېسابات ئۇچۇرى ئۆزگەرتىلدى. كونا ھېسابات بىكار قىلىندى، يېڭى ھېسابات بىلەن قايتا كىرىڭ.", Toast.LENGTH_LONG).show()
                }
            }
        }

        // 2. Remote Device Termination Check
        val blocked = repository.isDeviceBlocked(currentDeviceId)
        _isCurrentDeviceBlocked.value = blocked

        // 3. Local per-device unread notification alert & app icon badge
        if (data.updates.isNotEmpty()) {
            val unreadCount = com.example.util.LocalReadNoticeTracker.getUnreadCount(getApplication(), data.updates)
            data.updates.forEach { u ->
                val isRead = com.example.util.LocalReadNoticeTracker.isNoticeRead(getApplication(), u.id)
                val notified = com.example.util.LocalReadNoticeTracker.hasBeenNotified(getApplication(), u.id)
                if (!isRead && !notified) {
                    com.example.util.AppNotificationManager.showUrgentNotification(
                        context = getApplication(),
                        notificationId = u.id.toInt(),
                        title = u.title,
                        message = u.content,
                        author = u.authorName,
                        groupTargetName = u.groupName,
                        unreadCount = unreadCount
                    )
                    com.example.util.LocalReadNoticeTracker.markAsNotified(getApplication(), u.id)
                }
            }
        }
    }

    private fun startPeriodicSync() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            while (isActive) {
                kotlinx.coroutines.delay(18_000) // sync every 18 seconds
                try {
                    val curUser = _currentUser.value?.displayName ?: "باشقۇرغۇچى / ئەزا"
                    val devName = "${android.os.Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${android.os.Build.MODEL}"
                    val osVer = "Android ${android.os.Build.VERSION.RELEASE}"
                    repository.registerOrUpdateDeviceSession(currentDeviceId, devName, osVer, curUser)

                    val result = SupabaseSyncService.pullAllData()
                    result.onSuccess { data ->
                        val totalPulled = data.groups.size + data.users.size + data.members.size +
                                data.attendance.size + data.equipment.size + data.updates.size +
                                data.contacts.size + data.receipts.size + data.deviceSessions.size
                        if (totalPulled > 0) {
                            processRestoredSupabaseData(data)
                        }
                    }
                } catch (e: Exception) {
                    // Ignore background network blips
                }
            }
        }
    }

    fun pullAllFromSupabase(
        context: Context? = null,
        silent: Boolean = true,
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            _isSupabaseSyncing.value = true
            val result = SupabaseSyncService.pullAllData()
            _isSupabaseSyncing.value = false
            result.onSuccess { data ->
                val totalPulled = data.groups.size + data.users.size + data.members.size +
                        data.attendance.size + data.equipment.size + data.updates.size +
                        data.contacts.size + data.receipts.size + data.deviceSessions.size
                if (totalPulled > 0) {
                    processRestoredSupabaseData(data)
                    val msg = "Supabase تىن $totalPulled تۈرلۈك مەلۇمات مۇۋەپپەقىيەتلىك ئەسلىگە كەلتۈرۈلدى!"
                    if (context != null && !silent) {
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    }
                    onComplete(true, msg)
                } else {
                    val msg = "Supabase تىن ھېچقانداق سانلىق مەلۇمات تېپىلمىدى (جەدۋەللەر بوش)."
                    if (context != null && !silent) {
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    }
                    onComplete(false, msg)
                }
            }.onFailure { ex ->
                val errorMsg = "Supabase تىن چۈشۈرۈش مەغلۇپ بولدى: ${ex.localizedMessage ?: "نامەلۇم خاتالىق"}"
                if (context != null) {
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                }
                onComplete(false, errorMsg)
            }
        }
    }

    fun getSupabaseSqlSchema(): String {
        return SupabaseSyncService.generateSupabaseSqlSchema()
    }

    fun resetDemoData() {
        viewModelScope.launch {
            repository.resetDatabase()
        }
    }
}

data class DutyGroupAttendanceSummary(
    val dutyGroup: GroupEntity? = null,
    val dutySubGroup: Int = 1,
    val dutySubGroupList: List<Int> = listOf(1),
    val dutySubGroupName: String = "",
    val dutySubGroupLeader: String = "",
    val dutySubGroupContact: String = "",
    val dutySubGroupTelegram: String = "",
    val dutySubGroupWhatsapp: String = "",
    val subGroupTotalMembers: Int = 0,
    val subGroupPresentCount: Int = 0,
    val subGroupAbsentCount: Int = 0,
    val subGroupExcusedCount: Int = 0,
    val subGroupAttendanceRate: Float = 0f,
    val subGroupExcusedRate: Float = 0f,
    val totalMembers: Int = 0,
    val isSubmitted: Boolean = false,
    val presentCount: Int = 0,
    val absentCount: Int = 0,
    val excusedCount: Int = 0,
    val attendanceRate: Float = 0f,
    val excusedRate: Float = if (totalMembers > 0) (excusedCount.toFloat() / (presentCount + absentCount + excusedCount).coerceAtLeast(totalMembers)) * 100f else 0f,
    val lastSubmittedTime: String? = null,
    val dutyNotes: String = ""
)

