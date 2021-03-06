package goodtime.training.wod.timer.ui.timer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import goodtime.training.wod.timer.common.StringUtils
import goodtime.training.wod.timer.common.timers.CountDownTimer
import goodtime.training.wod.timer.data.model.Session
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.data.model.SessionType
import goodtime.training.wod.timer.data.model.TypeConverter
import goodtime.training.wod.timer.data.workout.TimerState

class WorkoutManager(private val notifier: TimerNotificationHelper) {

    private lateinit var timer: CountDownTimer
    private val listener = object : CountDownTimer.Listener {
        override fun onTick(seconds: Int) {
            handleTimerTick(seconds)
        }

        override fun onFinishSet() {
            handleFinishTimer()
        }

        override fun onHalfwayThere() {
            if (!getIsResting()) {
                notifier.notifyMiddleOfTraining()
            }
        }
    }

    var sessionToAdd = Session()
    var sessions = ArrayList<SessionSkeleton>()

    private val _timerState = MutableLiveData(TimerState.INACTIVE)
    val timerState: LiveData<TimerState>
        get() = _timerState


    /** Holds the counted rounds for each session of the workout*/
    private var countedRounds = ArrayList<Int>(0)
    fun getCurrentSessionCountedRounds(): Int = countedRounds[getCurrentSessionIdx()]
    fun addRound() {
        countedRounds[_currentSessionIdx.value!!] += 1
    }

    /** Holds the duration of each session of the workout */
    var durations = ArrayList<Int>()


    /**
     * Indicates the index of a session part of a custom workout
     * For a regular workout this will be maximum 1; 0 for the pre-workout countdown and 1 for the actual workout
     */
    private val _currentSessionIdx = MutableLiveData(0)
    val currentSessionIdx: LiveData<Int>
        get() = _currentSessionIdx

    private fun getCurrentSessionIdx() = currentSessionIdx.value!!

    /**
     * For Intervals and HIIT workouts this will indicate the current round
     */
    private val _currentRoundIdx = MutableLiveData<Int>()
    val currentRoundIdx: LiveData<Int>
        get() = _currentRoundIdx

    fun getCurrentRound() = currentRoundIdx.value!! + 1

    private val _secondsUntilFinished = MutableLiveData<Int>()
    val secondsUntilFinished: LiveData<Int>
        get() = _secondsUntilFinished

    /**
     * used for HIIT workouts to signal the rest
     */
    private val _isResting = MutableLiveData(false)
    val isResting: LiveData<Boolean>
        get() = _isResting

    fun getIsResting() = isResting.value!!

    var isCustomWorkout = false

    fun getCurrentSessionTotalRounds() = sessions[getCurrentSessionIdx()].numRounds
    fun getCurrentSessionType(): SessionType = sessions[getCurrentSessionIdx()].type

    fun getCurrentSessionDuration(): Int {
        val session = sessions[getCurrentSessionIdx()]
        return if (getIsResting() && session.type != SessionType.REST)
            session.breakDuration
        else
            session.duration
    }

    private fun isLastSession() = sessions.size == _currentSessionIdx.value!! + 1
    private fun isLastSession(idx: Int) = sessions.size == idx + 1
    private fun isLastRound() = sessions[_currentSessionIdx.value!!].numRounds == _currentRoundIdx.value!! + 1

    fun init(sessionsRaw: String, name: String?) {
        sessions = TypeConverter.toSessionSkeletons(sessionsRaw)
        sessionToAdd = Session()
        sessionToAdd.name = name

        durations.clear()
        for (index in 0 until sessions.size) {
            durations.add(0)
        }
        countedRounds = ArrayList()
        for (i in 0 until sessions.size) {
            countedRounds.add(0)
        }

        _currentRoundIdx.value = 0
        _currentSessionIdx.value = 0
        _secondsUntilFinished.value = sessions[0].duration
        _isResting.value = false

        isCustomWorkout = sessions.size > 2 // 2 because of the pre-workout countdown
    }

