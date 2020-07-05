package com.adrcotfas.wod.ui.tabata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adrcotfas.wod.data.repository.SessionsRepository

class TabataViewModelFactory(private val sessionsRepository: SessionsRepository)
    : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return TabataViewModel(sessionsRepository) as T
    }
}
