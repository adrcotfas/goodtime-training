package com.adrcotfas.wod.ui.for_time

import androidx.lifecycle.ViewModel
import com.adrcotfas.wod.data.model.SessionMinimal
import com.adrcotfas.wod.ui.common.TimeSpinnerData

class ForTimeViewModel : ViewModel() {
    val timeData = TimeSpinnerData(15, 0)

    fun setDuration(duration: Pair<Int, Int>) {
        timeData.setMinutes(duration.first)
        timeData.setSeconds(duration.second)
    }

    lateinit var session : SessionMinimal

    val minutesPickerData = ArrayList<Int>().apply{ addAll(0..60)}
    val secondsPickerData = ArrayList<Int>().apply { addAll(0..59)}
}
