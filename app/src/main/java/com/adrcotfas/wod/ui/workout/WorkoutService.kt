package com.adrcotfas.wod.ui.workout

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.adrcotfas.wod.common.notifications.NotificationHelper
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance

class WorkoutService : Service(), KodeinAware {

    override val kodein by closestKodein()
    private val notificationHelper : NotificationHelper by instance()

    @Synchronized
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        // start pre-workout countdown
        startForeground(NotificationHelper.TRAINING_NOTIFICATION_ID, notificationHelper.builder.build())

        notificationHelper.setText(true,0, "Get ready")
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = null
}