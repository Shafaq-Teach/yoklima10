package com.example.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
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
        Log.d(TAG, "LiveSyncForegroundService onCreate")
        startInForeground()
        startSyncLoop()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "LiveSyncForegroundService onStartCommand")
        startInForeground()
        if (!isSyncRunning) {
            startSyncLoop()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "LiveSyncForegroundService onDestroy, auto-restarting...")
        serviceScope.cancel()
        try {
            val restartIntent = Intent(applicationContext, LiveSyncForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                applicationContext.startForegroundService(restartIntent)
            } else {
                applicationContext.startService(restartIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to auto-restart service", e)
        }
    }

    private fun startInForeground() {
        initServiceChannel()
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val notification = NotificationCompat.Builder(this, SILENT_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setContentTitle("يوقلىما سىستېمىسى قوغدىغۇچىسى")
            .setContentText("جىددىي ئۇقتۇرۇشلار ئۈچۈن تور ئۇلىنىشى ئاكتىپ قوغدىلىۋاتىدۇ")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        try {
            startForeground(FOREGROUND_NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Log.e(TAG, "Failed startForeground", e)
        }
    }

    private fun initServiceChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                SILENT_CHANNEL_ID,
                "يوقلىما ئۇلىنىش قوغدىغۇچىسى",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "ئەپ تاقاق ۋاقتىدىمۇ جىددىي ئۇقتۇرۇشلارنى دەرھال قوبۇل قىلىش ئۈچۈن ئىشلىتىلىدۇ"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_SECRET
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    private fun startSyncLoop() {
        isSyncRunning = true
        serviceScope.launch {
            Log.d(TAG, "Live sync loop started (continuous 6s poll)")
            while (isActive) {
                try {
                    val db = AppDatabase.getInstance(applicationContext)
                    val repository = AttendanceRepository(db.attendanceDao())
                    val result = SupabaseSyncService.pullAllData()
                    result.onSuccess { data ->
                        val total = data.groups.size + data.users.size + data.members.size +
                                data.attendance.size + data.equipment.size + data.updates.size +
                                data.contacts.size + data.receipts.size + data.deviceSessions.size
                        if (total > 0) {
                            repository.restoreFromSupabaseData(data)
                            processIncomingNotices(applicationContext, data.updates)
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Poll error: ${e.message}")
                }
                delay(6_000) // Poll every 6 seconds in background
            }
        }
    }

    private fun processIncomingNotices(context: Context, updates: List<com.example.data.model.DailyUpdateEntity>) {
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
        private const val SILENT_CHANNEL_ID = "live_sync_silent_channel"
        private const val FOREGROUND_NOTIFICATION_ID = 8821

        fun startService(context: Context) {
            try {
                val intent = Intent(context, LiveSyncForegroundService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
                Log.d(TAG, "LiveSyncForegroundService started successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start LiveSyncForegroundService", e)
            }
        }
    }
}
