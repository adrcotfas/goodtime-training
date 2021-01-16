package goodtime.training.wod.timer.ui.timer

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import goodtime.training.wod.timer.common.StringUtils
import goodtime.training.wod.timer.common.StringUtils.Companion.secondsToNiceFormat
import goodtime.training.wod.timer.common.preferences.PreferenceHelper

import goodtime.training.wod.timer.common.timers.CountDownTimer
import goodtime.training.wod.timer.data.model.Session
import goodtime.training.wod.timer.data.model.Session.Companion.prepareSessionToAdd
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.data.model.SessionType
import goodtime.training.wod.timer.data.model.TypeConverter
import goodtime.training.wod.timer.data.repository.AppRepository
import goodtime.training.wod.timer.data.workout.TimerState

class TimerViewModel(
    private val notifier: TimerNotificationHelper,
    private val preferenceHelper: PreferenceHelper,
    private val repository: AppRepository
) : ViewModel() {

    val timerState = MutableLiveData(TimerState.INACTIVE)
    var sessions = ArrayList<SessionSkeleton>()

    var sessionToAdd = Session()

    /**
     * Holds the counted rounds in seconds elapsed
     */
    var countedRounds = ArrayList<Int>(0)

    // store the working time for each session
    var durations = ArrayList<Int>()
    lateinit var timer: CountDownTimer

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

    var isCustomWorkout = false

    fun getTotalRounds(): Int {
        val sessionId = currentSessionIdx.value!!
        return sessions[sessionId].numRounds
    }

    fun getCurrentSessionType(): SessionType = sessions[currentSessionIdx.value!!].type
    fun getCurrentSessionDuration(): Int {
        val session = sessions[currentSessionIdx.value!!]
        return if (isResting.value!! && session.type != SessionType.REST)
            session.breakDuration
        else
            session.duration
    }

    fun init(sessionsRaw: String) {
        sessions = TypeConverter.toSessionSkeletons(sessionsRaw)
        sessionToAdd = Session()

        durations.clear()
        for (index in 0 until sessions.size) {
            durations.add(0)
        }
        countedRounds = ArrayList()
        for (i in 0 until sessions.size) {
            countedRounds.add(0)
        }

        currentRoundIdx.value = 0
        currentSessionIdx.value = 0
        secondsUntilFinished.value = sessions[0].duration
        isResting.value = false

        isCustomWorkout = sessions.size > 2 // 2 because of the pre-workout countdown

        if (preferenceHelper.isDndModeEnabled()) {
            notifier.toggleDndMode(true)
        }
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
            if (prev != null && prev != 0L) prev
            else originalSeconds

        timer = CountDownTimer(secondsToUse, originalSeconds, object : CountDownTimer.Listener {
            override fun onTick(seconds: Int) {
                handleTimerTick(seconds)
            }

            override fun onFinishSet() {
                handleFinishTimer()
            }

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

    private fun stopTimer() {
        timer.cancel()
        notifier.stop()
        timerState.value = TimerState.INACTIVE
    }

    fun abandonWorkout() {
        stopTimer()
        if (preferenceHelper.isDndModeEnabled()) {
            notifier.toggleDndMode(false)
        }
        if (preferenceHelper.logIncompleteSessions()) {
            prepareSessionToAdd()
        }
        repository.addSession(sessionToAdd)
    }

    fun prepareSessionToAdd() {
        val index = currentSessionIdx.value!!
        updateDurations(getCurrentSessionType(), index, true)
        if (isCustomWorkout) {
            sessionToAdd.notes = ""
            for (session in sessions.withIndex()) {
                if (session.index == 0) continue
                if (session.index > index) break
                sessionToAdd.actualRounds += getRounds(session.index)
                sessionToAdd.actualDuration += durations[session.index]
                sessionToAdd.notes +=
                    "${StringUtils.toFavoriteFormatExtended(session.value)} " +
                            "/ ${secondsToNiceFormat(durations[session.index])}"
                if (getRounds(session.index) > 0) {
                    sessionToAdd.notes += " / ${getRounds(session.index)} rounds"
                }
                sessionToAdd.notes +=
                    if (session.index < sessions.size - 1) "\n" else "(incomplete)"
            }
            sessionToAdd.isTimeBased = sessions.find { it.type == SessionType.FOR_TIME } != null
        } else {
            if (sessions[index].type != SessionType.REST) {
                sessionToAdd = prepareSessionToAdd(
                    sessions[index],
                    durations[index],
                    countedRounds[index]
                )
            }
        }
    }

    /**
     * Valid for FOR_TIME sessions
     */
    fun finishCurrentSession() {
        timer.cancel()
        notifier.stop()
        timerState.value = TimerState.INACTIVE
        // when the timer is finished, save the last round but only if the user used the round counter
        if (countedRounds[currentSessionIdx.value!!] != 0) {
            addRound()
        }
        handleFinishTimer()
    }

    private fun isLastSession(): Boolean {
        return sessions.size == currentSessionIdx.value!! + 1
    }

    private fun isLastRound(): Boolean {
        return sessions[currentSessionIdx.value!!].numRounds == currentRoundIdx.value!! + 1
    }

    private fun handleTimerTick(seconds: Int) {
        secondsUntilFinished.value = seconds
        if (seconds == 2) {
            //TODO: handle bug when pausing exactly here -> sound should not restart
            notifier.notifyCountDown()
        }
    }

    private fun updateDurations(sessionType: SessionType, index: Int, abandoned: Boolean = false) {
        val currentRoundIdx = currentRoundIdx.value!!
        val secondsUntilFinished = secondsUntilFinished.value!!
        val sessionSkeleton = sessions[index]
        when (sessionType) {
            SessionType.AMRAP, SessionType.REST -> {
                if (abandoned) {
                    durations[index] = sessionSkeleton.duration - secondsUntilFinished
                } else {
                    durations[index] = sessionSkeleton.duration
                }
            }
            SessionType.FOR_TIME -> {
                durations[index] = sessionSkeleton.duration - secondsUntilFinished
            }
            SessionType.EMOM -> {
                if (abandoned) {
                    // rounds already finished
                    val fullRoundsDuration = sessionSkeleton.duration * currentRoundIdx
                    // the incomplete one
                    val lastRoundDuration = sessionSkeleton.duration - secondsUntilFinished
                    durations[index] = fullRoundsDuration + lastRoundDuration
                } else {
                    durations[index] = sessionSkeleton.duration * sessionSkeleton.numRounds
                }
            }
            SessionType.HIIT -> {
                if (abandoned) {
                    // rounds already finished
                    val fullRoundsDuration = sessionSkeleton.duration * currentRoundIdx +
                            sessionSkeleton.breakDuration * currentRoundIdx
                    if (isResting.value!!) {
                        durations[index] = fullRoundsDuration + sessionSkeleton.duration +
                                sessionSkeleton.breakDuration - secondsUntilFinished
                    } else {
                        durations[index] =
                            fullRoundsDuration + sessionSkeleton.duration - secondsUntilFinished
                    }
                } else {
                    durations[index] = sessionSkeleton.duration * sessionSkeleton.numRounds +
                            sessionSkeleton.breakDuration * (sessionSkeleton.numRounds)
                }
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
                    || (isLastRound() && isLastSession() && !isResting.value!!)
                ) {
                    if (isLastSession()) {
                        timerState.value = TimerState.FINISHED
                        notifier.notifyTrainingComplete()
                    } else {
                        if (isLastRound()) {
                            isResting.value = !isResting.value!!
                        }
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
        countedRounds[currentSessionIdx.value!!] += 1
    }

    fun getRounds(idx: Int) = countedRounds[idx]

    fun getNumCurrentSessionRounds(): Int {
        return countedRounds[currentSessionIdx.value!!]
    }

    /**
     * This is called when the DONE button is pressed
     */
    fun handleCompletion() {
        if (preferenceHelper.isDndModeEnabled()) {
            notifier.toggleDndMode(false)
        }
        repository.addSession(sessionToAdd)
    }
}