    /**
     * Called at the start of each session
     */
    fun startWorkout() {
        _timerState.value = TimerState.ACTIVE
        val index = _currentSessionIdx.value!!
        val session = sessions[index]

        if (session.type == SessionType.REST) {
            _isResting.value = true
        }

        // will be used if the timer was paused
        val prev = _secondsUntilFinished.value?.toLong()

        // will be used when starting fresh
        val originalSeconds =
            if (getIsResting() && session.type != SessionType.REST)
                session.breakDuration.toLong()
            else
                session.duration.toLong()

        val secondsToUse =
            if (prev != null && prev != 0L) prev
            else originalSeconds

        timer = CountDownTimer(secondsToUse, originalSeconds, listener)
        timer.start()

    }

    fun toggleTimer() {
        if (_timerState.value == TimerState.ACTIVE) {
            timer.cancel()
            _timerState.value = TimerState.PAUSED
            notifier.stop()
        } else if (_timerState.value == TimerState.PAUSED) {
            startWorkout()
        }
    }

    private fun stopTimer() {
        timer.cancel()
        notifier.stop()
        _timerState.value = TimerState.INACTIVE
    }

    fun abandonWorkout() {
        stopTimer()
        updateDuration(getCurrentSessionIdx(), false)
    }

    fun prepareSessionToAdd(completed: Boolean = true) {
        val index = getCurrentSessionIdx()
        if (isCustomWorkout) {
            sessionToAdd.skeleton.type = SessionType.CUSTOM
            sessionToAdd.notes = ""
            for (session in sessions.withIndex()) {
                if (session.index == 0) continue
                if (session.index > index) break

                val duration = durations[session.index]
                val countedRounds = countedRounds[session.index]

                sessionToAdd.actualDuration += duration
                sessionToAdd.actualRounds += countedRounds
            }
            sessionToAdd.isTimeBased = sessions.find { it.type == SessionType.FOR_TIME } != null
            sessionToAdd.isCompleted = completed
        } else {
            if (sessions[index].type != SessionType.REST) {
                sessionToAdd = Session.prepareSessionToAdd(
                    sessions[index], 0,
                    durations[index],
                    countedRounds[index],
                    completed = completed
                )
            }
        }
    }

    /**
     * Valid for FOR_TIME sessions
     */
    fun onForTimeComplete() {
        timer.cancel()
        notifier.stop()
        _timerState.value = TimerState.INACTIVE
        // when the timer is finished, save the last round but only if the user used the round counter
        if (countedRounds[getCurrentSessionIdx()] != 0) addRound()
        handleFinishTimer()
    }

    private fun handleTimerTick(seconds: Int) {
        _secondsUntilFinished.value = seconds
        if (seconds == 2) {
            //TODO: handle bug when pausing exactly here -> sound should not restart
            notifier.notifyCountDown()
        }
    }

    private fun updateDuration(index: Int, completed: Boolean = true) {
        val currentRoundIdx = currentRoundIdx.value!!
        val secondsUntilFinished = secondsUntilFinished.value!!
        val sessionSkeleton = sessions[index]
        if (completed) {
            if (sessionSkeleton.type == SessionType.FOR_TIME) {
                durations[index] = sessionSkeleton.duration - secondsUntilFinished
            } else {
                durations[index] = sessionSkeleton.getActualDuration() +
                        // For HIIT, include the last break too if this is not the last session
                        if (sessionSkeleton.type == SessionType.HIIT && !isLastSession(index)) sessionSkeleton.breakDuration else 0
            }
        } else { // abandoned
            when (sessionSkeleton.type) {
                SessionType.AMRAP, SessionType.REST -> durations[index] =
                    sessionSkeleton.duration - secondsUntilFinished
                SessionType.FOR_TIME -> durations[index] = sessionSkeleton.duration - secondsUntilFinished
                SessionType.INTERVALS -> {
                    // rounds already finished
                    val fullRoundsDuration = sessionSkeleton.duration * currentRoundIdx
                    // the incomplete one
                    val lastRoundDuration = sessionSkeleton.duration - secondsUntilFinished
                    durations[index] = fullRoundsDuration + lastRoundDuration
                }
                SessionType.HIIT -> {
                    // rounds already finished
                    val fullRoundsDuration = sessionSkeleton.duration * currentRoundIdx +
                            sessionSkeleton.breakDuration * currentRoundIdx
                    if (getIsResting()) {
                        durations[index] = fullRoundsDuration + sessionSkeleton.duration +
                                sessionSkeleton.breakDuration - secondsUntilFinished
                    } else {
                        durations[index] =
                            fullRoundsDuration + sessionSkeleton.duration - secondsUntilFinished
                    }
                }
                else -> {
                }
            }
        }
    }

