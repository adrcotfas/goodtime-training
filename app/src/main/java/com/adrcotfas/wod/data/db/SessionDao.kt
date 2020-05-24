package com.adrcotfas.wod.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.TypeConverters
import com.adrcotfas.wod.data.model.Session
import com.adrcotfas.wod.data.model.SessionTypeConverter

@Dao
interface SessionDao {

    @Insert
    @TypeConverters(SessionTypeConverter::class)
    fun addSession(session: Session)

    @Query("select * from Session")
    @TypeConverters(SessionTypeConverter::class)
    fun getSessions(): LiveData<List<Session>>
}
