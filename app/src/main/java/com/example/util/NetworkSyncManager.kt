package com.example.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.example.data.repository.AttendanceRepository
import com.example.data.supabase.SupabaseSyncService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object NetworkSyncManager {
    private const val TAG = "NetworkSyncManager"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isRegistered = false

    fun startListening(context: Context, repository: AttendanceRepository) {
        if (isRegistered) return
        val appContext = context.applicationContext
        val connectivityManager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                Log.d(TAG, "Internet connection available! Auto-triggering background sync...")
                triggerAutoSync(appContext, repository)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                Log.d(TAG, "Internet connection lost.")
            }
        }

        try {
            connectivityManager.registerNetworkCallback(request, callback)
            isRegistered = true
            // Also trigger once on launch if currently connected
            triggerAutoSync(appContext, repository)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register network callback", e)
        }
    }

    fun triggerAutoSync(context: Context, repository: AttendanceRepository) {
        scope.launch {
            try {
                val result = SupabaseSyncService.pullAllData()
                result.onSuccess { data ->
                    val total = data.groups.size + data.users.size + data.members.size +
                            data.attendance.size + data.equipment.size + data.updates.size +
                            data.contacts.size + data.receipts.size + data.deviceSessions.size
                    if (total > 0) {
                        repository.restoreFromSupabaseData(data)

                        // Check unread updates and show notifications with badge
                        checkAndNotifyUnread(context, data.updates)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Auto sync failed", e)
            }
        }
    }

    private fun checkAndNotifyUnread(context: Context, updates: List<com.example.data.model.DailyUpdateEntity>) {
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
