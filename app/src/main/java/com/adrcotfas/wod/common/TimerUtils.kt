package com.adrcotfas.wod.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

class TimerUtils {


    companion object {

        const val SECONDS_STEP = 5
        const val MINUTES_STEP = 1

        /**
         * Put an extra "0" at the beginning if the unit is smaller than 10
         * for a nice timer style
         */
        fun unitToTimerStyle(unit : Int) : String {
            return if (unit < 10) "0$unit" else unit.toString()
        }

        /**
         * Generates an array of formatted strings corresponding to the given arguments
         * @param min   the minimum value of the range
         * @param max   the maximum value of the range
         * @param step  the step of the range
         */
        private fun generateTimeValues(min: Int, max: Int, step : Int) : ArrayList<String> {
            val data = ArrayList<String>()
            for (i in min until max step step) {
                data.add(unitToTimerStyle(i))
            }
            return data
        }

        fun generateTimeValuesMinutes(max: Int) = generateTimeValues(0, max, MINUTES_STEP)
        fun generateTimeValuesSeconds(max: Int) = generateTimeValues(0, max, SECONDS_STEP)

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
    }
}