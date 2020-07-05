package com.adrcotfas.wod.ui.emom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adrcotfas.wod.data.repository.SessionsRepository

class EmomViewModelFactory(private val sessionsRepository: SessionsRepository)
    : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return EmomViewModel(sessionsRepository) as T
    }
}
