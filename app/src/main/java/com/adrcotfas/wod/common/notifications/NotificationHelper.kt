package com.adrcotfas.wod.common.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.adrcotfas.wod.MainActivity
import com.adrcotfas.wod.R

class NotificationHelper(context: Context) : ContextWrapper(context) {

    companion object{
        const val TRAINING_NOTIFICATION_ID = "goodtime.training.notification"
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager

    val builder: NotificationCompat.Builder

    init {
        createChannelIfNeeded()

        builder = NotificationCompat.Builder(applicationContext, TRAINING_NOTIFICATION_ID)
            .setContentTitle("Title")
            .setTicker("ticker")
            .setContentText("content text")
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(R.drawable.ic_menu_share)
            .setOngoing(true)
            .setShowWhen(false)
            .setContentIntent(getPendingIntentWithStack(context, MainActivity::class.java))
    }

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                TRAINING_NOTIFICATION_ID, "Goodtime Training",
                NotificationManager.IMPORTANCE_LOW
            )
            channel.apply {
                setBypassDnd(true)
                setShowBadge(true)
                setSound(null, null)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun <T> getPendingIntentWithStack(context: Context, javaClass: Class<T>): PendingIntent {
        val resultIntent = Intent(context, javaClass)
        resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(javaClass)
        stackBuilder.addNextIntent(resultIntent)

        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}