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
            sessionDao.add(session)
        }
    }

    override fun getSessions() : LiveData<List<Session>> = sessionDao.get()

    override fun addSessionMinimal(session: SessionMinimal) {
        GlobalScope.launch {
            sessionMinimalDao.add(session)
        }
    }

    override fun getSessionsMinimal(type: SessionType): LiveData<List<SessionMinimal>>
        = sessionMinimalDao.get(type)

    override fun removeSessionMinimal(id: Int) {
      GlobalScope.launch {
          sessionMinimalDao.remove(id)
      }
    }

    override fun editSessionMinimal(id: Int, session: SessionMinimal) {
        GlobalScope.launch {
            sessionMinimalDao.edit(id, session.name, session.duration, session.breakDuration, session.numRounds, session.type, session.notes)
        }
    }
}
