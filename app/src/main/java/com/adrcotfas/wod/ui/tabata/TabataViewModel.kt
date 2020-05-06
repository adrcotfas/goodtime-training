package com.adrcotfas.wod.ui.tabata

import androidx.lifecycle.ViewModel
import com.adrcotfas.wod.ui.common.RoundSpinnerData
import com.adrcotfas.wod.ui.common.TimeSpinnerData

class TabataViewModel : ViewModel() {
    val workSpinnerData = TimeSpinnerData(0, 20)
    val restSpinnerData = TimeSpinnerData(0, 10)
    val roundSpinnerData = RoundSpinnerData(8)
}
