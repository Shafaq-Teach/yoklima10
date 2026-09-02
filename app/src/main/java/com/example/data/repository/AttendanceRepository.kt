package com.example.data.repository

import com.example.data.db.AttendanceDao
import com.example.data.db.populateInitialData
import com.example.data.model.AttendanceRecordEntity
import com.example.data.model.AttendanceStatus
import com.example.data.model.GroupEntity
import com.example.data.model.MemberEntity
import com.example.data.model.MemberStatus
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.Flow

class AttendanceRepository(private val dao: AttendanceDao) {

    val allGroups: Flow<List<GroupEntity>> = dao.getAllGroups()
    val allUsers: Flow<List<UserEntity>> = dao.getAllUsers()
    val allMembers: Flow<List<MemberEntity>> = dao.getAllMembers()
    val allAttendance: Flow<List<AttendanceRecordEntity>> = dao.getAllAttendance()
    val allEquipment: Flow<List<com.example.data.model.EquipmentEntity>> = dao.getAllEquipment()
    val allDailyUpdates: Flow<List<com.example.data.model.DailyUpdateEntity>> = dao.getAllDailyUpdates()
    val allExecutiveContacts: Flow<List<com.example.data.model.ExecutiveContactEntity>> = dao.getAllExecutiveContacts()
    val allNoticeReceipts: Flow<List<com.example.data.model.NoticeReceiptEntity>> = dao.getAllNoticeReceipts()
    val allGroupLeaders: Flow<List<com.example.data.model.GroupLeaderEntity>> = dao.getAllGroupLeaders()
    val allGroupLeaderAttendance: Flow<List<com.example.data.model.GroupLeaderAttendanceEntity>> = dao.getAllGroupLeaderAttendance()
    val allSanjaqLeaders: Flow<List<com.example.data.model.SanjaqLeaderEntity>> = dao.getAllSanjaqLeaders()
    val allDeviceSessions: Flow<List<com.example.data.model.DeviceSessionEntity>> = dao.getAllDeviceSessions()

    fun getReceiptsForNotice(noticeId: Long): Flow<List<com.example.data.model.NoticeReceiptEntity>> =
        dao.getReceiptsForNotice(noticeId)

    fun getMembersByGroup(groupId: Long): Flow<List<MemberEntity>> = dao.getMembersByGroup(groupId)

    fun getEquipmentByGroup(groupId: Long): Flow<List<com.example.data.model.EquipmentEntity>> =
        dao.getEquipmentByGroup(groupId)

    fun getDailyUpdatesForGroup(groupId: Long): Flow<List<com.example.data.model.DailyUpdateEntity>> =
        dao.getDailyUpdatesForGroup(groupId)

    fun getAttendanceByGroupAndDate(groupId: Long, date: String): Flow<List<AttendanceRecordEntity>> =
        dao.getAttendanceByGroupAndDate(groupId, date)

    fun getAttendanceByDate(date: String): Flow<List<AttendanceRecordEntity>> =
        dao.getAttendanceByDate(date)

    fun getAttendanceForGroup(groupId: Long): Flow<List<AttendanceRecordEntity>> =
        dao.getAttendanceForGroup(groupId)

    suspend fun ensureInitialized() {
        if (dao.getGroupCount() == 0 || dao.getUserCount() == 0) {
            populateInitialData(dao)
        }
    }

    suspend fun authenticate(loginName: String, passwordAttempt: String): UserEntity? {
        val trimmedName = loginName.trim()
        val trimmedPass = passwordAttempt.trim()

        ensureInitialized()

        var user = dao.getUserByLoginName(trimmedName)

        // Fallback checks for admin / default roles if needed
        if (user == null) {
            if (trimmedName.equals("admin", ignoreCase = true) || trimmedName.equals("باش باشقۇرغۇچى", ignoreCase = true)) {
                user = dao.getUserByLoginName("admin")
            }
        }

        if (user == null) {
            // Check all users in db for match
            val allList = dao.getAllUsersList()
            user = allList.find {
                it.loginName.trim().equals(trimmedName, ignoreCase = true) ||
                it.displayName.trim().equals(trimmedName, ignoreCase = true)
            }
        }

        if (user == null) return null

        val storedHash = user.passwordHash.trim()
        val attempt = trimmedPass

        // If user has no password set (empty string), allow direct login
        if (storedHash.isEmpty()) {
            return user
        }

        return if (storedHash == attempt) user else null
    }

    suspend fun removeUserPassword(userId: Long) {
        dao.updatePassword(userId, "")
    }

