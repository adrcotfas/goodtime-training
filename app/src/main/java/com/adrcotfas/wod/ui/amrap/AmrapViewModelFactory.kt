package com.adrcotfas.wod.ui.amrap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adrcotfas.wod.data.repository.SessionsRepository

class AmrapViewModelFactory(private val sessionsRepository: SessionsRepository)
    : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AmrapViewModel(sessionsRepository) as T
    }
}
