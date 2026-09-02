package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AttendanceRecordEntity
import com.example.data.model.AttendanceStatus
import com.example.data.model.DailyUpdateEntity
import com.example.data.model.EquipmentEntity
import com.example.data.model.ExecutiveContactEntity
import com.example.data.model.GroupEntity
import com.example.data.model.MemberEntity
import com.example.data.model.MemberStatus
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Converters {
    @TypeConverter
    fun fromUserRole(role: UserRole): String = role.name

    @TypeConverter
    fun toUserRole(value: String): UserRole = runCatching { UserRole.valueOf(value) }.getOrDefault(UserRole.GROUP_LEAD)

    @TypeConverter
    fun fromAttendanceStatus(status: AttendanceStatus): String = status.name

    @TypeConverter
    fun toAttendanceStatus(value: String): AttendanceStatus = runCatching { AttendanceStatus.valueOf(value) }.getOrDefault(AttendanceStatus.PRESENT)

    @TypeConverter
    fun fromMemberStatus(status: MemberStatus): String = status.name

    @TypeConverter
    fun toMemberStatus(value: String): MemberStatus = runCatching { MemberStatus.valueOf(value) }.getOrDefault(MemberStatus.ACTIVE)
}

@Database(
    entities = [
        GroupEntity::class,
        UserEntity::class,
        MemberEntity::class,
        AttendanceRecordEntity::class,
        EquipmentEntity::class,
        DailyUpdateEntity::class,
        ExecutiveContactEntity::class,
        com.example.data.model.NoticeReceiptEntity::class,
        com.example.data.model.GroupLeaderEntity::class,
        com.example.data.model.GroupLeaderAttendanceEntity::class,
        com.example.data.model.SanjaqLeaderEntity::class,
        com.example.data.model.DeviceSessionEntity::class
    ],
    version = 9,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun attendanceDao(): AttendanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "yoqlima_database"
                )
                    .addCallback(DatabasePrepopulateCallback(context))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabasePrepopulateCallback(
        private val context: Context
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                populateInitialData(getInstance(context).attendanceDao())
            }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            CoroutineScope(Dispatchers.IO).launch {
                val dao = getInstance(context).attendanceDao()
                if (dao.getGroupCount() == 0 || dao.getUserCount() == 0) {
                    populateInitialData(dao)
                }
            }
        }

        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
            super.onDestructiveMigration(db)
            CoroutineScope(Dispatchers.IO).launch {
                populateInitialData(getInstance(context).attendanceDao())
            }
        }
    }
}