    suspend fun saveAttendanceRecord(
        memberId: Long,
        groupId: Long,
        date: String,
        status: AttendanceStatus,
        note: String = ""
    ): AttendanceRecordEntity {
        val record = AttendanceRecordEntity(
            memberId = memberId,
            groupId = groupId,
            date = date,
            status = status,
            note = note,
            timestamp = System.currentTimeMillis()
        )
        dao.insertOrUpdateAttendance(record)
        return record
    }

    suspend fun deleteAttendanceRecord(memberId: Long, date: String) {
        dao.deleteAttendanceRecord(memberId, date)
    }

    suspend fun markAllPresent(groupId: Long, date: String, members: List<MemberEntity>): List<AttendanceRecordEntity> {
        val records = members.filter { it.status == MemberStatus.ACTIVE }.map { member ->
            AttendanceRecordEntity(
                memberId = member.id,
                groupId = groupId,
                date = date,
                status = AttendanceStatus.PRESENT,
                note = "",
                timestamp = System.currentTimeMillis()
            )
        }
        dao.insertAttendanceBatch(records)
        return records
    }

    suspend fun addMember(
        name: String,
        groupId: Long,
        subGroup: Int = 1,
        contactAddress: String = "",
        telegramContact: String = "",
        whatsappContact: String = "",
        otherContact: String = "",
        joinDate: String = "",
        status: MemberStatus = MemberStatus.ACTIVE
    ): MemberEntity {
        val uniqueId = System.currentTimeMillis()
        val member = MemberEntity(
            id = uniqueId,
            name = name.trim(),
            groupId = groupId,
            subGroup = subGroup,
            contactAddress = contactAddress.trim(),
            telegramContact = telegramContact.trim(),
            whatsappContact = whatsappContact.trim(),
            otherContact = otherContact.trim(),
            joinDate = joinDate,
            status = status
        )
        dao.insertMember(member)
        return member
    }

    suspend fun addMembersBatch(members: List<MemberEntity>) {
        dao.insertMembers(members)
    }

    suspend fun updateMember(member: MemberEntity) {
        dao.updateMember(member)
    }

    suspend fun deleteMember(member: MemberEntity) {
        dao.deleteMember(member)
    }

