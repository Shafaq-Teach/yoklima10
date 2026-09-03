package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import com.example.data.db.AppDatabase
import com.example.data.repository.AttendanceRepository
import com.example.data.supabase.SupabaseSyncService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BootAndNetworkReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("BootAndNetworkReceiver", "Received broadcast: $action")

        schedulePeriodicJob(context)
        scheduleSyncAlarm(context)
        LiveSyncForegroundService.startService(context.applicationContext)

        if (isNetworkConnected(context)) {
            val pendingResult = goAsync()
            scope.launch {
                try {
                    val db = AppDatabase.getInstance(context.applicationContext)
                    val repository = AttendanceRepository(db.attendanceDao())
                    val result = SupabaseSyncService.pullAllData()
                    result.onSuccess { data ->
                        val total = data.groups.size + data.users.size + data.members.size +
                                data.attendance.size + data.equipment.size + data.updates.size +
                                data.contacts.size + data.receipts.size + data.deviceSessions.size
                        if (total > 0) {
                            repository.restoreFromSupabaseData(data)
                            notifyUnread(context.applicationContext, data.updates)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("BootAndNetworkReceiver", "Sync failed on broadcast", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    private fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val net = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(net) ?: return false
            return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            @Suppress("DEPRECATION")
            val info = cm.activeNetworkInfo
            @Suppress("DEPRECATION")
            return info != null && info.isConnected
        }
    }

    private fun notifyUnread(context: Context, updates: List<com.example.data.model.DailyUpdateEntity>) {
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

    companion object {
        const val JOB_ID = 9922
        const val ALARM_ACTION = "com.example.ACTION_SYNC_ALARM"

        fun schedulePeriodicJob(context: Context) {
            try {
                val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as? JobScheduler ?: return
                val component = ComponentName(context, BackgroundSyncJobService::class.java)
                val builder = JobInfo.Builder(JOB_ID, component)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setPeriodic(15 * 60 * 1000)
                    .setPersisted(true)

                jobScheduler.schedule(builder.build())
                Log.d("BootAndNetworkReceiver", "Scheduled persistent background sync job ($JOB_ID)")
            } catch (e: Exception) {
                Log.e("BootAndNetworkReceiver", "Failed to schedule background job", e)
            }
        }

        fun scheduleSyncAlarm(context: Context) {
            try {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
                val intent = Intent(context, BootAndNetworkReceiver::class.java).apply {
                    action = ALARM_ACTION
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    9933,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
                )
                val triggerAt = System.currentTimeMillis() + 60_000L // 1 minute
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                }
            } catch (e: Exception) {
                Log.w("BootAndNetworkReceiver", "Alarm schedule failed: ${e.message}")
            }
        }
    }
}
