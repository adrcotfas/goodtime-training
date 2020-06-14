package com.adrcotfas.wod.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.TypeConverters
import com.adrcotfas.wod.data.model.SessionMinimal
import com.adrcotfas.wod.data.model.SessionType
import com.adrcotfas.wod.data.model.SessionTypeConverter

@Dao
interface SessionMinimalDao {

    @Insert
    @TypeConverters(SessionTypeConverter::class)
    suspend fun addSession(session: SessionMinimal)

    @Query("select * from SessionMinimal where type = :type")
    @TypeConverters(SessionTypeConverter::class)
    fun getSessions(type: SessionType) : LiveData<List<SessionMinimal>>
}