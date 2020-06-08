package com.adrcotfas.wod.ui.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adrcotfas.wod.data.repository.SessionsRepository

class LogViewModelFactory(private val sessionsRepository: SessionsRepository)
    : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return LogViewModel(sessionsRepository) as T
    }
}