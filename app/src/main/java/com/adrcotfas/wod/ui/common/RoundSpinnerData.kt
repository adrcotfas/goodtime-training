package com.adrcotfas.wod.ui.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class RoundSpinnerData(rounds: Int) {
    private val _rounds = MutableLiveData(rounds)

    fun getRounds() : LiveData<Int> { return _rounds }
    fun setRounds(value: Int) { _rounds.value = value }
}