package com.adrcotfas.wod.ui.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adrcotfas.wod.data.db.SessionDao

class LogViewModelFactory(private val sessionDao : SessionDao)
    : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return LogViewModel(sessionDao) as T
    }
}