package com.adrcotfas.wod.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adrcotfas.wod.data.repository.SessionsRepository
import com.adrcotfas.wod.ui.amrap.AmrapViewModel

class ViewModelFactory(private val sessionsRepository: SessionsRepository)
    : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AmrapViewModel(sessionsRepository) as T
    }
}
