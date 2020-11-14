package com.adrcotfas.wod.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.adrcotfas.wod.data.model.SessionSkeleton
import com.adrcotfas.wod.data.model.SessionType
import com.adrcotfas.wod.data.model.TypeConverter

@Dao
interface SessionSkeletonDao {

    @Insert
    @TypeConverters(TypeConverter::class)
    suspend fun add(session: SessionSkeleton)

    @Query("select * from SessionSkeleton where type = :type order by duration")
    @TypeConverters(TypeConverter::class)
    fun get(type: SessionType) : LiveData<List<SessionSkeleton>>

    @Query("delete from SessionSkeleton where id = :id")
    suspend fun remove(id: Int)
}
