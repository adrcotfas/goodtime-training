package goodtime.training.wod.timer.ui.timer

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import goodtime.training.wod.timer.common.notifications.NotificationHelper
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance

class TimerService: Service(), KodeinAware{

    companion object {
        var START = "goodtime.action.start"
        var FINALIZE = "goodtime.action.finalize"
        const val GOODTIME_NOTIFICATION_ID = 42

        private const val TAG = "TimerService"
    }

    override val kodein by closestKodein()
    private val workoutManager: WorkoutManager by instance()

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val result = START_STICKY
        when(intent.action) {
            START -> {
                Log.i(TAG, "START")
                startForeground(GOODTIME_NOTIFICATION_ID, NotificationHelper.getNotification(this))
            }
            FINALIZE -> {
                Log.i(TAG, "FINALIZE")
                onStop()
            }
        }
        return result
    }

    private fun onStop() {
        stopForeground(true)
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