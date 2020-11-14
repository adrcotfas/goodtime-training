package com.adrcotfas.wod.ui.workout

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adrcotfas.wod.common.soundplayer.SoundPlayer
import com.adrcotfas.wod.common.soundplayer.SoundPlayer.Companion.START_COUNTDOWN
import com.adrcotfas.wod.common.soundplayer.SoundPlayer.Companion.WORKOUT_COMPLETE

import com.adrcotfas.wod.common.timers.CountDownTimer
import com.adrcotfas.wod.data.model.Session.Companion.constructSession
import com.adrcotfas.wod.data.model.SessionSkeleton
import com.adrcotfas.wod.data.model.SessionType
import com.adrcotfas.wod.data.model.TypeConverter
import com.adrcotfas.wod.data.repository.AppRepository
import com.adrcotfas.wod.data.workout.TimerState

class WorkoutViewModel(private val soundPlayer : SoundPlayer, private val repository: AppRepository) : ViewModel() {

    val timerState = MutableLiveData(TimerState.INACTIVE)
    var sessions = ArrayList<SessionSkeleton>()

    /**
     * Holds the counted rounds in seconds elapsed
     */
    var countedRounds = ArrayList<Int>(0)

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
    fun getCurrentSessionDuration() : Int =
        if (isResting.value!!) sessions[currentSessionIdx.value!!].breakDuration
        else sessions[currentSessionIdx.value!!].duration

    fun init(sessionsRaw: String) {
        sessions = TypeConverter.toSessionSkeletons(sessionsRaw)
        durations.clear()
        for (index in 0 until sessions.size) {
            durations.add(0)
        }
        currentRoundIdx.value = 0
        currentSessionIdx.value = 0
        secondsUntilFinished.value = sessions[0].duration
        isResting.value = false
        countedRounds = ArrayList(0)
    }

    fun startWorkout() {
        timerState.value = TimerState.ACTIVE
        val index = currentSessionIdx.value!!
        val session = sessions[index]

        if (session.type == SessionType.REST) {
            isResting.value = true
        }

        val seconds = secondsUntilFinished.value?.toLong()
            ?: if (isResting.value!!)
                session.breakDuration.toLong()
            else
                session.duration.toLong()

        timer = CountDownTimer(seconds, object : CountDownTimer.Listener {
            override fun onTick(seconds: Int) { handleTimerTick(seconds) }
            override fun onFinishSet() { handleFinishTimer()}
            override fun onHalfwayThere() { //TODO: don't play for BREAK and REST
            }
        })
        timer.start()
    }

    fun toggleTimer() {
        if (timerState.value == TimerState.ACTIVE) {
            timer.cancel()
            timerState.value = TimerState.PAUSED
            soundPlayer.stop()
        } else if (timerState.value == TimerState.PAUSED) {
            startWorkout()
        }
    }

    fun stopTimer() {
        timer.cancel()
        soundPlayer.stop()
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
        soundPlayer.stop()
        timerState.value = TimerState.INACTIVE
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
            soundPlayer.play(START_COUNTDOWN)
        }
    }

    private fun handleFinishTimer() {
        val type = getCurrentSessionType()
        var index = currentSessionIdx.value!!
        when (type) {
            SessionType.AMRAP, SessionType.FOR_TIME, SessionType.REST -> {

                if (type == SessionType.REST) {
                    // reset resting value here
                    isResting.value = false
                }

                if (isLastSession()) {
                    if (type != SessionType.REST) {
                        if (countedRounds.isNotEmpty()) {
                            addRound()
                        }
                        if (type == SessionType.FOR_TIME) {
                            durations[index] = sessions[index].duration - secondsUntilFinished.value!!
                        } else {
                            durations[index] = sessions[index].duration
                        }
                        repository.addSession(
                            constructSession(
                                sessions[index],
                                System.currentTimeMillis(),
                                countedRounds,
                                durations[index]))
                    }
                    timerState.value = TimerState.FINISHED
                    soundPlayer.play(WORKOUT_COMPLETE)
                } else {
                    ++index
                    currentSessionIdx.value = index

                    secondsUntilFinished.value = sessions[index].duration
                    startWorkout()
                }
            }
            SessionType.EMOM -> {
                if (isLastRound()) {
                    if (isLastSession()) {
                        durations[index] = sessions[index].duration * sessions[index].numRounds

                        repository.addSession(constructSession(sessions[index], System.currentTimeMillis()))

                        timerState.value = TimerState.FINISHED
                        soundPlayer.play(WORKOUT_COMPLETE)
                    } else {
                        currentRoundIdx.value = 0
                        ++index
                        currentSessionIdx.value = index
                    }
                } else {
                    currentRoundIdx.value = currentRoundIdx.value!! + 1
                    secondsUntilFinished.value = sessions[currentSessionIdx.value!!].duration
                    startWorkout()
                }
            }
            SessionType.TABATA -> {
                if (isLastRound()) {
                    //TODO: for custom workouts, take the last break anyway if this is not the last session
                    if (isLastSession()) {
                        durations[index] = sessions[index].duration * sessions[index].numRounds +
                                sessions[index].breakDuration * (sessions[index].numRounds)

                        repository.addSession(constructSession(sessions[index], System.currentTimeMillis()))

                        timerState.value = TimerState.FINISHED
                        soundPlayer.play(WORKOUT_COMPLETE)
                    } else {
                        currentRoundIdx.value = 0
                        ++index
                        currentSessionIdx.value = index
                    }
                } else {
                    isResting.value = !isResting.value!!
                    if (!isResting.value!!) {
                        currentRoundIdx.value = currentRoundIdx.value!! + 1
                    }
                    secondsUntilFinished.value =
                        if (isResting.value!!)
                            sessions[index].breakDuration
                        else
                            sessions[index].duration
                    startWorkout()
                }
            }
        }
    }

    fun addRound() {
        val totalSeconds = getCurrentSessionDuration()
        if (countedRounds.isEmpty()) {
            countedRounds.add(totalSeconds - secondsUntilFinished.value!!)
        } else {
            val elapsed = totalSeconds - secondsUntilFinished.value!!
            countedRounds.add(elapsed)
        }
    }

    fun getRounds() : ArrayList<Int> {
        val result = ArrayList<Int>(0)
        for (i in 0 until countedRounds.size) {
            if (i == 0) {
                result.add(countedRounds[i])
            } else {
                result.add(countedRounds[i] - countedRounds[i - 1])
            }
        }
        return result
    }
}
