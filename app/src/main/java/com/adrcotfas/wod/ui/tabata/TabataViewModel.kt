package com.adrcotfas.wod.ui.tabata

import androidx.lifecycle.ViewModel
import com.adrcotfas.wod.data.model.SessionMinimal
import com.adrcotfas.wod.ui.common.TabataSpinnerData

class TabataViewModel : ViewModel() {
    val tabataData =  TabataSpinnerData()

    fun setTabataData(durationWork: Pair<Int, Int>, breakDuration: Pair<Int, Int>, numRounds: Int) {
        tabataData.setMinutesWork(durationWork.first)
        tabataData.setSecondsWork(durationWork.second)
        tabataData.setMinutesBreak(breakDuration.first)
        tabataData.setSecondsBreak(breakDuration.second)
        tabataData.setRounds(numRounds)
    }

    lateinit var session : SessionMinimal

    val secondsPickerData = ArrayList<Int>().apply { addAll(1..90) }
    val roundsPickerData = ArrayList<Int>().apply { addAll(1..60) }
}
