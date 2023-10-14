package goodtime.training.wod.timer.ui.timer

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import goodtime.training.wod.timer.common.preferences.PreferenceHelper
import goodtime.training.wod.timer.data.repository.AppRepository
import kotlinx.coroutines.launch

class TimerViewModel(
    private val workoutManager: WorkoutManager,
    private val repository: AppRepository,
    private val preferenceHelper: PreferenceHelper,
    private var dndHandler: DNDHandler
) : ViewModel() {

    fun getTimerState() = workoutManager.timerState
    fun getSecondsUntilFinished() = workoutManager.secondsUntilFinished
    fun getIsResting() = workoutManager.isResting
    fun getCurrentSessionIdx() = workoutManager.currentSessionIdx
    fun getCurrentRoundIdx() = workoutManager.currentRoundIdx
    fun getCurrentSessionTotalRounds() = workoutManager.getCurrentSessionTotalRounds()
    fun getCurrentSessionType() = workoutManager.getCurrentSessionType()
    fun getCurrentSessionDuration() = workoutManager.getCurrentSessionDuration()
    fun getCurrentSessionCountedRounds() = workoutManager.getCurrentSessionCountedRounds()

    fun getSessions() = workoutManager.sessions
    fun getDurations() = workoutManager.durations
    fun getPreparedSession() = workoutManager.sessionToAdd

    fun storeWorkout() {
        // don't save sessions interrupted during the pre-workout countdown
        if (workoutManager.sessionToAdd.actualDuration == 0) return

        Log.i(TAG, "sessionToAdd: ${workoutManager.sessionToAdd}")
        viewModelScope.launch {
            repository.addSession(workoutManager.sessionToAdd)
        }
    }

    fun start(sessionsRaw: String, name: String?) {
        workoutManager.init(sessionsRaw, name)

        if (preferenceHelper.isDndModeEnabled()) dndHandler.toggleDndMode(true)
        workoutManager.notifyGetReady()
        workoutManager.startWorkout()
    }

    fun addRound() {
        workoutManager.addRound()
    }

    fun setInactive() {
        workoutManager.setInactive()
    }

    fun prepareSession() {
        workoutManager.prepareSessionToAdd(true)
    }

    fun toggle() {
        workoutManager.toggleTimer()
    }

    fun finalize() {
        if (preferenceHelper.isDndModeEnabled()) dndHandler.toggleDndMode(false)
        workoutManager.setInactive()
    }

    fun abandon() {
        if (preferenceHelper.isDndModeEnabled()) {
            dndHandler.toggleDndMode(false)
        }

        workoutManager.abandonWorkout()
        if (preferenceHelper.logIncompleteSessions()) {
            storeIncompleteWorkout()
        }
    }

    fun finishForTime() {
        workoutManager.onForTimeComplete()
    }

    private fun storeIncompleteWorkout() {
        workoutManager.prepareSessionToAdd(false)
        storeWorkout()
    }

    companion object {
        private const val TAG = "TimerViewModel"
    }
}

class TimerViewModelFactory(
    private val workoutManager: WorkoutManager,
    private val appRepository: AppRepository,
    private val preferenceHelper: PreferenceHelper,
    private var dndHandler: DNDHandler
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TimerViewModel(workoutManager, appRepository, preferenceHelper, dndHandler) as T
    }
}
