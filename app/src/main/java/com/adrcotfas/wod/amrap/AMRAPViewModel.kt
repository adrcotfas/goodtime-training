package com.adrcotfas.wod.amrap

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adrcotfas.wod.common.TimerUtils.Companion.SECONDS_STEP
import com.adrcotfas.wod.common.TimerUtils.Companion.combine

class AMRAPViewModel : ViewModel() {

    val minutes = MutableLiveData(15)
    val seconds = MutableLiveData(0)

    val duration = combine(minutes, seconds) { minutes, seconds ->
        if ((minutes == null) || (seconds  == null))
            0
        else
            minutes * 60 + seconds * SECONDS_STEP
    }
}
