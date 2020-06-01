package com.adrcotfas.wod.ui.workout

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.adrcotfas.wod.R
import com.adrcotfas.wod.common.preferences.PrefUtil
import com.adrcotfas.wod.data.model.SessionMinimal
import com.google.gson.Gson
import kotlinx.coroutines.delay
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance

class WorkoutWorker(context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters), KodeinAware {

    override val kodein by closestKodein(context)
    private val preferences : PrefUtil by instance()

    companion object {
        const val CHANNEL_ID = "goodtime.training.CHANNEL_ID"
        const val PROGRESS = "Progress"
    }

    private var sessions = ArrayList<SessionMinimal>()

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager

    override suspend fun doWork(): Result {
        val gson = Gson()
        sessions = gson.fromJson(inputData.getString(KEY_INPUT_SESSION_LIST), ArrayList<SessionMinimal>().javaClass)
        setForeground(createForegroundInfo())
        startSessions()
        return Result.success()
    }

    private suspend fun startSessions() {
        var i = 0
        preferences.setState(true)
        while(preferences.getState()) {
            ++i
            setProgress(workDataOf(PROGRESS to i))
            delay(1000)
            if (i == 4) {
                preferences.setState(false)
            }
        }
    }

    // Creates an instance of ForegroundInfo which can be used to update the
    // ongoing notification.
    private fun createForegroundInfo(): ForegroundInfo {

        val cancelIntent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(id)

        createChannelIfNeeded()

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("Title")
            .setTicker("ticker")
            .setContentText("content text")
            .setSmallIcon(R.drawable.ic_menu_share)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_delete, "cancel", cancelIntent)
            .build()

        return ForegroundInfo(1, notification)
    }

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Goodtime Training",
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
}
