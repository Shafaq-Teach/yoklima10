package com.example.data.supabase

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.AttendanceRecordEntity
import com.example.data.model.AttendanceStatus
import com.example.data.model.DailyUpdateEntity
import com.example.data.model.DeviceSessionEntity
import com.example.data.model.EquipmentEntity
import com.example.data.model.ExecutiveContactEntity
import com.example.data.model.GroupEntity
import com.example.data.model.MemberEntity
import com.example.data.model.MemberStatus
import com.example.data.model.NoticeReceiptEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class SupabaseConnectionStatus(
    val isConnected: Boolean,
    val httpCode: Int,
    val latencyMs: Long,
    val message: String,
    val projectId: String,
    val supabaseUrl: String,
    val details: String = ""
)

data class SupabaseSyncResult(
    val isSuccess: Boolean,
    val message: String,
    val groupsUploaded: Int = 0,
    val usersUploaded: Int = 0,
    val membersUploaded: Int = 0,
    val attendanceUploaded: Int = 0,
    val equipmentUploaded: Int = 0,
    val updatesUploaded: Int = 0,
    val contactsUploaded: Int = 0,
    val receiptsUploaded: Int = 0,
    val logDetails: List<String> = emptyList()
)

data class SupabasePullData(
    val groups: List<GroupEntity> = emptyList(),
    val users: List<UserEntity> = emptyList(),
    val members: List<MemberEntity> = emptyList(),
    val attendance: List<AttendanceRecordEntity> = emptyList(),
    val equipment: List<EquipmentEntity> = emptyList(),
    val updates: List<DailyUpdateEntity> = emptyList(),
    val contacts: List<ExecutiveContactEntity> = emptyList(),
    val receipts: List<NoticeReceiptEntity> = emptyList(),
    val deviceSessions: List<DeviceSessionEntity> = emptyList()
)

object SupabaseSyncService {

    private const val TAG = "SupabaseSyncService"

    // Default configuration from user request
    const val DEFAULT_PROJECT_URL = "https://sjfvcxijfgbmeryuezoc.supabase.co"
    const val DEFAULT_API_KEY = "sb_publishable_kgJPz5drPW8S6sgBAgaOhQ_7An9qUqQ"
    const val DEFAULT_PROJECT_ID = "sjfvcxijfgbmeryuezoc"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun getProjectUrl(): String {
        return try {
            val url = BuildConfig.SUPABASE_URL
            if (url.isNotBlank() && url.startsWith("http")) url.trim() else DEFAULT_PROJECT_URL
        } catch (e: Exception) {
            DEFAULT_PROJECT_URL
        }
    }

    fun getApiKey(): String {
        return try {
            val key = BuildConfig.SUPABASE_KEY
            if (key.isNotBlank()) key.trim() else DEFAULT_API_KEY
        } catch (e: Exception) {
            DEFAULT_API_KEY
        }
    }

    fun getProjectId(): String {
        return try {
            val pid = BuildConfig.SUPABASE_PROJECT_ID
            if (pid.isNotBlank()) pid.trim() else DEFAULT_PROJECT_ID
        } catch (e: Exception) {
            DEFAULT_PROJECT_ID
        }
    }

