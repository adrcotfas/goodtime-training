package goodtime.training.wod.timer.ui.timer

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import goodtime.training.wod.timer.common.timers.CountDownTimer
import goodtime.training.wod.timer.data.model.Session.Companion.constructSession
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.data.model.SessionType
import goodtime.training.wod.timer.data.model.TypeConverter
import goodtime.training.wod.timer.data.repository.AppRepository
import goodtime.training.wod.timer.data.workout.TimerState

class TimerViewModel(
        private val notifier: TimerNotificationHelper,
        private val repository: AppRepository) : ViewModel() {

    val timerState = MutableLiveData(TimerState.INACTIVE)
    var sessions = ArrayList<SessionSkeleton>()

    /**
     * Holds the counted rounds in seconds elapsed
     */
    var countedRounds = ArrayList<ArrayList<Int>>(0)

    // store the working time for each session
    var durations = ArrayList<Int>()
    lateinit var timer : CountDownTimer

    /**
     * Indicates the index of a session part of a custom workout
     * For a regular workout this will be maximum 1; 0 for the pre-workout countdown and 1 for the actual workout
     */
    val currentSessionIdx = MutableLiveData(0)

    /**
     * For EMOM and Tabata workouts this will indicate the current round
     */
    val currentRoundIdx = MutableLiveData<Int>()

    val secondsUntilFinished = MutableLiveData<Int>()

    /**
     * used for Tabata workouts to signal the rest
     */
    val isResting = MutableLiveData(false)

    fun getTotalRounds() : Int  {
        val sessionId = currentSessionIdx.value!!
        return sessions[sessionId].numRounds
    }

    fun getCurrentSessionType() : SessionType = sessions[currentSessionIdx.value!!].type
    fun getCurrentSessionDuration() : Int {
        val session = sessions[currentSessionIdx.value!!]
        return if (isResting.value!! && session.type != SessionType.REST)
            session.breakDuration
        else
            session.duration
    }

    fun init(sessionsRaw: String) {
        sessions = TypeConverter.toSessionSkeletons(sessionsRaw)
        durations.clear()
        for (index in 0 until sessions.size) {
            durations.add(0)
        }
        countedRounds = ArrayList()
        for(i in 0 until  sessions.size) {
            countedRounds.add(ArrayList(0))
        }

        currentRoundIdx.value = 0
        currentSessionIdx.value = 0
        secondsUntilFinished.value = sessions[0].duration
        isResting.value = false
    }

    fun startWorkout() {
        timerState.value = TimerState.ACTIVE
        val index = currentSessionIdx.value!!
        val session = sessions[index]

        if (session.type == SessionType.REST) {
            isResting.value = true
        }

        // will be used if the timer was paused
        val prev = secondsUntilFinished.value?.toLong()

        // will be used when starting fresh
        val originalSeconds =
            if (isResting.value!! && session.type != SessionType.REST)
                session.breakDuration.toLong()
            else
                session.duration.toLong()

        val secondsToUse =
            if (prev != null && prev != 0L ) prev
            else originalSeconds

        timer = CountDownTimer(secondsToUse, originalSeconds, object : CountDownTimer.Listener {
            override fun onTick(seconds: Int) { handleTimerTick(seconds) }
            override fun onFinishSet() { handleFinishTimer()}
            override fun onHalfwayThere() {
                if (isResting.value == false) {
                    notifier.notifyMiddleOfTraining()
                }
            }
        })
        timer.start()

    }

    fun toggleTimer() {
        if (timerState.value == TimerState.ACTIVE) {
            timer.cancel()
            timerState.value = TimerState.PAUSED
            notifier.stop()
        } else if (timerState.value == TimerState.PAUSED) {
            startWorkout()
        }
    }

    fun stopTimer() {
        timer.cancel()
        notifier.stop()
        timerState.value = TimerState.INACTIVE
        //TODO: do we want to save unfinished sessions?
        //val index = currentSessionIdx.value!!
//        val currentSession = sessions[index]
//        val activeSeconds =
//            (index + 1) * currentSession.duration +
//                    (secondsUntilFinished.value?.toInt() ?: 0)

//if (currentSession.type != SessionType.BREAK) {
//            repository.addSession(constructIncompleteSession(
//                currentSession.type,
//                activeSeconds,
//                System.currentTimeMillis(),
//                countedRounds.value!!))
//        }
    }

    /**
     * Valid for FOR_TIME sessions
     */
    fun finishCurrentSession() {
        timer.cancel()
        notifier.stop()
        timerState.value = TimerState.INACTIVE
        // when the timer is finished, save the last round but only if the user used the round counter
        if (countedRounds[currentSessionIdx.value!!].isNotEmpty()) {
            addRound()
        }
        handleFinishTimer()
    }

    private fun isLastSession() : Boolean {
        return sessions.size == currentSessionIdx.value!! + 1
    }

    private fun isLastRound() : Boolean {
        return sessions[currentSessionIdx.value!!].numRounds == currentRoundIdx.value!! + 1
    }

    private fun handleTimerTick(seconds : Int) {
        secondsUntilFinished.value = seconds
        if (seconds == 2) {
            //TODO: handle bug when pausing exactly here -> sound should not restart
            notifier.notifyCountDown()
        }
    }

    private fun updateDurations(sessionType: SessionType, index: Int) {
        when(sessionType) {
            SessionType.AMRAP, SessionType.REST -> {
                durations[index] = sessions[index].duration
            }
            SessionType.FOR_TIME -> {
                durations[index] = sessions[index].duration - secondsUntilFinished.value!!
            }
            SessionType.EMOM -> {
                durations[index] = sessions[index].duration * sessions[index].numRounds
            }
            SessionType.HIIT -> {
                durations[index] = sessions[index].duration * sessions[index].numRounds +
                        sessions[index].breakDuration * (sessions[index].numRounds)
            }
        }
    }

    //TODO: clean-up this mess
    private fun handleFinishTimer() {
        val type = getCurrentSessionType()
        var index = currentSessionIdx.value!!
        updateDurations(type, index)
        when (type) {
            SessionType.AMRAP, SessionType.FOR_TIME, SessionType.REST -> {
                if (type == SessionType.REST) {
                    // reset resting value here
                    isResting.value = false
                }
                if (type != SessionType.REST) {
                    repository.addSession(
                        constructSession(
                            sessions[index],
                            System.currentTimeMillis(),
                            countedRounds[index],
                            durations[index]))
                }
                if (isLastSession()) {
                    timerState.value = TimerState.FINISHED
                    notifier.notifyTrainingComplete()
                } else {
                    ++index
                    currentSessionIdx.value = index
                    secondsUntilFinished.value = sessions[index].duration

                    startWorkout()

                    if (sessions[index].type == SessionType.REST) {
                        notifier.notifyRest()
                    } else {
                        notifier.notifyStart()
                    }
                }
            }
            SessionType.EMOM -> {
                if (isLastRound()) {
                    repository.addSession(constructSession(sessions[index], System.currentTimeMillis()))
                    if (isLastSession()) {
                        timerState.value = TimerState.FINISHED
                        notifier.notifyTrainingComplete()
                    } else {
                        currentRoundIdx.value = 0
                        ++index
                        currentSessionIdx.value = index
                        secondsUntilFinished.value = sessions[index].duration

                        startWorkout()

                        if (sessions[index].type == SessionType.REST) {
                            notifier.notifyRest()
                        } else {
                            notifier.notifyStart()
                        }
                    }
                } else {
                    currentRoundIdx.value = currentRoundIdx.value!! + 1
                    secondsUntilFinished.value = sessions[currentSessionIdx.value!!].duration
                    startWorkout()

                    if (isLastRound()) {
                        notifier.notifyLastRound()
                    } else {
                        notifier.notifyStart()
                    }
                }
            }
            SessionType.HIIT -> {
                // if this is the last round but not in the final session, take the break
                if ((isLastRound() && !isLastSession() && isResting.value!!)
                    // if this is the final session and final work round, skip the break
                    || (isLastRound() && isLastSession() && !isResting.value!!)) {
                    repository.addSession(constructSession(sessions[index], System.currentTimeMillis()))
                    if (isLastSession()) {
                        timerState.value = TimerState.FINISHED

                        notifier.notifyTrainingComplete()
                    } else {
                        currentRoundIdx.value = 0
                        ++index
                        currentSessionIdx.value = index
                        secondsUntilFinished.value = sessions[index].duration
                        startWorkout()

                        if (sessions[index].type == SessionType.REST) {
                            notifier.notifyRest()
                        } else {
                            notifier.notifyStart()
                        }
                    }
                } else {
                    isResting.value = !isResting.value!!
                    val isRestingVal: Boolean = isResting.value!!
                    if (!isRestingVal) {
                        currentRoundIdx.value = currentRoundIdx.value!! + 1
                    }
                    secondsUntilFinished.value =
                        if (isRestingVal)
                            sessions[index].breakDuration
                        else
                            sessions[index].duration
                    startWorkout()

                    if (isLastRound()) {
                        notifier.notifyLastRound()
                    } else {
                        if (isRestingVal) {
                            notifier.notifyRest()
                        } else {
                            notifier.notifyStart()
                        }
                    }
                }
            }
        }
    }

    fun addRound() {
        val totalSeconds = getCurrentSessionDuration()
        if (countedRounds.isEmpty()) {
            countedRounds[currentSessionIdx.value!!].add(totalSeconds - secondsUntilFinished.value!!)
        } else {
            val elapsed = totalSeconds - secondsUntilFinished.value!!
            countedRounds[currentSessionIdx.value!!].add(elapsed)
        }
    }

    fun getRounds(idx: Int) : ArrayList<Int> {
        val result = ArrayList<Int>(0)
        for (i in 0 until countedRounds[idx].size) {
            if (i == 0) {
                result.add(countedRounds[idx][i])
            } else {
                result.add(countedRounds[idx][i] - countedRounds[idx][i - 1])
            }
        }
        return result
    }

    fun getNumCurrentSessionRounds(): Int {
        return countedRounds[currentSessionIdx.value!!].size
    }
}
