package com.adrcotfas.wod.common

import java.util.concurrent.TimeUnit

class TimerUtils {

    companion object {

        fun generateNumbers(min: Int = 0, max: Int, step : Int) : ArrayList<Int> {
            val data = ArrayList<Int>()
            for (i in min..max step step) {
                data.add(i)
            }
            return data
        }

        fun secondsToTimerFormat(elapsed: Int): String {
            val hours = TimeUnit.SECONDS.toHours(elapsed.toLong())
            val minutes =
                TimeUnit.SECONDS.toMinutes(elapsed.toLong()) - hours * 60
            val seconds = elapsed - minutes * 60
            return (insertPrefixZero(hours)
                    + ":" + insertPrefixZero(minutes)
                    + ":" + insertPrefixZero(seconds))
        }

        private fun insertPrefixZero(value: Long): String {
            return if (value < 10) "0$value" else value.toString()
        }
    }
}