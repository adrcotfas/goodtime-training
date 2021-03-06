package goodtime.training.wod.timer.ui.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import goodtime.training.wod.timer.common.preferences.PreferenceHelper
import goodtime.training.wod.timer.data.model.Session
import goodtime.training.wod.timer.data.repository.AppRepository
import kotlinx.coroutines.launch

class TimerViewModel(
    private val workoutManager: WorkoutManager,
    private val repository: AppRepository,
    private val preferenceHelper: PreferenceHelper
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

    fun finalize() {
        // don't save sessions interrupted during the pre-workout countdown
        if (workoutManager.sessionToAdd.actualDuration == 0) return

        viewModelScope.launch {
            repository.addSession(workoutManager.sessionToAdd)
        }
    }

    fun addRound() {
        workoutManager.addRound()
    }

    fun storeIncompleteWorkout() {
        workoutManager.prepareSessionToAdd(false)
        finalize()
    }

    fun init(sessionsRaw: String, name: String?) {
        workoutManager.init(sessionsRaw, name)
    }

    override fun onCleared() {
        preferenceHelper.setKilledDuringWorkout(true)
        super.onCleared()
    }

    fun setInactive() {
        workoutManager.setInactive()
    }

    fun prepareSession() {
        workoutManager.prepareSessionToAdd(true)
    }
}
