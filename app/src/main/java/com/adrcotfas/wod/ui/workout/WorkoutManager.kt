package com.adrcotfas.wod.ui.workout

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.adrcotfas.wod.common.stringToSessions

import com.adrcotfas.wod.common.timers.CountDownTimer
import com.adrcotfas.wod.data.model.SessionMinimal
import com.adrcotfas.wod.data.model.SessionType
import com.adrcotfas.wod.data.workout.TimerState

class WorkoutManager {

    val state = MutableLiveData<TimerState>()
    var sessions = ArrayList<SessionMinimal>()

    lateinit var timer : CountDownTimer

    var currentSessionIdx : Int = 0
    var currentRoundIdx : Int = 0
    val currentTick = MutableLiveData<Int>()

    /**
     * used for Tabata workouts to signal the rest
     */
    private var shouldRest : Boolean = false

    fun init(sessionsRaw: String) {
        sessions = stringToSessions(sessionsRaw)
        currentRoundIdx = 0
        currentSessionIdx = 0
        currentTick.value = sessions[currentSessionIdx].duration
    }

    fun startWorkout() {
        state.value = TimerState.ACTIVE
        val session = sessions[currentSessionIdx]
        val seconds = currentTick.value?.toLong()
            ?: if (shouldRest)
                sessions[currentSessionIdx].breakDuration.toLong()
            else
                sessions[currentSessionIdx].duration.toLong()

        //TODO: add Timber
        Log.e("WOD::startWorkout", "SessionType: ${session.type}, seconds: $seconds, shouldRest: $shouldRest, " +
                "currentSession: $currentSessionIdx, currentRound: $currentRoundIdx ")

        timer = CountDownTimer(seconds, object : CountDownTimer.Listener {
            override fun onTick(seconds: Int) { currentTick.value = seconds }
            override fun onFinishSet() { handleFinishTimer(session.type)}
            override fun onHalfwayThere() {
                //TODO: notify user according to preferences
            }
        })
        timer.start()
    }

    fun pauseTimer() {
        timer.cancel()
        state.value = TimerState.PAUSED
    }

    private fun isLastSession() : Boolean {
        return sessions.size == currentSessionIdx + 1
    }

    private fun isLastRound() : Boolean {
        return sessions[currentSessionIdx].numRounds == currentRoundIdx + 1
    }

    private fun handleFinishTimer(type : SessionType) {
        Log.e("WOD::handleFinishTimer", "SessionType: $type")
        when (type) {
            SessionType.AMRAP, SessionType.FOR_TIME, SessionType.BREAK -> {
                if (isLastSession()) {
                    state.value = TimerState.FINISHED
                } else {
                    currentSessionIdx++
                    currentTick.value = sessions[currentSessionIdx].duration
                    startWorkout()
                }
            }
            SessionType.EMOM -> {
                if (isLastRound()) {
                    if (isLastSession()) {
                        state.value = TimerState.FINISHED
                    } else {
                        currentRoundIdx = 0
                        currentSessionIdx++
                    }
                } else {
                    currentRoundIdx++
                    currentTick.value = sessions[currentSessionIdx].duration
                    startWorkout()
                }
            }
            SessionType.TABATA -> {
                if (isLastRound()) {
                    if (isLastSession()) {
                        state.value = TimerState.FINISHED
                    } else {
                        currentRoundIdx = 0
                        currentSessionIdx++
                    }
                } else {
                    shouldRest = !shouldRest
                    if (!shouldRest) {
                        currentRoundIdx++
                    }
                    currentTick.value =
                        if (shouldRest)
                            sessions[currentSessionIdx].breakDuration
                        else
                            sessions[currentSessionIdx].duration
                    startWorkout()
                }
            }
            SessionType.INVALID -> {
                //TODO: fail nice
            }
        }
    }
}
