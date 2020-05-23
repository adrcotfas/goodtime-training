package com.adrcotfas.wod.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.adrcotfas.wod.data.model.Session

@Dao
interface SessionDao {

    @Insert
    fun addSession(session: Session)

    @Query("select * from Session")
    fun getSessions(): LiveData<List<Session>>
}
