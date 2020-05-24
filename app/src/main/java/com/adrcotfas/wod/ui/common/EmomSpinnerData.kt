package com.adrcotfas.wod.ui.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.adrcotfas.wod.common.CombinedLiveData

class EmomSpinnerData(minutes: Int = 1, seconds: Int = 0, rounds: Int = 20) {
    private val _minutes = MutableLiveData(minutes)
    private val _seconds = MutableLiveData(seconds)
    private val _rounds = MutableLiveData(rounds)

    private val _data = CombinedLiveData(_minutes, _seconds, _rounds) { data: List<Any?> ->
        if ((data[0] == null) || (data[1] == null) || data[2] == null)
            Pair(0, 0)
        else
            Pair(data[0] as Int * 60 + data[1] as Int, data[2] as Int)
    }

    fun setMinutes(value: Int) { _minutes.value = value }
    fun setSeconds(value: Int) { _seconds.value = value }
    fun setRounds(value: Int) { _rounds.value = value }

    /**
     * Returns a pair or duration in seconds and rounds
     */
    fun get() : LiveData<Pair<Int, Int>> { return _data }
}
