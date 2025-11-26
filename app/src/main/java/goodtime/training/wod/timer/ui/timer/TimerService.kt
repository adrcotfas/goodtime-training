package goodtime.training.wod.timer.ui.timer

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
import android.os.Build
import android.os.IBinder
import android.util.Log
import goodtime.training.wod.timer.common.notifications.NotificationHelper
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance

class TimerService: Service(), DIAware{

    companion object {
        var START = "goodtime.action.start"
        var FINALIZE = "goodtime.action.finalize"
        const val GOODTIME_NOTIFICATION_ID = 42

        private const val TAG = "TimerService"
    }

    override val di by closestDI()
    private val workoutManager: WorkoutManager by instance()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null || intent.action == null) {
            Log.i(TAG, "onStartCommand: intent or action is null")
            return START_NOT_STICKY
        }
        val result = START_STICKY
        when(intent.action) {
            START -> {
                Log.i(TAG, "START")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        startForeground(GOODTIME_NOTIFICATION_ID, NotificationHelper.getNotification(this), FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
                } else {
                    startForeground(GOODTIME_NOTIFICATION_ID, NotificationHelper.getNotification(this))
                }
            }
            FINALIZE -> {
                Log.i(TAG, "FINALIZE")
                onStop()
            }
        }
        return result
    }

    private fun onStop() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    /**
     * When the user clears the app from the recent apps list, stop the ongoing workout
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        workoutManager.stopTimer()
        onStop()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}