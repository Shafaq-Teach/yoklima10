package com.example.util

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import com.example.data.db.AppDatabase
import com.example.data.repository.AttendanceRepository
import com.example.data.supabase.SupabaseSyncService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class LiveSyncForegroundService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isSyncRunning = false

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "LiveSyncForegroundService onCreate (Silent background mode)")
        startSyncLoop()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "LiveSyncForegroundService onStartCommand (Silent background mode)")
        if (!isSyncRunning) {
            startSyncLoop()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "LiveSyncForegroundService onDestroy, restarting...")
        serviceScope.cancel()
        try {
            val restartIntent = Intent(applicationContext, LiveSyncForegroundService::class.java)
            applicationContext.startService(restartIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to auto-restart service", e)
        }
    }

    private fun startSyncLoop() {
        isSyncRunning = true
        serviceScope.launch {
            Log.d(TAG, "High-speed silent notice sync loop started")
            var cycleCount = 0
            while (isActive) {
                try {
                    val db = AppDatabase.getInstance(applicationContext)
                    val repository = AttendanceRepository(db.attendanceDao())

                    // 1. Ultra-fast check for urgent updates every 2.5s
                    val updates = SupabaseSyncService.pullFastDailyUpdates()
                    if (updates.isNotEmpty()) {
                        processIncomingNotices(applicationContext, updates, repository)
                    }

                    // 2. Full background sync every 25s
                    cycleCount++
                    if (cycleCount >= 10) {
                        cycleCount = 0
                        val fullResult = SupabaseSyncService.pullAllData()
                        fullResult.onSuccess { data ->
                            repository.restoreFromSupabaseData(data)
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Sync loop error: ${e.message}")
                }
                delay(2500) // High speed check every 2.5 seconds
            }
        }
    }

    private suspend fun processIncomingNotices(
        context: Context,
        updates: List<com.example.data.model.DailyUpdateEntity>,
        repository: AttendanceRepository
    ) {
        if (updates.isEmpty()) return
        val prefs = context.getSharedPreferences("user_session_prefs", Context.MODE_PRIVATE)
        val savedRole = prefs.getString("role", null)
        val savedGroupId = prefs.getLong("groupId", 0L)

        val relevantUpdates = updates.filter { update ->
            when (savedRole) {
                "ADMIN" -> true
                "GROUP_LEAD" -> update.groupId == 0L || update.groupId == savedGroupId
                else -> update.groupId == 0L || update.groupId == savedGroupId || savedGroupId == 0L
            }
        }

        val unreadCount = LocalReadNoticeTracker.getUnreadCount(context, relevantUpdates)
        relevantUpdates.forEach { update ->
            val isRead = LocalReadNoticeTracker.isNoticeRead(context, update.id)
            val alreadyNotified = LocalReadNoticeTracker.hasBeenNotified(context, update.id)
            if (!isRead && !alreadyNotified) {
                try {
                    repository.restoreFromSupabaseData(
                        com.example.data.supabase.SupabasePullData(updates = listOf(update))
                    )
                } catch (e: Exception) {
                    // ignore
                }

                wakeUpScreen(context)
                AppNotificationManager.showUrgentNotification(
                    context = context,
                    notificationId = update.id.toInt(),
                    title = update.title,
                    message = update.content,
                    author = update.authorName,
                    groupTargetName = update.groupName,
                    unreadCount = unreadCount
                )
                LocalReadNoticeTracker.markAsNotified(context, update.id)
            }
        }
    }

    private fun wakeUpScreen(context: Context) {
        try {
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return
            @Suppress("DEPRECATION")
            val wakeLock = pm.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                "Yoqlima:UrgentNoticeWakeLock"
            )
            wakeLock.acquire(3000L) // Turn on screen for 3 seconds
        } catch (e: Exception) {
            Log.w(TAG, "WakeLock acquire failed: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "LiveSyncForeground"

        fun startService(context: Context) {
            try {
                val intent = Intent(context, LiveSyncForegroundService::class.java)
                context.startService(intent)
                Log.d(TAG, "LiveSyncForegroundService started silently")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start LiveSyncForegroundService", e)
            }
        }
    }
}
