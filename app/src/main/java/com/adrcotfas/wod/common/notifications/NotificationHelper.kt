package com.adrcotfas.wod.common.notifications

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.core.app.NotificationCompat
import com.adrcotfas.wod.GoodtimeApplication
import com.adrcotfas.wod.R
import com.adrcotfas.wod.common.TimerUtils

class NotificationHelper(context: Context) : ContextWrapper(context) {

    companion object{
        const val TRAINING_NOTIFICATION = "goodtime.training.notification"
        const val TRAINING_NOTIFICATION_ID = 42
    }

    private val manager: NotificationManager = (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
    val builder: NotificationCompat.Builder

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            initChannel()

        builder = NotificationCompat.Builder(this, TRAINING_NOTIFICATION)
            .setSmallIcon(R.drawable.ic_menu_send)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(GoodtimeApplication.getNavigationIntent(context, R.id.workoutFragment))
            .setShowWhen(false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun initChannel() {
        val c = NotificationChannel(
            TRAINING_NOTIFICATION, "Test test",
            NotificationManager.IMPORTANCE_LOW
        )
        c.setBypassDnd(true)
        c.setShowBadge(true)
        c.setSound(null, null)
        manager.createNotificationChannel(c)
    }

    fun setText(hide: Boolean, seconds: Int, title: String) {
        manager.notify(
            TRAINING_NOTIFICATION_ID,
            builder
                .setOnlyAlertOnce(true)
                .setContentText(if (hide) "" else TimerUtils.secondsToTimerFormat(seconds))
                .setContentTitle(title)
                .build()
        )
    }
}