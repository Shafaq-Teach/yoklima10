package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AttendanceRecordEntity
import com.example.data.model.GroupEntity
import com.example.data.model.MemberEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    // --- Groups ---
    @Query("SELECT * FROM groups ORDER BY id ASC")
    fun getAllGroups(): Flow<List<GroupEntity>>

    @Query("SELECT * FROM groups WHERE id = :id LIMIT 1")
    suspend fun getGroupById(id: Long): GroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroups(groups: List<GroupEntity>)

    @Update
    suspend fun updateGroup(group: GroupEntity)

    @Query("UPDATE groups SET subLeader1 = :leader1, subLeader1Contact = :contact1, subLeader1Telegram = :tele1, subLeader1Whatsapp = :wa1, subLeader1Other = :other1, subLeader2 = :leader2, subLeader2Contact = :contact2, subLeader2Telegram = :tele2, subLeader2Whatsapp = :wa2, subLeader2Other = :other2 WHERE id = :groupId")
    suspend fun updateGroupSubLeaders(
        groupId: Long,
        leader1: String,
        contact1: String,
        tele1: String,
        wa1: String,
        other1: String,
        leader2: String,
        contact2: String,
        tele2: String,
        wa2: String,
        other2: String
    )

    @Delete
    suspend fun deleteGroup(group: GroupEntity)

    @Query("DELETE FROM groups WHERE id = :groupId")
    suspend fun deleteGroupById(groupId: Long)

    @Query("UPDATE groups SET isSuspended = :isSuspended WHERE id = :groupId")
    suspend fun updateGroupSuspension(groupId: Long, isSuspended: Boolean)

    @Query("UPDATE groups SET lastActiveTime = :timestamp WHERE id = :groupId")
    suspend fun updateGroupLastActive(groupId: Long, timestamp: Long)

    // --- Users ---
    @Query("SELECT * FROM users WHERE LOWER(TRIM(loginName)) = LOWER(TRIM(:loginName)) OR LOWER(TRIM(displayName)) = LOWER(TRIM(:loginName)) LIMIT 1")
    suspend fun getUserByLoginName(loginName: String): UserEntity?

    @Query("SELECT * FROM users WHERE groupId = :groupId LIMIT 1")
    suspend fun getUserByGroupId(groupId: Long): UserEntity?

    @Query("DELETE FROM users WHERE groupId = :groupId")
    suspend fun deleteUserByGroupId(groupId: Long)

    @Query("SELECT * FROM users ORDER BY id ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users ORDER BY id ASC")
    suspend fun getAllUsersList(): List<UserEntity>

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET passwordHash = :newPasswordHash WHERE id = :userId")
    suspend fun updatePassword(userId: Long, newPasswordHash: String)

    @Query("UPDATE users SET loginName = :loginName, passwordHash = :passwordHash, displayName = :displayName WHERE id = :userId")
    suspend fun updateUserCredentials(userId: Long, loginName: String, passwordHash: String, displayName: String)

    // --- Members ---
    @Query("SELECT * FROM members WHERE groupId = :groupId ORDER BY id ASC")
    fun getMembersByGroup(groupId: Long): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members ORDER BY id ASC")
    fun getAllMembers(): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members WHERE id = :id LIMIT 1")
    suspend fun getMemberById(id: Long): MemberEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: MemberEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<MemberEntity>)

    @Update
    suspend fun updateMember(member: MemberEntity)

    @Delete
    suspend fun deleteMember(member: MemberEntity)

    // --- Attendance Records ---
    @Query("SELECT * FROM attendance_records WHERE groupId = :groupId AND date = :date")
    fun getAttendanceByGroupAndDate(groupId: Long, date: String): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records WHERE date = :date")
    fun getAttendanceByDate(date: String): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records ORDER BY date DESC, id DESC")
    fun getAllAttendance(): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records WHERE groupId = :groupId ORDER BY date DESC")
    fun getAttendanceForGroup(groupId: Long): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records WHERE memberId = :memberId ORDER BY date DESC")
    fun getAttendanceForMember(memberId: Long): Flow<List<AttendanceRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAttendance(record: AttendanceRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceBatch(records: List<AttendanceRecordEntity>)

    @Query("DELETE FROM attendance_records WHERE memberId = :memberId AND date = :date")
    suspend fun deleteAttendanceRecord(memberId: Long, date: String)

    // --- Equipment Records (قورال-ياراغ تىزىملىكى) ---
    @Query("SELECT * FROM equipment_records ORDER BY id ASC")
    fun getAllEquipment(): Flow<List<com.example.data.model.EquipmentEntity>>

    @Query("SELECT * FROM equipment_records WHERE groupId = :groupId ORDER BY id ASC")
    fun getEquipmentByGroup(groupId: Long): Flow<List<com.example.data.model.EquipmentEntity>>

    @Query("SELECT COUNT(*) FROM equipment_records")
    suspend fun getEquipmentCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipment(equipment: com.example.data.model.EquipmentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipmentBatch(items: List<com.example.data.model.EquipmentEntity>)

    @Update
    suspend fun updateEquipment(equipment: com.example.data.model.EquipmentEntity)

    @Delete
    suspend fun deleteEquipment(equipment: com.example.data.model.EquipmentEntity)

    @Query("DELETE FROM equipment_records WHERE id = :id")
    suspend fun deleteEquipmentById(id: Long)

    @Query("SELECT COUNT(*) FROM groups")
    suspend fun getGroupCount(): Int

    @Query("DELETE FROM equipment_records")
    suspend fun clearAllEquipment()

    // --- Daily Updates (يېڭىلىقلار) ---
    @Query("SELECT * FROM daily_updates ORDER BY timestamp DESC")
    fun getAllDailyUpdates(): Flow<List<com.example.data.model.DailyUpdateEntity>>

    @Query("SELECT * FROM daily_updates WHERE groupId = 0 OR groupId = :groupId ORDER BY timestamp DESC")
    fun getDailyUpdatesForGroup(groupId: Long): Flow<List<com.example.data.model.DailyUpdateEntity>>

    @Query("SELECT COUNT(*) FROM daily_updates")
    suspend fun getDailyUpdatesCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyUpdate(update: com.example.data.model.DailyUpdateEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyUpdatesBatch(updates: List<com.example.data.model.DailyUpdateEntity>)

    @Update
    suspend fun updateDailyUpdate(update: com.example.data.model.DailyUpdateEntity)

    @Delete
    suspend fun deleteDailyUpdate(update: com.example.data.model.DailyUpdateEntity)

    @Query("DELETE FROM daily_updates WHERE id = :id")
    suspend fun deleteDailyUpdateById(id: Long)

    // --- Executive Leadership Contacts (باش مەسئۇللارنىڭ ئالاقە ئادرېسلىرى) ---
    @Query("SELECT * FROM executive_contacts ORDER BY orderIndex ASC, id ASC")
    fun getAllExecutiveContacts(): Flow<List<com.example.data.model.ExecutiveContactEntity>>

    @Query("SELECT COUNT(*) FROM executive_contacts")
    suspend fun getExecutiveContactsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExecutiveContact(contact: com.example.data.model.ExecutiveContactEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExecutiveContactsBatch(contacts: List<com.example.data.model.ExecutiveContactEntity>)

    @Update
    suspend fun updateExecutiveContact(contact: com.example.data.model.ExecutiveContactEntity)

    @Delete
    suspend fun deleteExecutiveContact(contact: com.example.data.model.ExecutiveContactEntity)

    @Query("DELETE FROM executive_contacts WHERE id = :id")
    suspend fun deleteExecutiveContactById(id: Long)

    // --- Notice Receipts & Tracking (ئۇقتۇرۇش قوبۇل قىلىش ئەھۋالى) ---
    @Query("SELECT * FROM notice_receipts WHERE noticeId = :noticeId")
    fun getReceiptsForNotice(noticeId: Long): Flow<List<com.example.data.model.NoticeReceiptEntity>>

    @Query("SELECT * FROM notice_receipts")
    fun getAllNoticeReceipts(): Flow<List<com.example.data.model.NoticeReceiptEntity>>

    @Query("SELECT * FROM notice_receipts WHERE noticeId = :noticeId AND groupId = :groupId LIMIT 1")
    suspend fun getReceipt(noticeId: Long, groupId: Long): com.example.data.model.NoticeReceiptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateReceipt(receipt: com.example.data.model.NoticeReceiptEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceiptsBatch(receipts: List<com.example.data.model.NoticeReceiptEntity>)

    @Query("DELETE FROM notice_receipts WHERE noticeId = :noticeId")
    suspend fun deleteReceiptsForNotice(noticeId: Long)

    // --- Bayraq 3 Leaders (بايراق مەسئۇلى، ئەركان، ئىدارى) ---
    @Query("SELECT * FROM group_leaders ORDER BY groupId ASC, roleType ASC")
    fun getAllGroupLeaders(): Flow<List<com.example.data.model.GroupLeaderEntity>>

    @Query("SELECT * FROM group_leaders WHERE groupId = :groupId ORDER BY roleType ASC")
    fun getLeadersForGroup(groupId: Long): Flow<List<com.example.data.model.GroupLeaderEntity>>

    @Query("SELECT * FROM group_leaders WHERE groupId = :groupId AND roleType = :roleType LIMIT 1")
    suspend fun getLeaderByRole(groupId: Long, roleType: String): com.example.data.model.GroupLeaderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupLeader(leader: com.example.data.model.GroupLeaderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupLeadersBatch(leaders: List<com.example.data.model.GroupLeaderEntity>)

    @Update
    suspend fun updateGroupLeader(leader: com.example.data.model.GroupLeaderEntity)

    @Delete
    suspend fun deleteGroupLeader(leader: com.example.data.model.GroupLeaderEntity)

    @Query("SELECT COUNT(*) FROM group_leaders")
    suspend fun getGroupLeadersCount(): Int

    // --- Bayraq 3 Leaders Attendance (بار، يوق، باشقا يەردە خىزمەتتە، ئارام) ---
    @Query("SELECT * FROM group_leader_attendance ORDER BY date DESC, id DESC")
    fun getAllGroupLeaderAttendance(): Flow<List<com.example.data.model.GroupLeaderAttendanceEntity>>

    @Query("SELECT * FROM group_leader_attendance WHERE date = :date")
    fun getGroupLeaderAttendanceByDate(date: String): Flow<List<com.example.data.model.GroupLeaderAttendanceEntity>>

    @Query("SELECT * FROM group_leader_attendance WHERE groupId = :groupId AND date = :date")
    fun getGroupLeaderAttendance(groupId: Long, date: String): Flow<List<com.example.data.model.GroupLeaderAttendanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateGroupLeaderAttendance(record: com.example.data.model.GroupLeaderAttendanceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupLeaderAttendanceBatch(records: List<com.example.data.model.GroupLeaderAttendanceEntity>)

    @Query("DELETE FROM group_leader_attendance WHERE groupId = :groupId AND roleType = :roleType AND date = :date")
    suspend fun deleteGroupLeaderAttendance(groupId: Long, roleType: String, date: String)

    // --- Sanjaq Leadership (سانجاق مەسئۇلى ۋە نائىب سانجاق) ---
    @Query("SELECT * FROM sanjaq_leaders ORDER BY groupId ASC, sanjaqNumber ASC")
    fun getAllSanjaqLeaders(): Flow<List<com.example.data.model.SanjaqLeaderEntity>>

    @Query("SELECT * FROM sanjaq_leaders WHERE groupId = :groupId ORDER BY sanjaqNumber ASC")
    fun getSanjaqLeadersForGroup(groupId: Long): Flow<List<com.example.data.model.SanjaqLeaderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSanjaqLeader(leader: com.example.data.model.SanjaqLeaderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSanjaqLeadersBatch(leaders: List<com.example.data.model.SanjaqLeaderEntity>)

    @Update
    suspend fun updateSanjaqLeader(leader: com.example.data.model.SanjaqLeaderEntity)

    @Query("DELETE FROM sanjaq_leaders WHERE groupId = :groupId AND sanjaqNumber = :sanjaqNumber")
    suspend fun deleteSanjaqLeader(groupId: Long, sanjaqNumber: Int)

    @Query("SELECT COUNT(*) FROM sanjaq_leaders")
    suspend fun getSanjaqLeadersCount(): Int

    // --- Device Sessions & Termination Management ---
    @Query("SELECT * FROM device_sessions ORDER BY lastActiveTime DESC")
    fun getAllDeviceSessions(): Flow<List<com.example.data.model.DeviceSessionEntity>>

    @Query("SELECT * FROM device_sessions WHERE deviceId = :deviceId LIMIT 1")
    suspend fun getDeviceSession(deviceId: String): com.example.data.model.DeviceSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDeviceSession(session: com.example.data.model.DeviceSessionEntity)

    @Query("UPDATE device_sessions SET isBlocked = :isBlocked, blockedReason = :reason WHERE deviceId = :deviceId")
    suspend fun updateDeviceBlockStatus(deviceId: String, isBlocked: Boolean, reason: String)

    @Query("UPDATE device_sessions SET lastActiveTime = :timestamp, lastLoginUser = :userName WHERE deviceId = :deviceId")
    suspend fun updateDeviceActiveTime(deviceId: String, timestamp: Long, userName: String)

    @Query("DELETE FROM device_sessions WHERE deviceId = :deviceId")
    suspend fun deleteDeviceSession(deviceId: String)

    @Query("DELETE FROM attendance_records")
    suspend fun clearAllAttendance()

    @Query("DELETE FROM members")
    suspend fun clearAllMembers()

    @Query("DELETE FROM users")
    suspend fun clearAllUsers()

    @Query("DELETE FROM groups")
    suspend fun clearAllGroups()
}
