package com.example.util

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.util.Log
import com.example.data.db.AppDatabase
import com.example.data.repository.AttendanceRepository
import com.example.data.supabase.SupabaseSyncService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BackgroundSyncJobService : JobService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d("BackgroundSyncJobService", "Background sync job started")
        serviceScope.launch {
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
                        notifyUnreadAnnouncements(applicationContext, data.updates)
                    }
                }
            } catch (e: Exception) {
                Log.e("BackgroundSyncJobService", "Error in background sync job", e)
            } finally {
                jobFinished(params, false)
            }
        }
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }

    private fun notifyUnreadAnnouncements(context: Context, updates: List<com.example.data.model.DailyUpdateEntity>) {
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
}
