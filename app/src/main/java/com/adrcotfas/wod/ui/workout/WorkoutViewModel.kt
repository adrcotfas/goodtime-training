package com.adrcotfas.wod.ui.workout

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adrcotfas.wod.common.StringUtils.Companion.toFavoriteFormat
import com.adrcotfas.wod.common.soundplayer.SoundPlayer
import com.adrcotfas.wod.common.soundplayer.SoundPlayer.Companion.COUNTDOWN
import com.adrcotfas.wod.common.soundplayer.SoundPlayer.Companion.COUNTDOWN_LONG
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

    val state = MutableLiveData(TimerState.INACTIVE)
    var sessions = ArrayList<SessionMinimal>()

    lateinit var timer : CountDownTimer

    /**
     * Indicates the index of a session part of a custom workout
     * For a regular workout this will be maximum 1; 0 for the pre-workout countdown and 1 for the actual workout
     */
    val currentSessionIdx = MutableLiveData<Int>(0)

    fun getDurationString() : String {
        return toFavoriteFormat(sessions[currentSessionIdx.value!!])
    }

    /**
     * For EMOM and Tabata workouts this will indicate the current round
     */
    val currentRoundIdx = MutableLiveData<Int>()

    val secondsUntilFinished = MutableLiveData<Int>()

    fun getTotalRounds() : Int  {
        val sessionId = currentSessionIdx.value!!
        return sessions[sessionId].numRounds
    }

    fun getCurrentSessionType() : SessionType = sessions[currentSessionIdx.value!!].type
    fun getCurrentSessionDuration() : Int = sessions[currentSessionIdx.value!!].duration

    /**
     * used for Tabata workouts to signal the rest
     */
    private var shouldRest : Boolean = false

    fun init(sessionsRaw: String) {
        sessions = stringToSessions(sessionsRaw)
        currentRoundIdx.value = 0
        currentSessionIdx.value = 0
        secondsUntilFinished.value = sessions[0].duration
    }

    fun startWorkout() {
        state.value = TimerState.ACTIVE
        val index = currentSessionIdx.value!!
        val session = sessions[index]
        val seconds = secondsUntilFinished.value?.toLong()
            ?: if (shouldRest)
                sessions[index].breakDuration.toLong()
            else
                sessions[index].duration.toLong()

        //TODO: add Timber
        Log.e("WOD::startWorkout", "SessionType: ${session.type}, seconds: $seconds, shouldRest: $shouldRest, " +
                "currentSession: $currentSessionIdx, currentRound: $currentRoundIdx ")

        timer = CountDownTimer(seconds, object : CountDownTimer.Listener {
            override fun onTick(seconds: Int) { handleTimerTick(seconds) }
            override fun onFinishSet() { handleFinishTimer(session.type)}
            override fun onHalfwayThere() { //TODO: don't play for BREAK and REST
            }
        })
        timer.start()
    }

    fun pauseTimer() {
        timer.cancel()
        state.value = TimerState.PAUSED
    }

    fun stopTimer() {
        timer.cancel()
        state.value = TimerState.INACTIVE
        val index = currentSessionIdx.value!!

        val currentSession = sessions[index]
        val activeSeconds =
            (index + 1) * currentSession.duration +
                    (secondsUntilFinished.value?.toInt() ?: 0)

        if (currentSession.type != SessionType.BREAK) {
            repository.addSession(constructIncompleteSession(
                currentSession.type,
                activeSeconds,
                System.currentTimeMillis()))
        }
    }

    private fun isLastSession() : Boolean {
        return sessions.size == currentSessionIdx.value!! + 1
    }

    private fun isLastRound() : Boolean {
        return sessions[currentSessionIdx.value!!].numRounds == currentRoundIdx.value!! + 1
    }

    private fun handleTimerTick(seconds : Int) {
        secondsUntilFinished.value = seconds
        if (seconds == 0) {
            soundPlayer.play(COUNTDOWN_LONG)
        } else if (seconds <= 3) {
            soundPlayer.play(COUNTDOWN)
        }
    }

    private fun handleFinishTimer(type : SessionType) {
        Log.e("WOD::handleFinishTimer", "SessionType: $type")
        var index = currentSessionIdx.value!!
        when (type) {
            SessionType.AMRAP, SessionType.FOR_TIME, SessionType.BREAK -> {
                if (isLastSession()) {
                    state.value = TimerState.FINISHED
                    soundPlayer.play(WORKOUT_COMPLETE)
                    if (sessions[index].type != SessionType.BREAK) {
                        repository.addSession(constructSession(sessions[index], System.currentTimeMillis()))
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
                        state.value = TimerState.FINISHED
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
                        state.value = TimerState.FINISHED
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
                    shouldRest = !shouldRest
                    if (!shouldRest) {
                        currentRoundIdx.value = currentRoundIdx.value!! + 1
                    }
                    secondsUntilFinished.value =
                        if (shouldRest)
                            sessions[index].breakDuration
                        else
                            sessions[index].duration
                    startWorkout()
                }
            }
            SessionType.INVALID -> {
                //TODO: fail nice
            }
        }
    }
}
