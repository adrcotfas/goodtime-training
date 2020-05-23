package com.adrcotfas.wod.ui.log

import androidx.lifecycle.ViewModel
import com.adrcotfas.wod.data.db.SessionDao
import com.adrcotfas.wod.data.model.Session

class LogViewModel(private val sessionDao: SessionDao) : ViewModel() {

    fun addSession(session: Session) = sessionDao.addSession(session)
    fun getSessions() = sessionDao.getSessions()
}