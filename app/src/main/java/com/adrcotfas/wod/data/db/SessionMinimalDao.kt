package com.adrcotfas.wod.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.adrcotfas.wod.data.model.SessionMinimal
import com.adrcotfas.wod.data.model.SessionType
import com.adrcotfas.wod.data.model.SessionTypeConverter

@Dao
interface SessionMinimalDao {

    @Insert
    @TypeConverters(SessionTypeConverter::class)
    suspend fun add(session: SessionMinimal)

    @Query("select * from SessionMinimal where type = :type order by duration")
    @TypeConverters(SessionTypeConverter::class)
    fun get(type: SessionType) : LiveData<List<SessionMinimal>>

    @Query("delete from SessionMinimal where id = :id")
    suspend fun remove(id: Int)

    @Query("update SessionMinimal set duration = :duration, breakDuration = :breakDuration, " +
            "numRounds = :numRounds, type = :type where id = :id")
    fun edit(id: Int, duration: Int, breakDuration: Int, numRounds: Int, type: SessionType)
}
