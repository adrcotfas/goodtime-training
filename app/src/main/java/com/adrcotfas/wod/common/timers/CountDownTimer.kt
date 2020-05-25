package com.adrcotfas.wod.common.timers

import java.util.concurrent.TimeUnit

class CountDownTimer(secondsInTheFuture: Long, private val listener: Listener)
    : android.os.CountDownTimer(TimeUnit.SECONDS.toMillis(secondsInTheFuture), 1000) {

    var seconds = 0
        private set

    interface Listener {
        fun onFinishSet()
    }

    override fun onTick(millisUntilFinished: Long) {
        // workaround for a bug in CountDownTimer which causes
        // onTick to be called twice inside a countDownInterval
        if (millisUntilFinished < 100) {
            return
        }
        seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished).toInt()
    }

    override fun onFinish() {
        listener.onFinishSet()
    }
}
