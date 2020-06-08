package com.adrcotfas.wod.data.repository

import androidx.lifecycle.LiveData
import com.adrcotfas.wod.data.db.SessionDao
import com.adrcotfas.wod.data.model.Session
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SessionRepositoryImpl(private val sessionDao: SessionDao) : SessionsRepository {
    override fun addSession(session: Session) {
        GlobalScope.launch {
            sessionDao.addSession(session)
        }
    }

    override fun getSessions() : LiveData<List<Session>> = sessionDao.getSessions()
}
