package goodtime.training.wod.timer.ui.timer

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import goodtime.training.wod.timer.common.notifications.NotificationHelper
import goodtime.training.wod.timer.common.preferences.PreferenceHelper
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance

class TimerService: Service(), KodeinAware{

    companion object {
        var START = "goodtime.action.start"
        var TOGGLE = "goodtime.action.toggle"
        var ABANDON = "goodtime.action.abandon"
        var FINALIZE = "goodtime.action.finalize"
        var FOR_TIME_COMPLETE = "goodtime.action.complete_for_time"
        const val GOODTIME_NOTIFICATION_ID = 42

        private const val TAG = "TimerService"
    }

    override val kodein by closestKodein()
    private val workoutManager: WorkoutManager by instance()
    private val preferenceHelper: PreferenceHelper by instance()
    private lateinit var dndHandler: DNDHandler

    override fun onCreate() {
        super.onCreate()
        dndHandler = DNDHandler(this)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val result = START_STICKY
        when(intent.action) {
            START -> {
                Log.i(TAG, "START")
                //TODO: move all of these operations to the ViewModel and leave only notification related work?
                workoutManager.startWorkout()
                startForeground(GOODTIME_NOTIFICATION_ID, NotificationHelper.getNotification(this))
            }
            TOGGLE -> {
                Log.i(TAG, "TOGGLE")
                workoutManager.toggleTimer()
            }
            FINALIZE -> {
                Log.i(TAG, "FINALIZE")
                if (preferenceHelper.isDndModeEnabled()) dndHandler.toggleDndMode(true)
                workoutManager.setInactive()
                onStop()
            }
            ABANDON -> {
                Log.i(TAG, "ABANDON")
                if (preferenceHelper.isDndModeEnabled()) dndHandler.toggleDndMode(false)
                workoutManager.abandonWorkout()
                onStop()
            }
            FOR_TIME_COMPLETE -> {
                Log.i(TAG, "FOR_TIME_COMPLETE")
                workoutManager.onForTimeComplete()
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