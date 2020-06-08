package com.adrcotfas.wod.ui.log

import androidx.lifecycle.ViewModel
import com.adrcotfas.wod.data.model.Session
import com.adrcotfas.wod.data.repository.SessionsRepository

class LogViewModel(private val sessionsRepository: SessionsRepository) : ViewModel() {

    fun addSession(session: Session) = sessionsRepository.addSession(session)
    fun getSessions() = sessionsRepository.getSessions()
}