package goodtime.training.wod.timer.ui.timer

import android.app.Service
import android.content.Intent
import android.os.IBinder
import goodtime.training.wod.timer.GoodtimeApplication.Companion.context
import goodtime.training.wod.timer.common.notifications.NotificationHelper
import goodtime.training.wod.timer.common.preferences.PreferenceHelper
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance

class TimeService: Service(), KodeinAware{

    companion object {
        var START = "goodtime.action.start"
        var TOGGLE = "goodtime.action.toggle"
        var ABANDON = "goodtime.action.abandon"
        var FINALIZE = "goodtime.action.finalize"
        var FOR_TIME_COMPLETE = "goodtime.action.complete_for_time"
        const val GOODTIME_NOTIFICATION_ID = 42
    }

    override val kodein by closestKodein()
    private val workoutManager: WorkoutManager by instance()
    private val preferenceHelper: PreferenceHelper by instance()
    private lateinit var dndHandler: DNDHandler

    override fun onCreate() {
        super.onCreate()
        dndHandler = DNDHandler(context)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val result = START_STICKY
        when(intent.action) {
            START -> {
                workoutManager.startWorkout()
                startForeground(GOODTIME_NOTIFICATION_ID, NotificationHelper.getNotification(context))
            }
            TOGGLE -> workoutManager.toggleTimer()
            FINALIZE -> {
                if (preferenceHelper.isDndModeEnabled()) dndHandler.toggleDndMode(true)
                workoutManager.finalize()
                onStop()
            }
            ABANDON -> {
                if (preferenceHelper.isDndModeEnabled()) dndHandler.toggleDndMode(false)
                workoutManager.abandonWorkout()
                onStop()
            }
            FOR_TIME_COMPLETE -> workoutManager.onForTimeComplete()
        }
        return result
    }

    private fun onStop() {
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        preferenceHelper.setKilledDuringWorkout(false)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}