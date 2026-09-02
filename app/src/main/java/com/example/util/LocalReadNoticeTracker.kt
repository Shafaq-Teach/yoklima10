package com.example.util

import android.content.Context
import com.example.data.model.DailyUpdateEntity

object LocalReadNoticeTracker {
    private const val PREFS_NAME = "local_read_notice_prefs"
    private const val KEY_READ_IDS = "read_notice_ids"
    private const val KEY_NOTIFIED_IDS = "notified_notice_ids"

    fun isNoticeRead(context: Context, noticeId: Long): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val readIds = prefs.getStringSet(KEY_READ_IDS, emptySet()) ?: emptySet()
        return readIds.contains(noticeId.toString())
    }

    fun markNoticeAsRead(context: Context, noticeId: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentSet = prefs.getStringSet(KEY_READ_IDS, emptySet())?.toMutableSet() ?: mutableSetOf()
        currentSet.add(noticeId.toString())
        prefs.edit().putStringSet(KEY_READ_IDS, currentSet).apply()

        val unread = getUnreadCount(context, emptyList())
        if (unread <= 0) {
            AppNotificationManager.clearAllBadgeNotifications(context)
        }
    }

    fun markAllNoticesAsRead(context: Context, noticeIds: List<Long>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentSet = prefs.getStringSet(KEY_READ_IDS, emptySet())?.toMutableSet() ?: mutableSetOf()
        noticeIds.forEach { currentSet.add(it.toString()) }
        prefs.edit().putStringSet(KEY_READ_IDS, currentSet).apply()

        AppNotificationManager.clearAllBadgeNotifications(context)
    }

    fun getUnreadCount(context: Context, allNotices: List<DailyUpdateEntity>): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val readIds = prefs.getStringSet(KEY_READ_IDS, emptySet()) ?: emptySet()
        if (allNotices.isEmpty()) return 0
        return allNotices.count { !readIds.contains(it.id.toString()) }
    }

    fun hasBeenNotified(context: Context, noticeId: Long): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val notifiedIds = prefs.getStringSet(KEY_NOTIFIED_IDS, emptySet()) ?: emptySet()
        return notifiedIds.contains(noticeId.toString())
    }

    fun markAsNotified(context: Context, noticeId: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentSet = prefs.getStringSet(KEY_NOTIFIED_IDS, emptySet())?.toMutableSet() ?: mutableSetOf()
        currentSet.add(noticeId.toString())
        prefs.edit().putStringSet(KEY_NOTIFIED_IDS, currentSet).apply()
    }
}