    suspend fun resetUserPassword(userId: Long, newPasswordPlain: String) {
        dao.updatePassword(userId, newPasswordPlain.trim())
        val user = dao.getUserById(userId)
        if (user != null) {
            try {
                com.example.data.supabase.SupabaseSyncService.pushUsers(listOf(user))
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    suspend fun updateUserCredentials(userId: Long, loginName: String, passwordPlain: String, displayName: String) {
        dao.updateUserCredentials(userId, loginName.trim(), passwordPlain.trim(), displayName.trim())
        val user = dao.getUserById(userId)
        if (user != null) {
            try {
                com.example.data.supabase.SupabaseSyncService.pushUsers(listOf(user))
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    suspend fun updateGroup(group: GroupEntity) {
        dao.updateGroup(group)
    }

    suspend fun addGroup(
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
    ): Long {
        val groupId = dao.insertGroup(
            GroupEntity(
                name = name.trim(),
                code = code.trim(),
                description = description.trim(),
                subLeader1 = subLeader1.trim(),
                subLeader1Contact = subLeader1Contact.trim(),
                subLeader1Telegram = subLeader1Telegram.trim(),
                subLeader1Whatsapp = subLeader1Whatsapp.trim(),
                subLeader2 = subLeader2.trim(),
                subLeader2Contact = subLeader2Contact.trim(),
                subLeader2Telegram = subLeader2Telegram.trim(),
                subLeader2Whatsapp = subLeader2Whatsapp.trim(),
                isSuspended = false,
                lastActiveTime = System.currentTimeMillis()
            )
        )
        val loginName = if (leaderLoginName.isNotBlank()) leaderLoginName.trim() else "lead$groupId"
        dao.insertUser(
            UserEntity(
                groupId = groupId,
                loginName = loginName,
                passwordHash = if (leaderPasswordPlain.isNotBlank()) leaderPasswordPlain.trim() else "123456",
                displayName = "${name.trim()} مەسئۇلى",
                role = UserRole.GROUP_LEAD
            )
        )
        return groupId
    }

    suspend fun deleteGroup(groupId: Long) {
        dao.deleteUserByGroupId(groupId)
        dao.deleteGroupById(groupId)
    }

    suspend fun setGroupSuspended(groupId: Long, isSuspended: Boolean) {
        dao.updateGroupSuspension(groupId, isSuspended)
    }

    suspend fun recordGroupActive(groupId: Long) {
        dao.updateGroupLastActive(groupId, System.currentTimeMillis())
    }

    // --- Equipment Operations ---
    suspend fun addEquipment(
        groupId: Long,
        name: String,
        totalCount: Int,
        readyCount: Int,
        notReadyCount: Int,
        notes: String = ""
    ): com.example.data.model.EquipmentEntity {
        val uniqueId = System.currentTimeMillis()
        val eq = com.example.data.model.EquipmentEntity(
            id = uniqueId,
            groupId = groupId,
            name = name.trim(),
            totalCount = totalCount,
            readyCount = readyCount,
            notReadyCount = notReadyCount,
            notes = notes.trim(),
            updatedTimestamp = uniqueId
        )
        dao.insertEquipment(eq)
        return eq
    }

    suspend fun updateEquipment(equipment: com.example.data.model.EquipmentEntity) {
        dao.updateEquipment(equipment)
    }

    suspend fun deleteEquipment(equipment: com.example.data.model.EquipmentEntity) {
        dao.deleteEquipment(equipment)
    }

    suspend fun deleteEquipmentById(id: Long) {
        dao.deleteEquipmentById(id)
    }

    suspend fun updateGroupSubLeaders(
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
        dao.updateGroupSubLeaders(
            groupId = groupId,
            leader1 = leader1.trim(),
            contact1 = contact1.trim(),
            tele1 = tele1.trim(),
            wa1 = wa1.trim(),
            other1 = other1.trim(),
            leader2 = leader2.trim(),
            contact2 = contact2.trim(),
            tele2 = tele2.trim(),
            wa2 = wa2.trim(),
            other2 = other2.trim()
        )
    }

    // --- Daily Updates Operations ---
    suspend fun addDailyUpdate(
        groupId: Long,
        groupName: String,
        authorName: String,
        title: String,
        content: String,
        date: String,
        priority: String = "NORMAL"
    ): com.example.data.model.DailyUpdateEntity {
        val uniqueId = System.currentTimeMillis()
        val entity = com.example.data.model.DailyUpdateEntity(
            id = uniqueId,
            groupId = groupId,
            groupName = groupName,
            authorName = authorName.trim(),
            title = title.trim(),
            content = content.trim(),
            date = date,
            priority = priority,
            timestamp = uniqueId
        )
        dao.insertDailyUpdate(entity)
        return entity
    }

    suspend fun updateDailyUpdate(update: com.example.data.model.DailyUpdateEntity) {
        dao.updateDailyUpdate(update)
    }

    suspend fun deleteDailyUpdate(update: com.example.data.model.DailyUpdateEntity) {
        dao.deleteDailyUpdate(update)
    }

    suspend fun deleteDailyUpdateById(id: Long) {
        dao.deleteReceiptsForNotice(id)
        dao.deleteDailyUpdateById(id)
    }

    // --- Notice Tracking & Receipts ---
    suspend fun markNoticeDelivered(noticeId: Long, groupId: Long): com.example.data.model.NoticeReceiptEntity {
        val existing = dao.getReceipt(noticeId, groupId)
        val entity = if (existing == null) {
            com.example.data.model.NoticeReceiptEntity(
                noticeId = noticeId,
                groupId = groupId,
                isDelivered = true,
                deliveredTimestamp = System.currentTimeMillis(),
                isAcknowledged = false,
                acknowledgedTimestamp = 0L
            )
        } else {
            existing.copy(isDelivered = true, deliveredTimestamp = System.currentTimeMillis())
        }
        dao.insertOrUpdateReceipt(entity)
        return entity
    }

    suspend fun acknowledgeNotice(noticeId: Long, groupId: Long): com.example.data.model.NoticeReceiptEntity {
        val existing = dao.getReceipt(noticeId, groupId)
        val now = System.currentTimeMillis()
        val entity = if (existing != null) {
            existing.copy(
                isDelivered = true,
                isAcknowledged = true,
                acknowledgedTimestamp = now
            )
        } else {
            com.example.data.model.NoticeReceiptEntity(
                noticeId = noticeId,
                groupId = groupId,
                isDelivered = true,
                deliveredTimestamp = now,
                isAcknowledged = true,
                acknowledgedTimestamp = now
            )
        }
        dao.insertOrUpdateReceipt(entity)
        return entity
    }

    // --- Executive Contacts Operations ---
    suspend fun addExecutiveContact(
        name: String,
        title: String,
        phone: String = "",
        telegram: String = "",
        whatsapp: String = "",
        radioComms: String = "",
        other: String = "",
        notes: String = ""
    ): com.example.data.model.ExecutiveContactEntity {
        val uniqueId = System.currentTimeMillis()
        val entity = com.example.data.model.ExecutiveContactEntity(
            id = uniqueId,
            name = name.trim(),
            title = title.trim(),
            phone = phone.trim(),
            telegram = telegram.trim(),
            whatsapp = whatsapp.trim(),
            radioComms = radioComms.trim(),
            other = other.trim(),
            notes = notes.trim()
        )
        dao.insertExecutiveContact(entity)
        return entity
    }

    suspend fun updateExecutiveContact(contact: com.example.data.model.ExecutiveContactEntity) {
        dao.updateExecutiveContact(contact)
    }

    suspend fun deleteExecutiveContact(contact: com.example.data.model.ExecutiveContactEntity) {
        dao.deleteExecutiveContact(contact)
    }

    suspend fun deleteExecutiveContactById(id: Long) {
        dao.deleteExecutiveContactById(id)
    }

    // --- Bayraq 3 Key Leaders Operations ---
    suspend fun saveOrUpdateGroupLeader(leader: com.example.data.model.GroupLeaderEntity): Long {
        return dao.insertGroupLeader(leader)
    }

    suspend fun saveGroupLeaderAttendance(
        groupId: Long,
        roleType: String,
        date: String,
        status: String,
        note: String = ""
    ) {
        dao.insertOrUpdateGroupLeaderAttendance(
            com.example.data.model.GroupLeaderAttendanceEntity(
                groupId = groupId,
                roleType = roleType,
                date = date,
                status = status,
                note = note,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    // --- Sanjaq Leadership Operations ---
    suspend fun saveOrUpdateSanjaqLeader(sanjaq: com.example.data.model.SanjaqLeaderEntity): Long {
        return dao.insertSanjaqLeader(sanjaq)
    }

    suspend fun deleteSanjaq(groupId: Long, sanjaqNumber: Int) {
        dao.deleteSanjaqLeader(groupId, sanjaqNumber)
    }

    // --- Device Sessions Management ---
    suspend fun registerOrUpdateDeviceSession(
        deviceId: String,
        deviceName: String,
        osVersion: String,
        userName: String
    ) {
        val existing = dao.getDeviceSession(deviceId)
        val now = System.currentTimeMillis()
        val session = if (existing == null) {
            com.example.data.model.DeviceSessionEntity(
                deviceId = deviceId,
                deviceName = deviceName,
                osVersion = osVersion,
                lastLoginUser = userName,
                firstSeenTime = now,
                lastActiveTime = now,
                isBlocked = false
            )
        } else {
            existing.copy(
                deviceName = deviceName,
                osVersion = osVersion,
                lastLoginUser = userName,
                lastActiveTime = now
            )
        }
        dao.insertOrUpdateDeviceSession(session)
        try {
            com.example.data.supabase.SupabaseSyncService.pushDeviceSessions(listOf(session))
        } catch (e: Exception) {
            // ignore
        }
    }

    suspend fun setDeviceBlockStatus(deviceId: String, isBlocked: Boolean, reason: String = "") {
        dao.updateDeviceBlockStatus(deviceId, isBlocked, reason)
        val session = dao.getDeviceSession(deviceId)
        if (session != null) {
            try {
                com.example.data.supabase.SupabaseSyncService.pushDeviceSessions(listOf(session))
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    suspend fun deleteDeviceSession(deviceId: String) {
        dao.deleteDeviceSession(deviceId)
        try {
            val baseUrl = com.example.data.supabase.SupabaseSyncService.getProjectUrl()
            val key = com.example.data.supabase.SupabaseSyncService.getApiKey()
            com.example.data.supabase.SupabaseSyncService.deleteFromTable(baseUrl, key, "device_sessions", "deviceId=eq.$deviceId")
        } catch (e: Exception) {
            // ignore
        }
    }

    suspend fun isDeviceBlocked(deviceId: String): Boolean {
        val session = dao.getDeviceSession(deviceId)
        return session?.isBlocked == true
    }

    suspend fun restoreFromSupabaseData(data: com.example.data.supabase.SupabasePullData) {
        if (data.groups.isNotEmpty()) dao.insertGroups(data.groups)
        if (data.users.isNotEmpty()) dao.insertUsers(data.users)
        if (data.members.isNotEmpty()) dao.insertMembers(data.members)
        if (data.attendance.isNotEmpty()) dao.insertAttendanceBatch(data.attendance)
        if (data.equipment.isNotEmpty()) dao.insertEquipmentBatch(data.equipment)
        if (data.updates.isNotEmpty()) dao.insertDailyUpdatesBatch(data.updates)
        if (data.contacts.isNotEmpty()) dao.insertExecutiveContactsBatch(data.contacts)
        if (data.receipts.isNotEmpty()) dao.insertReceiptsBatch(data.receipts)
        if (data.deviceSessions.isNotEmpty()) dao.insertDeviceSessionsBatch(data.deviceSessions)
    }

    suspend fun resetDatabase() {
        dao.clearAllAttendance()
        dao.clearAllMembers()
        dao.clearAllEquipment()
        dao.clearAllUsers()
        dao.clearAllGroups()
        populateInitialData(dao)
    }
}
