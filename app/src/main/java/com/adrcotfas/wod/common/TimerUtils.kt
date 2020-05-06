package com.adrcotfas.wod.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

class TimerUtils {


    companion object {

        private const val MINUTES_STEP_1 = 1
        const val SECONDS_STEP_5 = 5
        const val SECONDS_STEP_15 = 15

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
        private fun generateTimeValues(min: Int = 0, max: Int, step : Int) : ArrayList<String> {
            val data = ArrayList<String>()
            for (i in min until max step step) {
                data.add(unitToTimerStyle(i))
            }
            return data
        }

        fun generateTimeValuesMinutes(max: Int) = generateTimeValues(max = max, step = MINUTES_STEP_1)
        fun generateTimeValuesSeconds(step: Int) = generateTimeValues(max = 60, step = step)

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