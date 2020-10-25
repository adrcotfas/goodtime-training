package com.adrcotfas.wod.common.notifications

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.adrcotfas.wod.MainActivity
import com.adrcotfas.wod.R

class NotificationHelper(context: Context) : ContextWrapper(context) {

    companion object{
        private const val TRAINING_CHANNEL_ID = "goodtime.training.notification"
        private const val TRAINING_NOTIFICATION_ID = 42

        fun showNotification(context: Context) {
            val builder = getBasicNotificationBuilder(context)
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel()
            manager.notify(TRAINING_NOTIFICATION_ID, builder.build())
        }

        fun hideNotification(context: Context) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel()
            manager.cancel(TRAINING_NOTIFICATION_ID)
        }

        private fun getBasicNotificationBuilder(context: Context) : NotificationCompat.Builder {
            return NotificationCompat.Builder(context, TRAINING_CHANNEL_ID)
                .setContentText("Training in progress")
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_menu_share)
                .setOngoing(true)
                .setAutoCancel(true)
                .setShowWhen(false)
                .setContentIntent(getPendingIntentWithStack(context))
        }

        private fun getPendingIntentWithStack(context: Context): PendingIntent {
            val notificationIntent = Intent(context, MainActivity::class.java)
            notificationIntent.action = Intent.ACTION_MAIN
            notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            return PendingIntent.getActivity(
                context, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        @TargetApi(26)
        private fun NotificationManager.createNotificationChannel() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    TRAINING_CHANNEL_ID, "Goodtime Training",
                    NotificationManager.IMPORTANCE_HIGH
                )
                channel.apply {
                    setBypassDnd(true)
                    setShowBadge(true)
                    setSound(null, null)
                }
                this.createNotificationChannel(channel)
            }
        }
    }
}