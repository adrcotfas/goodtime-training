package com.adrcotfas.wod.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
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

        fun <T, K, R> combine(
            liveData1: LiveData<T>,
            liveData2: LiveData<K>,
            block: (T?, K?) -> R
        ): LiveData<R> {
            val result = MediatorLiveData<R>()
            result.addSource(liveData1) {
                result.value = block.invoke(liveData1.value, liveData2.value)
            }
            result.addSource(liveData2) {
                result.value = block.invoke(liveData1.value, liveData2.value)
            }
            return result
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