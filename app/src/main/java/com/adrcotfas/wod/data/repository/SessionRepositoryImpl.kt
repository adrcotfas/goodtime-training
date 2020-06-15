package com.adrcotfas.wod.data.repository

import androidx.lifecycle.LiveData
import com.adrcotfas.wod.data.db.SessionDao
import com.adrcotfas.wod.data.db.SessionMinimalDao
import com.adrcotfas.wod.data.model.Session
import com.adrcotfas.wod.data.model.SessionMinimal
import com.adrcotfas.wod.data.model.SessionType
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SessionRepositoryImpl(private val sessionDao: SessionDao, private val sessionMinimalDao: SessionMinimalDao)
    : SessionsRepository {
    override fun addSession(session: Session) {
        GlobalScope.launch {
            sessionDao.addSession(session)
        }
    }

    override fun getSessions() : LiveData<List<Session>> = sessionDao.getSessions()

    override fun addSessionMinimal(session: SessionMinimal) {
        GlobalScope.launch {
            sessionMinimalDao.addSession(session)
        }
    }

    override fun getSessionsMinimal(type: SessionType): LiveData<List<SessionMinimal>>
        = sessionMinimalDao.getSessions(type)

    override fun removeSessionMinimal(id: Int) {
      GlobalScope.launch {
          sessionMinimalDao.removeSession(id)
      }
    }
}
