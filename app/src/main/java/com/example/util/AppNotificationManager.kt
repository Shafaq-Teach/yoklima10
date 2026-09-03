package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

object AppNotificationManager {
    private const val CHANNEL_ID_HIGH_PRIORITY = "announcements_urgent_channel"
    private const val CHANNEL_NAME = "جىددىي ئۇقتۇرۇش ۋە خەۋەرلەر"
    private const val CHANNEL_DESC = "ھەر قايسى بايراق ۋە قىسىملارغا يوللانغان يېڭى ئۇچۇرلارنىڭ سىگنالى"

    fun initNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = android.media.AudioAttributes.Builder()
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID_HIGH_PRIORITY,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 450, 200, 450, 200, 450)
                setSound(soundUri, audioAttributes)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                setBypassDnd(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun showUrgentNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
        author: String = "",
        groupTargetName: String = "",
        unreadCount: Int = 1
    ) {
        initNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val displayHeader = if (groupTargetName.isNotBlank()) "[$groupTargetName] $title" else title
        val subText = if (author.isNotBlank()) "يوللىغۇچى: $author" else "يېڭى مۇھىم ئۇچۇر"
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Public fallback version for locked / secure screens
        val publicNotification = NotificationCompat.Builder(context, CHANNEL_ID_HIGH_PRIORITY)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("📢 يېڭى جىددىي ئۇقتۇرۇش!")
            .setContentText(if (groupTargetName.isNotBlank()) "[$groupTargetName] يېڭى مۇھىم ئۇچۇر كەلدى" else "يېڭى مۇھىم ئۇقتۇرۇش بار")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_HIGH_PRIORITY)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(displayHeader)
            .setContentText(message)
            .setSubText(subText)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$message\n\n$subText"))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(pendingIntent, true) // Triggers heads-up pop-up banner on screen!
            .setSound(soundUri)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVibrate(longArrayOf(0, 450, 200, 450, 200, 450))
            .setLights(Color.RED, 1000, 500)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            .setNumber(unreadCount)
            .setTicker("📢 $displayHeader: $message")
            .setPublicVersion(publicNotification)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.notify(notificationId, builder.build())
    }

    fun clearNotification(context: Context, notificationId: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.cancel(notificationId)
    }

    fun clearAllBadgeNotifications(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.cancelAll()
    }
}
