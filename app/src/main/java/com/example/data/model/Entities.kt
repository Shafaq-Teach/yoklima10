package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class UserRole {
    ADMIN,
    GROUP_LEAD
}

enum class AttendanceStatus {
    PRESENT,
    ABSENT,
    LATE,
    EXCUSED
}

enum class MemberStatus {
    ACTIVE,
    INACTIVE
}

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val code: String,
    val description: String = "",
    val subLeader1: String = "",
    val subLeader1Contact: String = "",
    val subLeader1Telegram: String = "",
    val subLeader1Whatsapp: String = "",
    val subLeader1Other: String = "",
    val subLeader2: String = "",
    val subLeader2Contact: String = "",
    val subLeader2Telegram: String = "",
    val subLeader2Whatsapp: String = "",
    val subLeader2Other: String = "",
    val isSuspended: Boolean = false,
    val lastActiveTime: Long = 0L,
    val dutySubGroup: Int = 1,
    val dutySubGroupCustomName: String = "",
    val dutyNotes: String = ""
)

@Entity(
    tableName = "users",
    indices = [Index(value = ["loginName"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val groupId: Long? = null,
    val loginName: String,
    val passwordHash: String,
    val displayName: String,
    val role: UserRole
)

@Entity(
    tableName = "members",
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("groupId")]
)
data class MemberEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val groupId: Long,
    val subGroup: Int = 1, // 1: 1-گۇرۇپ, 2: 2-گۇرۇپ
    val name: String,
    val contactAddress: String = "", // ئالاقىلىشىش تېلېفون/ئادرېسى
    val telegramContact: String = "", // تېلېگرام ئالاقىسى
    val whatsappContact: String = "", // ۋاتسئاپ ئالاقىسى
    val otherContact: String = "", // باشقا ئالاقە ياكى ئىزاھات
    val joinDate: String = "",
    val status: MemberStatus = MemberStatus.ACTIVE,
    val notes: String = ""
)

@Entity(
    tableName = "attendance_records",
    foreignKeys = [
        ForeignKey(
            entity = MemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["memberId", "date"], unique = true),
        Index("groupId"),
        Index("date")
    ]
)
data class AttendanceRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val memberId: Long,
    val groupId: Long,
    val date: String, // format YYYY-MM-DD
    val status: AttendanceStatus,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "equipment_records",
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("groupId")]
)
data class EquipmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val groupId: Long,
    val name: String, // قورالنىڭ ئىسمى
    val totalCount: Int, // ئومۇمىي سانى
    val readyCount: Int, // تەييار سانى
    val notReadyCount: Int, // تەييار ئەمەس سانى
    val notes: String = "", // ئىزاھات
    val updatedTimestamp: Long = System.currentTimeMillis()
)

// 1. Daily Updates / News & Announcements (يېڭىلىقلار)
@Entity(
    tableName = "daily_updates",
    indices = [Index("groupId"), Index("date")]
)
data class DailyUpdateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val groupId: Long = 0L, // 0 for All Groups / Global HQ Announcement, or 1..N for specific group
    val groupName: String = "", // e.g. "مەركەز / باش قوماندانلىق", "1-گۇرۇپپا", etc.
    val authorName: String,
    val title: String,
    val content: String,
    val date: String, // YYYY-MM-DD
    val priority: String = "NORMAL", // NORMAL, URGENT, IMPORTANT
    val timestamp: Long = System.currentTimeMillis()
)

// 2. Executive Leadership Directory (باش مەسئۇللارنىڭ ئالاقە ئادرېسلىرى)
@Entity(
    tableName = "executive_contacts"
)
data class ExecutiveContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // مەسئۇلنىڭ ئىسمى
    val title: String, // ۋەزىپىسى / سالاھىيىتى
    val phone: String = "", // تېلېفون نومۇرى
    val telegram: String = "", // تېلېگرام
    val whatsapp: String = "", // ۋاتسئاپ
    val radioComms: String = "", // مۇخابىرات ئادرېسى / سىمسىز ئالاقە
    val other: String = "", // باشقا
    val notes: String = "", // قوشۇمچە ئىزاھات
    val orderIndex: Int = 0
)

// 3. Notification Receipts & Tracking (ئۇقتۇرۇشنى قايسى گۇرۇپپا تاپشۇرۇپ ئالغانلىقىنى ئىز قوغلاش)
@Entity(
    tableName = "notice_receipts",
    indices = [Index(value = ["noticeId", "groupId"], unique = true)]
)
data class NoticeReceiptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val noticeId: Long,
    val groupId: Long,
    val isDelivered: Boolean = true, // delivered to phone (سېرىق رەڭ)
    val deliveredTimestamp: Long = System.currentTimeMillis(),
    val isAcknowledged: Boolean = false, // acknowledged "تاپشۇرۇپ ئالدىم" (يېشىل رەڭ)
    val acknowledgedTimestamp: Long = 0L
)

// 4. Bayraq 3 Key Leaders (بايراق مەسئۇلى، بايراق ئەركان، بايراق ئىدارى)
@Entity(
    tableName = "group_leaders",
    indices = [Index(value = ["groupId", "roleType"], unique = true)]
)
data class GroupLeaderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val groupId: Long,
    val roleType: String, // "LEADER" (بايراق مەسئۇلى), "ERKAN" (بايراق ئەركان), "IDARI" (بايراق ئىدارى)
    val name: String = "",
    val phone: String = "",
    val telegram: String = "",
    val whatsapp: String = "",
    val otherContact: String = "",
    val notes: String = ""
)

// 5. Bayraq 3 Key Leaders Attendance (بار، يوق، باشقا يەردە خىزمەتتە، ئارام)
@Entity(
    tableName = "group_leader_attendance",
    indices = [
        Index(value = ["groupId", "roleType", "date"], unique = true),
        Index("date"),
        Index("groupId")
    ]
)
data class GroupLeaderAttendanceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val groupId: Long,
    val roleType: String, // "LEADER", "ERKAN", "IDARI"
    val date: String, // YYYY-MM-DD
    val status: String = "PRESENT", // PRESENT (بار), ABSENT (يوق), EXTERNAL_MISSION (باشقا يەردە خىزمەتتە), REST (ئارام)
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

// 6. Sanjaq Leadership Directory & Info (سانجاق مەسئۇلى ۋە نائىب سانجاق)
@Entity(
    tableName = "sanjaq_leaders",
    indices = [Index(value = ["groupId", "sanjaqNumber"], unique = true)]
)
data class SanjaqLeaderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val groupId: Long,
    val sanjaqNumber: Int, // 1, 2, 3, 4, 5...
    val sanjaqCustomName: String = "",
    val leaderName: String = "",
    val leaderPhone: String = "",
    val leaderTelegram: String = "",
    val leaderWhatsapp: String = "",
    val leaderOther: String = "",
    val deputyName: String = "",
    val deputyPhone: String = "",
    val deputyTelegram: String = "",
    val deputyWhatsapp: String = "",
    val deputyOther: String = ""
)

// 7. Active Devices & Remote Termination Management (ئېچىلغان تېلېفونلارنى كۆرۈش ۋە توختىتىش)
@Entity(
    tableName = "device_sessions",
    indices = [Index(value = ["deviceId"], unique = true)]
)
data class DeviceSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val deviceId: String,
    val deviceName: String,
    val osVersion: String = "",
    val lastLoginUser: String = "",
    val firstSeenTime: Long = System.currentTimeMillis(),
    val lastActiveTime: Long = System.currentTimeMillis(),
    val isBlocked: Boolean = false,
    val blockedReason: String = ""
)



