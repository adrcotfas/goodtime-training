package com.adrcotfas.wod.ui.emom

import androidx.lifecycle.ViewModel
import com.adrcotfas.wod.data.model.SessionMinimal
import com.adrcotfas.wod.ui.common.EmomSpinnerData

class EmomViewModel : ViewModel() {
    val emomData = EmomSpinnerData()

    fun setEmomData(duration: Pair<Int, Int>, rounds: Int) {
        emomData.setMinutes(duration.first)
        emomData.setSeconds(duration.second)
        emomData.setRounds(rounds)
    }

    lateinit var session : SessionMinimal

    val minutesPickerData = ArrayList<Int>().apply { addAll(0..3) }
    val secondsPickerData = ArrayList<Int>().apply { addAll(0..59) }
    val roundsPickerData = ArrayList<Int>().apply { addAll(1..60) }
}