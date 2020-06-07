package com.adrcotfas.wod.common.timers

import java.util.concurrent.TimeUnit
import kotlin.math.ceil

class CountDownTimer(secondsInTheFuture: Long, private val listener: Listener)
    : android.os.CountDownTimer(TimeUnit.SECONDS.toMillis(secondsInTheFuture), 1000) {

    private val halfway = ceil(secondsInTheFuture / 2.0).toInt()

    var seconds = 0
        private set

    interface Listener {
        fun onTick(seconds : Int)
        fun onFinishSet()
        fun onHalfwayThere()
    }

    override fun onTick(millisUntilFinished: Long) {
        // workaround for a bug in CountDownTimer which causes
        // onTick to be called twice inside a countDownInterval
        if (millisUntilFinished < 100) {
            return
        }

        seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished).toInt()
        listener.onTick(seconds)

        if (seconds == halfway) {
            listener.onHalfwayThere()
        }
    }

    override fun onFinish() {
        listener.onFinishSet()
    }
}
