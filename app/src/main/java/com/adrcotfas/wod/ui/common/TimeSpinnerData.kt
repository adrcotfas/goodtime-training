package com.adrcotfas.wod.ui.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.adrcotfas.wod.common.TimerUtils

class TimeSpinnerData(minutes: Int, seconds: Int = 0) {
    private val _minutes = MutableLiveData(minutes)
    private val _seconds = MutableLiveData(seconds)

    private val _duration = TimerUtils.combine(getMinutes(), getSeconds()) { minutes, seconds ->
        if ((minutes == null) || (seconds == null))
            0
        else
            minutes * 60 + seconds
    }

    fun getMinutes() : LiveData<Int> { return _minutes }
    fun setMinutes(value: Int) { _minutes.value = value }

    fun getSeconds() : LiveData<Int> { return _seconds}
    fun setSeconds(value: Int) { _seconds.value = value }

    fun getDuration() : LiveData<Int> { return _duration }
}