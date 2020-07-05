package com.adrcotfas.wod.ui.emom

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.adrcotfas.wod.data.model.SessionMinimal
import com.adrcotfas.wod.data.model.SessionType
import com.adrcotfas.wod.data.repository.SessionsRepository
import com.adrcotfas.wod.ui.common.EmomSpinnerData

class EmomViewModel(sessionsRepository: SessionsRepository) : ViewModel() {
    val emomData = EmomSpinnerData()
    val favorites : LiveData<List<SessionMinimal>> = sessionsRepository.getSessionsMinimal(
        SessionType.EMOM)

    fun setEmomData(duration: Pair<Int, Int>, rounds: Int) {
        emomData.setMinutes(duration.first)
        emomData.setSeconds(duration.second)
        emomData.setRounds(rounds)
    }
}