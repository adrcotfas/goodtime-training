package com.adrcotfas.wod.data.repository

import androidx.lifecycle.LiveData
import com.adrcotfas.wod.data.model.Session

interface SessionsRepository {
    fun addSession(session: Session)
    fun getSessions() : LiveData<List<Session>>
}
