package com.adrcotfas.wod.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.TypeConverters
import com.adrcotfas.wod.data.model.Session
import com.adrcotfas.wod.data.model.TypeConverter

@Dao
interface SessionDao {

    @Insert
    @TypeConverters(TypeConverter::class)
    suspend fun add(session: Session)

    @Query("select * from Session")
    @TypeConverters(TypeConverter::class)
    fun get(): LiveData<List<Session>>
}
