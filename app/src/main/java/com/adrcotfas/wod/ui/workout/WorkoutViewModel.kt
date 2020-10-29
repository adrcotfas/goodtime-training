package com.adrcotfas.wod.ui.workout

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adrcotfas.wod.common.soundplayer.SoundPlayer
import com.adrcotfas.wod.common.soundplayer.SoundPlayer.Companion.START_COUNTDOWN
import com.adrcotfas.wod.common.soundplayer.SoundPlayer.Companion.WORKOUT_COMPLETE
import com.adrcotfas.wod.common.stringToSessions

import com.adrcotfas.wod.common.timers.CountDownTimer
import com.adrcotfas.wod.data.model.Session.Companion.constructIncompleteSession
import com.adrcotfas.wod.data.model.Session.Companion.constructSession
import com.adrcotfas.wod.data.model.SessionMinimal
import com.adrcotfas.wod.data.model.SessionType
import com.adrcotfas.wod.data.repository.SessionsRepository
import com.adrcotfas.wod.data.workout.TimerState

class WorkoutViewModel(private val soundPlayer : SoundPlayer, private val repository: SessionsRepository) : ViewModel() {

    val timerState = MutableLiveData(TimerState.INACTIVE)
    var sessions = ArrayList<SessionMinimal>()
    var countedRounds = MutableLiveData(0)

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
        sessions = stringToSessions(sessionsRaw)
        currentRoundIdx.value = 0
        currentSessionIdx.value = 0
        secondsUntilFinished.value = sessions[0].duration
        isResting.value = false
        countedRounds.value = 0
    }

    fun startWorkout() {
        timerState.value = TimerState.ACTIVE
        val index = currentSessionIdx.value!!
        val session = sessions[index]

        if (session.type == SessionType.BREAK) {
            // pre-workout session is of type BREAK
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
        val index = currentSessionIdx.value!!

        val currentSession = sessions[index]
        val activeSeconds =
            (index + 1) * currentSession.duration +
                    (secondsUntilFinished.value?.toInt() ?: 0)

        if (currentSession.type != SessionType.BREAK) {
            repository.addSession(constructIncompleteSession(
                currentSession.type,
                activeSeconds,
                System.currentTimeMillis(),
                countedRounds.value!!))
        }
    }

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
            SessionType.AMRAP, SessionType.FOR_TIME, SessionType.BREAK -> {

                if (type == SessionType.BREAK) {
                    // reset resting value here
                    isResting.value = false
                }

                if (isLastSession()) {
                    timerState.value = TimerState.FINISHED
                    soundPlayer.play(WORKOUT_COMPLETE)
                    if (sessions[index].type != SessionType.BREAK) {
                        repository.addSession(
                            constructSession(
                                sessions[index],
                                System.currentTimeMillis(),
                                countedRounds.value!!))
                    }
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
                        timerState.value = TimerState.FINISHED
                        soundPlayer.play(WORKOUT_COMPLETE)
                        if (sessions[index].type != SessionType.BREAK) {
                            repository.addSession(constructSession(sessions[index], System.currentTimeMillis()))
                        }
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
                    if (isLastSession()) {
                        timerState.value = TimerState.FINISHED
                        soundPlayer.play(WORKOUT_COMPLETE)
                        if (sessions[index].type != SessionType.BREAK) {
                            repository.addSession(constructSession(sessions[index], System.currentTimeMillis()))
                        }
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
}