suspend fun populateInitialData(dao: AttendanceDao) {
    // 1. Initial 6 Groups with clean empty fields for real data input:
    // 1-گۇرۇپ = م ط, 2- گۇرۇپ = قنص, 3-گۇرۇپپا = م د, 4- گۇرۇپپا = اسناد, 5- گۇرۇپپا = إدارى, 6-گۇرۇپپا = عمليات
    val groups = listOf(
        GroupEntity(
            id = 1, name = "م ط", code = "م-ط", description = "مجموعة م ط",
            subLeader1 = "", subLeader1Contact = "", subLeader1Telegram = "", subLeader1Whatsapp = "", subLeader1Other = "",
            subLeader2 = "", subLeader2Contact = "", subLeader2Telegram = "", subLeader2Whatsapp = "", subLeader2Other = ""
        ),
        GroupEntity(
            id = 2, name = "قنص", code = "قنص", description = "مجموعة قنص",
            subLeader1 = "", subLeader1Contact = "", subLeader1Telegram = "", subLeader1Whatsapp = "", subLeader1Other = "",
            subLeader2 = "", subLeader2Contact = "", subLeader2Telegram = "", subLeader2Whatsapp = "", subLeader2Other = ""
        ),
        GroupEntity(
            id = 3, name = "م د", code = "م-د", description = "مجموعة م د",
            subLeader1 = "", subLeader1Contact = "", subLeader1Telegram = "", subLeader1Whatsapp = "", subLeader1Other = "",
            subLeader2 = "", subLeader2Contact = "", subLeader2Telegram = "", subLeader2Whatsapp = "", subLeader2Other = ""
        ),
        GroupEntity(
            id = 4, name = "اسناد", code = "اسناد", description = "مجموعة اسناد",
            subLeader1 = "", subLeader1Contact = "", subLeader1Telegram = "", subLeader1Whatsapp = "", subLeader1Other = "",
            subLeader2 = "", subLeader2Contact = "", subLeader2Telegram = "", subLeader2Whatsapp = "", subLeader2Other = ""
        ),
        GroupEntity(
            id = 5, name = "إدارى", code = "إدارى", description = "مجموعة إدارى",
            subLeader1 = "", subLeader1Contact = "", subLeader1Telegram = "", subLeader1Whatsapp = "", subLeader1Other = "",
            subLeader2 = "", subLeader2Contact = "", subLeader2Telegram = "", subLeader2Whatsapp = "", subLeader2Other = ""
        ),
        GroupEntity(
            id = 6, name = "عمليات", code = "عمليات", description = "مجموعة عمليات",
            subLeader1 = "", subLeader1Contact = "", subLeader1Telegram = "", subLeader1Whatsapp = "", subLeader1Other = "",
            subLeader2 = "", subLeader2Contact = "", subLeader2Telegram = "", subLeader2Whatsapp = "", subLeader2Other = ""
        )
    )
    if (dao.getGroupCount() == 0) {
        dao.insertGroups(groups)
    }

    // 2. Initial Authentication Users (Admin + 6 Bayraq/Qisim Leads)
    val users = listOf(
        UserEntity(
            id = 1,
            groupId = null,
            loginName = "admin",
            passwordHash = "123456",
            displayName = "باش باشقۇرغۇچى",
            role = UserRole.ADMIN
        ),
        UserEntity(
            id = 2,
            groupId = 1,
            loginName = "lead1",
            passwordHash = "123456",
            displayName = "م ط بايرىقى",
            role = UserRole.GROUP_LEAD
        ),
        UserEntity(
            id = 3,
            groupId = 2,
            loginName = "lead2",
            passwordHash = "123456",
            displayName = "قنص بايرىقى",
            role = UserRole.GROUP_LEAD
        ),
        UserEntity(
            id = 4,
            groupId = 3,
            loginName = "lead3",
            passwordHash = "123456",
            displayName = "م د بايرىقى",
            role = UserRole.GROUP_LEAD
        ),
        UserEntity(
            id = 5,
            groupId = 4,
            loginName = "lead4",
            passwordHash = "123456",
            displayName = "اسناد بايرىقى",
            role = UserRole.GROUP_LEAD
        ),
        UserEntity(
            id = 6,
            groupId = 5,
            loginName = "lead5",
            passwordHash = "123456",
            displayName = "إدارى قىسمى",
            role = UserRole.GROUP_LEAD
        ),
        UserEntity(
            id = 7,
            groupId = 6,
            loginName = "lead6",
            passwordHash = "123456",
            displayName = "عمليات قىسمى",
            role = UserRole.GROUP_LEAD
        )
    )
    if (dao.getUserCount() == 0) {
        dao.insertUsers(users)
    } else {
        // Ensure admin user exists
        val admin = dao.getUserByLoginName("admin")
        if (admin == null) {
            dao.insertUser(users[0])
        }
    }

    // 3. Seed initial 3 Key Leaders per group (بايراق مەسئۇلى، بايراق ئەركان، بايراق ئىدارى)
    if (dao.getGroupLeadersCount() == 0) {
        val initialLeaders = mutableListOf<com.example.data.model.GroupLeaderEntity>()
        for (groupId in 1L..6L) {
            initialLeaders.add(com.example.data.model.GroupLeaderEntity(groupId = groupId, roleType = "LEADER", name = "", phone = "", telegram = "", whatsapp = "", otherContact = "", notes = ""))
            initialLeaders.add(com.example.data.model.GroupLeaderEntity(groupId = groupId, roleType = "ERKAN", name = "", phone = "", telegram = "", whatsapp = "", otherContact = "", notes = ""))
            initialLeaders.add(com.example.data.model.GroupLeaderEntity(groupId = groupId, roleType = "IDARI", name = "", phone = "", telegram = "", whatsapp = "", otherContact = "", notes = ""))
        }
        dao.insertGroupLeadersBatch(initialLeaders)
    }

    // 4. Seed initial 4 Sanjaqs per group (1-سانجاق, 2-سانجاق, 3-سانجاق, 4-سانجاق)
    if (dao.getSanjaqLeadersCount() == 0) {
        val initialSanjaqs = mutableListOf<com.example.data.model.SanjaqLeaderEntity>()
        for (groupId in 1L..6L) {
            for (sNum in 1..4) {
                initialSanjaqs.add(
                    com.example.data.model.SanjaqLeaderEntity(
                        groupId = groupId,
                        sanjaqNumber = sNum,
                        sanjaqCustomName = "$sNum-سانجاق",
                        leaderName = "",
                        leaderPhone = "",
                        leaderTelegram = "",
                        leaderWhatsapp = "",
                        leaderOther = "",
                        deputyName = "",
                        deputyPhone = "",
                        deputyTelegram = "",
                        deputyWhatsapp = "",
                        deputyOther = ""
                    )
                )
            }
        }
        dao.insertSanjaqLeadersBatch(initialSanjaqs)
    }
}
