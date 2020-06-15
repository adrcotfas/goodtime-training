package com.adrcotfas.wod.ui.amrap

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.adrcotfas.wod.data.model.SessionMinimal
import com.adrcotfas.wod.data.model.SessionType
import com.adrcotfas.wod.data.repository.SessionsRepository
import com.adrcotfas.wod.ui.common.TimeSpinnerData

class AmrapViewModel(private val sessionsRepository: SessionsRepository) : ViewModel() {
    val timeData = TimeSpinnerData(15, 0)
    val favorites : LiveData<List<SessionMinimal>> = sessionsRepository.getSessionsMinimal(SessionType.AMRAP)

    fun removeFavorite(id: Int) = sessionsRepository.removeSessionMinimal(id)

}
