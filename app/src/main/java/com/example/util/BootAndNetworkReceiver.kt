package com.example.util

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
        val unreadCount = LocalReadNoticeTracker.getUnreadCount(context, updates)
        updates.forEach { update ->
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
    }
}
