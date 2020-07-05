package com.adrcotfas.wod.ui.for_time

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.adrcotfas.wod.data.model.SessionMinimal
import com.adrcotfas.wod.data.model.SessionType
import com.adrcotfas.wod.data.repository.SessionsRepository
import com.adrcotfas.wod.ui.common.TimeSpinnerData

class ForTimeViewModel(sessionsRepository: SessionsRepository) : ViewModel() {
    val timeData = TimeSpinnerData(15, 0)
    val favorites : LiveData<List<SessionMinimal>> = sessionsRepository.getSessionsMinimal(
        SessionType.FOR_TIME)

    fun setDuration(duration: Pair<Int, Int>) {
        timeData.setMinutes(duration.first)
        timeData.setSeconds(duration.second)
    }
}
