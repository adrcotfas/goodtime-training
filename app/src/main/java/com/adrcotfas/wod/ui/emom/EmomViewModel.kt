package com.adrcotfas.wod.ui.emom

import androidx.lifecycle.ViewModel
import com.adrcotfas.wod.ui.common.RoundSpinnerData
import com.adrcotfas.wod.ui.common.TimeSpinnerData

class EmomViewModel : ViewModel() {
    val timeSpinnerData = TimeSpinnerData(1, 0)
    val roundSpinnerData = RoundSpinnerData(20)
}