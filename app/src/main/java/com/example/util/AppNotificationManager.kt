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
            val channel = NotificationChannel(
                CHANNEL_ID_HIGH_PRIORITY,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableLights(true)
                lightColor = Color.CYAN
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 350, 200, 350, 200, 400)
                setSound(soundUri, null)
                setShowBadge(true)
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
        groupTargetName: String = ""
    ) {
        initNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
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

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_HIGH_PRIORITY)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(displayHeader)
            .setContentText(message)
            .setSubText(subText)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$message\n\n$subText"))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 350, 200, 350, 200, 400))
            .setLights(Color.CYAN, 1000, 500)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setNumber(1)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.notify(notificationId, builder.build())
    }
}
