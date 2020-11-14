package com.adrcotfas.wod.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.adrcotfas.wod.data.model.CustomWorkoutSkeleton
import com.adrcotfas.wod.data.model.SessionSkeleton
import com.adrcotfas.wod.data.model.TypeConverter

@Dao

interface CustomWorkoutSkeletonDao {
    @Insert
    suspend fun add(skeleton: CustomWorkoutSkeleton)

    @Query("select * from CustomWorkoutSkeleton")
    fun get() : LiveData<List<CustomWorkoutSkeleton>>

    @Query("delete from CustomWorkoutSkeleton where name = :name")
    suspend fun remove(name: String)

    @Query("update CustomWorkoutSkeleton set name = :newName, sessions = :newSessions where name = :name")
    @TypeConverters(TypeConverter::class)
    fun edit(name: String, newName: String, newSessions: ArrayList<SessionSkeleton>)
}