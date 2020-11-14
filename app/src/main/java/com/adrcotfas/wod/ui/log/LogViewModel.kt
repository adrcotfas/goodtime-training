package com.adrcotfas.wod.ui.log

import androidx.lifecycle.ViewModel
import com.adrcotfas.wod.data.model.Session
import com.adrcotfas.wod.data.repository.AppRepository

class LogViewModel(private val appRepository: AppRepository) : ViewModel() {

    fun addSession(session: Session) = appRepository.addSession(session)
    fun getSessions() = appRepository.getSessions()
}