package com.adrcotfas.wod.ui.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.adrcotfas.wod.common.CombinedLiveData

class TimeSpinnerData(minutes: Int, seconds: Int = 0) {
    private val _minutes = MutableLiveData(minutes)
    private val _seconds = MutableLiveData(seconds)

    private val _data = CombinedLiveData(_minutes, _seconds) { data: List<Any?> ->
        if ((data[0] == null) || (data[1] == null))
            0
        else
            data[0] as Int * 60 + data[1] as Int
    }

    fun setMinutes(value: Int) { _minutes.value = value }
    fun setSeconds(value: Int) { _seconds.value = value }

    /**
     * Returns the duration in seconds
     */
    fun get() : LiveData<Int> { return _data }
}
