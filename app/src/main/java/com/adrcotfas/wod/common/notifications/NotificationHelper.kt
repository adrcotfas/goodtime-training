package com.adrcotfas.wod.common.notifications

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
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
            return NavDeepLinkBuilder(context)
                .setGraph(R.navigation.mobile_navigation)
                .setDestination(R.id.nav_workout)
                .createPendingIntent()
        }

        @TargetApi(26)
        private fun NotificationManager.createNotificationChannel() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    TRAINING_CHANNEL_ID, "Goodtime Training",
                    NotificationManager.IMPORTANCE_LOW
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