package com.adrcotfas.wod.ui.tabata

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.adrcotfas.wod.data.model.SessionMinimal
import com.adrcotfas.wod.data.model.SessionType
import com.adrcotfas.wod.data.repository.SessionsRepository
import com.adrcotfas.wod.ui.common.TabataSpinnerData

class TabataViewModel(sessionsRepository: SessionsRepository) : ViewModel() {
    val tabataData =  TabataSpinnerData()
    val favorites : LiveData<List<SessionMinimal>> = sessionsRepository.getSessionsMinimal(
        SessionType.TABATA)

    fun setTabataData(durationWork: Pair<Int, Int>, breakDuration: Pair<Int, Int>, numRounds: Int) {
        tabataData.setMinutesWork(durationWork.first)
        tabataData.setSecondsWork(durationWork.second)
        tabataData.setMinutesBreak(breakDuration.first)
        tabataData.setSecondsBreak(breakDuration.second)
        tabataData.setRounds(numRounds)
    }
}
