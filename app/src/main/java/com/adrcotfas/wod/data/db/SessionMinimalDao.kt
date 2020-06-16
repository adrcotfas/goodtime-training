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

    @Query("select * from SessionMinimal where type = :type")
    @TypeConverters(SessionTypeConverter::class)
    fun get(type: SessionType) : LiveData<List<SessionMinimal>>

    @Query("delete from SessionMinimal where id = :id")
    suspend fun remove(id: Int)

    @Query("update SessionMinimal set name = :name, duration = :duration, breakDuration = :breakDuration, " +
            "numRounds = :numRounds, type = :type, notes = :notes where id = :id")
    fun edit(id: Int, name: String, duration: Int, breakDuration: Int, numRounds: Int, type: SessionType, notes: String)
}