    /**
     * Test connection to the Supabase project
     */
    suspend fun testConnection(): SupabaseConnectionStatus = withContext(Dispatchers.IO) {
        val baseUrl = getProjectUrl()
        val key = getApiKey()
        val pid = getProjectId()
        val startTime = System.currentTimeMillis()

        try {
            // Check Supabase REST API root
            val request = Request.Builder()
                .url("$baseUrl/rest/v1/")
                .addHeader("apikey", key)
                .addHeader("Authorization", "Bearer $key")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val latency = System.currentTimeMillis() - startTime
                val code = response.code
                val bodyStr = response.body?.string() ?: ""

                if (code in 200..299 || code == 404 || code == 400) {
                    // Supabase endpoint is actively reachable
                    SupabaseConnectionStatus(
                        isConnected = true,
                        httpCode = code,
                        latencyMs = latency,
                        message = "Supabase غا مۇۋەپپەقىيەتلىك ئۇلاندى (${latency}ms)",
                        projectId = pid,
                        supabaseUrl = baseUrl,
                        details = "HTTP $code - مۇلازىمېتىر نورمال خىزمەت قىلىۋاتىدۇ."
                    )
                } else if (code == 401 || code == 403) {
                    SupabaseConnectionStatus(
                        isConnected = false,
                        httpCode = code,
                        latencyMs = latency,
                        message = "Supabase كىرىش كارتىسى (API Key) دەلىللەنمىدى (HTTP $code)",
                        projectId = pid,
                        supabaseUrl = baseUrl,
                        details = "ئۇلىنىش بار، ئەمما ئاچقۇچ ھوقۇقى چەكلەندى: $bodyStr"
                    )
                } else {
                    SupabaseConnectionStatus(
                        isConnected = false,
                        httpCode = code,
                        latencyMs = latency,
                        message = "Supabase خاتالىق قايتۇردى (HTTP $code)",
                        projectId = pid,
                        supabaseUrl = baseUrl,
                        details = bodyStr
                    )
                }
            }
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            Log.e(TAG, "Connection error", e)
            SupabaseConnectionStatus(
                isConnected = false,
                httpCode = 0,
                latencyMs = latency,
                message = "Supabase تور ئۇلىنىش مەغلۇپ بولدى: ${e.localizedMessage ?: "نامەلۇم خاتالىق"}",
                projectId = pid,
                supabaseUrl = baseUrl,
                details = e.stackTraceToString()
            )
        }
    }

    /**
     * Upload all local tables and data to Supabase
     */
    suspend fun uploadAllData(
        groups: List<GroupEntity>,
        users: List<UserEntity>,
        members: List<MemberEntity>,
        attendance: List<AttendanceRecordEntity>,
        equipment: List<EquipmentEntity>,
        updates: List<DailyUpdateEntity>,
        contacts: List<ExecutiveContactEntity>,
        receipts: List<NoticeReceiptEntity>,
        deviceSessions: List<DeviceSessionEntity> = emptyList()
    ): SupabaseSyncResult = withContext(Dispatchers.IO) {
        val baseUrl = getProjectUrl()
        val key = getApiKey()
        val logs = mutableListOf<String>()

        var gCount = 0
        var uCount = 0
        var mCount = 0
        var aCount = 0
        var eCount = 0
        var upCount = 0
        var cCount = 0
        var rCount = 0

        try {
            // 1. Upload Groups
            val groupsJson = JSONArray()
            groups.forEach { g ->
                val obj = JSONObject().apply {
                    put("id", g.id)
                    put("name", g.name)
                    put("code", g.code)
                    put("description", g.description)
                    put("subLeader1", g.subLeader1)
                    put("subLeader1Contact", g.subLeader1Contact)
                    put("subLeader1Telegram", g.subLeader1Telegram)
                    put("subLeader1Whatsapp", g.subLeader1Whatsapp)
                    put("subLeader1Other", g.subLeader1Other)
                    put("subLeader2", g.subLeader2)
                    put("subLeader2Contact", g.subLeader2Contact)
                    put("subLeader2Telegram", g.subLeader2Telegram)
                    put("subLeader2Whatsapp", g.subLeader2Whatsapp)
                    put("subLeader2Other", g.subLeader2Other)
                    put("isSuspended", g.isSuspended)
                    put("lastActiveTime", g.lastActiveTime)
                    put("dutySubGroup", g.dutySubGroup)
                    put("dutySubGroupCustomName", g.dutySubGroupCustomName)
                    put("dutyNotes", g.dutyNotes)
                }
                groupsJson.put(obj)
            }
            val groupsRes = upsertToTable(baseUrl, key, "groups", groupsJson.toString())
            if (groupsRes.isSuccess) {
                gCount = groups.size
                logs.add("✅ گۇرۇپپىلار: ${groups.size} گۇرۇپپا يۈكلەندى")
            } else {
                logs.add("⚠️ گۇرۇپپىلار يۈكلەش ئۇچۇرى: ${groupsRes.message}")
            }

            // 2. Upload Users
            val usersJson = JSONArray()
            users.forEach { u ->
                val obj = JSONObject().apply {
                    put("id", u.id)
                    if (u.groupId != null) put("groupId", u.groupId) else put("groupId", JSONObject.NULL)
                    put("loginName", u.loginName)
                    put("passwordHash", u.passwordHash)
                    put("displayName", u.displayName)
                    put("role", u.role.name)
                }
                usersJson.put(obj)
            }
            val usersRes = upsertToTable(baseUrl, key, "users", usersJson.toString())
            if (usersRes.isSuccess) {
                uCount = users.size
                logs.add("✅ كىرىش ھېساباتلىرى: ${users.size} ھېسابات يۈكلەندى")
            } else {
                logs.add("⚠️ كىرىش ھېساباتلىرى: ${usersRes.message}")
            }

            // 3. Upload Members
            val membersJson = JSONArray()
            members.forEach { m ->
                val obj = JSONObject().apply {
                    put("id", m.id)
                    put("groupId", m.groupId)
                    put("subGroup", m.subGroup)
                    put("name", m.name)
                    put("contactAddress", m.contactAddress)
                    put("telegramContact", m.telegramContact)
                    put("whatsappContact", m.whatsappContact)
                    put("otherContact", m.otherContact)
                    put("joinDate", m.joinDate)
                    put("status", m.status.name)
                    put("notes", m.notes)
                }
                membersJson.put(obj)
            }
            val membersRes = upsertToTable(baseUrl, key, "members", membersJson.toString())
            if (membersRes.isSuccess) {
                mCount = members.size
                logs.add("✅ گۇرۇپپا ئەزالىرى: ${members.size} ئەزا يۈكلەندى")
            } else {
                logs.add("⚠️ ئەزالار: ${membersRes.message}")
            }

            // 4. Upload Attendance Records
            val attendanceJson = JSONArray()
            attendance.forEach { a ->
                val obj = JSONObject().apply {
                    put("id", a.id)
                    put("memberId", a.memberId)
                    put("groupId", a.groupId)
                    put("date", a.date)
                    put("status", a.status.name)
                    put("note", a.note)
                    put("timestamp", a.timestamp)
                }
                attendanceJson.put(obj)
            }
            val attRes = upsertToTable(baseUrl, key, "attendance_records", attendanceJson.toString())
            if (attRes.isSuccess) {
                aCount = attendance.size
                logs.add("✅ يوقلىما خاتىرىلىرى: ${attendance.size} خاتىرە يۈكلەندى")
            } else {
                logs.add("⚠️ يوقلىما خاتىرىلىرى: ${attRes.message}")
            }

            // 5. Upload Equipment Records
            val equipJson = JSONArray()
            equipment.forEach { eq ->
                val obj = JSONObject().apply {
                    put("id", eq.id)
                    put("groupId", eq.groupId)
                    put("name", eq.name)
                    put("totalCount", eq.totalCount)
                    put("readyCount", eq.readyCount)
                    put("notReadyCount", eq.notReadyCount)
                    put("notes", eq.notes)
                    put("updatedTimestamp", eq.updatedTimestamp)
                }
                equipJson.put(obj)
            }
            val eqRes = upsertToTable(baseUrl, key, "equipment_records", equipJson.toString())
            if (eqRes.isSuccess) {
                eCount = equipment.size
                logs.add("✅ قورال-جابدۇقلار: ${equipment.size} تۈر يۈكلەندى")
            } else {
                logs.add("⚠️ قورال-جابدۇقلار: ${eqRes.message}")
            }

            // 6. Upload Daily Updates
            val updatesJson = JSONArray()
            updates.forEach { up ->
                val obj = JSONObject().apply {
                    put("id", up.id)
                    put("groupId", up.groupId)
                    put("groupName", up.groupName)
                    put("authorName", up.authorName)
                    put("title", up.title)
                    put("content", up.content)
                    put("date", up.date)
                    put("priority", up.priority)
                    put("timestamp", up.timestamp)
                }
                updatesJson.put(obj)
            }
            val upRes = upsertToTable(baseUrl, key, "daily_updates", updatesJson.toString())
            if (upRes.isSuccess) {
                upCount = updates.size
                logs.add("✅ يېڭىلىق ۋە ئۇقتۇرۇشلار: ${updates.size} دانە يۈكلەندى")
            } else {
                logs.add("⚠️ ئۇقتۇرۇشلار: ${upRes.message}")
            }

            // 7. Upload Executive Contacts
            val contactsJson = JSONArray()
            contacts.forEach { c ->
                val obj = JSONObject().apply {
                    put("id", c.id)
                    put("name", c.name)
                    put("title", c.title)
                    put("phone", c.phone)
                    put("telegram", c.telegram)
                    put("whatsapp", c.whatsapp)
                    put("radioComms", c.radioComms)
                    put("other", c.other)
                    put("notes", c.notes)
                    put("orderIndex", c.orderIndex)
                }
                contactsJson.put(obj)
            }
            val cRes = upsertToTable(baseUrl, key, "executive_contacts", contactsJson.toString())
            if (cRes.isSuccess) {
                cCount = contacts.size
                logs.add("✅ باش مەسئۇللار ئالاقىسى: ${contacts.size} مەسئۇل يۈكلەندى")
            } else {
                logs.add("⚠️ مەسئۇللار ئالاقىسى: ${cRes.message}")
            }

            // 8. Upload Notice Receipts
            val receiptsJson = JSONArray()
            receipts.forEach { r ->
                val obj = JSONObject().apply {
                    put("id", r.id)
                    put("noticeId", r.noticeId)
                    put("groupId", r.groupId)
                    put("isDelivered", r.isDelivered)
                    put("deliveredTimestamp", r.deliveredTimestamp)
                    put("isAcknowledged", r.isAcknowledged)
                    put("acknowledgedTimestamp", r.acknowledgedTimestamp)
                }
                receiptsJson.put(obj)
            }
            val rRes = upsertToTable(baseUrl, key, "notice_receipts", receiptsJson.toString())
            if (rRes.isSuccess) {
                rCount = receipts.size
                logs.add("✅ ئۇقتۇرۇش ئىز قوغلاش تىزىملىكى: ${receipts.size} كۈچكە ئىگە")
            } else {
                logs.add("⚠️ ئىز قوغلاش: ${rRes.message}")
            }

            // 9. Upload Device Sessions
            if (deviceSessions.isNotEmpty()) {
                val devJson = JSONArray()
                deviceSessions.forEach { d ->
                    val obj = JSONObject().apply {
                        put("deviceId", d.deviceId)
                        put("deviceName", d.deviceName)
                        put("osVersion", d.osVersion)
                        put("lastLoginUser", d.lastLoginUser)
                        put("firstSeenTime", d.firstSeenTime)
                        put("lastActiveTime", d.lastActiveTime)
                        put("isBlocked", d.isBlocked)
                        put("blockedReason", d.blockedReason)
                    }
                    devJson.put(obj)
                }
                val dRes = upsertToTable(baseUrl, key, "device_sessions", devJson.toString())
                if (dRes.isSuccess) {
                    logs.add("✅ ئۈسكۈنىلەر: ${deviceSessions.size} تېلېفون ھالىتى يۈكلەندى")
                } else {
                    logs.add("⚠️ ئۈسكۈنىلەر: ${dRes.message}")
                }
            }

            // Also upload a full consolidated snapshot backup
            uploadConsolidatedBackup(
                baseUrl, key, groupsJson, usersJson, membersJson,
                attendanceJson, equipJson, updatesJson, contactsJson, receiptsJson
            )

            SupabaseSyncResult(
                isSuccess = true,
                message = "مەلۇماتلار Supabase بۇلۇت بازىسىغا مۇۋەپپەقىيەتلىك يۈكلەندى!",
                groupsUploaded = gCount,
                usersUploaded = uCount,
                membersUploaded = mCount,
                attendanceUploaded = aCount,
                equipmentUploaded = eCount,
                updatesUploaded = upCount,
                contactsUploaded = cCount,
                receiptsUploaded = rCount,
                logDetails = logs
            )
        } catch (e: Exception) {
            Log.e(TAG, "Upload failed", e)
            SupabaseSyncResult(
                isSuccess = false,
                message = "Supabase كە يۈكلەش جەريانىدا خاتالىق يۈز بەردى: ${e.localizedMessage ?: "نامەلۇم خاتالىق"}",
                logDetails = logs + listOf("❌ خاتالىق: ${e.localizedMessage}")
            )
        }
    }

    /**
     * Pull all data from Supabase REST endpoints
     */
    suspend fun pullAllData(): Result<SupabasePullData> = withContext(Dispatchers.IO) {
        val baseUrl = getProjectUrl()
        val key = getApiKey()

        try {
            // First pull table by table for most accurate real-time data
            val updates = pullTableData(baseUrl, key, "daily_updates") { obj ->
                DailyUpdateEntity(
                    id = obj.optLong("id", 0L),
                    groupId = obj.optLong("groupId", 0L),
                    groupName = obj.optString("groupName", ""),
                    authorName = obj.optString("authorName", ""),
                    title = obj.optString("title", ""),
                    content = obj.optString("content", ""),
                    date = obj.optString("date", ""),
                    priority = obj.optString("priority", "NORMAL"),
                    timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                )
            }

            val attendance = pullTableData(baseUrl, key, "attendance_records") { obj ->
                AttendanceRecordEntity(
                    id = obj.optLong("id", 0L),
                    memberId = obj.optLong("memberId", 0L),
                    groupId = obj.optLong("groupId", 1L),
                    date = obj.optString("date", ""),
                    status = try { AttendanceStatus.valueOf(obj.optString("status", "PRESENT")) } catch (e: Exception) { AttendanceStatus.PRESENT },
                    note = obj.optString("note", ""),
                    timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                )
            }

            val members = pullTableData(baseUrl, key, "members") { obj ->
                MemberEntity(
                    id = obj.optLong("id", 0L),
                    groupId = obj.optLong("groupId", 1L),
                    subGroup = obj.optInt("subGroup", 1),
                    name = obj.optString("name", ""),
                    contactAddress = obj.optString("contactAddress", ""),
                    telegramContact = obj.optString("telegramContact", ""),
                    whatsappContact = obj.optString("whatsappContact", ""),
                    otherContact = obj.optString("otherContact", ""),
                    joinDate = obj.optString("joinDate", ""),
                    status = try { MemberStatus.valueOf(obj.optString("status", "ACTIVE")) } catch (e: Exception) { MemberStatus.ACTIVE },
                    notes = obj.optString("notes", "")
                )
            }

            val groups = pullTableData(baseUrl, key, "groups") { obj ->
                GroupEntity(
                    id = obj.optLong("id", 0L),
                    name = obj.optString("name", ""),
                    code = obj.optString("code", ""),
                    description = obj.optString("description", ""),
                    subLeader1 = obj.optString("subLeader1", ""),
                    subLeader1Contact = obj.optString("subLeader1Contact", ""),
                    subLeader1Telegram = obj.optString("subLeader1Telegram", ""),
                    subLeader1Whatsapp = obj.optString("subLeader1Whatsapp", ""),
                    subLeader1Other = obj.optString("subLeader1Other", ""),
                    subLeader2 = obj.optString("subLeader2", ""),
                    subLeader2Contact = obj.optString("subLeader2Contact", ""),
                    subLeader2Telegram = obj.optString("subLeader2Telegram", ""),
                    subLeader2Whatsapp = obj.optString("subLeader2Whatsapp", ""),
                    subLeader2Other = obj.optString("subLeader2Other", ""),
                    isSuspended = obj.optBoolean("isSuspended", false),
                    lastActiveTime = obj.optLong("lastActiveTime", 0L),
                    dutySubGroup = obj.optInt("dutySubGroup", 1),
                    dutySubGroupCustomName = obj.optString("dutySubGroupCustomName", ""),
                    dutyNotes = obj.optString("dutyNotes", "")
                )
            }

            val users = pullTableData(baseUrl, key, "users") { obj ->
                UserEntity(
                    id = obj.optLong("id", 0L),
                    groupId = if (obj.isNull("groupId")) null else obj.optLong("groupId"),
                    loginName = obj.optString("loginName", ""),
                    passwordHash = obj.optString("passwordHash", ""),
                    displayName = obj.optString("displayName", ""),
                    role = try { UserRole.valueOf(obj.optString("role", "GROUP_LEAD")) } catch (e: Exception) { UserRole.GROUP_LEAD }
                )
            }

            val equipment = pullTableData(baseUrl, key, "equipment_records") { obj ->
                EquipmentEntity(
                    id = obj.optLong("id", 0L),
                    groupId = obj.optLong("groupId", 1L),
                    name = obj.optString("name", ""),
                    totalCount = obj.optInt("totalCount", 0),
                    readyCount = obj.optInt("readyCount", 0),
                    notReadyCount = obj.optInt("notReadyCount", 0),
                    notes = obj.optString("notes", ""),
                    updatedTimestamp = obj.optLong("updatedTimestamp", System.currentTimeMillis())
                )
            }

            val contacts = pullTableData(baseUrl, key, "executive_contacts") { obj ->
                ExecutiveContactEntity(
                    id = obj.optLong("id", 0L),
                    name = obj.optString("name", ""),
                    title = obj.optString("title", ""),
                    phone = obj.optString("phone", ""),
                    telegram = obj.optString("telegram", ""),
                    whatsapp = obj.optString("whatsapp", ""),
                    radioComms = obj.optString("radioComms", ""),
                    other = obj.optString("other", ""),
                    notes = obj.optString("notes", ""),
                    orderIndex = obj.optInt("orderIndex", 0)
                )
            }

            val receipts = pullTableData(baseUrl, key, "notice_receipts") { obj ->
                NoticeReceiptEntity(
                    id = obj.optLong("id", 0L),
                    noticeId = obj.optLong("noticeId", 0L),
                    groupId = obj.optLong("groupId", 0L),
                    isDelivered = obj.optBoolean("isDelivered", true),
                    deliveredTimestamp = obj.optLong("deliveredTimestamp", System.currentTimeMillis()),
                    isAcknowledged = obj.optBoolean("isAcknowledged", false),
                    acknowledgedTimestamp = obj.optLong("acknowledgedTimestamp", 0L)
                )
            }

            val deviceSessions = pullTableData(baseUrl, key, "device_sessions") { obj ->
                DeviceSessionEntity(
                    deviceId = obj.optString("deviceId", obj.optString("device_id", "")),
                    deviceName = obj.optString("deviceName", obj.optString("device_name", "ئۈسكۈنە")),
                    osVersion = obj.optString("osVersion", obj.optString("os_version", "Android")),
                    lastLoginUser = obj.optString("lastLoginUser", obj.optString("last_login_user", "")),
                    firstSeenTime = obj.optLong("firstSeenTime", obj.optLong("first_seen_time", System.currentTimeMillis())),
                    lastActiveTime = obj.optLong("lastActiveTime", obj.optLong("last_active_time", System.currentTimeMillis())),
                    isBlocked = obj.optBoolean("isBlocked", obj.optBoolean("is_blocked", false)),
                    blockedReason = obj.optString("blockedReason", obj.optString("blocked_reason", ""))
                )
            }.filter { it.deviceId.isNotBlank() }

            val totalCount = updates.size + attendance.size + members.size + groups.size + users.size + equipment.size + contacts.size + receipts.size + deviceSessions.size

            if (totalCount > 0) {
                return@withContext Result.success(
                    SupabasePullData(
                        groups = groups,
                        users = users,
                        members = members,
                        attendance = attendance,
                        equipment = equipment,
                        updates = updates,
                        contacts = contacts,
                        receipts = receipts,
                        deviceSessions = deviceSessions
                    )
                )
            }

            // Fallback to consolidated backup if individual tables were empty
            val backupData = fetchConsolidatedBackup(baseUrl, key)
            if (backupData != null) {
                return@withContext Result.success(backupData)
            }

            Result.success(SupabasePullData())
        } catch (e: Exception) {
            Log.e(TAG, "Pull all data failed", e)
            Result.failure(e)
        }
    }

    suspend fun pushUsers(users: List<UserEntity>): SupabaseSyncResult = withContext(Dispatchers.IO) {
        val baseUrl = getProjectUrl()
        val key = getApiKey()
        val usersJson = JSONArray()
        users.forEach { u ->
            val obj = JSONObject().apply {
                put("id", u.id)
                if (u.groupId != null) put("groupId", u.groupId) else put("groupId", JSONObject.NULL)
                put("loginName", u.loginName)
                put("passwordHash", u.passwordHash)
                put("displayName", u.displayName)
                put("role", u.role.name)
            }
            usersJson.put(obj)
        }
        val res = upsertToTable(baseUrl, key, "users", usersJson.toString())
        SupabaseSyncResult(
            isSuccess = res.isSuccess,
            message = res.message,
            usersUploaded = if (res.isSuccess) users.size else 0
        )
    }

    suspend fun pushDeviceSessions(sessions: List<DeviceSessionEntity>): SupabaseSyncResult = withContext(Dispatchers.IO) {
        val baseUrl = getProjectUrl()
        val key = getApiKey()
        val devJson = JSONArray()
        sessions.forEach { d ->
            val obj = JSONObject().apply {
                put("deviceId", d.deviceId)
                put("deviceName", d.deviceName)
                put("osVersion", d.osVersion)
                put("lastLoginUser", d.lastLoginUser)
                put("firstSeenTime", d.firstSeenTime)
                put("lastActiveTime", d.lastActiveTime)
                put("isBlocked", d.isBlocked)
                put("blockedReason", d.blockedReason)
            }
            devJson.put(obj)
        }
        val res = upsertToTable(baseUrl, key, "device_sessions", devJson.toString())
        SupabaseSyncResult(
            isSuccess = res.isSuccess,
            message = res.message
        )
    }

    suspend fun upsertDailyUpdate(update: DailyUpdateEntity): Boolean = withContext(Dispatchers.IO) {
        val baseUrl = getProjectUrl()
        val key = getApiKey()
        val obj = JSONObject().apply {
            put("id", update.id)
            put("groupId", update.groupId)
            put("groupName", update.groupName)
            put("authorName", update.authorName)
            put("title", update.title)
            put("content", update.content)
            put("date", update.date)
            put("priority", update.priority)
            put("timestamp", update.timestamp)
        }
        val arr = JSONArray().apply { put(obj) }
        upsertToTable(baseUrl, key, "daily_updates", arr.toString()).isSuccess
    }

    suspend fun deleteDailyUpdate(id: Long): Boolean = withContext(Dispatchers.IO) {
        val baseUrl = getProjectUrl()
        val key = getApiKey()
        deleteFromTable(baseUrl, key, "daily_updates", "id=eq.$id")
    }

    suspend fun upsertAttendanceRecord(record: AttendanceRecordEntity): Boolean = withContext(Dispatchers.IO) {
        val baseUrl = getProjectUrl()
        val key = getApiKey()
        val obj = JSONObject().apply {
            put("id", record.id)
            put("memberId", record.memberId)
            put("groupId", record.groupId)
            put("date", record.date)
            put("status", record.status.name)
            put("note", record.note)
            put("timestamp", record.timestamp)
        }
        val arr = JSONArray().apply { put(obj) }
        upsertToTable(baseUrl, key, "attendance_records", arr.toString()).isSuccess
    }

    suspend fun upsertAttendanceBatch(records: List<AttendanceRecordEntity>): Boolean = withContext(Dispatchers.IO) {
        if (records.isEmpty()) return@withContext true
        val baseUrl = getProjectUrl()
        val key = getApiKey()
        val arr = JSONArray()
        records.forEach { r ->
            val obj = JSONObject().apply {
                put("id", r.id)
                put("memberId", r.memberId)
                put("groupId", r.groupId)
                put("date", r.date)
                put("status", r.status.name)
                put("note", r.note)
                put("timestamp", r.timestamp)
            }
            arr.put(obj)
        }
        upsertToTable(baseUrl, key, "attendance_records", arr.toString()).isSuccess
    }

    suspend fun deleteAttendanceRecord(memberId: Long, date: String): Boolean = withContext(Dispatchers.IO) {
        val baseUrl = getProjectUrl()
        val key = getApiKey()
        deleteFromTable(baseUrl, key, "attendance_records", "memberId=eq.$memberId&date=eq.$date")
    }

    suspend fun upsertMember(member: MemberEntity): Boolean = withContext(Dispatchers.IO) {
        val baseUrl = getProjectUrl()
        val key = getApiKey()
        val obj = JSONObject().apply {
            put("id", member.id)
            put("groupId", member.groupId)
            put("subGroup", member.subGroup)
            put("name", member.name)
            put("contactAddress", member.contactAddress)
            put("telegramContact", member.telegramContact)
            put("whatsappContact", member.whatsappContact)
            put("otherContact", member.otherContact)
            put("joinDate", member.joinDate)
            put("status", member.status.name)
            put("notes", member.notes)
        }
        val arr = JSONArray().apply { put(obj) }
        upsertToTable(baseUrl, key, "members", arr.toString()).isSuccess
    }

    suspend fun deleteMember(id: Long): Boolean = withContext(Dispatchers.IO) {
        val baseUrl = getProjectUrl()
        val key = getApiKey()
        deleteFromTable(baseUrl, key, "members", "id=eq.$id")
    }

    suspend fun upsertEquipment(eq: EquipmentEntity): Boolean = withContext(Dispatchers.IO) {
        val baseUrl = getProjectUrl()
        val key = getApiKey()
        val obj = JSONObject().apply {
            put("id", eq.id)
            put("groupId", eq.groupId)
            put("name", eq.name)
            put("totalCount", eq.totalCount)
            put("readyCount", eq.readyCount)
            put("notReadyCount", eq.notReadyCount)
            put("notes", eq.notes)
            put("updatedTimestamp", eq.updatedTimestamp)
        }
        val arr = JSONArray().apply { put(obj) }
        upsertToTable(baseUrl, key, "equipment_records", arr.toString()).isSuccess
    }

    suspend fun deleteEquipment(id: Long): Boolean = withContext(Dispatchers.IO) {
        val baseUrl = getProjectUrl()
        val key = getApiKey()
        deleteFromTable(baseUrl, key, "equipment_records", "id=eq.$id")
    }

    suspend fun upsertNoticeReceipt(r: NoticeReceiptEntity): Boolean = withContext(Dispatchers.IO) {
        val baseUrl = getProjectUrl()
        val key = getApiKey()
        val obj = JSONObject().apply {
            put("id", r.id)
            put("noticeId", r.noticeId)
            put("groupId", r.groupId)
            put("isDelivered", r.isDelivered)
            put("deliveredTimestamp", r.deliveredTimestamp)
            put("isAcknowledged", r.isAcknowledged)
            put("acknowledgedTimestamp", r.acknowledgedTimestamp)
        }
        val arr = JSONArray().apply { put(obj) }
        upsertToTable(baseUrl, key, "notice_receipts", arr.toString()).isSuccess
    }

    suspend fun upsertGroup(g: GroupEntity): Boolean = withContext(Dispatchers.IO) {
        val baseUrl = getProjectUrl()
        val key = getApiKey()
        val obj = JSONObject().apply {
            put("id", g.id)
            put("name", g.name)
            put("code", g.code)
            put("description", g.description)
            put("subLeader1", g.subLeader1)
            put("subLeader1Contact", g.subLeader1Contact)
            put("subLeader1Telegram", g.subLeader1Telegram)
            put("subLeader1Whatsapp", g.subLeader1Whatsapp)
            put("subLeader1Other", g.subLeader1Other)
            put("subLeader2", g.subLeader2)
            put("subLeader2Contact", g.subLeader2Contact)
            put("subLeader2Telegram", g.subLeader2Telegram)
            put("subLeader2Whatsapp", g.subLeader2Whatsapp)
            put("subLeader2Other", g.subLeader2Other)
            put("isSuspended", g.isSuspended)
            put("lastActiveTime", g.lastActiveTime)
            put("dutySubGroup", g.dutySubGroup)
            put("dutySubGroupCustomName", g.dutySubGroupCustomName)
            put("dutyNotes", g.dutyNotes)
        }
        val arr = JSONArray().apply { put(obj) }
        upsertToTable(baseUrl, key, "groups", arr.toString()).isSuccess
    }

    suspend fun upsertExecutiveContact(c: ExecutiveContactEntity): Boolean = withContext(Dispatchers.IO) {
        val baseUrl = getProjectUrl()
        val key = getApiKey()
        val obj = JSONObject().apply {
            put("id", c.id)
            put("name", c.name)
            put("title", c.title)
            put("phone", c.phone)
            put("telegram", c.telegram)
            put("whatsapp", c.whatsapp)
            put("radioComms", c.radioComms)
            put("other", c.other)
            put("notes", c.notes)
            put("orderIndex", c.orderIndex)
        }
        val arr = JSONArray().apply { put(obj) }
        upsertToTable(baseUrl, key, "executive_contacts", arr.toString()).isSuccess
    }

    suspend fun deleteExecutiveContact(id: Long): Boolean = withContext(Dispatchers.IO) {
        val baseUrl = getProjectUrl()
        val key = getApiKey()
        deleteFromTable(baseUrl, key, "executive_contacts", "id=eq.$id")
    }

    private fun deleteFromTable(baseUrl: String, key: String, tableName: String, queryParams: String): Boolean {
        val request = Request.Builder()
            .url("$baseUrl/rest/v1/$tableName?$queryParams")
            .addHeader("apikey", key)
            .addHeader("Authorization", "Bearer $key")
            .delete()
            .build()
        return try {
            client.newCall(request).execute().use { it.isSuccessful }
        } catch (e: Exception) {
            false
        }
    }

    private fun upsertToTable(
        baseUrl: String,
        key: String,
        tableName: String,
        jsonArrayString: String
    ): RequestResult {
        if (jsonArrayString == "[]") return RequestResult(true, "بوش", 200)

        val request = Request.Builder()
            .url("$baseUrl/rest/v1/$tableName")
            .addHeader("apikey", key)
            .addHeader("Authorization", "Bearer $key")
            .addHeader("Prefer", "resolution=merge-duplicates,return=minimal")
            .addHeader("Content-Type", "application/json")
            .post(jsonArrayString.toRequestBody(jsonMediaType))
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (response.isSuccessful || response.code == 201 || response.code == 200 || response.code == 204) {
                    RequestResult(true, "مۇۋەپپەقىيەتلىك", response.code)
                } else {
                    RequestResult(false, "HTTP ${response.code}: $body", response.code)
                }
            }
        } catch (e: Exception) {
            RequestResult(false, e.localizedMessage ?: "تور ئۇلىنىش خاتالىقى", 0)
        }
    }

    private fun uploadConsolidatedBackup(
        baseUrl: String,
        key: String,
        groups: JSONArray,
        users: JSONArray,
        members: JSONArray,
        attendance: JSONArray,
        equipment: JSONArray,
        updates: JSONArray,
        contacts: JSONArray,
        receipts: JSONArray
    ) {
        try {
            val root = JSONObject().apply {
                put("id", "latest_backup")
                put("backup_timestamp", System.currentTimeMillis())
                put("groups", groups)
                put("users", users)
                put("members", members)
                put("attendance", attendance)
                put("equipment", equipment)
                put("updates", updates)
                put("contacts", contacts)
                put("receipts", receipts)
            }

            val array = JSONArray().apply { put(root) }
            upsertToTable(baseUrl, key, "app_cloud_backups", array.toString())
        } catch (e: Exception) {
            Log.w(TAG, "Consolidated backup upload skipped: ${e.message}")
        }
    }

    private fun fetchConsolidatedBackup(baseUrl: String, key: String): SupabasePullData? {
        try {
            val request = Request.Builder()
                .url("$baseUrl/rest/v1/app_cloud_backups?id=eq.latest_backup&select=*")
                .addHeader("apikey", key)
                .addHeader("Authorization", "Bearer $key")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body?.string() ?: return null
                val jsonArr = JSONArray(body)
                if (jsonArr.length() == 0) return null
                val obj = jsonArr.getJSONObject(0)

                val gList = parseEntities(obj.optJSONArray("groups")) { g ->
                    GroupEntity(
                        id = g.optLong("id", 0L),
                        name = g.optString("name", ""),
                        code = g.optString("code", ""),
                        description = g.optString("description", ""),
                        subLeader1 = g.optString("subLeader1", ""),
                        subLeader1Contact = g.optString("subLeader1Contact", ""),
                        subLeader1Telegram = g.optString("subLeader1Telegram", ""),
                        subLeader1Whatsapp = g.optString("subLeader1Whatsapp", ""),
                        subLeader1Other = g.optString("subLeader1Other", ""),
                        subLeader2 = g.optString("subLeader2", ""),
                        subLeader2Contact = g.optString("subLeader2Contact", ""),
                        subLeader2Telegram = g.optString("subLeader2Telegram", ""),
                        subLeader2Whatsapp = g.optString("subLeader2Whatsapp", ""),
                        subLeader2Other = g.optString("subLeader2Other", ""),
                        isSuspended = g.optBoolean("isSuspended", false),
                        lastActiveTime = g.optLong("lastActiveTime", 0L),
                        dutySubGroup = g.optInt("dutySubGroup", 1),
                        dutySubGroupCustomName = g.optString("dutySubGroupCustomName", ""),
                        dutyNotes = g.optString("dutyNotes", "")
                    )
                }

                val uList = parseEntities(obj.optJSONArray("users")) { u ->
                    UserEntity(
                        id = u.optLong("id", 0L),
                        groupId = if (u.isNull("groupId")) null else u.optLong("groupId"),
                        loginName = u.optString("loginName", ""),
                        passwordHash = u.optString("passwordHash", ""),
                        displayName = u.optString("displayName", ""),
                        role = try { UserRole.valueOf(u.optString("role", "GROUP_LEAD")) } catch (e: Exception) { UserRole.GROUP_LEAD }
                    )
                }

                val mList = parseEntities(obj.optJSONArray("members")) { m ->
                    MemberEntity(
                        id = m.optLong("id", 0L),
                        groupId = m.optLong("groupId", 1L),
                        subGroup = m.optInt("subGroup", 1),
                        name = m.optString("name", ""),
                        contactAddress = m.optString("contactAddress", ""),
                        telegramContact = m.optString("telegramContact", ""),
                        whatsappContact = m.optString("whatsappContact", ""),
                        otherContact = m.optString("otherContact", ""),
                        joinDate = m.optString("joinDate", ""),
                        status = try { MemberStatus.valueOf(m.optString("status", "ACTIVE")) } catch (e: Exception) { MemberStatus.ACTIVE },
                        notes = m.optString("notes", "")
                    )
                }

                val aList = parseEntities(obj.optJSONArray("attendance")) { a ->
                    AttendanceRecordEntity(
                        id = a.optLong("id", 0L),
                        memberId = a.optLong("memberId", 0L),
                        groupId = a.optLong("groupId", 1L),
                        date = a.optString("date", ""),
                        status = try { AttendanceStatus.valueOf(a.optString("status", "PRESENT")) } catch (e: Exception) { AttendanceStatus.PRESENT },
                        note = a.optString("note", ""),
                        timestamp = a.optLong("timestamp", System.currentTimeMillis())
                    )
                }

                val eList = parseEntities(obj.optJSONArray("equipment")) { eq ->
                    EquipmentEntity(
                        id = eq.optLong("id", 0L),
                        groupId = eq.optLong("groupId", 1L),
                        name = eq.optString("name", ""),
                        totalCount = eq.optInt("totalCount", 0),
                        readyCount = eq.optInt("readyCount", 0),
                        notReadyCount = eq.optInt("notReadyCount", 0),
                        notes = eq.optString("notes", ""),
                        updatedTimestamp = eq.optLong("updatedTimestamp", System.currentTimeMillis())
                    )
                }

                val upList = parseEntities(obj.optJSONArray("updates")) { up ->
                    DailyUpdateEntity(
                        id = up.optLong("id", 0L),
                        groupId = up.optLong("groupId", 0L),
                        groupName = up.optString("groupName", ""),
                        authorName = up.optString("authorName", ""),
                        title = up.optString("title", ""),
                        content = up.optString("content", ""),
                        date = up.optString("date", ""),
                        priority = up.optString("priority", "NORMAL"),
                        timestamp = up.optLong("timestamp", System.currentTimeMillis())
                    )
                }

                val cList = parseEntities(obj.optJSONArray("contacts")) { c ->
                    ExecutiveContactEntity(
                        id = c.optLong("id", 0L),
                        name = c.optString("name", ""),
                        title = c.optString("title", ""),
                        phone = c.optString("phone", ""),
                        telegram = c.optString("telegram", ""),
                        whatsapp = c.optString("whatsapp", ""),
                        radioComms = c.optString("radioComms", ""),
                        other = c.optString("other", ""),
                        notes = c.optString("notes", ""),
                        orderIndex = c.optInt("orderIndex", 0)
                    )
                }

                val rList = parseEntities(obj.optJSONArray("receipts")) { r ->
                    NoticeReceiptEntity(
                        id = r.optLong("id", 0L),
                        noticeId = r.optLong("noticeId", 0L),
                        groupId = r.optLong("groupId", 0L),
                        isDelivered = r.optBoolean("isDelivered", true),
                        deliveredTimestamp = r.optLong("deliveredTimestamp", System.currentTimeMillis()),
                        isAcknowledged = r.optBoolean("isAcknowledged", false),
                        acknowledgedTimestamp = r.optLong("acknowledgedTimestamp", 0L)
                    )
                }

                return SupabasePullData(
                    groups = gList,
                    users = uList,
                    members = mList,
                    attendance = aList,
                    equipment = eList,
                    updates = upList,
                    contacts = cList,
                    receipts = rList
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Fetch consolidated backup failed: ${e.message}")
            return null
        }
    }

    private fun <T> parseEntities(jsonArray: JSONArray?, mapper: (JSONObject) -> T): List<T> {
        if (jsonArray == null) return emptyList()
        val list = mutableListOf<T>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.optJSONObject(i) ?: continue
            try {
                list.add(mapper(obj))
            } catch (e: Exception) {
                Log.w(TAG, "Error mapping item $i", e)
            }
        }
        return list
    }

    private fun <T> pullTableData(
        baseUrl: String,
        key: String,
        tableName: String,
        mapper: (JSONObject) -> T
    ): List<T> {
        val request = Request.Builder()
            .url("$baseUrl/rest/v1/$tableName?select=*")
            .addHeader("apikey", key)
            .addHeader("Authorization", "Bearer $key")
            .get()
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return emptyList()
                val body = response.body?.string() ?: return emptyList()
                val jsonArr = JSONArray(body)
                val list = mutableListOf<T>()
                for (i in 0 until jsonArr.length()) {
                    val obj = jsonArr.getJSONObject(i)
                    list.add(mapper(obj))
                }
                list
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error pulling table $tableName", e)
            emptyList()
        }
    }

    /**
     * Generates complete PostgreSQL DDL SQL script ready to copy-paste into Supabase SQL Editor
     */
    fun generateSupabaseSqlSchema(): String {
        return """
-- =========================================================================
-- Supabase Database Schema Script for Yoqlima Attendance Management
-- Project ID: ${getProjectId()}
-- =========================================================================

-- 1. Groups Table
CREATE TABLE IF NOT EXISTS public.groups (
    id BIGINT PRIMARY KEY,
    name TEXT NOT NULL,
    code TEXT NOT NULL,
    description TEXT DEFAULT '',
    "subLeader1" TEXT DEFAULT '',
    "subLeader1Contact" TEXT DEFAULT '',
    "subLeader1Telegram" TEXT DEFAULT '',
    "subLeader1Whatsapp" TEXT DEFAULT '',
    "subLeader1Other" TEXT DEFAULT '',
    "subLeader2" TEXT DEFAULT '',
    "subLeader2Contact" TEXT DEFAULT '',
    "subLeader2Telegram" TEXT DEFAULT '',
    "subLeader2Whatsapp" TEXT DEFAULT '',
    "subLeader2Other" TEXT DEFAULT '',
    "isSuspended" BOOLEAN DEFAULT FALSE,
    "lastActiveTime" BIGINT DEFAULT 0,
    "dutySubGroup" INT DEFAULT 1,
    "dutySubGroupCustomName" TEXT DEFAULT '',
    "dutyNotes" TEXT DEFAULT ''
);

-- 2. Users Table
CREATE TABLE IF NOT EXISTS public.users (
    id BIGINT PRIMARY KEY,
    "groupId" BIGINT,
    "loginName" TEXT NOT NULL,
    "passwordHash" TEXT NOT NULL,
    "displayName" TEXT NOT NULL,
    role TEXT NOT NULL DEFAULT 'GROUP_LEAD'
);

-- 3. Members Table
CREATE TABLE IF NOT EXISTS public.members (
    id BIGINT PRIMARY KEY,
    "groupId" BIGINT NOT NULL REFERENCES public.groups(id) ON DELETE CASCADE,
    "subGroup" INT DEFAULT 1,
    name TEXT NOT NULL,
    "contactAddress" TEXT DEFAULT '',
    "telegramContact" TEXT DEFAULT '',
    "whatsappContact" TEXT DEFAULT '',
    "otherContact" TEXT DEFAULT '',
    "joinDate" TEXT DEFAULT '',
    status TEXT DEFAULT 'ACTIVE',
    notes TEXT DEFAULT ''
);

-- 4. Attendance Records Table
CREATE TABLE IF NOT EXISTS public.attendance_records (
    id BIGINT PRIMARY KEY,
    "memberId" BIGINT NOT NULL,
    "groupId" BIGINT NOT NULL,
    date TEXT NOT NULL,
    status TEXT NOT NULL,
    note TEXT DEFAULT '',
    timestamp BIGINT DEFAULT 0
);

-- 5. Equipment Records Table
CREATE TABLE IF NOT EXISTS public.equipment_records (
    id BIGINT PRIMARY KEY,
    "groupId" BIGINT NOT NULL,
    name TEXT NOT NULL,
    "totalCount" INT DEFAULT 0,
    "readyCount" INT DEFAULT 0,
    "notReadyCount" INT DEFAULT 0,
    notes TEXT DEFAULT '',
    "updatedTimestamp" BIGINT DEFAULT 0
);

-- 6. Daily Updates / Announcements Table
CREATE TABLE IF NOT EXISTS public.daily_updates (
    id BIGINT PRIMARY KEY,
    "groupId" BIGINT DEFAULT 0,
    "groupName" TEXT DEFAULT '',
    "authorName" TEXT NOT NULL,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    date TEXT NOT NULL,
    priority TEXT DEFAULT 'NORMAL',
    timestamp BIGINT DEFAULT 0
);

-- 7. Executive Leadership Directory Table
CREATE TABLE IF NOT EXISTS public.executive_contacts (
    id BIGINT PRIMARY KEY,
    name TEXT NOT NULL,
    title TEXT NOT NULL,
    phone TEXT DEFAULT '',
    telegram TEXT DEFAULT '',
    whatsapp TEXT DEFAULT '',
    "radioComms" TEXT DEFAULT '',
    other TEXT DEFAULT '',
    notes TEXT DEFAULT '',
    "orderIndex" INT DEFAULT 0
);

-- 8. Notice Receipts Table
CREATE TABLE IF NOT EXISTS public.notice_receipts (
    id BIGINT PRIMARY KEY,
    "noticeId" BIGINT NOT NULL,
    "groupId" BIGINT NOT NULL,
    "isDelivered" BOOLEAN DEFAULT TRUE,
    "deliveredTimestamp" BIGINT DEFAULT 0,
    "isAcknowledged" BOOLEAN DEFAULT FALSE,
    "acknowledgedTimestamp" BIGINT DEFAULT 0
);

-- 9. Consolidated Backup & Sync Table
CREATE TABLE IF NOT EXISTS public.app_cloud_backups (
    id TEXT PRIMARY KEY,
    backup_timestamp BIGINT,
    groups JSONB,
    users JSONB,
    members JSONB,
    attendance JSONB,
    equipment JSONB,
    updates JSONB,
    contacts JSONB,
    receipts JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now())
);

-- Enable Row Level Security (RLS) & Allow anon public access policies
ALTER TABLE public.groups ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.members ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.attendance_records ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.equipment_records ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.daily_updates ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.executive_contacts ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.notice_receipts ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.app_cloud_backups ENABLE ROW LEVEL SECURITY;

DO ${'$'}${'$'}
BEGIN
    DROP POLICY IF EXISTS "Public access groups" ON public.groups;
    CREATE POLICY "Public access groups" ON public.groups FOR ALL USING (true) WITH CHECK (true);

    DROP POLICY IF EXISTS "Public access users" ON public.users;
    CREATE POLICY "Public access users" ON public.users FOR ALL USING (true) WITH CHECK (true);

    DROP POLICY IF EXISTS "Public access members" ON public.members;
    CREATE POLICY "Public access members" ON public.members FOR ALL USING (true) WITH CHECK (true);

    DROP POLICY IF EXISTS "Public access attendance" ON public.attendance_records;
    CREATE POLICY "Public access attendance" ON public.attendance_records FOR ALL USING (true) WITH CHECK (true);

    DROP POLICY IF EXISTS "Public access equipment" ON public.equipment_records;
    CREATE POLICY "Public access equipment" ON public.equipment_records FOR ALL USING (true) WITH CHECK (true);

    DROP POLICY IF EXISTS "Public access daily_updates" ON public.daily_updates;
    CREATE POLICY "Public access daily_updates" ON public.daily_updates FOR ALL USING (true) WITH CHECK (true);

    DROP POLICY IF EXISTS "Public access executive_contacts" ON public.executive_contacts;
    CREATE POLICY "Public access executive_contacts" ON public.executive_contacts FOR ALL USING (true) WITH CHECK (true);

    DROP POLICY IF EXISTS "Public access notice_receipts" ON public.notice_receipts;
    CREATE POLICY "Public access notice_receipts" ON public.notice_receipts FOR ALL USING (true) WITH CHECK (true);

    DROP POLICY IF EXISTS "Public access app_cloud_backups" ON public.app_cloud_backups;
    CREATE POLICY "Public access app_cloud_backups" ON public.app_cloud_backups FOR ALL USING (true) WITH CHECK (true);
END ${'$'}${'$'};
        """.trimIndent()
    }

    private data class RequestResult(val isSuccess: Boolean, val message: String, val code: Int)
}
