package com.adrcotfas.wod.data.repository

import androidx.lifecycle.LiveData
import com.adrcotfas.wod.data.model.Session
import com.adrcotfas.wod.data.model.SessionMinimal
import com.adrcotfas.wod.data.model.SessionType

interface SessionsRepository {

    // finished sessions
    fun addSession(session: Session)
    fun getSessions() : LiveData<List<Session>>

    // custom sessions selected before starting the workout
    fun addSessionMinimal(session: SessionMinimal)
    fun getSessionsMinimal(type: SessionType) : LiveData<List<SessionMinimal>>
    fun removeSessionMinimal(id: Int)
    fun editSessionMinimal(id: Int, session: SessionMinimal)
}