    /**
     * This is called whenever a session (part of a workout containing one or more sessions) is finished
     */
    private fun handleFinishTimer() {
        val type = getCurrentSessionType()
        val index = _currentSessionIdx.value!!
        updateDuration(index)
        when (type) {
            SessionType.AMRAP, SessionType.FOR_TIME, SessionType.REST -> {
                if (type == SessionType.REST) {
                    // reset resting value here
                    _isResting.value = false
                }
                if (isLastSession()) handleCompletion()
                else handleIntermediateSessionFinished()
            }
            SessionType.INTERVALS -> handleIntervalsSessionFinished()
            SessionType.HIIT -> handleHIITSessionFinished()
            else -> { // do nothing here
            }
        }
    }

    private fun handleIntervalsSessionFinished() {
        if (isLastRound()) {
            if (isLastSession()) {
                handleCompletion()
            } else {
                _currentRoundIdx.value = 0
                handleIntermediateSessionFinished()
            }
        } else {
            _currentRoundIdx.value = _currentRoundIdx.value!! + 1
            _secondsUntilFinished.value = sessions[getCurrentSessionIdx()].duration
            startWorkout()

            if (isLastRound()) notifier.notifyLastRound()
            else notifier.notifyStart()
        }
    }

    private fun handleHIITSessionFinished() {
        // if this is the last round but not in the final session, take the break
        if ((isLastRound() && !isLastSession() && getIsResting())
            // if this is the final session and final work round, skip the break
            || (isLastRound() && isLastSession() && !getIsResting())
        ) {
            if (isLastSession()) {
                handleCompletion()
            } else {
                if (isLastRound()) {
                    _isResting.value = !_isResting.value!!
                }
                _currentRoundIdx.value = 0
                handleIntermediateSessionFinished()
            }
        } else {
            _isResting.value = !_isResting.value!!
            val isRestingVal: Boolean = getIsResting()

            if (!isRestingVal) _currentRoundIdx.value = _currentRoundIdx.value!! + 1

            val index = getCurrentSessionIdx()
            _secondsUntilFinished.value =
                if (isRestingVal)
                    sessions[index].breakDuration
                else
                    sessions[index].duration
            startWorkout()

            if (isLastRound()) {
                notifier.notifyLastRound()
            } else {
                if (isRestingVal) notifier.notifyRest()
                else notifier.notifyStart()
            }
        }
    }

    /**
     * A session was finished but this is not the last one.
     * We need to handle the start of the next one.
     */
    private fun handleIntermediateSessionFinished() {
        _currentSessionIdx.value = _currentSessionIdx.value!! + 1
        _secondsUntilFinished.value = sessions[getCurrentSessionIdx()].duration

        startWorkout()

        if (sessions[getCurrentSessionIdx()].type == SessionType.REST)
            notifier.notifyRest()
        else
            notifier.notifyStart()
    }

    /**
     * The last session of the workout was completed
     */
    private fun handleCompletion() {
        _timerState.value = TimerState.FINISHED
        notifier.notifyTrainingComplete()
    }

    /**
     * This is called when the user leaves the timer workout screen
     */
    fun setInactive() {
        _timerState.value = TimerState.INACTIVE
        notifier.stop()
    }
}